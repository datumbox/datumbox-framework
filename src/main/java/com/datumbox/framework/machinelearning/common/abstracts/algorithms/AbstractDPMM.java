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
package com.datumbox.framework.machinelearning.common.abstracts.algorithms;

import com.datumbox.common.concurrency.ForkJoinStream;
import com.datumbox.common.concurrency.StreamMethods;
import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.MapType;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.StorageHint;
import com.datumbox.common.utilities.MapMethods;
import com.datumbox.common.utilities.PHPMethods;
import com.datumbox.framework.machinelearning.common.interfaces.PredictParallelizable;
import com.datumbox.framework.machinelearning.common.interfaces.PredictParallelizable.Prediction;

import com.datumbox.framework.machinelearning.common.abstracts.modelers.AbstractClusterer;
import com.datumbox.framework.machinelearning.common.interfaces.TrainParallelizable;
import com.datumbox.framework.machinelearning.common.validators.ClustererValidator;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.statistics.sampling.SimpleRandomSampling;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Base class for Dirichlet Process Mixtures Models.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <CL>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */

public abstract class AbstractDPMM<CL extends AbstractDPMM.AbstractCluster, MP extends AbstractDPMM.AbstractModelParameters, TP extends AbstractDPMM.AbstractTrainingParameters, VM extends AbstractDPMM.AbstractValidationMetrics> extends AbstractClusterer<CL, MP, TP, VM> implements PredictParallelizable, TrainParallelizable {
    
    /** {@inheritDoc} */
    public static abstract class AbstractCluster extends AbstractClusterer.AbstractCluster {
        /**
         * The featureIds provides a mapping between the column names
         * and their positions on the internal data vector.
         * This is only a reference to the real Map. It is used to convert the 
         * Records to Arrays.
         */
        protected transient Map<Object, Integer> featureIds;
        
        /** 
         * @param clusterId
         * @see com.datumbox.framework.machinelearning.common.abstracts.modelers.AbstractClusterer.AbstractCluster#AbstractCluster(java.lang.Integer) 
         */
        protected AbstractCluster(Integer clusterId) {
            super(clusterId);
        }
        
        /**
         * @param clusterId
         * @param copy 
         * @see com.datumbox.framework.machinelearning.common.abstracts.modelers.AbstractClusterer.AbstractCluster#AbstractCluster(java.lang.Integer, com.datumbox.framework.machinelearning.common.abstracts.modelers.AbstractClusterer.AbstractCluster) 
         */
        protected AbstractCluster(Integer clusterId, AbstractCluster copy) {
            super(clusterId, copy);
            featureIds = copy.featureIds;
        }

        /**
         * Getter for the featureIds which is a mapping between the column names
         * and their positions on the vector.
         * 
         * @return 
         */
        protected abstract Map<Object, Integer> getFeatureIds();
        
        /**
         * Setter for the featureIds which is a mapping between the column names
         * and their positions on the vector.
         * 
         * @param featureIds 
         */
        protected abstract void setFeatureIds(Map<Object, Integer> featureIds);
        
        /**
         * Updates the cluster parameters by using the assigned points.
         */
        protected abstract void updateClusterParameters();
        
        /**
         * Returns the log posterior PDF of a particular point xi, to belong to this
         * cluster.
         * 
         * @param r    The point for which we want to estimate the PDF.
         * @return      The log posterior PDF
         */
        protected abstract double posteriorLogPdf(Record r);
        
        /** {@inheritDoc} */
        @Override
        protected abstract void add(Record r);
        
        /** {@inheritDoc} */
        @Override
        protected abstract void remove(Record r);
        
        /**
         * Produces a shallow copy of the cluster with a newClusterId.
         * 
         * @param newClusterId
         * @return 
         */
        protected abstract AbstractCluster copy2new(Integer newClusterId);
    }
    
    /** 
     * {@inheritDoc}
     * @param <CL> 
     */
    public static abstract class AbstractModelParameters<CL extends AbstractDPMM.AbstractCluster> extends AbstractClusterer.AbstractModelParameters<CL> {
        
        private int totalIterations;

        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=false)
        private Map<Object, Integer> featureIds; //list of all the supported features
        
        /** 
         * @param dbc
         * @see com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer.AbstractModelParameters#AbstractModelParameters(com.datumbox.common.persistentstorage.interfaces.DatabaseConnector) 
         */
        protected AbstractModelParameters(DatabaseConnector dbc) {
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
        protected void setTotalIterations(int totalIterations) {
            this.totalIterations = totalIterations;
        }

        /**
         * Getter for the featureIds which is a mapping between the column names
         * and their positions on the vector.
         * 
         * @return 
         */
        public Map<Object, Integer> getFeatureIds() {
            return featureIds;
        }

        /**
         * Setter for the featureIds which is a mapping between the column names
         * and their positions on the vector.
         * 
         * @param featureIds 
         */
        protected void setFeatureIds(Map<Object, Integer> featureIds) {
            this.featureIds = featureIds;
        }
        
    }
    
    /** {@inheritDoc} */
    public static abstract class AbstractTrainingParameters extends AbstractClusterer.AbstractTrainingParameters {   
        /**
         * Alpha value of Dirichlet process
         */
        private double alpha;    
        
        private int maxIterations = 1000;
        
        /**
         * Initialization method enum.
         */
        public enum Initialization {
            /**
             * One cluster per record.
             */
            ONE_CLUSTER_PER_RECORD,
            
            /**
             * Random Assignment in a*log(n) clusters.
             */
            RANDOM_ASSIGNMENT;
        }
        
        //My optimization: it controls whether the algorithm will be initialized with every observation
        //being a separate cluster. If this is turned off, then alpha*log(n) clusters
        //are generated and the observations are assigned randomly in it.
        private Initialization initializationMethod = Initialization.ONE_CLUSTER_PER_RECORD; 
        
        /**
         * Getter for Alpha hyperparameter.
         * 
         * @return 
         */
        public double getAlpha() {
            return alpha;
        }
        
        /**
         * Setter for Alpha hyperparameter.
         * 
         * @param alpha 
         */
        public void setAlpha(double alpha) {
            this.alpha = alpha;
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
        
    }
    
    /** 
     * @param dbName
     * @param dbConf
     * @param mpClass
     * @param tpClass
     * @param vmClass
     * @see com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer#AbstractTrainer(java.lang.String, com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration, java.lang.Class, java.lang.Class...)  
     */
    protected AbstractDPMM(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass) {
        super(dbName, dbConf, mpClass, tpClass, vmClass, new ClustererValidator<>());
        streamExecutor = new ForkJoinStream();
    } 
    
    private boolean parallelized = true;
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
    protected void _predictDataset(Dataframe newData) {
        DatabaseConnector dbc = kb().getDbc();
        Map<Integer, Prediction> resultsBuffer = dbc.getBigMap("tmp_resultsBuffer", MapType.HASHMAP, StorageHint.IN_DISK, true, true);
        _predictDatasetParallel(newData, resultsBuffer);
        dbc.dropBigMap("tmp_resultsBuffer", resultsBuffer);
    }

    /** {@inheritDoc} */
    @Override
    public Prediction _predictRecord(Record r) {
        AbstractModelParameters modelParameters = kb().getModelParameters();
        Map<Integer, CL> clusterMap = modelParameters.getClusterMap();
        
        AssociativeArray clusterScores = new AssociativeArray();
        for(Integer clusterId : clusterMap.keySet()) {
            CL c = getFromClusterMap(clusterId, clusterMap);
            double probability = c.posteriorLogPdf(r);
            clusterScores.put(clusterId, probability);
        }
        
        Descriptives.normalizeExp(clusterScores);

        return new Prediction(getSelectedClusterFromScores(clusterScores), clusterScores);
    }

    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        AbstractModelParameters modelParameters = kb().getModelParameters();
        
        Set<Object> goldStandardClasses = modelParameters.getGoldStandardClasses();
        Map<Object, Integer> featureIds = modelParameters.getFeatureIds();
        
        //check if there are any gold standard classes and buld the featureIds maps
        int previousFeatureId = 0;
        for(Record r : trainingData) { 
            Object theClass=r.getY();
            if(theClass!=null) {
                goldStandardClasses.add(theClass); 
            }
            
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                if(featureIds.putIfAbsent(feature, previousFeatureId) == null) {
                    previousFeatureId++;
                }
            }
        }
        
        //perform Gibbs Sampling and calculate the clusters
        int totalIterations = collapsedGibbsSampling(trainingData);
        
        //set the actual iterations performed
        modelParameters.setTotalIterations(totalIterations);
        
        clearClusters();
    }
    
    /**
     * Always use this method to get the cluster from the clusterMap because it 
     * ensures that the featureIds are set.
     * The featureIds can be unset if we use a data structure which stores
     * stuff in file. Since the featureIds field of cluster is transient, the
     * information gets lost. This function ensures that it sets it back.
     * 
     * @param clusterId
     * @param clusterMap
     * @return 
     */
    private CL getFromClusterMap(int clusterId, Map<Integer, CL> clusterMap) {
        CL c = clusterMap.get(clusterId);
        if(c.getFeatureIds() == null) {
            c.setFeatureIds(kb().getModelParameters().getFeatureIds()); //fetch the featureIds from model parameters object
        }
        return c;
    }
    
    /**
     * Implementation of Collapsed Gibbs Sampling algorithm.
     * 
     * @param dataset The list of points that we want to cluster
     * @param maxIterations The maximum number of iterations
     */
    private int collapsedGibbsSampling(Dataframe dataset) {
        AbstractModelParameters modelParameters = kb().getModelParameters();
        
        Map<Integer, CL> tempClusterMap = kb().getDbc().getBigMap("tmp_tempClusterMap", MapType.HASHMAP, StorageHint.IN_CACHE, false, true);
        tempClusterMap.putAll(modelParameters.getClusterMap());
        AbstractTrainingParameters trainingParameters = kb().getTrainingParameters();
        
        double alpha = trainingParameters.getAlpha();
        
        //Initialize clusters, create a cluster for every xi
        Integer newClusterId = tempClusterMap.size(); //start counting the Ids based on clusters in the list

        if(trainingParameters.getInitializationMethod()==AbstractTrainingParameters.Initialization.ONE_CLUSTER_PER_RECORD) {
            for(Map.Entry<Integer, Record> e : dataset.entries()) {
                Integer rId = e.getKey();
                Record r = e.getValue();
                
                //generate a new cluster
                CL cluster = createNewCluster(newClusterId);
                cluster.add(r);
                tempClusterMap.put(newClusterId, cluster);

                //add the record in the new cluster
                r = new Record(r.getX(), r.getY(), newClusterId, r.getYPredictedProbabilities());
                dataset._unsafe_set(rId, r);

                ++newClusterId;
            }            
        }
        else {
            int numberOfNewClusters = (int)(Math.max(alpha, 1)*Math.log(dataset.size())); //a*log(n) clusters on average
            if(numberOfNewClusters<=0) {
                numberOfNewClusters=1;
            }
            
            //generate new clusters
            for(int i=0;i<numberOfNewClusters;++i) {
                //generate a new cluster
                CL cluster = createNewCluster(newClusterId);
                tempClusterMap.put(newClusterId, cluster);

                ++newClusterId;
            }
            
            int clusterMapSize = newClusterId;
            for(Map.Entry<Integer, Record> e : dataset.entries()) {
                Integer rId = e.getKey();
                Record r = e.getValue();
                
                Integer assignedClusterId = PHPMethods.mt_rand(0, clusterMapSize-1);
                
                r = new Record(r.getX(), r.getY(), assignedClusterId, r.getYPredictedProbabilities());
                dataset._unsafe_set(rId, r);
                
                CL c = getFromClusterMap(assignedClusterId, tempClusterMap);
                c.add(r);
                tempClusterMap.put(assignedClusterId, c);
            }
        }

                
        int n = tempClusterMap.size();
        
        int maxIterations = trainingParameters.getMaxIterations();
        
        boolean noChangeMade=false;
        int iteration=0;
        while(iteration<maxIterations && noChangeMade==false) {
            
            logger.debug("Iteration {}", iteration);
            
            noChangeMade=true;
            for(Map.Entry<Integer, Record> e : dataset.entries()) {
                Integer rId = e.getKey();
                Record r = e.getValue();
                
                Integer pointClusterId = (Integer) r.getYPredicted();
                CL ci = getFromClusterMap(pointClusterId, tempClusterMap);
                
                //remove the point from the cluster
                ci.remove(r);
                
                //if empty cluster remove it
                if(ci.size()==0) {
                    tempClusterMap.remove(pointClusterId);
                }
                else {
                    tempClusterMap.put(pointClusterId, ci);
                }
                
                AssociativeArray condProbCiGivenXiAndOtherCi = clusterProbabilities(r, n, tempClusterMap);
                                
                //Calculate the probabilities of assigning the point to a new cluster
                //compute P*(X[i]) = P(X[i]|λ)
                
                CL cNew = createNewCluster(newClusterId);
                
                double priorLogPredictive = cNew.posteriorLogPdf(r);

                //compute P(z[i] = * | z[-i], Data) = α/(α+N-1)
                double probNewCluster = alpha/(alpha+n-1.0);

                condProbCiGivenXiAndOtherCi.put(newClusterId, priorLogPredictive+Math.log(probNewCluster));
                
                //normalize probabilities P(z[i])
                Descriptives.normalizeExp(condProbCiGivenXiAndOtherCi);
                
                Integer sampledClusterId = (Integer)SimpleRandomSampling.weightedSampling(condProbCiGivenXiAndOtherCi, 1, true).iterator().next();
                //condProbCiGivenXiAndOtherCi=null;
                
                //Add Xi back to the sampled AbstractCluster
                if(Objects.equals(sampledClusterId, newClusterId)) { //if new cluster
                    //add the record in the new cluster
                    r = new Record(r.getX(), r.getY(), newClusterId, r.getYPredictedProbabilities());
                    dataset._unsafe_set(rId, r);
                    
                    cNew.add(r);
                    
                    tempClusterMap.put(newClusterId, cNew);
                    
                    noChangeMade=false;
                    
                    ++newClusterId;
                }
                else {
                    if(!Objects.equals(pointClusterId, sampledClusterId)) { //if it assigned in a different cluster update the record
                        r = new Record(r.getX(), r.getY(), sampledClusterId, r.getYPredictedProbabilities());
                        dataset._unsafe_set(rId, r);
                        noChangeMade=false;
                    }
                    
                    CL c = getFromClusterMap(sampledClusterId, tempClusterMap);
                    c.add(r); //add it to the cluster (or just add it back)
                    tempClusterMap.put(sampledClusterId, c);
                }
                
            }
            
            ++iteration;
        }
        
        //copy the values in the map and update the cluster ids
        Map<Integer, CL> clusterMap = modelParameters.getClusterMap();
        int cid = clusterMap.size();
        for(CL cluster : tempClusterMap.values()) {
            clusterMap.put(cid, (CL) cluster.copy2new(cid));
            cid++;
        }
        kb().getDbc().dropBigMap("tmp_tempClusterMap", tempClusterMap);
        
        return iteration;
    }
    
    private AssociativeArray clusterProbabilities(Record r, int n, Map<Integer, CL> clusterMap) {
        Map<Object, Object> condProbCiGivenXiAndOtherCi = new ConcurrentHashMap<>();
        double alpha = kb().getTrainingParameters().getAlpha();
        
        //Probabilities that appear on https://www.cs.cmu.edu/~kbe/dp_tutorial.pdf
        //Calculate the probabilities of assigning the point for every cluster
        streamExecutor.forEach(StreamMethods.stream(clusterMap.keySet().stream(), isParallelized()), clusterId -> {
            AbstractCluster ck = getFromClusterMap(clusterId, clusterMap);
            //compute P_k(X[i]) = P(X[i] | X[-i] = k)
            double marginalLogLikelihoodXi = ck.posteriorLogPdf(r);
            //set N_{k,-i} = dim({X[-i] = k})
            //compute P(z[i] = k | z[-i], Data) = N_{k,-i}/(a+N-1)
            double mixingXi = ck.size()/(alpha+n-1.0);
            
            condProbCiGivenXiAndOtherCi.put(clusterId, marginalLogLikelihoodXi+Math.log(mixingXi)); //concurrent map and non-overlapping keys for each thread
        });
        
        return new AssociativeArray(condProbCiGivenXiAndOtherCi);
    }
    
    private Object getSelectedClusterFromScores(AssociativeArray clusterScores) {
        Map.Entry<Object, Object> maxEntry = MapMethods.selectMaxKeyValue(clusterScores);
        
        return maxEntry.getKey();
    }
    
    /**
     * Creates a new cluster with the provided clusterId and it initializes it
     * accordingly.
     * 
     * @param clusterId
     * @return 
     */
    protected abstract CL createNewCluster(Integer clusterId); 
}
