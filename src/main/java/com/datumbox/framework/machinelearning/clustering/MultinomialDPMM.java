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

import com.datumbox.common.dataobjects.MatrixDataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.framework.machinelearning.common.bases.basemodels.BaseDPMM;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;
import java.util.Collection;
import java.util.Map;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;


/**
 * Multinomial with Dirichlet priors.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class MultinomialDPMM extends BaseDPMM<MultinomialDPMM.Cluster, MultinomialDPMM.ModelParameters, MultinomialDPMM.TrainingParameters, MultinomialDPMM.ValidationMetrics> {
    /*
      References:
           http://web.science.mq.edu.au/~mjohnson/papers/Johnson11MLSS-talk-extras.pdf
           https://web.archive.org/web/20100119210345/http://cog.brown.edu/~mj/classes/cg168/slides/ChineseRestaurants.pdf
           iso-notes.docx
           notes.txt
    */
    
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    public static final String SHORT_METHOD_NAME = "MDPMM";
    
    public static class Cluster extends BaseDPMM.Cluster {
        //informational fields
        private int dimensions;
        

        //hyper parameters
        private double alphaWords; //effectively we set alphaWords = 50. The alphaWords controls the amount of words in each cluster. In most notes it is notated as alpha.
        
        //cluster parameters
        private RealVector wordCounts;
        
        //Cache
        /**
         * Cached value of WordCountsPlusAlpha used only for speed optimization
         */
        private transient Double cache_wordcounts_plusalpha;

        
        
        
        public Cluster(Integer clusterId) {
            super(clusterId);
        }

        protected Map<Object, Integer> getFeatureIds() {
            return featureIds;
        }

        protected void setFeatureIds(Map<Object, Integer> featureIds) {
            this.featureIds = featureIds;
        }
        
        
        //hyper parameters getters/setters
        
        public double getAlphaWords() {
            return alphaWords;
        }

        public void setAlphaWords(double alphaWords) {
            this.alphaWords = alphaWords;
        }

        public int getDimensions() {
            return dimensions;
        }

        public void setDimensions(int dimensions) {
            this.dimensions = dimensions;
        }

        
        


        /**
         * Internal method that adds the record in cluster and updates clusterParams
         * 
         * @param r    The point that we wish to add in the cluster.
         * @return 
         */
        @Override
        public boolean add(Record r) {
            size= recordSet.size();
            
            if(recordSet.add(r)==false) {
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
            
            ++size; //increase the size since we added a record
            
            updateClusterParameters();
            
            return true;
        }

        @Override
        public boolean addAll(Collection<Record> c) {
            //created only for completion purposes and for implementing the abstract. 
            //it is not used and thus it is not optimized.
            boolean result = false;
            for(Record r : c) {
                result|=add(r);
            }
            return result;
        }
        
        @Override
        public boolean remove(Record r) {
            if(recordSet.remove(r)==false) {
                return false;
            }
            
            RealVector rv = MatrixDataset.parseRecord(r, featureIds);

            //update cluster clusterParameters
            wordCounts=wordCounts.subtract(rv);
            
            updateClusterParameters();
            
            return true;
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

        @Override
        protected void updateClusterParameters() {
            cache_wordcounts_plusalpha=null;
        }

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
    
    
    public static class ModelParameters extends BaseDPMM.ModelParameters<MultinomialDPMM.Cluster> {
        

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
    
    
    public static class TrainingParameters extends BaseDPMM.TrainingParameters {
        
        private double alphaWords = 50.0; //effectively we set alphaWords = 50. The alphaWords controls the amount of words in each cluster. In most notes it is notated as alpha.

        public double getAlphaWords() {
            return alphaWords;
        }

        public void setAlphaWords(double alphaWords) {
            this.alphaWords = alphaWords;
        }
        
    }
    
    
    public static class ValidationMetrics extends BaseDPMM.ValidationMetrics {
        
    }

    
    public MultinomialDPMM(String dbName) {
        super(dbName, MultinomialDPMM.ModelParameters.class, MultinomialDPMM.TrainingParameters.class, MultinomialDPMM.ValidationMetrics.class);
    }

    @Override
    public String shortMethodName() {
        return SHORT_METHOD_NAME;
    }
    
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
