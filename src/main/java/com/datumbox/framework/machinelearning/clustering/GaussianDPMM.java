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
import java.util.Map;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;



/**
 * The GaussianDPMM implements Dirichlet Process Mixture Models with Multivariate 
 * Normal and Normal-Inverse-Wishart prior. 
 * 
 * WARNING: This class copies the Dataset to a RealMatrix which forces all of the
 * data to be loaded in memory.
 * 
 * References:
 * http://blog.datumbox.com/overview-of-cluster-analysis-and-dirichlet-process-mixture-models/
 * http://blog.datumbox.com/clustering-documents-and-gaussian-data-with-dirichlet-process-mixture-models/
 * http://snippyhollow.github.io/blog/2013/03/10/collapsed-gibbs-sampling-for-dirichlet-process-gaussian-mixture-models/
 * http://blog.echen.me/2012/03/20/infinite-mixture-models-with-nonparametric-bayes-and-the-dirichlet-process/
 * http://www.cs.princeton.edu/courses/archive/fall07/cos597C/scribe/20070921.pdf
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class GaussianDPMM extends BaseDPMM<GaussianDPMM.Cluster, GaussianDPMM.ModelParameters, GaussianDPMM.TrainingParameters, GaussianDPMM.ValidationMetrics> {
    
    /**
     * The Cluster class of the GaussianDPMM model.
     */
    public static class Cluster extends BaseDPMM.Cluster {
        //informational fields
        private int dimensions;

        //hyper parameters
        private int kappa0;
        private int nu0;
        private RealVector mu0;
        private RealMatrix psi0;
        
        //cluster parameters
        private RealVector mean;
        private RealMatrix covariance;
        
        //validation - confidence interval vars
        private RealMatrix meanError;
        private int meanDf;
        
        //internal vars for calculation
        private transient RealVector xi_sum;
        private transient RealMatrix xi_square_sum; 
        
        //Cache
        private transient Double cache_covariance_determinant; //Cached value of Covariance determinant used only for speed optimization
        private transient RealMatrix cache_covariance_inverse; //Cached value of Inverse Covariance used only for speed optimization
        
        /**
         * Public constructor of Cluster which takes as argument a unique id.
         * 
         * @param clusterId 
         */
        public Cluster(Integer clusterId) {
            super(clusterId);
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
        
        
        //hyper parameters getters/setters
        /**
         * Getter for Kappa0 hyperparameter.
         * 
         * @return 
         */
        public int getKappa0() {
            return kappa0;
        }

        /**
         * Setter for Kappa0 hyperparameter.
         * 
         * @param kappa0
         */
        public void setKappa0(int kappa0) {
            this.kappa0 = kappa0;
        }

        /**
         * Getter for Nu0 hyperparameter.
         * 
         * @return 
         */
        public int getNu0() {
            return nu0;
        }

        /**
         * Getter for Nu0 hyperparameter.
         * 
         * @param nu0 
         */
        public void setNu0(int nu0) {
            this.nu0 = nu0;
        }

        /**
         * Getter for Mu0 hyperparameter.
         * 
         * @return 
         */
        public RealVector getMu0() {
            return mu0;
        }

        /**
         * Getter for Mu0 hyperparameter.
         * 
         * @param mu0 
         */
        public void setMu0(RealVector mu0) {
            this.mu0 = mu0;
        }

        /**
         * Getter for Psi0 hyperparameter.
         * 
         * @return 
         */
        public RealMatrix getPsi0() {
            return psi0;
        }
        
        /**
         * Setter for Psi0 hyperparameter.
         * 
         * @param psi0 
         */
        public void setPsi0(RealMatrix psi0) {
            this.psi0 = psi0;
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
         * Getter for the Mean Error.
         * 
         * @return 
         */
        public RealMatrix getMeanError() {
            return meanError;
        }
        
        /**
         * Getter for the Mean df (degrees of freedom).
         * 
         * @return 
         */
        public int getMeanDf() {
            return meanDf;
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

            x_mu = x_mu.subtract(mean);
            
            Double determinant;
            RealMatrix invCovariance;

            if(cache_covariance_determinant==null || cache_covariance_inverse==null) {
                LUDecomposition lud = new LUDecomposition(covariance);
                cache_covariance_determinant = lud.getDeterminant();
                cache_covariance_inverse = lud.getSolver().getInverse();
                lud =null;
            }
            determinant=cache_covariance_determinant;
            invCovariance=cache_covariance_inverse;

            double x_muInvSx_muT = (invCovariance.preMultiply(x_mu)).dotProduct(x_mu);

            double normConst = 1.0/( Math.pow(2*Math.PI, dimensions/2.0) * Math.pow(determinant, 0.5) );


            //double pdf = Math.exp(-0.5 * x_muInvSx_muT)*normConst;
            double logPdf = -0.5 * x_muInvSx_muT + Math.log(normConst);
            return logPdf;
        }

        /**
         * Initializes the cluster's internal parameters: mean, covariance, meanError and meanDf.
         */
        @Override
        public void initializeClusterParameters() {
            //Set default hyperparameters if not set

            if(nu0<dimensions) {
                nu0 = dimensions;
            }
            
            if(mu0==null) {
                mu0 = new ArrayRealVector(dimensions); //0 vector
            }
            
            if(psi0==null) {
                psi0 = MatrixUtils.createRealIdentityMatrix(dimensions); //identity matrix
            }

            mean = new ArrayRealVector(dimensions); //ContinuousDistributions.multinomialGaussianSample(mean, covariance)
            covariance = MatrixUtils.createRealIdentityMatrix(dimensions);
            
            meanError = calculateMeanError(psi0, kappa0, nu0);
            meanDf = nu0-dimensions+1;
            
            cache_covariance_determinant=null;
            cache_covariance_inverse=null;
        }
    
        /**
         * Internal method that adds the record in cluster and updates clusterParams.
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
                xi_sum=rv;
                xi_square_sum=rv.outerProduct(rv);
            }
            else {
                xi_sum=xi_sum.add(rv);
                xi_square_sum=xi_square_sum.add(rv.outerProduct(rv));
            }
            
            updateClusterParameters();
            
            return true;
        }
        
        /**
         * Internal method that removes the record from cluster and updates clusterParams.
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
            xi_sum=xi_sum.subtract(rv);
            xi_square_sum=xi_square_sum.subtract(rv.outerProduct(rv));
            
            updateClusterParameters();
            
            return true;
        }
        
        private RealMatrix calculateMeanError(RealMatrix Psi, int kappa, int nu) {
            //Reference: page 18, equation 228 at http://www.cs.ubc.ca/~murphyk/Papers/bayesGauss.pdf
            return Psi.scalarMultiply(1.0/(kappa*(nu-dimensions+1.0)));
        }
                
        @Override
        protected void clear() {
            super.clear();
            xi_sum = null;
            xi_square_sum = null;
        }

        @Override
        protected void updateClusterParameters() {
            int n = recordIdSet.size();

            //fetch hyperparameters

            int kappa_n = kappa0 + n;
            int nu = nu0 + n;

            RealVector mu = xi_sum.mapDivide(n);
            RealVector mu_mu0 = mu.subtract(mu0);

            RealMatrix C = xi_square_sum.subtract( ( mu.outerProduct(mu) ).scalarMultiply(n) );

            RealMatrix psi = psi0.add( C.add( ( mu_mu0.outerProduct(mu_mu0) ).scalarMultiply(kappa0*n/(double)kappa_n) ));
            C = null;
            mu_mu0 = null;

            mean = ( mu0.mapMultiply(kappa0) ).add( mu.mapMultiply(n) ).mapDivide(kappa_n);
            covariance = psi.scalarMultiply(  (kappa_n+1.0)/(kappa_n*(nu - dimensions + 1.0))  );

            //clear cache
            cache_covariance_determinant=null;
            cache_covariance_inverse=null;

            meanError = calculateMeanError(psi, kappa_n, nu);
            meanDf = nu-dimensions+1;
        }
    }
    
    /**
     * The ModelParameters class stores the coefficients that were learned during
     * the training of the algorithm.
     */
    public static class ModelParameters extends BaseDPMM.ModelParameters<GaussianDPMM.Cluster> {

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
        private int kappa0 = 0;
        private int nu0 = 1;
        private double[] mu0;
        
        private double[][] psi0;
        
        /**
         * Getter for Kappa0 hyperparameter.
         * 
         * @return 
         */
        public int getKappa0() {
            return kappa0;
        }

        /**
         * Setter for Kappa0 hyperparameter.
         * 
         * @param kappa0
         */
        public void setKappa0(int kappa0) {
            this.kappa0 = kappa0;
        }
        
        /**
         * Getter for Nu0 hyperparameter.
         * 
         * @return 
         */
        public int getNu0() {
            return nu0;
        }

        /**
         * Getter for Nu0 hyperparameter.
         * 
         * @param nu0 
         */
        public void setNu0(int nu0) {
            this.nu0 = nu0;
        }

        /**
         * Getter for Mu0 hyperparameter.
         * 
         * @return 
         */
        public double[] getMu0() {
            return mu0;
        }

        /**
         * Getter for Mu0 hyperparameter.
         * 
         * @param mu0 
         */
        public void setMu0(double[] mu0) {
            this.mu0 = mu0;
        }

        /**
         * Getter for Psi0 hyperparameter.
         * 
         * @return 
         */
        public double[][] getPsi0() {
            return psi0;
        }

        /**
         * Setter for Psi0 hyperparameter.
         * 
         * @param psi0 
         */
        public void setPsi0(double[][] psi0) {
            this.psi0 = psi0;
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
    public GaussianDPMM(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, GaussianDPMM.ModelParameters.class, GaussianDPMM.TrainingParameters.class, GaussianDPMM.ValidationMetrics.class);
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
        c.setKappa0(trainingParameters.getKappa0());
        c.setNu0(trainingParameters.getNu0());
        
        double[] mu0 = trainingParameters.getMu0();
        if(mu0!=null) {
            c.setMu0(new ArrayRealVector(mu0));
        }
        
        double[][] psi0 = trainingParameters.getPsi0();
        if(psi0!=null) {
            c.setPsi0(new BlockRealMatrix(psi0));
        }
        
        c.initializeClusterParameters();
        
        return c;
    }
    
}
