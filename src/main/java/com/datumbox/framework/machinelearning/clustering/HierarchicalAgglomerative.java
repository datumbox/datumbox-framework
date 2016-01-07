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
package com.datumbox.framework.machinelearning.clustering;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.MapType;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.StorageHint;
import com.datumbox.common.utilities.MapFunctions;

import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclusterer;
import com.datumbox.framework.machinelearning.common.validation.ClustererValidation;
import com.datumbox.framework.mathematics.distances.Distance;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
public class HierarchicalAgglomerative extends BaseMLclusterer<HierarchicalAgglomerative.Cluster, HierarchicalAgglomerative.ModelParameters, HierarchicalAgglomerative.TrainingParameters, HierarchicalAgglomerative.ValidationMetrics> {

    /**
     * The Cluster class of the HierarchicalAgglomerative model.
     */
    public static class Cluster extends BaseMLclusterer.Cluster {
        private static final long serialVersionUID = 1L;
        
        private Record centroid;
        
        private boolean active = true;
        
        private final transient AssociativeArray xi_sum;
        
        /** 
         * @see com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclusterer.Cluster 
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
         * @return 
         */
        protected boolean merge(Cluster c) {
            xi_sum.addValues(c.xi_sum);
            return recordIdSet.addAll(c.recordIdSet);
        }
        
        /**
         * Updates the cluster parameters.
         * 
         * @return 
         */
        protected boolean updateClusterParameters() {
            boolean changed=false;
            
            int size = recordIdSet.size();
            
            AssociativeArray centoidValues = new AssociativeArray();
            centoidValues.addValues(xi_sum);
            
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

        @Override
        protected boolean add(Integer rId, Record r) {
            boolean result = recordIdSet.add(rId);
            if(result) {
                xi_sum.addValues(r.getX());
            }
            return result;
        }
        
        @Override
        protected boolean remove(Integer rId, Record r) {
            throw new UnsupportedOperationException("Remove operation is not supported.");
        }
                
        @Override
        protected void clear() {
            super.clear();
            xi_sum.clear();
        }
    }
    
    /** {@inheritDoc} */
    public static class ModelParameters extends BaseMLclusterer.ModelParameters<HierarchicalAgglomerative.Cluster> {
        private static final long serialVersionUID = 1L;
          
        /** 
         * @see com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseModelParameters#BaseModelParameters(com.datumbox.common.persistentstorage.interfaces.DatabaseConnector) 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
    } 
    
    /** {@inheritDoc} */
    public static class TrainingParameters extends BaseMLclusterer.TrainingParameters {  
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
    
    /** {@inheritDoc} */
    public static class ValidationMetrics extends BaseMLclusterer.ValidationMetrics {
        private static final long serialVersionUID = 1L;
        
    }
    
    /**
     * Public constructor of the algorithm.
     * 
     * @param dbName
     * @param dbConf 
     */
    public HierarchicalAgglomerative(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, HierarchicalAgglomerative.ModelParameters.class, HierarchicalAgglomerative.TrainingParameters.class, HierarchicalAgglomerative.ValidationMetrics.class, new ClustererValidation<>());
    } 
    
    @Override
    protected void predictDataset(Dataframe newData) { 
        if(newData.isEmpty()) {
            return;
        }
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        Map<Integer, Cluster> clusterList = modelParameters.getClusterList();
        
        for(Map.Entry<Integer, Record> e : newData.entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            
            AssociativeArray clusterDistances = new AssociativeArray();
            for(Cluster c : clusterList.values()) {
                double distance = calculateDistance(r, c.getCentroid());
                clusterDistances.put(c.getClusterId(), distance);
            }
            
            Descriptives.normalize(clusterDistances);
            
            newData._unsafe_set(rId, new Record(r.getX(), r.getY(), getSelectedClusterFromDistances(clusterDistances), clusterDistances));
        }
        
    }
    
    @Override
    @SuppressWarnings("unchecked")
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
        
        
    }
    
    private double calculateDistance(Record r1, Record r2) {        
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        double distance = 0.0;
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
        Map.Entry<Object, Object> minEntry = MapFunctions.selectMinKeyValue(clusterDistances);
        
        return minEntry.getKey();
    }
    
    private void calculateClusters(Dataframe trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Map<Integer, Cluster> clusterList = modelParameters.getClusterList();
        
        DatabaseConnector dbc = knowledgeBase.getDbc();

        Map<List<Object>, Double> tmp_distanceArray = dbc.getBigMap("tmp_distanceArray", MapType.HASHMAP, StorageHint.IN_MEMORY, true); //it holds the distances between clusters
        Map<Integer, Integer> tmp_minClusterDistanceId = dbc.getBigMap("tmp_minClusterDistanceId", MapType.HASHMAP, StorageHint.IN_MEMORY, true); //it holds the ids of the min distances
        
        
        //initialize clusters, foreach point create a cluster
        Integer clusterId = 0;
        for(Map.Entry<Integer, Record> e : trainingData.entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            Cluster c = new Cluster(clusterId);
            
            c.add(rId, r);
            c.setActive(true);
            c.updateClusterParameters();
            clusterList.put(clusterId, c);
            
            ++clusterId;
        }
        
        //calculate distance table and minimum distances
        for(Map.Entry<Integer, Cluster> entry1 : clusterList.entrySet()) {
            Integer clusterId1 = entry1.getKey();
            Cluster c1 = entry1.getValue();
            
            for(Map.Entry<Integer, Cluster> entry2 : clusterList.entrySet()) {
                Integer clusterId2 = entry2.getKey();
                Cluster c2 = entry2.getValue();
                
                double distance = Double.MAX_VALUE;
                if(!Objects.equals(clusterId1, clusterId2)) {
                    distance = calculateDistance(c1.getCentroid(), c2.getCentroid());
                }
                
                tmp_distanceArray.put(Arrays.<Object>asList(clusterId1, clusterId2), distance);
                tmp_distanceArray.put(Arrays.<Object>asList(clusterId2, clusterId1), distance);
                
                Integer minDistanceId = tmp_minClusterDistanceId.get(clusterId1);
                if(minDistanceId==null || distance < tmp_distanceArray.get(Arrays.<Object>asList(clusterId1, minDistanceId))) {
                    tmp_minClusterDistanceId.put(clusterId1, clusterId2);
                }
            }
        }
        
        //merging process
        boolean continueMerging = true;
        while(continueMerging) {
            continueMerging = mergeClosest(tmp_minClusterDistanceId, tmp_distanceArray);
            
            //count all the active clusters
            int activeClusters = 0;
            for(Cluster c : clusterList.values()) {
                if(c.isActive()) {
                    ++activeClusters;
                }
            }
            
            if(activeClusters<=trainingParameters.getMinClustersThreshold()) {
                continueMerging = false;
            }
        }
        
        //update centroids. it does not update their IDs
        Iterator<Map.Entry<Integer, Cluster>> it = clusterList.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Integer, Cluster> entry = it.next();
            Cluster cluster = entry.getValue();
            if(cluster.isActive()) {
                cluster.updateClusterParameters();
            }
            else {
                it.remove(); //remove inactive clusters
            }
        }
        
        //Drop the temporary Collection
        dbc.dropBigMap("tmp_distanceArray", tmp_distanceArray);
        dbc.dropBigMap("tmp_minClusterDistanceId", tmp_minClusterDistanceId);
    }
    
    private boolean mergeClosest(Map<Integer, Integer> minClusterDistanceId, Map<List<Object>, Double> distanceArray) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Map<Integer, Cluster> clusterList = modelParameters.getClusterList();
        
        //find the two closest clusters 
        Integer minClusterId = null;
        double minDistance = Double.MAX_VALUE;
        
        
        for(Map.Entry<Integer, Cluster> entry : clusterList.entrySet()) {
            Integer clusterId = entry.getKey();
            if(entry.getValue().isActive()==false) {
                continue; //skip inactive clusters
            }
            
            double distance = distanceArray.get(Arrays.<Object>asList(clusterId, minClusterDistanceId.get(clusterId)));
            if(distance<minDistance) {
                minClusterId = clusterId;
                minDistance = distance;
            }
        }
        
        if(minDistance>=trainingParameters.getMaxDistanceThreshold()) {
            return false;
        }
        
        Integer clusterToBeMergedId = minClusterDistanceId.get(minClusterId);        
        Cluster c1 = clusterList.get(minClusterId);
        Cluster c2 = clusterList.get(clusterToBeMergedId);
        
        double c1Size = c1.size();
        double c2Size = c2.size();
        
        //merge together the two closest clusters
        c1.merge(c2);
        c2.setActive(false); //set the cluster that we just merged inactive
        
        
        //update the distances with the merged cluster
        TrainingParameters.Linkage linkageMethod = trainingParameters.getLinkageMethod();
        for(Map.Entry<Integer, Cluster> entry : clusterList.entrySet()) {
            //Integer clusterId = entry.getKey();
            Cluster ci = entry.getValue();
            if(ci.isActive()==false) {
                continue; //skip inactive clusters
            }
            
            double distance;
            if(Objects.equals(c1.getClusterId(), ci.getClusterId())) {
                distance = Double.MAX_VALUE;
            }
            else if(linkageMethod==TrainingParameters.Linkage.SINGLE) {
                double c1ciDistance = distanceArray.get(Arrays.<Object>asList(c1.getClusterId(), ci.getClusterId()));
                double c2ciDistance = distanceArray.get(Arrays.<Object>asList(c2.getClusterId(), ci.getClusterId()));
                distance = Math.min(c1ciDistance, c2ciDistance);
            }
            else if(linkageMethod==TrainingParameters.Linkage.COMPLETE) {
                double c1ciDistance = distanceArray.get(Arrays.<Object>asList(c1.getClusterId(), ci.getClusterId()));
                double c2ciDistance = distanceArray.get(Arrays.<Object>asList(c2.getClusterId(), ci.getClusterId()));
                distance = Math.max(c1ciDistance, c2ciDistance);
            }
            else if(linkageMethod==TrainingParameters.Linkage.AVERAGE) {
                double c1ciDistance = distanceArray.get(Arrays.<Object>asList(c1.getClusterId(), ci.getClusterId()));
                double c2ciDistance = distanceArray.get(Arrays.<Object>asList(c2.getClusterId(), ci.getClusterId()));
                distance = (c1ciDistance*c1Size + c2ciDistance*c2Size)/(c1Size+c2Size);
            }
            else {
                distance = calculateDistance(c1.getCentroid(), ci.getCentroid());
            }
            
            distanceArray.put(Arrays.<Object>asList(c1.getClusterId(), ci.getClusterId()), distance);
            distanceArray.put(Arrays.<Object>asList(ci.getClusterId(), c1.getClusterId()), distance);
        }
        
        //update minimum distance ids 
        for(Map.Entry<Integer, Cluster> entry1 : clusterList.entrySet()) {
            Integer id1 = entry1.getKey();
            if(entry1.getValue().isActive()==false) {
                continue; //skip inactive clusters
            }
            
            Integer minDistanceId = minClusterDistanceId.get(id1);
            
            if(Objects.equals(minDistanceId, c1.getClusterId()) || Objects.equals(minDistanceId, c2.getClusterId())) {
                Integer newMinDistanceId = id1;
                
                for(Map.Entry<Integer, Cluster> entry2 : clusterList.entrySet()) {
                    Integer id2 = entry2.getKey();
                    if(entry2.getValue().isActive()==false) {
                        continue; //skip inactive clusters
                    }
            
                    if(distanceArray.get(Arrays.<Object>asList(id1, id2)) < distanceArray.get(Arrays.<Object>asList(id1, newMinDistanceId)) ){
                        newMinDistanceId = id2;
                    }
                }
                
                minClusterDistanceId.put(id1, newMinDistanceId);
            }
        }
        
        
        return true;
    }
}