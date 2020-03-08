/**
 * Copyright (C) 2013-2020 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.core.machinelearning.modelselection.metrics;

import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelselection.AbstractMetrics;

import java.util.*;

/**
 * Estimates validation metrics for Clustering models.
 *
 * References:
 * http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html
 * http://thesis.neminis.org/wp-content/plugins/downloads-manager/upload/masterThesis-VR.pdf
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ClusteringMetrics extends AbstractMetrics {
    private static final long serialVersionUID = 1L;

    private double purity = 0.0;
    private double NMI = 0.0; //Normalized Mutual Information: I(Omega,Gama) calculation

    /**
     * Getter for Purity.
     *
     * @return
     */
    public Double getPurity() {
        return purity;
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
     * @param predictedData
     * @see AbstractMetrics#AbstractMetrics(Dataframe)
     */
    public ClusteringMetrics(Dataframe predictedData) {
        super(predictedData);

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

        if(!goldStandardClassesSet.isEmpty()) {
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

            double entropyW=0.0;
            for(Double Nw : countOfW.values()) {
                entropyW-=(Nw/n)*(Math.log(Nw)-logN);
            }

            double entropyC=0.0;
            for(Double Nc : countOfW.values()) {
                entropyC-=(Nc/n)*(Math.log(Nc)-logN);
            }

            purity /= n;
            NMI = Iwc/((entropyW+entropyC)/2.0);
        }
    }

    /**
     * @param validationMetricsList
     * @see AbstractMetrics#AbstractMetrics(List)
     */
    public ClusteringMetrics(List<ClusteringMetrics> validationMetricsList) {
        super(validationMetricsList);

        //estimate average values
        int k = 0;
        for(ClusteringMetrics vmSample : validationMetricsList) {
            NMI += vmSample.getNMI();
            purity += vmSample.getPurity();
            k++;
        }

        if(k>0) {
            NMI /= k;
            purity /= k;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        String sep = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(":").append(sep);
        sb.append("purity=").append(purity).append(sep);
        sb.append("NMI=").append(NMI).append(sep);
        return sb.toString();
    }

}
