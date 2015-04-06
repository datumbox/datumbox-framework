/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.MapFunctions;
import com.datumbox.common.utilities.PHPfunctions;
import com.datumbox.common.utilities.TypeConversions;


import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclusterer;
import com.datumbox.framework.mathematics.distances.Distance;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.statistics.sampling.SRS;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class Kmeans extends BaseMLclusterer<Kmeans.Cluster, Kmeans.ModelParameters, Kmeans.TrainingParameters, Kmeans.ValidationMetrics> {

    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    
    public static class Cluster extends BaseMLclusterer.Cluster {
        
        private Record centroid;
        
        private transient AssociativeArray xi_sum;

        public Cluster(int clusterId) {
            super(clusterId);
            centroid = new Record(new AssociativeArray(), null);
            xi_sum = new AssociativeArray();
        }
    
        public Record getCentroid() {
            return centroid;
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
            //No need to implement this method in this algorithm
            throw new UnsupportedOperationException();
        }
        
        public boolean updateClusterParameters() {
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
                
        @Override
        protected void clear() {
            super.clear();
            xi_sum.clear();
        }
    }
    
    public static class ModelParameters extends BaseMLclusterer.ModelParameters<Kmeans.Cluster> {
        
        private int totalIterations;
        
        @BigMap
        private Map<Object, Double> featureWeights; 
        

        public ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
        
        public int getTotalIterations() {
            return totalIterations;
        }

        public void setTotalIterations(int totalIterations) {
            this.totalIterations = totalIterations;
        }

        public Map<Object, Double> getFeatureWeights() {
            return featureWeights;
        }

        public void setFeatureWeights(Map<Object, Double> featureWeights) {
            this.featureWeights = featureWeights;
        }
        

        
        
    } 
    
    
    public static class TrainingParameters extends BaseMLclusterer.TrainingParameters {    

        public enum Initialization {
            FORGY, //Forgy
            RANDOM_PARTITION, //Random Partition
            SET_FIRST_K, //Set First K points as Initial Centroid
            FURTHEST_FIRST, //Furthest First: http://www.cs.utexas.edu/users/inderjit/elkankmeans.ppt
            SUBSET_FURTHEST_FIRST, //Subset Furthest First: http://www.cs.utexas.edu/users/inderjit/elkankmeans.ppt
            PLUS_PLUS; //Kmeans++: http://ilpubs.stanford.edu:8090/778/1/2006-13.pdf    http://www.ima.umn.edu/~iwen/REU/BATS-Means.pdf
        }
        
        public enum Distance {
            EUCLIDIAN,
            MANHATTAN;
        }
        
        //Vars
        
        private int k = 2;
        
        private Initialization initMethod = Initialization.PLUS_PLUS;
        
        private Distance distanceMethod = Distance.EUCLIDIAN;
        
        private int maxIterations = 200;

        private double subsetFurthestFirstcValue = 2;//c>1 This value is used for c*k*log k, Readmore: http://web.cs.swarthmore.edu/~turnbull/Papers/Turnbull_GenreRBF_KDE05.pdf

        private double categoricalGamaMultiplier = 1.0;  //used by Kprototype algorithm, multiplies the the categorical distance with this weight
        
        private boolean weighted = false; //whether the weighted version of the algorithm will run. The weighted version estimates weights for every feature
        
        //Getters Setters
        
        public int getK() {
            return k;
        }

        public void setK(int k) {
            this.k = k;
        }
        
        public Initialization getInitMethod() {
            return initMethod;
        }

        public void setInitMethod(Initialization initMethod) {
            this.initMethod = initMethod;
        }

        public Distance getDistanceMethod() {
            return distanceMethod;
        }

        public void setDistanceMethod(Distance distanceMethod) {
            this.distanceMethod = distanceMethod;
        }

        public int getMaxIterations() {
            return maxIterations;
        }

        public void setMaxIterations(int maxIterations) {
            this.maxIterations = maxIterations;
        }

        public double getSubsetFurthestFirstcValue() {
            return subsetFurthestFirstcValue;
        }

        public void setSubsetFurthestFirstcValue(double subsetFurthestFirstcValue) {
            this.subsetFurthestFirstcValue = subsetFurthestFirstcValue;
        }

        public double getCategoricalGamaMultiplier() {
            return categoricalGamaMultiplier;
        }

        public void setCategoricalGamaMultiplier(double categoricalGamaMultiplier) {
            this.categoricalGamaMultiplier = categoricalGamaMultiplier;
        }

        public boolean isWeighted() {
            return weighted;
        }

        public void setWeighted(boolean weighted) {
            this.weighted = weighted;
        }
        
    } 

    
    public static class ValidationMetrics extends BaseMLclusterer.ValidationMetrics {
        
    }
    
    
    public Kmeans(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, Kmeans.ModelParameters.class, Kmeans.TrainingParameters.class, Kmeans.ValidationMetrics.class);
    } 
    
    @Override
    protected void predictDataset(Dataset newData) { 
        if(newData.isEmpty()) {
            return;
        }
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        Map<Integer, Cluster> clusterList = modelParameters.getClusterList();
        
        for(Integer rId : newData) {
            Record r = newData.get(rId);
            
            AssociativeArray clusterDistances = new AssociativeArray();
            for(Cluster c : clusterList.values()) {
                double distance = calculateDistance(r, c.getCentroid());
                clusterDistances.put(c.getClusterId(), distance);
            }
            
            Descriptives.normalize(clusterDistances);
            
            newData.set(rId, new Record(r.getX(), r.getY(), getSelectedClusterFromDistances(clusterDistances), clusterDistances));
        }
        
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected void _fit(Dataset trainingData) {
        int n = trainingData.size();
        int d = trainingData.getColumnSize();
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        Map<Integer, Cluster> clusterList = modelParameters.getClusterList();
        
        //initialization
        modelParameters.setN(n);
        modelParameters.setD(d);
        
        Set<Object> goldStandardClasses = modelParameters.getGoldStandardClasses();
        
        //check if there are any gold standard classes
        for(Integer rId : trainingData) { 
            Record r = trainingData.get(rId);
            Object theClass=r.getY();
            if(theClass!=null) {
                goldStandardClasses.add(theClass); 
            }
        }
        
        //calculate the weights of the features
        calculateFeatureWeights(trainingData);
        
        //initialize clusters
        initializeClusters(trainingData);
        
        //calculate clusters
        calculateClusters(trainingData);
        
        
        //update the number of clusters
        modelParameters.setC(clusterList.size());
        
    }
    
    /**
     * Estimate the weights of each feature.
     * 
     * @param trainingData 
     */
    private void calculateFeatureWeights(Dataset trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Map<Object, Dataset.ColumnType> columnTypes = trainingData.getColumns();
        
        Map<Object, Double> featureWeights = modelParameters.getFeatureWeights();
        
        if(trainingParameters.isWeighted()==false) {
            //the unweighted version of the algorithm
            double gammaWeight = trainingParameters.getCategoricalGamaMultiplier(); 
            for(Integer rId : trainingData) { 
                Record r = trainingData.get(rId);
                for(Object feature : r.getX().keySet()) {
                    //standard kmeans has equal weights in all numeric features
                    //for categorical, dummy or ordinal feature, use the Gamma Multiplier of Kprototypes
                    double weight = (columnTypes.get(feature)!=Dataset.ColumnType.NUMERICAL)?gammaWeight:1.0;
                    
                    featureWeights.put(feature, weight); 
                }
            }
        }
        else {
            //calculate weights for the features
            
            int n = modelParameters.getN();
            
            DatabaseConnector dbc = knowledgeBase.getDbc();
            
            Map<Object, Double> tmp_categoricalFrequencies = dbc.getBigMap("tmp_categoricalFrequencies", true);
            Map<Object, Double> tmp_varianceSumX = dbc.getBigMap("tmp_varianceSumX", true);
            Map<Object, Double> tmp_varianceSumXsquare = dbc.getBigMap("tmp_varianceSumXsquare", true);
        
            //calculate variance and frequencies
            for(Integer rId : trainingData) { 
                Record r = trainingData.get(rId);
                for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                    Double value = TypeConversions.toDouble(entry.getValue());
                    if(value==null || value==0.0) {
                        continue;
                    }
                    
                    Object feature = entry.getKey();
                    
                    if(columnTypes.get(feature)!=Dataset.ColumnType.NUMERICAL) {
                        Double previousValue = tmp_categoricalFrequencies.get(feature);
                        if(previousValue==null) {
                            previousValue = 0.0;
                        }
                        tmp_categoricalFrequencies.put(feature, previousValue+1.0);
                    }
                    else {
                        Double previousValueSumX = tmp_varianceSumX.get(feature);
                        Double previousValueSumXsquare = tmp_varianceSumXsquare.get(feature);
                        if(previousValueSumX==null) {
                            previousValueSumX = 0.0;
                            previousValueSumXsquare = 0.0;
                        }
                        tmp_varianceSumX.put(feature, previousValueSumX+value);
                        tmp_varianceSumXsquare.put(feature, previousValueSumXsquare+value*value);
                    }
                    
                }
            }
            
            double gammaWeight = trainingParameters.getCategoricalGamaMultiplier();
            
            //estimate weights
            for(Map.Entry<Object, Dataset.ColumnType> entry : columnTypes.entrySet()) {
                Object feature = entry.getKey();
                Dataset.ColumnType type = entry.getValue();
                
                double weight;
                if(type!=Dataset.ColumnType.NUMERICAL) {
                    double percentage = tmp_categoricalFrequencies.get(feature)/n;
                    weight = 1.0 -percentage*percentage;
                }
                else {
                    double mean = tmp_varianceSumX.get(feature)/n;
                    weight = 2.0*((tmp_varianceSumXsquare.get(feature)/n)-mean*mean);
                }
                
                
                if(weight>0) {
                    weight = 1/weight;
                }
                
                //apply muliplier if categorical
                if(type!=Dataset.ColumnType.NUMERICAL) {
                    weight *= gammaWeight;
                }
                
                featureWeights.put(feature, weight);
            }
            
            //Drop the temporary Collection
            dbc.dropBigMap("tmp_categoricalFrequencies", tmp_categoricalFrequencies);
            dbc.dropBigMap("tmp_varianceSumX", tmp_categoricalFrequencies);
            dbc.dropBigMap("tmp_varianceSumXsquare", tmp_categoricalFrequencies);
        }
    }
    
    private double calculateDistance(Record r1, Record r2) {        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Map<Object, Double> featureWeights = modelParameters.getFeatureWeights();
        
        double distance = 0.0;
        TrainingParameters.Distance distanceMethod = trainingParameters.getDistanceMethod();
        if(distanceMethod==TrainingParameters.Distance.EUCLIDIAN) {
            distance = Distance.euclideanWeighhted(r1.getX(), r2.getX(), featureWeights);
        }
        else if(distanceMethod==TrainingParameters.Distance.MANHATTAN) {
            distance = Distance.manhattanWeighhted(r1.getX(), r2.getX(), featureWeights);
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
    
    private void initializeClusters(Dataset trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        int k = trainingParameters.getK();
        TrainingParameters.Initialization initMethod = trainingParameters.getInitMethod();
        
        Map<Integer, Cluster> clusterList = modelParameters.getClusterList();
        if(initMethod==TrainingParameters.Initialization.SET_FIRST_K || 
           initMethod==TrainingParameters.Initialization.FORGY) {
            int i = 0;
            for(Integer rId : trainingData) { 
                Record r = trainingData.get(rId);
                if(i>=k) {
                    break;
                } 
                Integer clusterId= i;
                Cluster c = new Cluster(clusterId);
                c.add(rId, r);
                c.updateClusterParameters();
                clusterList.put(clusterId, c);
                
                ++i;
            }
        }
        else if(initMethod==TrainingParameters.Initialization.RANDOM_PARTITION) {
            int i = 0;
            
            for(Integer rId : trainingData) { 
                Record r = trainingData.get(rId);
                Integer clusterId = i%k;
                
                Cluster c = clusterList.get(clusterId);
                if(c==null) {
                    c = new Cluster(clusterId);
                    clusterList.put(clusterId, c);
                }
                
                c.add(rId, r);
                ++i;
            }
            
            for(Cluster c : clusterList.values()) {
                c.updateClusterParameters();
            }
        }
        else if(initMethod==TrainingParameters.Initialization.FURTHEST_FIRST ||
                initMethod==TrainingParameters.Initialization.SUBSET_FURTHEST_FIRST) {
            
            int sampleSize = modelParameters.getN();
            if(initMethod==TrainingParameters.Initialization.SUBSET_FURTHEST_FIRST) {
                sampleSize = (int)Math.max(Math.ceil(trainingParameters.getSubsetFurthestFirstcValue()*k*PHPfunctions.log(k, 2)), k);
            }
            
            Set<Integer> alreadyAddedPoints = new HashSet(); //this is small. equal to k
            for(int i = 0; i<k; ++i) {
                Integer selectedRecordId = null;
                double maxMinDistance = 0.0;
                
                int samplePointCounter = 0;
                for(Integer rId : trainingData) { 
                    Record r = trainingData.get(rId);
                    if(samplePointCounter>sampleSize) {
                        break; //we only check a small part of the trainingData. This is equivalent to samplying only few observations from, the dataset
                    }
                    else if(alreadyAddedPoints.contains(rId)) {
                        continue; //ignore points that are already selected
                    }
                    
                    double minClusterDistance = Double.MAX_VALUE;
                    for(Cluster c : clusterList.values()) {
                        double distance = calculateDistance(r, c.getCentroid());
                        
                        if(distance<minClusterDistance) {
                            minClusterDistance=distance;
                        }
                    }
                    
                    if(minClusterDistance>maxMinDistance) {
                        maxMinDistance = minClusterDistance;
                        selectedRecordId = rId;
                    }
                    
                    ++samplePointCounter;
                }
                
                alreadyAddedPoints.add(selectedRecordId);
                
                Integer clusterId = clusterList.size();
                Cluster c = new Cluster(clusterId);
                c.add(selectedRecordId, trainingData.get(selectedRecordId));
                c.updateClusterParameters();
                
                clusterList.put(clusterId, c);
            }
            alreadyAddedPoints = null;
        }
        else if(initMethod==TrainingParameters.Initialization.PLUS_PLUS) {
            DatabaseConnector dbc = knowledgeBase.getDbc();
            Set<Integer> alreadyAddedPoints = new HashSet(); //this is small. equal to k
            for(int i = 0; i < k; ++i) {
                Map<Object, Object> tmp_minClusterDistance = dbc.getBigMap("tmp_minClusterDistance", true);
                AssociativeArray minClusterDistanceArray = new AssociativeArray(tmp_minClusterDistance);
                
                for(Integer rId : trainingData) { 
                    Record r = trainingData.get(rId);
                    if(alreadyAddedPoints.contains(rId)) {
                        continue; //ignore points that are already selected
                    }
                    
                    double minClusterDistance = 1.0;
                    if(clusterList.size()>0) {
                        minClusterDistance = Double.MAX_VALUE;
                        for(Cluster c : clusterList.values()) {
                            double distance = calculateDistance(r, c.getCentroid());

                            if(distance<minClusterDistance) {
                                minClusterDistance=distance;
                            }
                        }
                    }
                    
                    minClusterDistanceArray.put(rId, minClusterDistance);
                }
                
                Descriptives.normalize(minClusterDistanceArray);
                Integer selectedRecordId = (Integer)SRS.weightedSampling(minClusterDistanceArray, 1, true).iterator().next();
                
                dbc.dropBigMap("tmp_minClusterDistance", tmp_minClusterDistance);
                minClusterDistanceArray = null;
                
                
                alreadyAddedPoints.add(selectedRecordId);
                
                Integer clusterId = clusterList.size();
                Cluster c = new Cluster(clusterId);
                c.add(selectedRecordId, trainingData.get(selectedRecordId));
                c.updateClusterParameters();
                
                clusterList.put(clusterId, c);
            }
            alreadyAddedPoints = null;
        }
    }

    private void calculateClusters(Dataset trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Map<Integer, Cluster> clusterList = modelParameters.getClusterList();
        
        int maxIterations = trainingParameters.getMaxIterations();
        modelParameters.setTotalIterations(maxIterations);
        
        AssociativeArray clusterDistances = new AssociativeArray();
        for(int iteration=0;iteration<maxIterations;++iteration) {
            logger.debug("Iteration "+iteration);
            
            //reset cluster points
            for(Cluster c : clusterList.values()) {
                c.clear();
            }
            
            //assign records in clusters
            for(Integer rId : trainingData) { 
                Record r = trainingData.get(rId);
                //we are storing cluster references not clusterIds
                for(Cluster c : clusterList.values()) {
                    clusterDistances.put(c, calculateDistance(r, c.getCentroid()));
                }
                
                //find the closest cluster
                Cluster selectedCluster = (Cluster)getSelectedClusterFromDistances(clusterDistances);
                
                //add the record in the cluster
                selectedCluster.add(rId, r);
                
                //clear the distances
                clusterDistances.clear();
            }
            
            //update clusters
            boolean changed=false;
            for(Cluster c : clusterList.values()) {
                changed|=c.updateClusterParameters();
            }
            
            //if none of the clusters changed then exit
            if(changed==false) {
                modelParameters.setTotalIterations(iteration);
                break;
            }
        }
    }
}