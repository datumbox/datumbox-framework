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

import com.datumbox.common.dataobjects.MatrixDataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.bases.basemodels.BaseDPMM;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;
import java.util.Map;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;


/**
 * The MultinomialDPMM implements Dirichlet Process Mixture Models with Multinomial 
 * and Dirichlet priors. 
 * 
 * WARNING: This class copies the Dataset to a RealMatrix which forces all of the
 * data to be loaded in memory.
 * 
 * References:
 * http://blog.datumbox.com/overview-of-cluster-analysis-and-dirichlet-process-mixture-models/
 * http://blog.datumbox.com/clustering-documents-and-gaussian-data-with-dirichlet-process-mixture-models/
 * http://web.science.mq.edu.au/~mjohnson/papers/Johnson11MLSS-talk-extras.pdf
 * https://web.archive.org/web/20100119210345/http://cog.brown.edu/~mj/classes/cg168/slides/ChineseRestaurants.pdf
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MultinomialDPMM extends BaseDPMM<MultinomialDPMM.Cluster, MultinomialDPMM.ModelParameters, MultinomialDPMM.TrainingParameters, MultinomialDPMM.ValidationMetrics> {
    
    /**
     * The Cluster class of the MultinomialDPMM model.
     */
    public static class Cluster extends BaseDPMM.Cluster {
        //informational fields
        private int dimensions;
        
        //hyper parameters
        private double alphaWords; //effectively we set alphaWords = 50. The alphaWords controls the amount of words in each cluster. In most notes it is notated as alpha.
        
        //cluster parameters
        private RealVector wordCounts;
        
        //Cache
        private transient Double cache_wordcounts_plusalpha; //Cached value of WordCountsPlusAlpha used only for speed optimization

        /**
         * Public constructor of Cluster which takes as argument a unique id.
         * 
         * @param clusterId 
         */
        public Cluster(Integer clusterId) {
            super(clusterId);
        }
        
        //hyper parameters getters/setters
        /**
         * Getter for the Alpha hyperparameter of the words.
         * 
         * @return 
         */
        public double getAlphaWords() {
            return alphaWords;
        }
        
        /**
         * Setter for the Alpha hyperparameter of the words.
         * 
         * @param alphaWords 
         */
        public void setAlphaWords(double alphaWords) {
            this.alphaWords = alphaWords;
        }

        /**
         * Getter for number of Dimensions.
         * 
         * @return 
         */
        public int getDimensions() {
            return dimensions;
        }

        /**
         * Setter for number of Dimensions.
         * 
         * @param dimensions 
         */
        public void setDimensions(int dimensions) {
            this.dimensions = dimensions;
        }
        
        /**
         * Initializes the cluster's internal parameters: mean, covariance, meanError and meanDf.
         */
        @Override
        public void initializeClusterParameters() {
            //Set default hyperparameters if not set

            cache_wordcounts_plusalpha=null;
            wordCounts = new ArrayRealVector(dimensions); 
        }

        /**
         * Estimates the posterior LogPdf of a Record belonging to the cluster.
         * 
         * @param r
         * @return 
         */
        @Override
        public double posteriorLogPdf(Record r) {
            RealVector x_mu = MatrixDataset.parseRecord(r, featureIds);    

            RealVector aVector = new ArrayRealVector(dimensions, alphaWords);
            RealVector wordCountsPlusAlpha = wordCounts.add(aVector);

            double cOfWordCountsPlusAlpha;
            if(cache_wordcounts_plusalpha==null) {
                cache_wordcounts_plusalpha=C(wordCountsPlusAlpha);
            }
            cOfWordCountsPlusAlpha=cache_wordcounts_plusalpha;

            //double pdf= C(wordCountsPlusAlpha.add(x_mu))/C(wordCountsPlusAlpha);
            double logPdf= C(wordCountsPlusAlpha.add(x_mu))-cOfWordCountsPlusAlpha;
            return logPdf;
        }

        /**
         * Getter for the featureIds which is a mapping between the column names
         * and their positions on the vector.
         * 
         * @return 
         */
        protected Map<Object, Integer> getFeatureIds() {
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

        /**
         * Internal method that adds the record in cluster and updates clusterParams
         * 
         * @param rId  The id of the record in the dataset.
         * @param r    The point that we wish to add in the cluster.
         * @return 
         */
        @Override
        protected boolean add(Integer rId, Record r) {
            int size= recordIdSet.size();
            
            if(recordIdSet.add(rId)==false) {
                return false;
            }
            
            RealVector rv = MatrixDataset.parseRecord(r, featureIds);

            //update cluster clusterParameters
            if(size==0) {
                wordCounts=rv;
            }
            else {
                wordCounts=wordCounts.add(rv);
            }
            
            updateClusterParameters();
            
            return true;
        }
        
        /**
         * Internal method that removes the record in cluster and updates clusterParams
         * 
         * @param rId  The id of the record in the dataset.
         * @param r    The point that we wish to add in the cluster.
         * @return 
         */
        @Override
        protected boolean remove(Integer rId, Record r) {
            if(recordIdSet.remove(rId)==false) {
                return false;
            }
            
            RealVector rv = MatrixDataset.parseRecord(r, featureIds);

            //update cluster clusterParameters
            wordCounts=wordCounts.subtract(rv);
            
            updateClusterParameters();
            
            return true;
        }
        
        @Override
        protected void updateClusterParameters() {
            cache_wordcounts_plusalpha=null;
        }
        
        /**
         * Internal method that estimates the value of C(a).
         * 
         * @param alphaVector   Vector with alpha values
         * @return              Returns the value of C(a)
         */
        private double C(RealVector alphaVector) {
            double Cvalue;
            double sumAi=0.0;
            //double productGammaAi=1.0;
            double sumLogGammaAi=0.0;

            int aLength=alphaVector.getDimension();
            double tmp;
            for(int i=0;i<aLength;++i) {
                tmp=alphaVector.getEntry(i);
                sumAi+= tmp;
                //productGammaAi*=ContinuousDistributions.gamma(tmp);
                sumLogGammaAi+=ContinuousDistributions.LogGamma(tmp);
            }

            //Cvalue = productGammaAi/Gamma.gamma(sumAi);
            Cvalue = sumLogGammaAi-ContinuousDistributions.LogGamma(sumAi);

            return Cvalue;
        }
    }
    
    /**
     * The ModelParameters class stores the coefficients that were learned during
     * the training of the algorithm.
     */
    public static class ModelParameters extends BaseDPMM.ModelParameters<MultinomialDPMM.Cluster> {
        
        /**
         * Protected constructor which accepts as argument the DatabaseConnector.
         * 
         * @param dbc 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }

        /**
         * Getter for the List of Clusters.
         * 
         * @return 
         */
        @Override
        public Map<Integer, Cluster> getClusterList() {
            Map<Integer, Cluster> clusterList = super.getClusterList();
            
            //overrides the method in order to ensure that the featureIds parameters are loaded in the clusters
            Map<Object, Integer> featureIds = getFeatureIds();
            for(Cluster c : clusterList.values()) {
                if(c.getFeatureIds()==null) {
                    c.setFeatureIds(featureIds);
                }
            }
            
            return clusterList;
        }
    }
    
    /**
     * The TrainingParameters class stores the parameters that can be changed
     * before training the algorithm.
     */
    public static class TrainingParameters extends BaseDPMM.TrainingParameters {
        
        private double alphaWords = 50.0; //effectively we set alphaWords = 50. The alphaWords controls the amount of words in each cluster. In most notes it is notated as alpha.
        
        /**
         * Getter for the Alpha hyperparameter of the words.
         * 
         * @return 
         */
        public double getAlphaWords() {
            return alphaWords;
        }
        
        /**
         * Setter for the Alpha hyperparameter of the words.
         * 
         * @param alphaWords 
         */
        public void setAlphaWords(double alphaWords) {
            this.alphaWords = alphaWords;
        }
        
    }
    
    /**
     * The ValidationMetrics class stores information about the performance of the
     * algorithm.
     */
    public static class ValidationMetrics extends BaseDPMM.ValidationMetrics {
        
    }

    /**
     * Public constructor of the algorithm.
     * 
     * @param dbName
     * @param dbConf 
     */
    public MultinomialDPMM(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, MultinomialDPMM.ModelParameters.class, MultinomialDPMM.TrainingParameters.class, MultinomialDPMM.ValidationMetrics.class);
    }
    
    /**
     * Creates a new cluster with the provided clusterId and it initializes it
     * accordingly.
     * 
     * @param clusterId
     * @return 
     */
    @Override
    protected Cluster createNewCluster(Integer clusterId) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Cluster c = new Cluster(clusterId);
        
        c.setDimensions(modelParameters.getD());
        c.setFeatureIds(modelParameters.getFeatureIds());
        c.setAlphaWords(trainingParameters.getAlphaWords());
        
        c.initializeClusterParameters();
        
        return c;
    }
    
}
