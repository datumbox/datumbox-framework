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
import com.datumbox.framework.core.common.dataobjects.DataframeMatrix;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.algorithms.AbstractDPMM;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelers.AbstractClusterer;
import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.Map;


/**
 * The MultinomialDPMM implements Dirichlet Process Mixture Models with Multinomial 
 * and Dirichlet priors. 
 * 
 * References:
 * http://blog.datumbox.com/overview-of-cluster-analysis-and-dirichlet-process-mixture-models/
 * http://blog.datumbox.com/clustering-documents-and-gaussian-data-with-dirichlet-process-mixture-models/
 * http://web.science.mq.edu.au/~mjohnson/papers/Johnson11MLSS-talk-extras.pdf
 * https://web.archive.org/web/20100119210345/http://cog.brown.edu/~mj/classes/cg168/slides/ChineseRestaurants.pdf
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MultinomialDPMM extends AbstractDPMM<MultinomialDPMM.Cluster, MultinomialDPMM.ModelParameters, MultinomialDPMM.TrainingParameters> {
    
    /**
     * The AbstractCluster class of the MultinomialDPMM model.
     */
    public static class Cluster extends AbstractDPMM.AbstractCluster {
        private static final long serialVersionUID = 2L;
        
        //hyper parameters
        private final double alphaWords; //effectively we set alphaWords = 50. The alphaWords controls the amount of words in each cluster. In most notes it is notated as alpha.
        
        //cluster parameters
        private RealVector wordCounts;
        
        private Double wordcounts_plusalpha; //internal cached value of WordCountsPlusAlpha
        
        /** 
         * @param clusterId
         * @param dimensions
         * @param alphaWords
         * @see AbstractClusterer.AbstractCluster#AbstractCluster(java.lang.Integer)
         */
        protected Cluster(Integer clusterId, int dimensions, double alphaWords) {
            super(clusterId);

            this.alphaWords = alphaWords;
            
            wordCounts = new OpenMapRealVector(dimensions);
            wordcounts_plusalpha = estimateWordCountsPlusAlpha();
        }
        
        /** {@inheritDoc} */
        @Override
        protected double posteriorLogPdf(Record r) {
            RealVector x_mu = DataframeMatrix.parseRecord(r, featureIds);

            RealVector wordCountsPlusAlpha = wordCounts.mapAdd(alphaWords);

            double logPdf = C(wordCountsPlusAlpha.add(x_mu))-wordcounts_plusalpha;
            return logPdf;
        }

        /** {@inheritDoc} */
        @Override
        protected Map<Object, Integer> getFeatureIds() {
            return featureIds;
        }
        
        /** {@inheritDoc} */
        @Override
        protected void setFeatureIds(Map<Object, Integer> featureIds) {
            this.featureIds = featureIds;
        }

        /** {@inheritDoc} */
        @Override
        protected void add(Record r) {
            RealVector rv = DataframeMatrix.parseRecord(r, featureIds);

            //update cluster clusterParameters
            if(size==0) {
                wordCounts=rv;
            }
            else {
                wordCounts=wordCounts.add(rv);
            }
            
            size++;
            
            updateClusterParameters();
        }
        
        /** {@inheritDoc} */
        @Override
        protected void remove(Record r) {
            size--;
            
            RealVector rv = DataframeMatrix.parseRecord(r, featureIds);

            //update cluster clusterParameters
            wordCounts=wordCounts.subtract(rv);
            
            updateClusterParameters();
        }
        
        /** {@inheritDoc} */
        @Override
        protected void updateClusterParameters() {
            wordcounts_plusalpha = estimateWordCountsPlusAlpha();
        }

        /** {@inheritDoc} */
        @Override
        protected void clear() {
            
        }
        
        /**
         * Estimates the WordCountsPlusAlpha which is stored internally in the
         * cluster for performance reasons.
         * 
         * @return 
         */
        private double estimateWordCountsPlusAlpha() {
            RealVector wordCountsPlusAlpha = wordCounts.mapAdd(alphaWords);
            return C(wordCountsPlusAlpha);
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
            double sumLogGammaAi=0.0;

            int aLength=alphaVector.getDimension();
            double tmp;
            for(int i=0;i<aLength;++i) {
                tmp=alphaVector.getEntry(i);
                sumAi+= tmp;
                sumLogGammaAi+=ContinuousDistributions.logGamma(tmp);
            }

            Cvalue = sumLogGammaAi-ContinuousDistributions.logGamma(sumAi);

            return Cvalue;
        }
    }
    
    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractDPMM.AbstractModelParameters<MultinomialDPMM.Cluster> {
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
    public static class TrainingParameters extends AbstractDPMM.AbstractTrainingParameters {
        private static final long serialVersionUID = 1L;
        
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
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainer.AbstractTrainingParameters, Configuration)
     */
    protected MultinomialDPMM(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected MultinomialDPMM(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }
    
    /** {@inheritDoc} */
    @Override
    protected Cluster createNewCluster(Integer clusterId) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Cluster c = new Cluster(clusterId, modelParameters.getD(), trainingParameters.getAlphaWords());
        c.setFeatureIds(modelParameters.getFeatureIds());
        
        return c;
    }
    
}
