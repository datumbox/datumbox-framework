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
import java.util.Collection;
import java.util.Map;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Transient;


/**
 * Multivariate Normal with Normal-Inverse-Wishart prior.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class GaussianDPMM extends BaseDPMM<GaussianDPMM.Cluster, GaussianDPMM.ModelParameters, GaussianDPMM.TrainingParameters, GaussianDPMM.ValidationMetrics> {
    /*
      References:
           http://snippyhollow.github.io/blog/2013/03/10/collapsed-gibbs-sampling-for-dirichlet-process-gaussian-mixture-models/
           http://blog.echen.me/2012/03/20/infinite-mixture-models-with-nonparametric-bayes-and-the-dirichlet-process/
           http://www.cs.princeton.edu/courses/archive/fall07/cos597C/scribe/20070921.pdf
           iso-notes.docx
           notes.txt
    */
    
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    public static final String SHORT_METHOD_NAME = "GDPMM";
    
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
        private RealVector xi_sum;
        private RealMatrix xi_square_sum; 
        
        
        //Cache
        /**
         * Cached value of Covariance determinant used only for speed optimization
         */
        private transient Double cache_covariance_determinant;

        /**
         * Cached value of Inverse Covariance used only for speed optimization
         */
        private transient RealMatrix cache_covariance_inverse;
        
        
        

        
        
        
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
        public int getKappa0() {
            return kappa0;
        }

        public void setKappa0(int kappa0) {
            this.kappa0 = kappa0;
        }

        public int getNu0() {
            return nu0;
        }

        public void setNu0(int nu0) {
            this.nu0 = nu0;
        }

        public RealVector getMu0() {
            return mu0;
        }

        public void setMu0(RealVector mu0) {
            this.mu0 = mu0;
        }

        public RealMatrix getPsi0() {
            return psi0;
        }

        public void setPsi0(RealMatrix psi0) {
            this.psi0 = psi0;
        }

        public int getDimensions() {
            return dimensions;
        }

        public void setDimensions(int dimensions) {
            this.dimensions = dimensions;
        }

        public RealMatrix getMeanError() {
            return meanError;
        }

        public int getMeanDf() {
            return meanDf;
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
                xi_sum=rv;
                xi_square_sum=rv.outerProduct(rv);
            }
            else {
                xi_sum=xi_sum.add(rv);
                xi_square_sum=xi_square_sum.add(rv.outerProduct(rv));
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
            xi_sum=xi_sum.subtract(rv);
            xi_square_sum=xi_square_sum.subtract(rv.outerProduct(rv));
            
            updateClusterParameters();
            
            return true;
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
    
        private RealMatrix calculateMeanError(RealMatrix Psi, int kappa, int nu) {
            //Reference: page 18, equation 228 at http://www.cs.ubc.ca/~murphyk/Papers/bayesGauss.pdf
            return Psi.scalarMultiply(1.0/(kappa*(nu-dimensions+1.0)));
        }
        

        @Override
        protected void updateClusterParameters() {
            int n = recordSet.size();

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
    }
    
    
    public static class ModelParameters extends BaseDPMM.ModelParameters<GaussianDPMM.Cluster> {
        

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
        private int kappa0 = 0;
        private int nu0 = 1;
        private double[] mu0;
        
        
        //MORPHIA does not support storing 2d arrays. svmModel has 2d arrays 
        //inside it. Thus to store it I have to serialize deserialize it and store
        //it as binary.
        @Transient
        private double[][] psi0; //the parameters of the svm model
        private transient double[] psi0_1d;
        
        @PrePersist
        public void prePersist(){
            if(psi0 != null && psi0_1d==null){
                int dimensions = psi0.length;
                psi0_1d = new double[dimensions*dimensions];
                for(int row=0;row<dimensions;++row) {
                    for(int col=0;col<dimensions;++col) {
                        psi0_1d[row*dimensions+col] = psi0[row][col];
                    }
                }
            }
        }

        @PostLoad
        public void postLoad(){
            if(psi0 == null && psi0_1d!=null){
                int dimensions = (int)Math.sqrt(psi0_1d.length);
                psi0 = new double[dimensions][dimensions];
                for(int row=0;row<dimensions;++row) {
                    for(int col=0;col<dimensions;++col) {
                        psi0[row][col] = psi0_1d[row*dimensions+col];
                    }
                }
            }
        }

        public int getKappa0() {
            return kappa0;
        }

        public void setKappa0(int kappa0) {
            this.kappa0 = kappa0;
        }

        public int getNu0() {
            return nu0;
        }

        public void setNu0(int nu0) {
            this.nu0 = nu0;
        }

        public double[] getMu0() {
            return mu0;
        }

        public void setMu0(double[] mu0) {
            this.mu0 = mu0;
        }

        public double[][] getPsi0() {
            return psi0;
        }

        public void setPsi0(double[][] psi0) {
            this.psi0 = psi0;
        }
    }
    
    
    public static class ValidationMetrics extends BaseDPMM.ValidationMetrics {
        
    }

    
    public GaussianDPMM(String dbName) {
        super(dbName, GaussianDPMM.ModelParameters.class, GaussianDPMM.TrainingParameters.class, GaussianDPMM.ValidationMetrics.class);
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
