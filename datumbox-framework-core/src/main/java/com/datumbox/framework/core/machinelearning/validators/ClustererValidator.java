/**
 * Copyright (C) 2013-2016 Vasilis Vryniotis <bbriniotis@datumbox.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datumbox.framework.core.machinelearning.validators;

import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.Record;
import com.datumbox.framework.core.machinelearning.common.abstracts.validators.AbstractValidator;

import java.util.*;

/**
 * Estimates validation metrics for Clustering models.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ClustererValidator extends AbstractValidator<ClustererValidator.ValidationMetrics> {

    /**
     *
     * {@inheritDoc}
     *
     * References:
     * http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html
     * http://thesis.neminis.org/wp-content/plugins/downloads-manager/upload/masterThesis-VR.pdf
     */
    public static class ValidationMetrics extends AbstractValidator.AbstractValidationMetrics {

        private Double purity = null;
        private Double NMI = null; //Normalized Mutual Information: I(Omega,Gama) calculation

        /**
         * Getter for Purity.
         *
         * @return
         */
        public Double getPurity() {
            return purity;
        }

        /**
         * Setter for Purity.
         *
         * @param purity
         */
        public void setPurity(Double purity) {
            this.purity = purity;
        }

        /**
         * Getter for NMI.
         *
         * @return
         */
        public Double getNMI() {
            return NMI;
        }

        /**
         * Setter for NMI.
         *
         * @param NMI
         */
        public void setNMI(Double NMI) {
            this.NMI = NMI;
        }

    }


    /** {@inheritDoc} */
    @Override
    public ValidationMetrics validate(Dataframe predictedData) {
        int n = predictedData.size();

        Set<Object> clusterIdSet = new HashSet<>();
        Set<Object> goldStandardClassesSet = new HashSet<>();
        for(Record r : predictedData) {
            Object y = r.getY();
            if(y != null) {
                goldStandardClassesSet.add(y);
            }
            clusterIdSet.add(r.getYPredicted());
        }

        //create new validation metrics object
        ValidationMetrics validationMetrics = new ValidationMetrics();

        if(goldStandardClassesSet.isEmpty()) {
            return validationMetrics;
        }

        //We don't store the Contingency Table because we can't average it with
        //k-cross fold validation. Each clustering produces a different number
        //of clusters and thus different enumeration. Thus averaging the results
        //is impossible and that is why we don't store it in the validation object.

        //List<Object> = [Clusterid,GoldStandardClass]
        Map<List<Object>, Double> ctMap = new HashMap<>();

        //frequency tables
        Map<Object, Double> countOfW = new HashMap<>(); //this is small equal to number of clusters
        Map<Object, Double> countOfC = new HashMap<>(); //this is small equal to number of classes

        //initialize the tables with zeros
        for(Object clusterId : clusterIdSet) {
            countOfW.put(clusterId, 0.0);
            for(Object theClass : goldStandardClassesSet) {
                ctMap.put(Arrays.asList(clusterId, theClass), 0.0);

                countOfC.put(theClass, 0.0);
            }
        }

        //count the co-occurrences of ClusterId-GoldStanardClass
        for(Record r : predictedData) {
            Object clusterId = r.getYPredicted(); //fetch cluster assignment
            Object goldStandardClass = r.getY(); //the original class of the objervation
            List<Object> tpk = Arrays.asList(clusterId, goldStandardClass);
            ctMap.put(tpk, ctMap.get(tpk) + 1.0);

            //update cluster and class counts
            countOfW.put(clusterId, countOfW.get(clusterId)+1.0);
            countOfC.put(goldStandardClass, countOfC.get(goldStandardClass)+1.0);
        }

        double logN = Math.log((double)n);
        double purity=0.0;
        double Iwc=0.0; //http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html
        for(Object clusterId : clusterIdSet) {
            double maxCounts=Double.NEGATIVE_INFINITY;

            //loop through the possible classes and find the most popular one
            for(Object goldStandardClass : goldStandardClassesSet) {
                List<Object> tpk = Arrays.asList(clusterId, goldStandardClass);
                double Nwc = ctMap.get(tpk);
                if(Nwc>maxCounts) {
                    maxCounts=Nwc;
                }

                if(Nwc>0) {
                    Iwc+= (Nwc/n)*(Math.log(Nwc) -Math.log(countOfC.get(goldStandardClass))
                            -Math.log(countOfW.get(clusterId)) + logN);
                }
            }
            purity += maxCounts;
        }
        //ctMap = null;
        purity/=n;

        validationMetrics.setPurity(purity);

        double entropyW=0.0;
        for(Double Nw : countOfW.values()) {
            entropyW-=(Nw/n)*(Math.log(Nw)-logN);
        }

        double entropyC=0.0;
        for(Double Nc : countOfW.values()) {
            entropyC-=(Nc/n)*(Math.log(Nc)-logN);
        }

        validationMetrics.setNMI(Iwc/((entropyW+entropyC)/2.0));

        return validationMetrics;
    }

    /** {@inheritDoc} */
    @Override
    protected ValidationMetrics calculateAverageValidationMetrics(List<ValidationMetrics> validationMetricsList) {
        if(validationMetricsList.isEmpty()) {
            return null;
        }
        
        int k = validationMetricsList.size(); //number of samples

        
        //create a new empty ValidationMetrics Object
        ValidationMetrics avgValidationMetrics = new ValidationMetrics();
        
        //estimate average values
        for(ValidationMetrics vmSample : validationMetricsList) {
            if(vmSample.getNMI()==null) { //it is null when we don't have goldStandardClass information
                continue; //
            }
            
            //update metrics
            Double NMI = avgValidationMetrics.getNMI();
            if(NMI==null) {
                NMI = 0.0;
            }
            avgValidationMetrics.setNMI(NMI+ vmSample.getNMI()/k);
            Double purity = avgValidationMetrics.getPurity();
            if(purity==null) {
                purity = 0.0;
            }
            avgValidationMetrics.setPurity(purity+ vmSample.getPurity()/k);
        }
        
        
        return avgValidationMetrics;
    }
}
