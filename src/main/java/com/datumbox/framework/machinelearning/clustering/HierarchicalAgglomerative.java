/* 
 * Copyright (C) 2014 Vasilis Vryniotis <bbriniotis at datumbox.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.datumbox.framework.machinelearning.clustering;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.common.utilities.MapFunctions;
import com.datumbox.configuration.StorageConfiguration;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclusterer;
import com.datumbox.framework.mathematics.distances.Distance;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class HierarchicalAgglomerative extends BaseMLclusterer<HierarchicalAgglomerative.Cluster, HierarchicalAgglomerative.ModelParameters, HierarchicalAgglomerative.TrainingParameters, HierarchicalAgglomerative.ValidationMetrics> {

    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    public static final String SHORT_METHOD_NAME = "HiAgg";
    
    
    public static class Cluster extends BaseMLclusterer.Cluster {
        
        private final Record centroid;
        
        private boolean active=true;
        
        public Cluster(int clusterId) {
            super(clusterId);
            centroid = new Record();
        }
        
        public Record getCentroid() {
            return centroid;
        }

        protected boolean isActive() {
            return active;
        }

        protected void setActive(boolean active) {
            this.active = active;
        }

        @Override
        public boolean add(Record r) {
            boolean result = recordSet.add(r);
            if(result) {
                ++size;
            }
            return result;
        }
        
        @Override
        public boolean addAll(Collection<Record> c) {
            boolean result = recordSet.addAll(c);
            if(result) {
                size+=c.size();
            }
            return result;
        }
        
        @Override
        public boolean remove(Record r) {
            boolean result = recordSet.remove(r);
            if(result) {
                --size;
            }
            return result;
        }
        
        public boolean merge(Cluster c) {
            size += c.size();
            return recordSet.addAll(c.recordSet);
        }
        
        public boolean updateClusterParameters() {
            boolean changed=false;
            
            size = recordSet.size();
            
            AssociativeArray centoidValues = new AssociativeArray(new LinkedHashMap<>());
            for(Record r : recordSet) {
                centoidValues.addValues(r.getX());
            }
            
            if(size>0) {
                centoidValues.multiplyValues(1.0/size);
            }
            if(!centroid.getX().equals(centoidValues)) {
                changed=true;
                centroid.setX(centoidValues);
            }
            
            return changed;
        }
                
    }
    
    public static class ModelParameters extends BaseMLclusterer.ModelParameters<HierarchicalAgglomerative.Cluster> {
          
    } 
    
    
    public static class TrainingParameters extends BaseMLclusterer.TrainingParameters {    
        
        public enum Linkage {
            AVERAGE, //Average Linkage (all points to all points)
            SINGLE, //Nearest Neightbour
            COMPLETE; //Farther Neightbour
        }
        
        public enum Distance {
            EUCLIDIAN,
            MANHATTAN,
            MAXIMUM;
        }
        
        //Vars
        
        private Linkage linkageMethod = Linkage.COMPLETE;
        
        private Distance distanceMethod = Distance.EUCLIDIAN;
        
        private double maxDistanceThreshold = Double.MAX_VALUE;
        
        private double minClustersThreshold = 2;
        
        //Getters Setters
        public Linkage getLinkageMethod() {
            return linkageMethod;
        }

        public void setLinkageMethod(Linkage linkageMethod) {
            this.linkageMethod = linkageMethod;
        }

        public Distance getDistanceMethod() {
            return distanceMethod;
        }

        public void setDistanceMethod(Distance distanceMethod) {
            this.distanceMethod = distanceMethod;
        }

        public double getMaxDistanceThreshold() {
            return maxDistanceThreshold;
        }

        public void setMaxDistanceThreshold(double maxDistanceThreshold) {
            this.maxDistanceThreshold = maxDistanceThreshold;
        }

        public double getMinClustersThreshold() {
            return minClustersThreshold;
        }

        public void setMinClustersThreshold(double minClustersThreshold) {
            this.minClustersThreshold = minClustersThreshold;
        }
        
    } 

    
    public static class ValidationMetrics extends BaseMLclusterer.ValidationMetrics {
        
    }
    
    
    public HierarchicalAgglomerative(String dbName) {
        super(dbName, HierarchicalAgglomerative.ModelParameters.class, HierarchicalAgglomerative.TrainingParameters.class, HierarchicalAgglomerative.ValidationMetrics.class);
    } 

    @Override
    public final String shortMethodName() {
        return SHORT_METHOD_NAME;
    }
    
    @Override
    protected void predictDataset(Dataset newData) { 
        if(newData.isEmpty()) {
            return;
        }
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        Map<Integer, Cluster> clusterList = modelParameters.getClusterList();
        
        for(Record r : newData) {
            
            AssociativeArray clusterDistances = new AssociativeArray();
            for(Cluster c : clusterList.values()) {
                double distance = calculateDistance(r, c.getCentroid());
                clusterDistances.put(c.getClusterId(), distance);
            }
            
            r.setYPredicted(getSelectedClusterFromDistances(clusterDistances));
            
            Descriptives.normalize(clusterDistances);
            r.setYPredictedProbabilities(clusterDistances);
        }
        
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected void estimateModelParameters(Dataset trainingData) {
        int n = trainingData.size();
        int d = trainingData.getColumnSize();
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        //initialization
        modelParameters.setN(n);
        modelParameters.setD(d);
        
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
        
        
        Map<Integer, Cluster> clusterList = modelParameters.getClusterList();
        
        //update the number of clusters
        modelParameters.setC(clusterList.size());
        
        //clear dataclusters
        for(Cluster c : clusterList.values()) {
            c.clear();
        }
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
            throw new RuntimeException("Unsupported Distance method");
        }
        
        return distance;
    } 
    
    private Object getSelectedClusterFromDistances(AssociativeArray clusterDistances) {
        Map.Entry<Object, Object> minEntry = MapFunctions.selectMinKeyValue(clusterDistances);
        
        return minEntry.getKey();
    }
    
    private void calculateClusters(Dataset trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Map<Integer, Cluster> clusterList = modelParameters.getClusterList();
        
        String tmpPrefix=StorageConfiguration.getTmpPrefix();
        
        BigDataStructureFactory bdsf = knowledgeBase.getBdsf();

        BigDataStructureFactory.MapType mapType = knowledgeBase.getMemoryConfiguration().getMapType();
        int LRUsize = knowledgeBase.getMemoryConfiguration().getLRUsize();

        Map<List<Object>, Double> distanceArray = bdsf.getMap(tmpPrefix+"distanceArray", mapType, LRUsize); //it holds the distances between clusters
        Map<Integer, Integer> minClusterDistanceId = bdsf.getMap(tmpPrefix+"minClusterDistanceId", mapType, LRUsize); //it holds the ids of the min distances
        
        
        //initialize clusters, foreach point create a cluster
        Integer clusterId = 0;
        for(Record r : trainingData) {
            Cluster c = new Cluster(clusterId);
            
            c.add(r);
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
                
                distanceArray.put(Arrays.<Object>asList(clusterId1, clusterId2), distance);
                distanceArray.put(Arrays.<Object>asList(clusterId2, clusterId1), distance);
                
                Integer minDistanceId = minClusterDistanceId.get(clusterId1);
                if(minDistanceId==null || distance < distanceArray.get(Arrays.<Object>asList(clusterId1, minDistanceId))) {
                    minClusterDistanceId.put(clusterId1, clusterId2);
                }
            }
        }
        
        //merging process
        boolean continueMerging = true;
        while(continueMerging) {
            continueMerging = mergeClosest(minClusterDistanceId, distanceArray);
            
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
        bdsf.dropTable(tmpPrefix+"distanceArray", distanceArray);
        bdsf.dropTable(tmpPrefix+"minClusterDistanceId", minClusterDistanceId);
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