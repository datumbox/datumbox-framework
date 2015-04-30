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
package com.datumbox.framework.machinelearning.common.bases.basemodels;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.MapFunctions;
import com.datumbox.common.utilities.PHPfunctions;

import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclusterer;
import com.datumbox.framework.machinelearning.common.validation.ClustererValidation;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.statistics.sampling.SRS;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * Base class for Dirichlet Process Mixtures Models.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <CL>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */

public abstract class BaseDPMM<CL extends BaseDPMM.Cluster, MP extends BaseDPMM.ModelParameters, TP extends BaseDPMM.TrainingParameters, VM extends BaseDPMM.ValidationMetrics>  extends BaseMLclusterer<CL, MP, TP, VM> {
    
    /**
     * Base class for the Cluster class of the DPMM.
     */
    public static abstract class Cluster extends BaseMLclusterer.Cluster {
        /**
         * The featureIds provides a mapping between the column names
         * and their positions on the internal data vector.
         * This is only a reference to the real Map. It is used to convert the 
         * Records to Arrays.
         */
        protected transient Map<Object, Integer> featureIds;
        
        /**
         * Public constructor.
         * 
         * @param clusterId 
         */
        protected Cluster(Integer clusterId) {
            super(clusterId);
        }
        
        /**
         * Setter for the Cluster id.
         * 
         * @param clusterId 
         */
        protected void setClusterId(Integer clusterId) {
            this.clusterId = clusterId;
        }
        
        /**
         * Updates the cluster parameters by using the assigned points.
         */
        protected abstract void updateClusterParameters();
        
        /**
         * Initializes the cluster's internal parameters.
         */
        protected abstract void initializeClusterParameters();
        
        /**
         * Returns the log posterior PDF of a particular point xi, to belong to this
         * cluster.
         * 
         * @param r    The point for which we want to estimate the PDF.
         * @return      The log posterior PDF
         */
        protected abstract double posteriorLogPdf(Record r);
        
        @Override
        protected abstract boolean add(Integer rId, Record r);
        
        @Override
        protected abstract boolean remove(Integer rId, Record r);
    }
    
    /**
     * Base class for the Model Parameters of the algorithm.
     * 
     * @param <CL> 
     */
    public static abstract class ModelParameters<CL extends BaseDPMM.Cluster> extends BaseMLclusterer.ModelParameters<CL> {
        
        private int totalIterations;

        @BigMap
        private Map<Object, Integer> featureIds; //list of all the supported features

        /**
         * Protected constructor which accepts as argument the DatabaseConnector.
         * 
         * @param dbc 
         */
        protected ModelParameters(DatabaseConnector dbc) {
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
        public void setFeatureIds(Map<Object, Integer> featureIds) {
            this.featureIds = featureIds;
        }
        
    }
    
    /**
     * Base class for the Training Parameters of the algorithm.
     */
    public static abstract class TrainingParameters extends BaseMLclusterer.TrainingParameters {   
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
     * Base class for the Validation Parameters of the algorithm.
     */
    public static abstract class ValidationMetrics extends BaseMLclusterer.ValidationMetrics {
        
    }
    
    /**
     * Protected constructor of the algorithm.
     * 
     * @param dbName
     * @param dbConf
     * @param mpClass
     * @param tpClass
     * @param vmClass 
     */
    protected BaseDPMM(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass) {
        super(dbName, dbConf, mpClass, tpClass, vmClass, new ClustererValidation<>());
    } 
    
    @Override
    @SuppressWarnings("unchecked")
    protected void _fit(Dataset trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        Set<Object> goldStandardClasses = modelParameters.getGoldStandardClasses();
        Map<Object, Integer> featureIds = modelParameters.getFeatureIds();
        
        //check if there are any gold standard classes and buld the featureIds maps
        int previousFeatureId = 0;
        for(Integer rId : trainingData) { 
            Record r = trainingData.get(rId);
            Object theClass=r.getY();
            if(theClass!=null) {
                goldStandardClasses.add(theClass); 
            }
            
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                if(!featureIds.containsKey(feature)) {
                    featureIds.put(feature, previousFeatureId++);
                }
            }
        }
        
        //perform Gibbs Sampling and calculate the clusters
        int totalIterations = collapsedGibbsSampling(trainingData);
        
        //set the actual iterations performed
        modelParameters.setTotalIterations(totalIterations);
        
    }
    
    /**
     * Implementation of Collapsed Gibbs Sampling algorithm.
     * 
     * @param dataset The list of points that we want to cluster
     * @param maxIterations The maximum number of iterations
     */
    private int collapsedGibbsSampling(Dataset dataset) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        Map<Integer, CL> tempClusterMap = new HashMap<>(modelParameters.getClusterList());
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        double alpha = trainingParameters.getAlpha();
        
        //Initialize clusters, create a cluster for every xi
        Integer newClusterId = tempClusterMap.size(); //start counting the Ids based on clusters in the list

        if(trainingParameters.getInitializationMethod()==TrainingParameters.Initialization.ONE_CLUSTER_PER_RECORD) {
            for(Integer rId : dataset) {
                Record r = dataset.get(rId);
                //generate a new cluster
                CL cluster = createNewCluster(newClusterId);

                //add the record in the new cluster
                r = new Record(r.getX(), r.getY(), newClusterId, r.getYPredictedProbabilities());
                dataset.set(rId, r);
                
                cluster.add(rId, r);

                //add the cluster in clusterList
                tempClusterMap.put(newClusterId, cluster);

                ++newClusterId;
            }            
        }
        else {
            int numberOfNewClusters = (int)(Math.max(alpha, 1)*Math.log(dataset.getRecordNumber())); //a*log(n) clusters on average
            if(numberOfNewClusters<=0) {
                numberOfNewClusters=1;
            }
            
            //generate new clusters
            for(int i=0;i<numberOfNewClusters;++i) {
                //generate a new cluster
                CL cluster = createNewCluster(newClusterId);
                
                //add the cluster in clusterList
                tempClusterMap.put(newClusterId, cluster);

                ++newClusterId;
            }
            
            int clusterMapSize = newClusterId;
            for(Integer rId : dataset) {
                Record r = dataset.get(rId);
                
                int assignedClusterId = PHPfunctions.mt_rand(0, clusterMapSize-1);
                
                r = new Record(r.getX(), r.getY(), assignedClusterId, r.getYPredictedProbabilities());
                dataset.set(rId, r);
                
                tempClusterMap.get((Integer)assignedClusterId).add(rId, r);
            }
        }

                
        int n = tempClusterMap.size();
        
        int maxIterations = trainingParameters.getMaxIterations();
        
        boolean noChangeMade=false;
        int iteration=0;
        while(iteration<maxIterations && noChangeMade==false) {
            
            logger.debug("Iteration {}", iteration);
            
            noChangeMade=true;
            for(Integer rId : dataset) {
                Record r = dataset.get(rId);
                
                Integer pointClusterId = (Integer) r.getYPredicted();
                CL ci = tempClusterMap.get(pointClusterId);
                
                //remove the point from the cluster
                ci.remove(rId, r);
                
                //if empty cluster remove it
                if(ci.size()==0) {
                    tempClusterMap.remove(pointClusterId);
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
                
                Integer sampledClusterId = (Integer)SRS.weightedSampling(condProbCiGivenXiAndOtherCi, 1, true).iterator().next();
                condProbCiGivenXiAndOtherCi=null;
                
                //Add Xi back to the sampled Cluster
                if(Objects.equals(sampledClusterId, newClusterId)) { //if new cluster
                    //add the record in the new cluster
                    r = new Record(r.getX(), r.getY(), newClusterId, r.getYPredictedProbabilities());
                    dataset.set(rId, r);
                    
                    cNew.add(rId, r);
                    
                    //add the cluster in clusterList
                    tempClusterMap.put(newClusterId, cNew);
                    
                    noChangeMade=false;
                    
                    ++newClusterId;
                }
                else {
                    r = new Record(r.getX(), r.getY(), sampledClusterId, r.getYPredictedProbabilities());
                    dataset.set(rId, r);
                    
                    tempClusterMap.get(sampledClusterId).add(rId, r);
                    if(noChangeMade && !Objects.equals(pointClusterId, sampledClusterId)) {
                        noChangeMade=false;
                    }
                }
                
            }
            
            ++iteration;
        }
        
        //copy the values in the map and update the cluster ids
        Map<Integer, CL> clusterList = modelParameters.getClusterList();
        newClusterId = clusterList.size();
        for(CL cluster : tempClusterMap.values()) {
            cluster.setClusterId(newClusterId);
            clusterList.put(newClusterId, cluster);
            ++newClusterId;
        }
        tempClusterMap = null;
        
        return iteration;
    }
    
    private AssociativeArray clusterProbabilities(Record r, int n, Map<Integer, CL> clusterMap) {
        AssociativeArray condProbCiGivenXiAndOtherCi = new AssociativeArray();
        double alpha = knowledgeBase.getTrainingParameters().getAlpha();
        
        //Probabilities that appear on https://www.cs.cmu.edu/~kbe/dp_tutorial.posteriorLogPdf
        //Calculate the probabilities of assigning the point for every cluster
        for(CL ck : clusterMap.values()) {
            //compute P_k(X[i]) = P(X[i] | X[-i] = k)
            double marginalLogLikelihoodXi = ck.posteriorLogPdf(r);
            //set N_{k,-i} = dim({X[-i] = k})
            //compute P(z[i] = k | z[-i], Data) = N_{k,-i}/(a+N-1)
            double mixingXi = ck.size()/(alpha+n-1.0);

            
            condProbCiGivenXiAndOtherCi.put(ck.getClusterId(), marginalLogLikelihoodXi+Math.log(mixingXi));
        }
        
        return condProbCiGivenXiAndOtherCi;
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
            
            AssociativeArray clusterScores = new AssociativeArray();
            for(Cluster c : clusterList.values()) {
                double probability = c.posteriorLogPdf(r);
                clusterScores.put(c.getClusterId(), probability);
            }
           
            
            Descriptives.normalizeExp(clusterScores);
            
            r = new Record(r.getX(), r.getY(), getSelectedClusterFromScores(clusterScores), clusterScores);
            newData.set(rId, r);
        }
        
    }

    private Object getSelectedClusterFromScores(AssociativeArray clusterScores) {
        Map.Entry<Object, Object> maxEntry = MapFunctions.selectMaxKeyValue(clusterScores);
        
        return maxEntry.getKey();
    }
    
    /**
     * Creates a new cluster, sets the ID, initializes it and passes it the
     * featureIds variable, WITHOUT adding it in the clusterList.
     * 
     * @param clusterId
     * @return 
     */
    protected abstract CL createNewCluster(Integer clusterId); 
}
