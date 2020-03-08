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
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.common.storage.interfaces.BigMap;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.common.storage.interfaces.StorageEngine.MapType;
import com.datumbox.framework.common.storage.interfaces.StorageEngine.StorageHint;
import com.datumbox.framework.core.common.utilities.MapMethods;
import com.datumbox.framework.core.common.utilities.PHPMethods;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelers.AbstractClusterer;
import com.datumbox.framework.core.machinelearning.common.interfaces.PredictParallelizable;
import com.datumbox.framework.core.machinelearning.common.interfaces.TrainParallelizable;
import com.datumbox.framework.core.mathematics.distances.Distance;
import com.datumbox.framework.core.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.core.statistics.sampling.SimpleRandomSampling;

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
public class Kmeans extends AbstractClusterer<Kmeans.Cluster, Kmeans.ModelParameters, Kmeans.TrainingParameters> implements PredictParallelizable, TrainParallelizable {

    /** {@inheritDoc} */
    public static class Cluster extends AbstractClusterer.AbstractCluster {
        private static final long serialVersionUID = 1L;
        
        private Record centroid;
        
        private final AssociativeArray xi_sum;
        
        /** 
         * @param clusterId
         * @see AbstractClusterer.AbstractCluster#AbstractCluster(java.lang.Integer)
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
         * Updates the cluster parameters by estimating again the centroid.
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
        
        /**
         * Resets the cluster while keeping the centroid the same. This is a 
         * method required by the implementation of Kmeans and it should not 
         * be linked to the clear() method.
         */
        protected void reset() {
            xi_sum.clear();
            size = 0;
        }
    }
    
    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractClusterer.AbstractModelParameters<Kmeans.Cluster> {
        private static final long serialVersionUID = 1L;
        
        private int totalIterations;
        
        @BigMap(keyClass=Object.class, valueClass=Double.class, mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=true)
        private Map<Object, Double> featureWeights; 
        
        /** 
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected ModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
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
        protected void setTotalIterations(int totalIterations) {
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
        protected void setFeatureWeights(Map<Object, Double> featureWeights) {
            this.featureWeights = featureWeights;
        }
        
    } 
    
    /** {@inheritDoc} */
    public static class TrainingParameters extends AbstractClusterer.AbstractTrainingParameters {    
        private static final long serialVersionUID = 1L;
        
        /**
         * The Initialization method that we use.
         */
        public enum Initialization {
            /**
             * Forgy.
             */
            FORGY,
            
            /**
             * Random Partition.
             */
            RANDOM_PARTITION,
            
            /**
             * Set First K points as Initial Centroid.
             */
            SET_FIRST_K,
            
            /**
             * Furthest First.
             * References: 
             * http://www.cs.utexas.edu/users/inderjit/elkankmeans.ppt
             */
            FURTHEST_FIRST,
            
            /**
             * Subset Furthest First.
             * References: 
             * http://www.cs.utexas.edu/users/inderjit/elkankmeans.ppt
             */
            SUBSET_FURTHEST_FIRST,
            
            /**
             * Kmeans++.
             * References: 
             * http://ilpubs.stanford.edu:8090/778/1/2006-13.pdf
             * http://www.ima.umn.edu/~iwen/REU/BATS-Means.pdf
             */
            PLUS_PLUS;
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
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainingParameters, Configuration)
     */
    protected Kmeans(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
        streamExecutor = new ForkJoinStream(knowledgeBase.getConfiguration().getConcurrencyConfiguration());
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected Kmeans(String storageName, Configuration configuration) {
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
            Cluster c= e.getValue();
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
        
        //calculate the weights of the features
        calculateFeatureWeights(trainingData);
        
        //initialize clusters
        initializeClusters(trainingData);
        
        //calculate clusters
        calculateClusters(trainingData);
        
        clearClusters();
    }
    
    /**
     * Estimate the weights of each feature.
     * 
     * @param trainingData 
     */
    private void calculateFeatureWeights(Dataframe trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Map<Object, TypeInference.DataType> columnTypes = trainingData.getXDataTypes();
        
        Map<Object, Double> featureWeights = modelParameters.getFeatureWeights();
        
        if(trainingParameters.isWeighted()==false) {
            //the unweighted version of the algorithm
            double gammaWeight = trainingParameters.getCategoricalGamaMultiplier();
            streamExecutor.forEach(StreamMethods.stream(columnTypes.entrySet().stream(), isParallelized()), e -> {
                //standard kmeans has equal weights in all numeric features
                //for categorical, dummy or ordinal feature, use the Gamma Multiplier of Kprototypes
                double weight = (e.getValue()!=TypeInference.DataType.NUMERICAL)?gammaWeight:1.0;
                featureWeights.put(e.getKey(), weight);
            });
        }
        else {
            //calculate weights for the features
            
            int n = trainingData.size();
            
            StorageEngine storageEngine = knowledgeBase.getStorageEngine();
            
            Map<Object, Double> tmp_categoricalFrequencies = storageEngine.getBigMap("tmp_categoricalFrequencies", Object.class, Double.class, MapType.HASHMAP, StorageHint.IN_MEMORY, true, true);
            Map<Object, Double> tmp_varianceSumX = storageEngine.getBigMap("tmp_varianceSumX", Object.class, Double.class, MapType.HASHMAP, StorageHint.IN_MEMORY, true, true);
            Map<Object, Double> tmp_varianceSumXsquare = storageEngine.getBigMap("tmp_varianceSumXsquare", Object.class, Double.class, MapType.HASHMAP, StorageHint.IN_MEMORY, true, true);
        
            //calculate variance and frequencies
            for(Record r : trainingData) { 
                for(Map.Entry<Object, Object> e : r.getX().entrySet()) {
                    Double value = TypeInference.toDouble(e.getValue());
                    if (value!=null && value!=0.0) {
                        Object feature = e.getKey();
                        
                        if(columnTypes.get(feature)!=TypeInference.DataType.NUMERICAL) {
                            Double previousValue = tmp_categoricalFrequencies.getOrDefault(feature, 0.0);
                            tmp_categoricalFrequencies.put(feature, previousValue+1.0);
                        }
                        else {
                            Double previousValueSumX = tmp_varianceSumX.getOrDefault(feature, 0.0);
                            Double previousValueSumXsquare = tmp_varianceSumXsquare.getOrDefault(feature, 0.0);
                            tmp_varianceSumX.put(feature, previousValueSumX+value);
                            tmp_varianceSumXsquare.put(feature, previousValueSumXsquare+value*value);
                        }
                    }
                }
            }
            
            double gammaWeight = trainingParameters.getCategoricalGamaMultiplier();
            
            //estimate weights
            streamExecutor.forEach(StreamMethods.stream(columnTypes.entrySet().stream(), isParallelized()), e -> {
                Object feature = e.getKey();
                TypeInference.DataType type = e.getValue();
                
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
            });
            
            //Drop the temporary Collection
            storageEngine.dropBigMap("tmp_categoricalFrequencies", tmp_categoricalFrequencies);
            storageEngine.dropBigMap("tmp_varianceSumX", tmp_categoricalFrequencies);
            storageEngine.dropBigMap("tmp_varianceSumXsquare", tmp_categoricalFrequencies);
        }
    }
    
    private double calculateDistance(Record r1, Record r2) {        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Map<Object, Double> featureWeights = modelParameters.getFeatureWeights();
        
        double distance;
        TrainingParameters.Distance distanceMethod = trainingParameters.getDistanceMethod();
        if(distanceMethod==TrainingParameters.Distance.EUCLIDIAN) {
            distance = Distance.euclideanWeighted(r1.getX(), r2.getX(), featureWeights);
        }
        else if(distanceMethod==TrainingParameters.Distance.MANHATTAN) {
            distance = Distance.manhattanWeighted(r1.getX(), r2.getX(), featureWeights);
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
    
    private void initializeClusters(Dataframe trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        int k = trainingParameters.getK();
        TrainingParameters.Initialization initializationMethod = trainingParameters.getInitializationMethod();
        
        Map<Integer, Cluster> clusterMap = modelParameters.getClusterMap();
        if(initializationMethod==TrainingParameters.Initialization.SET_FIRST_K || 
           initializationMethod==TrainingParameters.Initialization.FORGY) {
            int i = 0;
            for(Record r : trainingData.values()) {
                if(i>=k) {
                    break;
                } 
                Integer clusterId= i;
                Cluster c = new Cluster(clusterId);
                c.add(r);
                c.updateClusterParameters();
                clusterMap.put(clusterId, c);
                
                ++i;
            }
        }
        else if(initializationMethod==TrainingParameters.Initialization.RANDOM_PARTITION) {
            int i = 0;
            
            for(Record r : trainingData.values()) {
                Integer clusterId = i%k;
                
                Cluster c = clusterMap.get(clusterId);
                if(c==null) {
                    c = new Cluster(clusterId);
                }
                
                c.add(r);
                clusterMap.put(clusterId, c);
                ++i;
            }
            
            for(Map.Entry<Integer, Cluster> e : clusterMap.entrySet()) {
                Integer clusterId = e.getKey();
                Cluster c = e.getValue();
                c.updateClusterParameters();
                clusterMap.put(clusterId, c);
            }
        }
        else if(initializationMethod==TrainingParameters.Initialization.FURTHEST_FIRST ||
                initializationMethod==TrainingParameters.Initialization.SUBSET_FURTHEST_FIRST) {
            
            int sampleSize = trainingData.size();
            if(initializationMethod==TrainingParameters.Initialization.SUBSET_FURTHEST_FIRST) {
                sampleSize = (int)Math.max(Math.ceil(trainingParameters.getSubsetFurthestFirstcValue()*k*PHPMethods.log(k, 2)), k);
            }
            
            Set<Integer> alreadyAddedPoints = new HashSet(); //this is small. equal to k
            for(int i = 0; i<k; ++i) {
                Integer selectedRecordId = null;
                double maxMinDistance = 0.0;
                
                int samplePointCounter = 0;
                for(Map.Entry<Integer, Record> e : trainingData.entries()) {
                    Integer rId = e.getKey();
                    Record r = e.getValue();
                    if(samplePointCounter>sampleSize) {
                        break; //we only check a small part of the trainingData. This is equivalent to samplying only few observations from, the dataset
                    }
                    else if(alreadyAddedPoints.contains(rId)) {
                        continue; //ignore points that are already selected
                    }
                    
                    double minClusterDistance = Double.MAX_VALUE;
                    for(Cluster c : clusterMap.values()) {
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
                
                Integer clusterId = clusterMap.size();
                Cluster c = new Cluster(clusterId);
                c.add(trainingData.get(selectedRecordId));
                c.updateClusterParameters();
                
                clusterMap.put(clusterId, c);
            }
            //alreadyAddedPoints = null;
        }
        else if(initializationMethod==TrainingParameters.Initialization.PLUS_PLUS) {
            StorageEngine storageEngine = knowledgeBase.getStorageEngine();
            Set<Integer> alreadyAddedPoints = new HashSet(); //this is small. equal to k
            for(int i = 0; i < k; ++i) {
                Map<Object, Double> tmp_minClusterDistance = storageEngine.getBigMap("tmp_minClusterDistance", Object.class, Double.class, MapType.HASHMAP, StorageHint.IN_MEMORY, true, true);
                AssociativeArray minClusterDistanceArray = new AssociativeArray((Map)tmp_minClusterDistance);
                
                streamExecutor.forEach(StreamMethods.stream(trainingData.entries(), isParallelized()), e -> {
                    Integer rId = e.getKey();
                    Record r = e.getValue();
                    if(alreadyAddedPoints.contains(rId)==false) {
                        double minClusterDistance = 1.0;
                        if(clusterMap.size()>0) {
                            minClusterDistance = Double.MAX_VALUE;
                            for(Cluster c : clusterMap.values()) {
                                double distance = calculateDistance(r, c.getCentroid());

                                if(distance<minClusterDistance) {
                                    minClusterDistance=distance;
                                }
                            }
                        }

                        minClusterDistanceArray.put(rId, minClusterDistance);
                    }
                });
                
                Descriptives.normalize(minClusterDistanceArray);
                Integer selectedRecordId = (Integer) SimpleRandomSampling.weightedSampling(minClusterDistanceArray, 1, true).iterator().next();
                
                storageEngine.dropBigMap("tmp_minClusterDistance", tmp_minClusterDistance);
                
                
                alreadyAddedPoints.add(selectedRecordId);
                
                Integer clusterId = clusterMap.size();
                Cluster c = new Cluster(clusterId);
                c.add(trainingData.get(selectedRecordId));
                c.updateClusterParameters();
                
                clusterMap.put(clusterId, c);
            }
            //alreadyAddedPoints = null;
        }
    }

    private void calculateClusters(Dataframe trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Map<Integer, Cluster> clusterMap = modelParameters.getClusterMap();
        
        int maxIterations = trainingParameters.getMaxIterations();
        modelParameters.setTotalIterations(maxIterations);
        
        for(int iteration=0;iteration<maxIterations;++iteration) {
            logger.debug("Iteration {}", iteration);
            
            //reset cluster points
            for(Map.Entry<Integer, Cluster> entry1 : clusterMap.entrySet()) {
                Integer clusterId = entry1.getKey();
                Cluster cluster = entry1.getValue();
                cluster.reset();
                clusterMap.put(clusterId, cluster);
            }
            
            //assign records to clusters
            Map<Integer, Integer> tmp_clusterAssignments = knowledgeBase.getStorageEngine().getBigMap("tmp_clusterAssignments", Integer.class, Integer.class, MapType.HASHMAP, StorageHint.IN_MEMORY, true, true);
            streamExecutor.forEach(StreamMethods.stream(trainingData.entries(), isParallelized()), e -> {
                Integer rId = e.getKey();
                Record r = e.getValue();
                //we are storing cluster references not clusterIds
                AssociativeArray clusterDistances = new AssociativeArray();
                for(Map.Entry<Integer, Cluster> entry : clusterMap.entrySet()) {
                    Integer cId = entry.getKey();
                    Cluster c = entry.getValue();
                    clusterDistances.put(cId, calculateDistance(r, c.getCentroid()));
                }
                
                //find the closest cluster
                Integer selectedClusterId = (Integer)getSelectedClusterFromDistances(clusterDistances);
                tmp_clusterAssignments.put(rId, selectedClusterId);
            });
            
            for(Map.Entry<Integer, Record> e : trainingData.entries()) {
                Integer rId = e.getKey();
                Record r = e.getValue();
                
                //add the record in the cluster
                Integer selectedClusterId = tmp_clusterAssignments.get(rId);
                Cluster selectedCluster = clusterMap.get(selectedClusterId);
                selectedCluster.add(r);
                clusterMap.put(selectedClusterId, selectedCluster);
            }
            knowledgeBase.getStorageEngine().dropBigMap("tmp_clusterAssignments", tmp_clusterAssignments);
            
            //update clusters
            boolean changed=false;
            for(Map.Entry<Integer, Cluster> e : clusterMap.entrySet()) {
                Integer cId = e.getKey();
                Cluster c = e.getValue();
                changed|=c.updateClusterParameters();
                clusterMap.put(cId, c);
            }
            
            //if none of the clusters changed then exit
            if(changed==false) {
                modelParameters.setTotalIterations(iteration);
                break;
            }
        }
    }

}