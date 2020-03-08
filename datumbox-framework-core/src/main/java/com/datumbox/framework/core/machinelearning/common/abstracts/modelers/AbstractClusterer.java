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
package com.datumbox.framework.core.machinelearning.common.abstracts.modelers;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.common.storage.interfaces.BigMap;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.common.storage.interfaces.StorageEngine.MapType;
import com.datumbox.framework.common.storage.interfaces.StorageEngine.StorageHint;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.interfaces.Cluster;

import java.util.*;


/**
 * Base Class for all the Clustering algorithms.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <CL>
 * @param <MP>
 * @param <TP>
 */
public abstract class AbstractClusterer<CL extends AbstractClusterer.AbstractCluster, MP extends AbstractClusterer.AbstractModelParameters, TP extends AbstractClusterer.AbstractTrainingParameters> extends AbstractModeler<MP, TP> {

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
         * Protected constructor of Cluster which takes as argument a unique id.
         * 
         * @param clusterId 
         */
        protected AbstractCluster(Integer clusterId) {
            this.clusterId = clusterId;
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
        @BigMap(keyClass=Integer.class, valueClass=AbstractCluster.class, mapType=MapType.HASHMAP, storageHint=StorageHint.IN_CACHE, concurrent=false)
        private Map<Integer, CL> clusterMap;

        /** 
         * @param storageEngine
         * @see AbstractModeler.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected AbstractModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
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
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainingParameters, Configuration)
     */
    protected AbstractClusterer(TP trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected AbstractClusterer(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }
    
    /**
     * Clears the clusters from their internal caching parameters before storing
     * them. It should be called in the _fit() method of each Clusterer.
     */
    protected void clearClusters() {
        MP modelParameters = knowledgeBase.getModelParameters();
        Map<Integer, CL> clusterMap = modelParameters.getClusterMap();
        for(Map.Entry<Integer, CL> e : clusterMap.entrySet()) {
            Integer clusterId = e.getKey();
            CL c = e.getValue();
            c.clear();
            clusterMap.put(clusterId, c);
        }
    }

    /**
     * Getter for the AbstractCluster list.
     * 
     * @return 
     */
    public Map<Integer, CL> getClusters() {
        return knowledgeBase.getModelParameters().getClusterMap();
    }

}