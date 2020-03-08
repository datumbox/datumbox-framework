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
package com.datumbox.framework.core.machinelearning.clustering;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.concurrency.ForkJoinStream;
import com.datumbox.framework.common.concurrency.StreamMethods;
import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.common.storage.interfaces.StorageEngine.MapType;
import com.datumbox.framework.common.storage.interfaces.StorageEngine.StorageHint;
import com.datumbox.framework.core.common.utilities.MapMethods;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelers.AbstractClusterer;
import com.datumbox.framework.core.machinelearning.common.interfaces.PredictParallelizable;
import com.datumbox.framework.core.machinelearning.common.interfaces.TrainParallelizable;
import com.datumbox.framework.core.mathematics.distances.Distance;
import com.datumbox.framework.core.statistics.descriptivestatistics.Descriptives;

import java.util.*;

/**
 * This class implements the Hierarchical Agglomerative clustering algorithm
 * supporting different Linkage and Distance methods.
 * 
 * References:
 * http://nlp.stanford.edu/IR-book/html/htmledition/hierarchical-agglomerative-clustering-1.html
 * http://php-nlp-tools.com/posts/faster-hierarchical-clustering.html
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class HierarchicalAgglomerative extends AbstractClusterer<HierarchicalAgglomerative.Cluster, HierarchicalAgglomerative.ModelParameters, HierarchicalAgglomerative.TrainingParameters> implements PredictParallelizable, TrainParallelizable {

    /** {@inheritDoc} */
    public static class Cluster extends AbstractClusterer.AbstractCluster {
        private static final long serialVersionUID = 1L;
        
        private Record centroid;
        
        private boolean active = true;
        
        private final AssociativeArray xi_sum;
        
        /** 
         * @param clusterId
         * @see AbstractCluster#AbstractCluster(java.lang.Integer)
         */
        protected Cluster(int clusterId) {
            super(clusterId);
            centroid = new Record(new AssociativeArray(), null);
            xi_sum = new AssociativeArray();
        }
        
        /**
         * Returns the centroid of the cluster.
         * 
         * @return 
         */
        public Record getCentroid() {
            return centroid;
        }
        
        /**
         * Merges this cluster with the provided cluster.
         * 
         * @param c
         */
        protected void merge(Cluster c) {
            xi_sum.addValues(c.xi_sum);
            size += c.size;
        }
        
        /**
         * Updates the cluster parameters.
         * 
         * @return 
         */
        protected boolean updateClusterParameters() {
            boolean changed=false;
            
            AssociativeArray centoidValues = xi_sum.copy();
            
            if(size>0) {
                centoidValues.multiplyValues(1.0/size);
            }
            if(!centroid.getX().equals(centoidValues)) {
                changed=true;
                centroid = new Record(centoidValues, centroid.getY());
            }
            
            return changed;
        }
        
        /**
         * Getter for whether the cluster is active.
         * 
         * @return 
         */
        protected boolean isActive() {
            return active;
        }
        
        /**
         * Setter for whether the cluster is active.
         * 
         * @param active 
         */
        protected void setActive(boolean active) {
            this.active = active;
        }
        
        /** {@inheritDoc} */
        @Override
        protected void add(Record r) {
            size++;
            xi_sum.addValues(r.getX());
        }
        
        /** {@inheritDoc} */
        @Override
        protected void remove(Record r) {
            throw new UnsupportedOperationException("Remove operation is not supported.");
        }
                
        /** {@inheritDoc} */
        @Override
        protected void clear() {
            xi_sum.clear();
        }
    }
    
    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractClusterer.AbstractModelParameters<HierarchicalAgglomerative.Cluster> {
        private static final long serialVersionUID = 1L;
          
        /** 
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected ModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
        }
        
    } 
    
    /** {@inheritDoc} */
    public static class TrainingParameters extends AbstractClusterer.AbstractTrainingParameters {
        private static final long serialVersionUID = 1L;
        
        /**
         * The Linkage method used in the calculations.
         */
        public enum Linkage {
            /**
             * Average Linkage (all points to all points).
             */
            AVERAGE,
            
            /**
             * Nearest Neighbour.
             */
            SINGLE,
            
            /**
             * Farther Neighbour.
             */
            COMPLETE;
        }
        
        /**
         * The Distance method used in the calculations.
         */
        public enum Distance {
            /**
             * Euclidian distance.
             */
            EUCLIDIAN,
            
            /**
             * Manhattan distance.
             */
            MANHATTAN,
            
            /**
             * Maximum distance.
             */
            MAXIMUM;
        }
        
        //Vars
        
        private Linkage linkageMethod = Linkage.COMPLETE;
        
        private Distance distanceMethod = Distance.EUCLIDIAN;
        
        private double maxDistanceThreshold = Double.MAX_VALUE;
        
        private double minClustersThreshold = 2;
        
        //Getters Setters
        /**
         * Getter for Linkage Method.
         * 
         * @return 
         */
        public Linkage getLinkageMethod() {
            return linkageMethod;
        }
        
        /**
         * Setter for Linkage Method.
         * 
         * @param linkageMethod 
         */
        public void setLinkageMethod(Linkage linkageMethod) {
            this.linkageMethod = linkageMethod;
        }
        
        /**
         * Getter for Distance Method.
         * 
         * @return 
         */
        public Distance getDistanceMethod() {
            return distanceMethod;
        }
        
        /**
         * Setter for Distance Method.
         * 
         * @param distanceMethod 
         */
        public void setDistanceMethod(Distance distanceMethod) {
            this.distanceMethod = distanceMethod;
        }
        
        /**
         * Getter for the maximum distance threshold.
         * 
         * @return 
         */
        public double getMaxDistanceThreshold() {
            return maxDistanceThreshold;
        }
        
        /**
         * Setter for the maximum distance threshold.
         * 
         * @param maxDistanceThreshold 
         */
        public void setMaxDistanceThreshold(double maxDistanceThreshold) {
            this.maxDistanceThreshold = maxDistanceThreshold;
        }

        /**
         * Getter for the minimum clusters threshold.
         * 
         * @return 
         */
        public double getMinClustersThreshold() {
            return minClustersThreshold;
        }
        
        /**
         * Setter for the minimum clusters threshold.
         * 
         * @param minClustersThreshold 
         */
        public void setMinClustersThreshold(double minClustersThreshold) {
            this.minClustersThreshold = minClustersThreshold;
        }
        
    }


    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainingParameters, Configuration)
     */
    protected HierarchicalAgglomerative(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
        streamExecutor = new ForkJoinStream(knowledgeBase.getConfiguration().getConcurrencyConfiguration());
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected HierarchicalAgglomerative(String storageName, Configuration configuration) {
        super(storageName, configuration);
        streamExecutor = new ForkJoinStream(knowledgeBase.getConfiguration().getConcurrencyConfiguration());
    }
    
    private boolean parallelized = true;
    
    /**
     * This executor is used for the parallel processing of streams with custom 
     * Thread pool.
     */
    protected final ForkJoinStream streamExecutor;
    
    /** {@inheritDoc} */
    @Override
    public boolean isParallelized() {
        return parallelized;
    }

    /** {@inheritDoc} */
    @Override
    public void setParallelized(boolean parallelized) {
        this.parallelized = parallelized;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _predict(Dataframe newData) {
        _predictDatasetParallel(newData, knowledgeBase.getStorageEngine(), knowledgeBase.getConfiguration().getConcurrencyConfiguration());
    }

    /** {@inheritDoc} */
    @Override
    public Prediction _predictRecord(Record r) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        Map<Integer, Cluster> clusterMap = modelParameters.getClusterMap();

        AssociativeArray clusterDistances = new AssociativeArray();
        for(Map.Entry<Integer, Cluster> e : clusterMap.entrySet()) {
            Integer clusterId = e.getKey();
            Cluster c = e.getValue();
            double distance = calculateDistance(r, c.getCentroid());
            clusterDistances.put(clusterId, distance);
        }

        Descriptives.normalize(clusterDistances);
        
        return new Prediction(getSelectedClusterFromDistances(clusterDistances), clusterDistances);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        Set<Object> goldStandardClasses = modelParameters.getGoldStandardClasses();
        
        //check if there are any gold standard classes
        for(Record r : trainingData) { 
            Object theClass=r.getY();
            if(theClass!=null) {
                goldStandardClasses.add(theClass); 
            }
        }
        
        
        //calculate clusters
        calculateClusters(trainingData);
        
        clearClusters();
    }
    
    private double calculateDistance(Record r1, Record r2) {        
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        double distance;
        TrainingParameters.Distance distanceMethod = trainingParameters.getDistanceMethod();
        if(distanceMethod==TrainingParameters.Distance.EUCLIDIAN) {
            distance = Distance.euclidean(r1.getX(), r2.getX());
        }
        else if(distanceMethod==TrainingParameters.Distance.MANHATTAN) {
            distance = Distance.manhattan(r1.getX(), r2.getX());
        }
        else if(distanceMethod==TrainingParameters.Distance.MAXIMUM) {
            distance = Distance.maximum(r1.getX(), r2.getX());
        }
        else {
            throw new IllegalArgumentException("Unsupported Distance method.");
        }
        
        return distance;
    } 
    
    private Object getSelectedClusterFromDistances(AssociativeArray clusterDistances) {
        Map.Entry<Object, Object> minEntry = MapMethods.selectMinKeyValue(clusterDistances);
        
        return minEntry.getKey();
    }
    
    private void calculateClusters(Dataframe trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Map<Integer, Cluster> clusterMap = modelParameters.getClusterMap();
        
        StorageEngine storageEngine = knowledgeBase.getStorageEngine();

        Map<List<Object>, Double> tmp_distanceArray = storageEngine.getBigMap("tmp_distanceArray", (Class<List<Object>>)(Class<?>)List.class, Double.class, MapType.HASHMAP, StorageHint.IN_CACHE, true, true); //it holds the distances between clusters
        Map<Integer, Integer> tmp_minClusterDistanceId = storageEngine.getBigMap("tmp_minClusterDistanceId", Integer.class, Integer.class, MapType.HASHMAP, StorageHint.IN_CACHE, true, true); //it holds the ids of the min distances
        
        
        //initialize clusters, foreach point create a cluster
        Integer clusterId = 0;
        for(Record r : trainingData.values()) {
            Cluster c = new Cluster(clusterId);
            
            c.add(r);
            c.updateClusterParameters();
            clusterMap.put(clusterId, c);
            
            ++clusterId;
        }
        
        //calculate distance table and minimum distances
        streamExecutor.forEach(StreamMethods.stream(clusterMap.entrySet().stream(), isParallelized()), entry1 -> {
            Integer clusterId1 = entry1.getKey();
            Cluster c1 = entry1.getValue();
            
            for(Map.Entry<Integer, Cluster> entry2 : clusterMap.entrySet()) {
                Integer clusterId2 = entry2.getKey();
                Cluster c2 = entry2.getValue();
                
                double distance = Double.MAX_VALUE;
                if(!Objects.equals(clusterId1, clusterId2)) {
                    distance = calculateDistance(c1.getCentroid(), c2.getCentroid());
                }
                
                tmp_distanceArray.put(Arrays.asList(clusterId1, clusterId2), distance);
                tmp_distanceArray.put(Arrays.asList(clusterId2, clusterId1), distance);
                
                Integer minDistanceId = tmp_minClusterDistanceId.get(clusterId1);
                if(minDistanceId==null || distance < tmp_distanceArray.get(Arrays.asList(clusterId1, minDistanceId))) {
                    tmp_minClusterDistanceId.put(clusterId1, clusterId2);
                }
            }
        });
        
        //merging process
        boolean continueMerging = true;
        while(continueMerging) {
            continueMerging = mergeClosest(tmp_minClusterDistanceId, tmp_distanceArray);
            
            //count all the active clusters
            int activeClusters = 0;
            for(Cluster c : clusterMap.values()) {
                if(c.isActive()) {
                    ++activeClusters;
                }
            }
            
            if(activeClusters<=trainingParameters.getMinClustersThreshold()) {
                continueMerging = false;
            }
        }
        
        //update centroids. it does not update their IDs
        Iterator<Map.Entry<Integer, Cluster>> it = clusterMap.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Integer, Cluster> entry = it.next();
            Integer cId = entry.getKey();
            Cluster cluster = entry.getValue();
            if(cluster.isActive()) {
                cluster.updateClusterParameters();
                clusterMap.put(cId, cluster);
            }
            else {
                it.remove(); //remove inactive clusters
            }
        }
        
        //Drop the temporary Collection
        storageEngine.dropBigMap("tmp_distanceArray", tmp_distanceArray);
        storageEngine.dropBigMap("tmp_minClusterDistanceId", tmp_minClusterDistanceId);
    }
    
    private boolean mergeClosest(Map<Integer, Integer> minClusterDistanceId, Map<List<Object>, Double> distanceArray) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Map<Integer, Cluster> clusterMap = modelParameters.getClusterMap();
        
        //find the two closest clusters 
        Integer minClusterId = null;
        double minDistance = Double.MAX_VALUE;
        
        
        for(Map.Entry<Integer, Cluster> entry : clusterMap.entrySet()) {
            Integer clusterId = entry.getKey();
            if(entry.getValue().isActive()==false) {
                continue; //skip inactive clusters
            }
            
            double distance = distanceArray.get(Arrays.asList(clusterId, minClusterDistanceId.get(clusterId)));
            if(distance<minDistance) {
                minClusterId = clusterId;
                minDistance = distance;
            }
        }
        
        if(minDistance>=trainingParameters.getMaxDistanceThreshold()) {
            return false;
        }
        
        final Integer clusterThatMergesId = minClusterId;
        final Integer clusterToBeMergedId = minClusterDistanceId.get(clusterThatMergesId);        
        Cluster c1 = clusterMap.get(clusterThatMergesId);
        Cluster c2 = clusterMap.get(clusterToBeMergedId);
        
        double c1Size = c1.size();
        double c2Size = c2.size();
        
        //merge together the two closest clusters
        c1.merge(c2);
        clusterMap.put(clusterThatMergesId, c1);
        c2.setActive(false); //set the cluster that we just merged inactive
        clusterMap.put(clusterToBeMergedId, c2);
        
        //update the distances with the merged cluster
        TrainingParameters.Linkage linkageMethod = trainingParameters.getLinkageMethod();
        streamExecutor.forEach(StreamMethods.stream(clusterMap.entrySet().stream(), isParallelized()), entry -> {
        
            Integer clusterId = entry.getKey();
            Cluster ci = entry.getValue();
            if(ci.isActive()) { //skip inactive clusters
            
                double distance;
                if(Objects.equals(clusterThatMergesId, clusterId)) {
                    distance = Double.MAX_VALUE;
                }
                else if(linkageMethod==TrainingParameters.Linkage.SINGLE) {
                    double c1ciDistance = distanceArray.get(Arrays.asList(clusterThatMergesId, clusterId));
                    double c2ciDistance = distanceArray.get(Arrays.asList(clusterToBeMergedId, clusterId));
                    distance = Math.min(c1ciDistance, c2ciDistance);
                }
                else if(linkageMethod==TrainingParameters.Linkage.COMPLETE) {
                    double c1ciDistance = distanceArray.get(Arrays.asList(clusterThatMergesId, clusterId));
                    double c2ciDistance = distanceArray.get(Arrays.asList(clusterToBeMergedId, clusterId));
                    distance = Math.max(c1ciDistance, c2ciDistance);
                }
                else if(linkageMethod==TrainingParameters.Linkage.AVERAGE) {
                    double c1ciDistance = distanceArray.get(Arrays.asList(clusterThatMergesId, clusterId));
                    double c2ciDistance = distanceArray.get(Arrays.asList(clusterToBeMergedId, clusterId));
                    distance = (c1ciDistance*c1Size + c2ciDistance*c2Size)/(c1Size+c2Size);
                }
                else {
                    distance = calculateDistance(c1.getCentroid(), ci.getCentroid());
                }

                distanceArray.put(Arrays.asList(clusterThatMergesId, clusterId), distance);
                distanceArray.put(Arrays.asList(clusterId, clusterThatMergesId), distance);
            }
        });
        
        //update minimum distance ids
        streamExecutor.forEach(StreamMethods.stream(clusterMap.entrySet().stream(), isParallelized()), entry1 -> {
            
            Integer id1 = entry1.getKey();
            if (entry1.getValue().isActive()) { //skip inactive clusters
                Integer minDistanceId = minClusterDistanceId.get(id1);
                if (Objects.equals(minDistanceId, clusterThatMergesId) || Objects.equals(minDistanceId, clusterToBeMergedId)) {
                    Integer newMinDistanceId = id1;
                    
                    for(Map.Entry<Integer, Cluster> entry2 : clusterMap.entrySet()) {
                        Integer id2 = entry2.getKey();
                        if(entry2.getValue().isActive()==false) {
                            continue; //skip inactive clusters
                        }
                        
                        if(distanceArray.get(Arrays.asList(id1, id2)) < distanceArray.get(Arrays.asList(id1, newMinDistanceId)) ){
                            newMinDistanceId = id2;
                        }
                    }
                    
                    minClusterDistanceId.put(id1, newMinDistanceId);
                }
            }
        });
        
        
        return true;
    }
}