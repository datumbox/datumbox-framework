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
package com.datumbox.framework.machinelearning.common.bases.mlmodels;

import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.objecttypes.Learnable;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.framework.machinelearning.common.bases.validation.ModelValidation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * Base Class for all the Clustering algorithms.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <CL>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public abstract class BaseMLclusterer<CL extends BaseMLclusterer.Cluster, MP extends BaseMLclusterer.ModelParameters, TP extends BaseMLclusterer.TrainingParameters, VM extends BaseMLclusterer.ValidationMetrics> extends BaseMLmodel<MP, TP, VM> {

    /**
     * The Cluster class stores all the information of the cluster, including the
     * assigned points, the parameters and the label.
     */
    public static abstract class Cluster implements Learnable, Iterable<Integer> {
        /**
         * The id of the cluster.
         */
        protected Integer clusterId;
        
        /**
         * The set which contains the id of the records included in the cluster.
         */
        protected Set<Integer> recordIdSet; 
        
        /**
         * The Y label of the cluster is the "gold standard class" (if available).
         */
        protected Object labelY;
        
        /**
         * Protected Constructor.
         * 
         * @param clusterId 
         */
        protected Cluster(Integer clusterId) {
            this.clusterId = clusterId;
            recordIdSet = new HashSet<>(); //This is large but it should store only references of the Records not the actualy objects
        }

        /**
         * Getter for cluster id.
         * 
         * @return 
         */
        public Integer getClusterId() {
            return clusterId;
        }
        
        /**
         * Getter for the set of Record Ids assigned to this cluster.
         * 
         * @return 
         */
        public Set<Integer> getRecordIdSet() {
            return Collections.unmodifiableSet(recordIdSet);
        }
        
        /**
         * Getter for the label Y of the cluster.
         * 
         * @return 
         */
        public Object getLabelY() {
            return labelY;
        }
        
        /**
         * Setter for the label Y of the cluster.
         * 
         * @param labelY 
         */
        protected void setLabelY(Object labelY) {
            this.labelY = labelY;
        }
        
        /**
         * Returns the number of records assigned to this cluster.
         * 
         * @return 
         */
        public int size() {
            if(recordIdSet == null) {
                return 0;
            }
            return recordIdSet.size();
        }
        
        /**
         * Implements a read-only iterator on recordIdSet to use it in loops.
         * 
         * @return 
         */
        @Override
        public Iterator<Integer> iterator() {
            return new Iterator<Integer>() {
                private final Iterator<Integer> it = recordIdSet.iterator();

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Integer next() {
                    return it.next();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("This is a read-only iterator, remove operation is not supported.");
                }
            };
        }
        
        /**
         * Clears all the assignments and internal parameters of the cluster.
         */
        protected void clear() {
            if(recordIdSet!=null) {
                recordIdSet.clear();
            }
        }
        
        /**
         * Returns the hash of the cluster.
         * 
         * @return 
         */
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + this.clusterId;
            return hash;
        }
        
        /**
         * Checks if another object is equal to the current object.
         * 
         * @param obj
         * @return 
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            else if (!Objects.equals(this.clusterId, ((Cluster) obj).clusterId)) {
                return false;
            }
            return true;
        }
        
        //abstract methods that modify the cluster set and should update its statistics in each implementation
        
        /**
         * Adds the Record r with unique identified rId in the cluster.
         * 
         * @param rId
         * @param r
         * @return 
         */
        protected abstract boolean add(Integer rId, Record r);
        
        /**
         * Removes the Record r with unique identified rId from the cluster.
         * 
         * @param rId
         * @param r
         * @return 
         */
        protected abstract boolean remove(Integer rId, Record r);

    }
    
    /**
     * The ModelParameters class stores the coefficients that were learned during
     * the training of the algorithm.
     * @param <CL>
     */
    public static abstract class ModelParameters<CL extends BaseMLclusterer.Cluster> extends BaseMLmodel.ModelParameters {
        
        //number of classes if the dataset is annotated. Use Linked Hash Set to ensure that the order of classes will be maintained. 
        private Set<Object> goldStandardClasses = new LinkedHashSet<>();
        
        private Map<Integer, CL> clusterList = new HashMap<>(); //the cluster objects of the model

        /**
         * Protected constructor which accepts as argument the DatabaseConnector.
         * 
         * @param dbc 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
        /**
         * Returns the number of clusters.
         * 
         * @return 
         */
        public Integer getC() {
            return clusterList.size();
        }
        
        /**
         * Getter for Gold Standard Classes.
         * 
         * @return 
         */
        public Set<Object> getGoldStandardClasses() {
            return goldStandardClasses;
        }
        
        /**
         * Setter for Gold Standard Classes.
         * 
         * @param goldStandardClasses 
         */
        protected void setGoldStandardClasses(Set<Object> goldStandardClasses) {
            this.goldStandardClasses = goldStandardClasses;
        }
        
        /**
         * Getter for Cluster List.
         * 
         * @return 
         */
        public Map<Integer, CL> getClusterList() {
            return clusterList;
        }
        
        /**
         * Setter for Cluster List.
         * 
         * @param clusterList 
         */
        protected void setClusterList(Map<Integer, CL> clusterList) {
            this.clusterList = clusterList;
        }
        
    } 
    
    /**
     * The TrainingParameters class stores the parameters that can be changed
     * before training the algorithm.
     */
    public static abstract class TrainingParameters extends BaseMLmodel.TrainingParameters {    

    } 

    /**
     * The ValidationMetrics class stores information about the performance of the
     * algorithm.
     * 
     * References: 
     * http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html
     * http://thesis.neminis.org/wp-content/plugins/downloads-manager/upload/masterThesis-VR.pdf
     */
    public static abstract class ValidationMetrics extends BaseMLmodel.ValidationMetrics {
        
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
    
    /**
     * Protected constructor of the clusterer.
     * 
     * @param dbName
     * @param dbConf
     * @param mpClass
     * @param tpClass
     * @param vmClass
     * @param modelValidator 
     */
    protected BaseMLclusterer(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass, ModelValidation<MP, TP, VM> modelValidator) {
        super(dbName, dbConf, mpClass, tpClass, vmClass, modelValidator);
    } 
    
    /**
     * Validates the model with the provided dataset and returns the validation
     * metrics.
     * 
     * @param validationData
     * @return 
     */
    @Override
    protected VM validateModel(Dataframe validationData) {
        predictDataset(validationData);
        
        int n = validationData.size();
        
        MP modelParameters = knowledgeBase.getModelParameters();
        Map<Integer, CL> clusterList = modelParameters.getClusterList();
        Set<Object> goldStandardClassesSet = modelParameters.getGoldStandardClasses();
        
        //create new validation metrics object
        VM validationMetrics = knowledgeBase.getEmptyValidationMetricsObject();
        
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
        Map<Integer, Double> countOfW = new HashMap<>(); //this is small equal to number of clusters
        Map<Object, Double> countOfC = new HashMap<>(); //this is small equal to number of classes
        
        //initialize the tables with zeros
        for(CL cluster : clusterList.values()) {
            Integer clusterId = cluster.getClusterId();
            countOfW.put(clusterId, 0.0);
            for(Object theClass : modelParameters.getGoldStandardClasses()) {
                ctMap.put(Arrays.<Object>asList(clusterId, theClass), 0.0);
                
                countOfC.put(theClass, 0.0);
            }
        }
        
        
        //count the co-occurrences of ClusterId-GoldStanardClass
        for(Record r : validationData) {
            Integer clusterId = (Integer) r.getYPredicted(); //fetch cluster assignment
            Object goldStandardClass = r.getY(); //the original class of the objervation
            List<Object> tpk = Arrays.<Object>asList(clusterId, goldStandardClass);
            ctMap.put(tpk, ctMap.get(tpk) + 1.0);
            
            //update cluster and class counts
            countOfW.put(clusterId, countOfW.get(clusterId)+1.0);
            countOfC.put(goldStandardClass, countOfC.get(goldStandardClass)+1.0);
        }
        
        double logN = Math.log((double)n);
        double purity=0.0;
        double Iwc=0.0; //http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html
        for(CL cluster : clusterList.values()) {
            Integer clusterId = cluster.getClusterId();
            double maxCounts=Double.NEGATIVE_INFINITY;
            
            //loop through the possible classes and find the most popular one
            for(Object goldStandardClass : modelParameters.getGoldStandardClasses()) {
                List<Object> tpk = Arrays.<Object>asList(clusterId, goldStandardClass);
                double Nwc = ctMap.get(tpk);
                if(Nwc>maxCounts) {
                    maxCounts=Nwc;
                    cluster.setLabelY(goldStandardClass);
                }
                
                if(Nwc>0) {
                    Iwc+= (Nwc/n)*(Math.log(Nwc) -Math.log(countOfC.get(goldStandardClass))
                                   -Math.log(countOfW.get(clusterId)) + logN);
                }
            }
            purity += maxCounts;
        }
        ctMap = null;
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
    
    /**
     * Getter for the Cluster list.
     * 
     * @return 
     */
    public Map<Integer, CL> getClusters() {
        if(knowledgeBase==null) {
            return null;
        }
        
        return knowledgeBase.getModelParameters().getClusterList();
    }
}