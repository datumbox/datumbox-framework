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
import org.apache.commons.math3.linear.*;

import java.util.Map;



/**
 * The GaussianDPMM implements Dirichlet Process Mixture Models with Multivariate 
 * Normal and Normal-Inverse-Wishart prior.
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
public class GaussianDPMM extends AbstractDPMM<GaussianDPMM.Cluster, GaussianDPMM.ModelParameters, GaussianDPMM.TrainingParameters> {

    /**
     * The AbstractCluster class of the GaussianDPMM model.
     */
    public static class Cluster extends AbstractDPMM.AbstractCluster {
        private static final long serialVersionUID = 2L;

        //informational fields
        private final int dimensions;

        //hyper parameters
        private final int kappa0;
        private final int nu0;
        private final RealVector mu0;
        private final RealMatrix psi0;

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
        private volatile Double cache_covariance_determinant;
        private volatile Array2DRowRealMatrix cache_covariance_inverse;

        /**
         * @param clusterId
         * @param kappa0
         * @param nu0
         * @param dimensions
         * @param mu0
         * @param psi0
         * @see AbstractClusterer.AbstractCluster
         */
        protected Cluster(Integer clusterId, int dimensions, int kappa0, int nu0, RealVector mu0, RealMatrix psi0) {
            super(clusterId);

            if(nu0<dimensions) {
                nu0 = dimensions;
            }

            mean = new OpenMapRealVector(dimensions);
            covariance = new OpenMapRealMatrix(dimensions, dimensions);
            for(int i=0;i<dimensions;i++) {
                covariance.setEntry(i, i, 1.0);
            }

            meanError = calculateMeanError(psi0, kappa0, nu0);
            meanDf = nu0-dimensions+1;


            this.kappa0 = kappa0;
            this.nu0 = nu0;
            this.mu0 = new OpenMapRealVector(mu0);
            this.psi0 = new OpenMapRealMatrix(dimensions, dimensions).add(psi0);
            this.dimensions = dimensions;

            xi_sum = new OpenMapRealVector(dimensions);
            xi_square_sum = new OpenMapRealMatrix(dimensions,dimensions);
            cache_covariance_determinant = null;
            cache_covariance_inverse = null;
        }

        /**
         * Ensure that the cluster parameters can be modified.
         */
        private void assertModifiable() {
            if(xi_sum == null || xi_square_sum == null) {
                throw new RuntimeException("The cluster parameters are already estimated.");
            }
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

        /**
         * Getter for the Mean Error.
         *
         * @return
         */
        protected RealMatrix getMeanError() {
            return meanError;
        }

        /**
         * Getter for the Mean df (degrees of freedom).
         *
         * @return
         */
        protected int getMeanDf() {
            return meanDf;
        }

        /** {@inheritDoc} */
        @Override
        protected double posteriorLogPdf(Record r) {
            RealVector x_mu = DataframeMatrix.parseRecord(r, featureIds);

            x_mu = x_mu.subtract(mean);

            if(cache_covariance_determinant==null || cache_covariance_inverse==null) {
                synchronized(this) {
                    if(cache_covariance_determinant==null || cache_covariance_inverse==null) {
                        LUDecomposition lud = new LUDecomposition(covariance);
                        cache_covariance_determinant = lud.getDeterminant();
                        cache_covariance_inverse = (Array2DRowRealMatrix) lud.getSolver().getInverse();
                    }
                }
            }

            double x_muInvSx_muT = (cache_covariance_inverse.preMultiply(x_mu)).dotProduct(x_mu);
            double normConst = 1.0/( Math.pow(2*Math.PI, dimensions/2.0) * Math.pow(cache_covariance_determinant, 0.5) );
            //double pdf = Math.exp(-0.5 * x_muInvSx_muT)*normConst;
            double logPdf = -0.5 * x_muInvSx_muT + Math.log(normConst);
            return logPdf;
        }

        /** {@inheritDoc} */
        @Override
        protected void add(Record r) {
            assertModifiable();

            RealVector rv = DataframeMatrix.parseRecord(r, featureIds);

            //update cluster clusterParameters
            xi_sum = xi_sum.add(rv);
            xi_square_sum = xi_square_sum.add(rv.outerProduct(rv));

            size++;

            updateClusterParameters();
        }

        /** {@inheritDoc} */
        @Override
        protected void remove(Record r) {
            assertModifiable();
            if(size == 0) {
                throw new IllegalArgumentException("The cluster is empty.");
            }

            size--;

            RealVector rv = DataframeMatrix.parseRecord(r, featureIds);

            //update cluster clusterParameters
            xi_sum=xi_sum.subtract(rv);
            xi_square_sum=xi_square_sum.subtract(rv.outerProduct(rv));

            updateClusterParameters();
        }

        private RealMatrix calculateMeanError(RealMatrix Psi, int kappa, int nu) {
            //Reference: page 18, equation 228 at http://www.cs.ubc.ca/~murphyk/Papers/bayesGauss.pdf
            return Psi.scalarMultiply(1.0/(kappa*(nu-dimensions+1.0)));
        }

        /** {@inheritDoc} */
        @Override
        protected void clear() {
            xi_sum = null;
            xi_square_sum = null;
            cache_covariance_determinant = null;
            cache_covariance_inverse = null;
        }

        /** {@inheritDoc} */
        @Override
        protected void updateClusterParameters() {
            assertModifiable();
            //fetch hyperparameters

            int kappa_n = kappa0 + size;
            int nu = nu0 + size;

            RealVector mu = xi_sum.mapDivide(size);
            RealVector mu_mu0 = mu.subtract(mu0);

            RealMatrix C = xi_square_sum.subtract( ( mu.outerProduct(mu) ).scalarMultiply(size) );

            RealMatrix psi = psi0.add( C.add( ( mu_mu0.outerProduct(mu_mu0) ).scalarMultiply(kappa0*size/(double)kappa_n) ));//

            mean = ( mu0.mapMultiply(kappa0) ).add( mu.mapMultiply(size) ).mapDivide(kappa_n);

            synchronized(this) {
                covariance = psi.scalarMultiply(  (kappa_n+1.0)/(kappa_n*(nu - dimensions + 1.0))  );
                cache_covariance_determinant = null;
                cache_covariance_inverse = null;
            }

            meanError = calculateMeanError(psi, kappa_n, nu);
            meanDf = nu-dimensions+1;
        }
    }

    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractDPMM.AbstractModelParameters<GaussianDPMM.Cluster> {
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
        private static final long serialVersionUID = 2L;

        private int kappa0 = 0;
        private int nu0 = 1;
        private RealVector mu0;

        private RealMatrix psi0;

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

    }


    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainer.AbstractTrainingParameters, Configuration)
     */
    protected GaussianDPMM(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected GaussianDPMM(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }

    /** {@inheritDoc} */
    @Override
    protected Cluster createNewCluster(Integer clusterId) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();

        Cluster c = new Cluster(
                clusterId,
                modelParameters.getD(),
                trainingParameters.getKappa0(),
                trainingParameters.getNu0(),
                trainingParameters.getMu0(),
                trainingParameters.getPsi0()

        );
        c.setFeatureIds(modelParameters.getFeatureIds());

        return c;
    }

}
