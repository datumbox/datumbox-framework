/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
import com.datumbox.common.dataobjects.TypeInference;


import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclusterer;
import com.datumbox.framework.mathematics.distances.Distance;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.statistics.sampling.SRS;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * This class implements the K-means clustering algorithm supporting different 
 * Initialization and distance methods. Several different expansions on the standard
 * algorithm are used to ensure we can handle mixed data (k-representative, k-prototype,
 * etc).
 * 
 * References:
 * http://cs.gsu.edu/~wkim/index_files/papers/kprototype.pdf
 * http://ilpubs.stanford.edu:8090/778/1/2006-13.pdf
 * http://www.ima.umn.edu/~iwen/REU/BATS-Means.pdf
 * http://web.cs.swarthmore.edu/~turnbull/Papers/Turnbull_GenreRBF_KDE05.pdf
 * http://thesis.neminis.org/wp-content/plugins/downloads-manager/upload/masterThesis-VR.pdf
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Kmeans extends BaseMLclusterer<Kmeans.Cluster, Kmeans.ModelParameters, Kmeans.TrainingParameters, Kmeans.ValidationMetrics> {

    /**
     * The Cluster class of the Kmeans model.
     */
    public static class Cluster extends BaseMLclusterer.Cluster {
        
        private Record centroid;
        
        private transient AssociativeArray xi_sum;

        /**
         * Public constructor of Cluster which takes as argument a unique id.
         * 
         * @param clusterId 
         */
        public Cluster(int clusterId) {
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
         * Updates the cluster parameters by estimating again the centroid.
         * 
         * @return 
         */
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
                
        @Override
        protected void clear() {
            super.clear();
            xi_sum.clear();
        }
    }
    
    /**
     * The ModelParameters class stores the coefficients that were learned during
     * the training of the algorithm.
     */
    public static class ModelParameters extends BaseMLclusterer.ModelParameters<Kmeans.Cluster> {
        
        private int totalIterations;
        
        @BigMap
        private Map<Object, Double> featureWeights; 
        
        /**
         * Public constructor which accepts as argument the DatabaseConnector.
         * 
         * @param dbc 
         */
        public ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
        /**
         * Getter for the total number of iterations used in training.
         * 
         * @return 
         */
        public int getTotalIterations() {
            return totalIterations;
        }
        
        /**
         * Setter for the total number of iterations used in training.
         * 
         * @param totalIterations 
         */
        public void setTotalIterations(int totalIterations) {
            this.totalIterations = totalIterations;
        }
        
        /**
         * Getter for the estimated weights of each feature.
         * 
         * @return 
         */
        public Map<Object, Double> getFeatureWeights() {
            return featureWeights;
        }
        
        /**
         * Setter for the estimated weights of each feature.
         * 
         * @param featureWeights 
         */
        public void setFeatureWeights(Map<Object, Double> featureWeights) {
            this.featureWeights = featureWeights;
        }
        
    } 
    
    /**
     * The TrainingParameters class stores the parameters that can be changed
     * before training the algorithm.
     */
    public static class TrainingParameters extends BaseMLclusterer.TrainingParameters {    
        /**
         * The Initialization method that we use.
         */
        public enum Initialization {
            FORGY, //Forgy
            RANDOM_PARTITION, //Random Partition
            SET_FIRST_K, //Set First K points as Initial Centroid
            FURTHEST_FIRST, //Furthest First: http://www.cs.utexas.edu/users/inderjit/elkankmeans.ppt
            SUBSET_FURTHEST_FIRST, //Subset Furthest First: http://www.cs.utexas.edu/users/inderjit/elkankmeans.ppt
            PLUS_PLUS; //Kmeans++: http://ilpubs.stanford.edu:8090/778/1/2006-13.pdf    http://www.ima.umn.edu/~iwen/REU/BATS-Means.pdf
        }
        
        /**
         * The Distance method used in the calculations.
         */
        public enum Distance {
            EUCLIDIAN,
            MANHATTAN;
        }
        
        //Vars
        
        private int k = 2;
        
        private Initialization initializationMethod = Initialization.PLUS_PLUS;
        
        private Distance distanceMethod = Distance.EUCLIDIAN;
        
        private int maxIterations = 200;

        private double subsetFurthestFirstcValue = 2;//c>1 This value is used for c*k*log k, Readmore: http://web.cs.swarthmore.edu/~turnbull/Papers/Turnbull_GenreRBF_KDE05.pdf

        private double categoricalGamaMultiplier = 1.0;  //used by Kprototype algorithm, multiplies the the categorical distance with this weight
        
        private boolean weighted = false; //whether the weighted version of the algorithm will run. The weighted version estimates weights for every feature
        
        //Getters Setters
        /**
         * Getter for the number of clusters k.
         * 
         * @return 
         */
        public int getK() {
            return k;
        }
        
        /**
         * Setter for the number of clusters k.
         * 
         * @param k 
         */
        public void setK(int k) {
            this.k = k;
        }
        
        /**
         * Getter for the initialization method that we use.
         * 
         * @return 
         */
        public Initialization getInitializationMethod() {
            return initializationMethod;
        }
        
        /**
         * Setter for the initialization method that we use.
         * 
         * @param initializationMethod 
         */
        public void setInitializationMethod(Initialization initializationMethod) {
            this.initializationMethod = initializationMethod;
        }
        
        /**
         * Getter for the distance method that we use.
         * 
         * @return 
         */
        public Distance getDistanceMethod() {
            return distanceMethod;
        }
        
        /**
         * Setter for the distance method that we use.
         * 
         * @param distanceMethod 
         */
        public void setDistanceMethod(Distance distanceMethod) {
            this.distanceMethod = distanceMethod;
        }
        
        /**
         * Getter for the maximum permitted iterations during training.
         * 
         * @return 
         */
        public int getMaxIterations() {
            return maxIterations;
        }
        
        /**
         * Setter for the maximum permitted iterations during training.
         * 
         * @param maxIterations 
         */
        public void setMaxIterations(int maxIterations) {
            this.maxIterations = maxIterations;
        }
        
        /**
         * Getter for the C value of the SubsetFurthestFirst initialization method. 
         * 
         * @return 
         */
        public double getSubsetFurthestFirstcValue() {
            return subsetFurthestFirstcValue;
        }
        
        /**
         * Setter for the C value of the SubsetFurthestFirst initialization method.
         * 
         * @param subsetFurthestFirstcValue 
         */
        public void setSubsetFurthestFirstcValue(double subsetFurthestFirstcValue) {
            this.subsetFurthestFirstcValue = subsetFurthestFirstcValue;
        }
        
        /**
         * Getter for the Categorical Gama Multiplier.
         * 
         * @return 
         */
        public double getCategoricalGamaMultiplier() {
            return categoricalGamaMultiplier;
        }
        
        /**
         * Setter for the Categorical Gama Multiplier.
         * 
         * @param categoricalGamaMultiplier 
         */
        public void setCategoricalGamaMultiplier(double categoricalGamaMultiplier) {
            this.categoricalGamaMultiplier = categoricalGamaMultiplier;
        }
        
        /**
         * Getter for whether the algorithm should estimate the weights of the
         * features dynamically.
         * 
         * @return 
         */
        public boolean isWeighted() {
            return weighted;
        }
        
        /**
         * Setter for whether the algorithm should estimate the weights of the
         * features dynamically.
         * 
         * @param weighted 
         */
        public void setWeighted(boolean weighted) {
            this.weighted = weighted;
        }
        
    } 

    /**
     * The ValidationMetrics class stores information about the performance of the
     * algorithm.
     */
    public static class ValidationMetrics extends BaseMLclusterer.ValidationMetrics {
        
    }
    
    /**
     * Public constructor of the algorithm.
     * 
     * @param dbName
     * @param dbConf 
     */
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
        int n = trainingData.getRecordNumber();
        int d = trainingData.getVariableNumber();
        
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
        Map<Object, TypeInference.DataType> columnTypes = trainingData.getXDataTypes();
        
        Map<Object, Double> featureWeights = modelParameters.getFeatureWeights();
        
        if(trainingParameters.isWeighted()==false) {
            //the unweighted version of the algorithm
            double gammaWeight = trainingParameters.getCategoricalGamaMultiplier(); 
            for(Integer rId : trainingData) { 
                Record r = trainingData.get(rId);
                for(Object feature : r.getX().keySet()) {
                    //standard kmeans has equal weights in all numeric features
                    //for categorical, dummy or ordinal feature, use the Gamma Multiplier of Kprototypes
                    double weight = (columnTypes.get(feature)!=TypeInference.DataType.NUMERICAL)?gammaWeight:1.0;
                    
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
                    Double value = TypeInference.toDouble(entry.getValue());
                    if(value==null || value==0.0) {
                        continue;
                    }
                    
                    Object feature = entry.getKey();
                    
                    if(columnTypes.get(feature)!=TypeInference.DataType.NUMERICAL) {
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
            for(Map.Entry<Object, TypeInference.DataType> entry : columnTypes.entrySet()) {
                Object feature = entry.getKey();
                TypeInference.DataType type = entry.getValue();
                
                double weight;
                if(type!=TypeInference.DataType.NUMERICAL) {
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
                if(type!=TypeInference.DataType.NUMERICAL) {
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
            distance = Distance.euclideanWeighted(r1.getX(), r2.getX(), featureWeights);
        }
        else if(distanceMethod==TrainingParameters.Distance.MANHATTAN) {
            distance = Distance.manhattanWeighted(r1.getX(), r2.getX(), featureWeights);
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
        TrainingParameters.Initialization initializationMethod = trainingParameters.getInitializationMethod();
        
        Map<Integer, Cluster> clusterList = modelParameters.getClusterList();
        if(initializationMethod==TrainingParameters.Initialization.SET_FIRST_K || 
           initializationMethod==TrainingParameters.Initialization.FORGY) {
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
        else if(initializationMethod==TrainingParameters.Initialization.RANDOM_PARTITION) {
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
        else if(initializationMethod==TrainingParameters.Initialization.FURTHEST_FIRST ||
                initializationMethod==TrainingParameters.Initialization.SUBSET_FURTHEST_FIRST) {
            
            int sampleSize = modelParameters.getN();
            if(initializationMethod==TrainingParameters.Initialization.SUBSET_FURTHEST_FIRST) {
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
        else if(initializationMethod==TrainingParameters.Initialization.PLUS_PLUS) {
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
            logger.debug("Iteration {}", iteration);
            
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