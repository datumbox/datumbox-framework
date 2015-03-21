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
package com.datumbox.framework.machinelearning.common.bases.basemodels;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureMarker;
import com.datumbox.common.utilities.MapFunctions;
import com.datumbox.common.utilities.PHPfunctions;
import com.datumbox.configuration.GeneralConfiguration;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclusterer;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.statistics.sampling.SRS;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <CL>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */

public abstract class BaseDPMM<CL extends BaseDPMM.Cluster, MP extends BaseDPMM.ModelParameters, TP extends BaseDPMM.TrainingParameters, VM extends BaseDPMM.ValidationMetrics>  extends BaseMLclusterer<CL, MP, TP, VM> {
    
    public static abstract class Cluster extends BaseMLclusterer.Cluster {
        
        protected transient Map<Object, Integer> featureIds; //This is only a reference to the real Map. It is used to convert the Records to Arrays
        
        public Cluster(Integer clusterId) {
            super(clusterId);
        }
        
        protected void setClusterId(Integer clusterId) {
            this.clusterId = clusterId;
        }
        
        protected abstract void updateClusterParameters();
        
        protected abstract void initializeClusterParameters();
        
        /**
         * Returns the log posterior PDF of a particular point xi, to belong to this
         * cluster.
         * 
         * @param r    The point for which we want to estimate the PDF.
         * @return      The log posterior PDF
         */
        public abstract double posteriorLogPdf(Record r);
    }
    
    public static abstract class ModelParameters<CL extends BaseDPMM.Cluster> extends BaseMLclusterer.ModelParameters<CL> {
        
        private int totalIterations;

        /**
         * Feature set
         */
        @BigDataStructureMarker
        
        private Map<Object, Integer> featureIds; //list of all the supported features

        
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf) {
            super.bigDataStructureInitializer(bdsf);
            
            featureIds = bdsf.getMap("featureIds");
        }
        
        public int getTotalIterations() {
            return totalIterations;
        }

        public void setTotalIterations(int totalIterations) {
            this.totalIterations = totalIterations;
        }

        public Map<Object, Integer> getFeatureIds() {
            return featureIds;
        }

        public void setFeatureIds(Map<Object, Integer> featureIds) {
            this.featureIds = featureIds;
        }
        
    }
    
    public static abstract class TrainingParameters extends BaseMLclusterer.TrainingParameters {   
        /**
         * Alpha value of Dirichlet process
         */
        private double alpha;    
        
        private int maxIterations = 1000;

        public enum Initialization {
            ONE_CLUSTER_PER_RECORD,
            RANDOM_ASSIGNMENT
        }
        
        //My optimization: it controls whether the algorithm will be initialized with every observation
        //being a separate cluster. If this is turned off, then alpha*log(n) clusters
        //are generated and the observations are assigned randomly in it.
        private Initialization initializationMethod = Initialization.ONE_CLUSTER_PER_RECORD; 
        
        public double getAlpha() {
            return alpha;
        }

        public void setAlpha(double alpha) {
            this.alpha = alpha;
        }

        public int getMaxIterations() {
            return maxIterations;
        }

        public void setMaxIterations(int maxIterations) {
            this.maxIterations = maxIterations;
        }

        public Initialization getInitializationMethod() {
            return initializationMethod;
        }

        public void setInitializationMethod(Initialization initializationMethod) {
            this.initializationMethod = initializationMethod;
        }
        
    }
    
    public static abstract class ValidationMetrics extends BaseMLclusterer.ValidationMetrics {
        
    }
    
    protected BaseDPMM(String dbName, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass) {
        super(dbName, mpClass, tpClass, vmClass);
    } 
    

    @Override
    @SuppressWarnings("unchecked")
    protected void estimateModelParameters(Dataset trainingData) {
        int n = trainingData.size();
        int d = trainingData.getColumnSize();
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        Map<Integer, CL> clusterList = modelParameters.getClusterList();
        
        //initialization
        modelParameters.setN(n);
        modelParameters.setD(d);
        
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
                if(!featureIds.containsKey(feature)) {
                    featureIds.put(feature, previousFeatureId++);
                }
            }
        }
        
        //perform Gibbs Sampling and calculate the clusters
        int totalIterations = collapsedGibbsSampling(trainingData);
        
        //set the actual iterations performed
        modelParameters.setTotalIterations(totalIterations);
        
        //update the number of clusters
        modelParameters.setC(clusterList.size());
        
        //clear dataclusters
        for(CL c : clusterList.values()) {
            c.clear();
        }
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
            for(Record r : dataset) {
                //generate a new cluster
                CL cluster = createNewCluster(newClusterId);

                //add the record in the new cluster
                r.setYPredicted(newClusterId);
                cluster.add(r);

                //add the cluster in clusterList
                tempClusterMap.put(newClusterId, cluster);

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
                
                //add the cluster in clusterList
                tempClusterMap.put(newClusterId, cluster);

                ++newClusterId;
            }
            
            int clusterMapSize = newClusterId;
            for(Record r : dataset) {
                
                int assignedClusterId = PHPfunctions.mt_rand(0, clusterMapSize-1);
                
                r.setYPredicted(assignedClusterId);
                tempClusterMap.get((Integer)assignedClusterId).add(r);
            }
        }

                
        int n = tempClusterMap.size();
        
        int maxIterations = trainingParameters.getMaxIterations();
        
        boolean noChangeMade=false;
        int iteration=0;
        while(iteration<maxIterations && noChangeMade==false) {
            
            if(GeneralConfiguration.DEBUG) {
                System.out.println("Iteration "+iteration);
            }
            
            noChangeMade=true;
            for(Record r : dataset) {
                Integer pointClusterId = (Integer) r.getYPredicted();
                CL ci = tempClusterMap.get(pointClusterId);
                
                //remove the point from the cluster
                ci.remove(r);
                
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
                
                Integer sampledClusterId = (Integer)SRS.weightedProbabilitySampling(condProbCiGivenXiAndOtherCi, 1, true).iterator().next();
                condProbCiGivenXiAndOtherCi=null;
                
                //Add Xi back to the sampled Cluster
                if(sampledClusterId==newClusterId) { //if new cluster
                    //add the record in the new cluster
                    r.setYPredicted(newClusterId);
                    cNew.add(r);
                    
                    //add the cluster in clusterList
                    tempClusterMap.put(newClusterId, cNew);
                    
                    noChangeMade=false;
                    
                    ++newClusterId;
                }
                else {
                    r.setYPredicted(sampledClusterId);
                    
                    tempClusterMap.get(sampledClusterId).add(r);
                    if(noChangeMade && pointClusterId!=sampledClusterId) {
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
    
    public AssociativeArray clusterProbabilities(Record r, int n, Map<Integer, CL> clusterMap) {
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
        
        
        for(Record r : newData) {
            
            AssociativeArray clusterScores = new AssociativeArray();
            for(Cluster c : clusterList.values()) {
                double probability = c.posteriorLogPdf(r);
                clusterScores.put(c.getClusterId(), probability);
            }
            
            r.setYPredicted(getSelectedClusterFromScores(clusterScores));
            
            Descriptives.normalizeExp(clusterScores);
            
            r.setYPredictedProbabilities(clusterScores);
        }
        
    }

    private Object getSelectedClusterFromScores(AssociativeArray clusterScores) {
        Map.Entry<Object, Object> maxEntry = MapFunctions.selectMaxKeyValue(clusterScores);
        
        return maxEntry.getKey();
    }
    
    //create new cluster, set the ID, initialize it and pass to it the featureIds
    //DO NOT add it on the clusterList
    protected abstract CL createNewCluster(Integer clusterId); 
}
