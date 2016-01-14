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
package com.datumbox.framework.machinelearning.common.abstracts.modelers;

import com.datumbox.common.Configuration;
import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.MapType;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.StorageHint;
import com.datumbox.framework.machinelearning.common.abstracts.validators.AbstractValidator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import com.datumbox.framework.machinelearning.common.interfaces.Cluster;


/**
 * Base Class for all the Clustering algorithms.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <CL>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public abstract class AbstractClusterer<CL extends AbstractClusterer.AbstractCluster, MP extends AbstractClusterer.AbstractModelParameters, TP extends AbstractClusterer.AbstractTrainingParameters, VM extends AbstractClusterer.AbstractValidationMetrics> extends AbstractModeler<MP, TP, VM> {

    /** {@inheritDoc} */
    public static abstract class AbstractCluster implements Cluster {

        /**
         * The id of the cluster.
         */
        protected final Integer clusterId;

        /**
         * The number of points assigned to the cluster.
         */
        protected int size = 0;

        /**
         * The Y label of the cluster is the "gold standard class" (if available).
         */
        private Object labelY;

        /**
         * Protected constructor of Cluster which takes as argument a unique id.
         * 
         * @param clusterId 
         */
        protected AbstractCluster(Integer clusterId) {
            this.clusterId = clusterId;
        }
        
        /**
         * Copy constructor, which takes as argument the new cluster id and the
         * cluster that we want to copy. We produce a shallow copy of the object.
         * 
         * @param clusterId
         * @param copy 
         */
        protected AbstractCluster(Integer clusterId, AbstractCluster copy) {
            this.clusterId = clusterId;
            labelY = copy.labelY;
        }

        /** {@inheritDoc} */
        @Override
        public Object getLabelY() {
            return labelY;
        }

        /** {@inheritDoc} */
        @Override
        public int size() {
            return size;
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + this.clusterId;
            return hash;
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            else if (!Objects.equals(this.clusterId, ((AbstractCluster) obj).clusterId)) {
                return false;
            }
            return true;
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
         * Clears all the assignments and internal parameters of the cluster.
         */
        protected abstract void clear();


        //abstract methods that modify the cluster set and should update its statistics in each implementation

        /**
         * Adds the Record r with in the cluster and updates the cluster's parameters.
         * 
         * @param r
         */
        protected abstract void add(Record r);

        /**
         * Removes the Record r from the cluster and updates the cluster's parameters
         * 
         * @param r
         */
        protected abstract void remove(Record r);
    }
    
    /**
     * {@inheritDoc}
     * @param <CL>
     */
    public static abstract class AbstractModelParameters<CL extends AbstractClusterer.AbstractCluster> extends AbstractModeler.AbstractModelParameters {
        
        //number of classes if the dataset is annotated. Use Linked Hash Set to ensure that the order of classes will be maintained. 
        private Set<Object> goldStandardClasses = new LinkedHashSet<>();
        
        /**
         * It stores a map with all the clusters.
         */
        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_CACHE, concurrent=false)
        private Map<Integer, CL> clusterMap;

        /** 
         * @param dbc
         * @see com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer.AbstractModelParameters#AbstractModelParameters(com.datumbox.common.persistentstorage.interfaces.DatabaseConnector) 
         */
        protected AbstractModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
        /**
         * Returns the number of clusters.
         * 
         * @return 
         */
        public Integer getC() {
            if(clusterMap == null) {
                return 0;
            }
            return clusterMap.size();
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
         * Getter for AbstractCluster List.
         * 
         * @return 
         */
        public Map<Integer, CL> getClusterMap() {
            return clusterMap;
        }
        
        /**
         * Setter for AbstractCluster List.
         * 
         * @param clusterMap 
         */
        protected void setClusterMap(Map<Integer, CL> clusterMap) {
            this.clusterMap = clusterMap;
        }
        
    } 
    
    /**
     * 
     * {@inheritDoc}
     * 
     * References: 
     * http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html
     * http://thesis.neminis.org/wp-content/plugins/downloads-manager/upload/masterThesis-VR.pdf
     */
    public static abstract class AbstractValidationMetrics extends AbstractModeler.AbstractValidationMetrics {
        
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
     * @param dbName
     * @param conf
     * @param mpClass
     * @param tpClass
     * @param vmClass
     * @param modelValidator
     * @see com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer#AbstractTrainer(java.lang.String, com.datumbox.common.Configuration, java.lang.Class, java.lang.Class...)  
     */
    protected AbstractClusterer(String dbName, Configuration conf, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass, AbstractValidator<MP, TP, VM> modelValidator) {
        super(dbName, conf, mpClass, tpClass, vmClass, modelValidator);
    } 
    
    /**
     * Clears the clusters from their internal caching parameters before storing
     * them. It should be called in the _fit() method of each Clusterer.
     */
    protected void clearClusters() {
        MP modelParameters = kb().getModelParameters();
        Map<Integer, CL> clusterMap = modelParameters.getClusterMap();
        for(Map.Entry<Integer, CL> e : clusterMap.entrySet()) {
            Integer clusterId = e.getKey();
            CL c = e.getValue();
            c.clear();
            clusterMap.put(clusterId, c);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    protected VM validateModel(Dataframe validationData) {
        _predictDataset(validationData);
        
        int n = validationData.size();
        
        MP modelParameters = kb().getModelParameters();
        Map<Integer, CL> clusterMap = modelParameters.getClusterMap();
        Set<Object> goldStandardClassesSet = modelParameters.getGoldStandardClasses();
        
        //create new validation metrics object
        VM validationMetrics = kb().getEmptyValidationMetricsObject();
        
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
        for(Integer clusterId : clusterMap.keySet()) {
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
        for(Map.Entry<Integer, CL> e : clusterMap.entrySet()) {
            Integer clusterId = e.getKey();
            CL c = e.getValue();
            double maxCounts=Double.NEGATIVE_INFINITY;
            
            //loop through the possible classes and find the most popular one
            for(Object goldStandardClass : modelParameters.getGoldStandardClasses()) {
                List<Object> tpk = Arrays.<Object>asList(clusterId, goldStandardClass);
                double Nwc = ctMap.get(tpk);
                if(Nwc>maxCounts) {
                    maxCounts=Nwc;
                    c.setLabelY(goldStandardClass);
                    clusterMap.put(clusterId, c);
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
    
    /**
     * Getter for the AbstractCluster list.
     * 
     * @return 
     */
    public Map<Integer, CL> getClusters() {
        return kb().getModelParameters().getClusterMap();
    }
}