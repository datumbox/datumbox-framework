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

import com.datumbox.common.dataobjects.MatrixDataframe;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.abstracts.algorithms.AbstractDPMM;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;
import java.util.Map;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;


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
public class MultinomialDPMM extends AbstractDPMM<MultinomialDPMM.Cluster, MultinomialDPMM.ModelParameters, MultinomialDPMM.TrainingParameters, MultinomialDPMM.ValidationMetrics> {
    
    /**
     * The AbstractCluster class of the MultinomialDPMM model.
     */
    public static class Cluster extends AbstractDPMM.AbstractCluster {
        private static final long serialVersionUID = 1L;
        
        //informational fields
        private int dimensions;
        
        //hyper parameters
        private double alphaWords; //effectively we set alphaWords = 50. The alphaWords controls the amount of words in each cluster. In most notes it is notated as alpha.
        
        //cluster parameters
        private RealVector wordCounts;
        
        //Cache
        private transient volatile Double cache_wordcounts_plusalpha; //Cached value of WordCountsPlusAlpha used only for speed optimization
        
        /** 
         * @param clusterId
         * @see com.datumbox.framework.machinelearning.common.abstracts.modelers.AbstractClusterer.AbstractCluster 
         */
        protected Cluster(Integer clusterId) {
            super(clusterId);
        }
        
        //hyper parameters getters/setters
        /**
         * Getter for the Alpha hyperparameter of the words.
         * 
         * @return 
         */
        protected double getAlphaWords() {
            return alphaWords;
        }
        
        /**
         * Setter for the Alpha hyperparameter of the words.
         * 
         * @param alphaWords 
         */
        protected void setAlphaWords(double alphaWords) {
            this.alphaWords = alphaWords;
        }

        /**
         * Getter for number of Dimensions.
         * 
         * @return 
         */
        protected int getDimensions() {
            return dimensions;
        }

        /**
         * Setter for number of Dimensions.
         * 
         * @param dimensions 
         */
        protected void setDimensions(int dimensions) {
            this.dimensions = dimensions;
        }
        
        /** {@inheritDoc} */
        @Override
        protected void initializeClusterParameters() {
            //Set default hyperparameters if not set
            synchronized(this) {
                cache_wordcounts_plusalpha=null;
            }
            wordCounts = new ArrayRealVector(dimensions); 
        }
        
        /** {@inheritDoc} */
        @Override
        protected double posteriorLogPdf(Record r) {
            if(wordCounts==null) {
                throw new RuntimeException("The cluster is empty or uninitialized.");
            }
            RealVector x_mu = MatrixDataframe.parseRecord(r, featureIds);    

            RealVector aVector = new ArrayRealVector(dimensions, alphaWords);
            RealVector wordCountsPlusAlpha = wordCounts.add(aVector);

            double cOfWordCountsPlusAlpha;
            if(cache_wordcounts_plusalpha==null) {
                synchronized(this) {
                    if(cache_wordcounts_plusalpha==null) {
                        cache_wordcounts_plusalpha=C(wordCountsPlusAlpha);
                    }
                }
            }
            cOfWordCountsPlusAlpha=cache_wordcounts_plusalpha;

            double logPdf= C(wordCountsPlusAlpha.add(x_mu))-cOfWordCountsPlusAlpha;
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
        protected boolean add(Integer rId, Record r) {
            int size= recordIdSet.size();
            
            if(recordIdSet.add(rId)==false) {
                return false;
            }
            
            RealVector rv = MatrixDataframe.parseRecord(r, featureIds);

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
        
        /** {@inheritDoc} */
        @Override
        protected boolean remove(Integer rId, Record r) {
            if(wordCounts==null) {
                return false; //The cluster is empty or uninitialized.
            }
            if(recordIdSet.remove(rId)==false) {
                return false;
            }
            
            RealVector rv = MatrixDataframe.parseRecord(r, featureIds);

            //update cluster clusterParameters
            wordCounts=wordCounts.subtract(rv);
            
            updateClusterParameters();
            
            return true;
        }
        
        /** {@inheritDoc} */
        @Override
        protected void updateClusterParameters() {
            synchronized(this) {
                cache_wordcounts_plusalpha=null;
            }
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
                sumLogGammaAi+=ContinuousDistributions.logGamma(tmp);
            }

            //Cvalue = productGammaAi/Gamma.gamma(sumAi);
            Cvalue = sumLogGammaAi-ContinuousDistributions.logGamma(sumAi);

            return Cvalue;
        }
    }
    
    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractDPMM.AbstractModelParameters<MultinomialDPMM.Cluster> {
        private static final long serialVersionUID = 1L;
        
        /** 
         * @param dbc
         * @see com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer.AbstractModelParameters#AbstractModelParameters(com.datumbox.common.persistentstorage.interfaces.DatabaseConnector) 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
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
    
    /** {@inheritDoc} */
    public static class ValidationMetrics extends AbstractDPMM.AbstractValidationMetrics {
        private static final long serialVersionUID = 1L;
        
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
    
    /** {@inheritDoc} */
    @Override
    protected Cluster createNewCluster(Integer clusterId) {
        ModelParameters modelParameters = kb().getModelParameters();
        TrainingParameters trainingParameters = kb().getTrainingParameters();
        Cluster c = new Cluster(clusterId);
        
        c.setDimensions(modelParameters.getD());
        c.setFeatureIds(modelParameters.getFeatureIds());
        c.setAlphaWords(trainingParameters.getAlphaWords());
        
        c.initializeClusterParameters();
        
        return c;
    }
    
}
