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
package com.datumbox.framework.machinelearning.common.bases.basemodels;

import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLregressor;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.MapType;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.StorageHint;
import com.datumbox.framework.machinelearning.common.validation.LinearRegressionValidation;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;
import com.datumbox.framework.statistics.nonparametrics.onesample.Lilliefors;
import com.datumbox.framework.statistics.parametrics.onesample.DurbinWatson;
import java.util.Map;


/**
 * Base class for Linear Regression Models.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public abstract class BaseLinearRegression<MP extends BaseLinearRegression.ModelParameters, TP extends BaseLinearRegression.TrainingParameters, VM extends BaseLinearRegression.ValidationMetrics> extends BaseMLregressor<MP, TP, VM> {
    
    /** {@inheritDoc} */
    public static abstract class ModelParameters extends BaseMLregressor.ModelParameters {

        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY)
        private Map<Object, Double> thitas; //the thita parameters of the model

        /** 
         * @param dbc
         * @see com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseModelParameters#BaseModelParameters(com.datumbox.common.persistentstorage.interfaces.DatabaseConnector) 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
        /**
         * Getter for the Thita coefficients.
         * 
         * @return 
         */
        public Map<Object, Double> getThitas() {
            return thitas;
        }
        
        /**
         * Setter for the Thita coefficients.
         * 
         * @param thitas 
         */
        protected void setThitas(Map<Object, Double> thitas) {
            this.thitas = thitas;
        }
    } 

    /** {@inheritDoc} */
    public static abstract class TrainingParameters extends BaseMLregressor.TrainingParameters {     

    } 
    
    /** {@inheritDoc} */
    public static abstract class ValidationMetrics extends BaseMLregressor.ValidationMetrics {
        private double RSquare = 0.0; 
        private double RSquareAdjusted = 0.0; 
        private double SSE = 0.0; 
        private double SSR = 0.0; 
        private double SST = 0.0; 
        private double dfRegression = 0.0; 
        private double dfResidual = 0.0; 
        private double dfTotal = 0.0; 
        private double F = 0.0; 
        private double FPValue = 0.0; 
        private Double StdErrorOfEstimate = 0.0; //this can have null value if dfResidual is 0
        private double DW = 0.0; //Durbin–Watson statistic
        private double NormalResiduals = 0.0; //Test on whether the residuals can be considered Normal
        
        /**
         * Getter for the R Square.
         * 
         * @return 
         */
        public double getRSquare() {
            return RSquare;
        }
        
        /**
         * Setter for the R Square.
         * 
         * @param RSquare 
         */
        public void setRSquare(double RSquare) {
            this.RSquare = RSquare;
        }
        
        /**
         * Getter for the R Square Adjusted.
         * 
         * @return 
         */
        public double getRSquareAdjusted() {
            return RSquareAdjusted;
        }
        
        /**
         * Setter for the R Square Adjusted.
         * 
         * @param RSquareAdjusted 
         */
        public void setRSquareAdjusted(double RSquareAdjusted) {
            this.RSquareAdjusted = RSquareAdjusted;
        }
        
        /**
         * Getter for the Sum of Squared Errors.
         * 
         * @return 
         */
        public double getSSE() {
            return SSE;
        }
        
        /**
         * Setter for the Sum of Squared Errors.
         * 
         * @param SSE 
         */
        public void setSSE(double SSE) {
            this.SSE = SSE;
        }
        
        /**
         * Getter for the Sum of Squared due to Regression.
         * 
         * @return 
         */
        public double getSSR() {
            return SSR;
        }
        
        /**
         * Setter for the Sum of Squared due to Regression.
         * 
         * @param SSR 
         */
        public void setSSR(double SSR) {
            this.SSR = SSR;
        }

        /**
         * Getter for the Sum of Squared Total.
         * 
         * @return 
         */
        public double getSST() {
            return SST;
        }
        
        /**
         * Setter for the Sum of Squared Total.
         * 
         * @param SST 
         */
        public void setSST(double SST) {
            this.SST = SST;
        }

        /**
         * Getter for the degrees of freedom of Regression.
         * 
         * @return 
         */
        public double getDfRegression() {
            return dfRegression;
        }
        
        /**
         * Setter for the degrees of freedom of Regression.
         * 
         * @param dfRegression 
         */
        public void setDfRegression(double dfRegression) {
            this.dfRegression = dfRegression;
        }
        
        /**
         * Getter for the degrees of freedom of Residual.
         * 
         * @return 
         */
        public double getDfResidual() {
            return dfResidual;
        }
        
        /**
         * Setter for the degrees of freedom of Residual.
         * 
         * @param dfResidual 
         */
        public void setDfResidual(double dfResidual) {
            this.dfResidual = dfResidual;
        }
        
        /**
         * Getter for the degrees of freedom of Total.
         * 
         * @return 
         */
        public double getDfTotal() {
            return dfTotal;
        }
        
        /**
         * Setter for the degrees of freedom of Total.
         * 
         * @param dfTotal 
         */
        public void setDfTotal(double dfTotal) {
            this.dfTotal = dfTotal;
        }
        
        /**
         * Getter for F score.
         * 
         * @return 
         */
        public double getF() {
            return F;
        }
        
        /**
         * Setter for F score.
         * 
         * @param F 
         */
        public void setF(double F) {
            this.F = F;
        }
        
        /**
         * Getter for F p-value.
         * 
         * @return 
         */
        public double getFPValue() {
            return FPValue;
        }
        
        /**
         * Setter for F p-value.
         * 
         * @param FPValue 
         */
        public void setFPValue(double FPValue) {
            this.FPValue = FPValue;
        }
        
        /**
         * Getter for Standard Error of Estimate.
         * 
         * @return 
         */
        public Double getStdErrorOfEstimate() {
            return StdErrorOfEstimate;
        }
        
        /**
         * Setter for Standard Error of Estimate.
         * 
         * @param StdErrorOfEstimate 
         */
        public void setStdErrorOfEstimate(Double StdErrorOfEstimate) {
            this.StdErrorOfEstimate = StdErrorOfEstimate;
        }
        
        /**
         * Getter of Durbin Watson statistic.
         * 
         * @return 
         */
        public double getDW() {
            return DW;
        }
        
        /**
         * Setter of Durbin Watson statistic.
         * 
         * @param DW 
         */
        public void setDW(double DW) {
            this.DW = DW;
        }
        
        /**
         * Getter for Normal Residuals.
         * 
         * @return 
         */
        public double getNormalResiduals() {
            return NormalResiduals;
        }
        
        /**
         * Setter for Normal Residuals.
         * 
         * @param NormalResiduals 
         */
        public void setNormalResiduals(double NormalResiduals) {
            this.NormalResiduals = NormalResiduals;
        }
        
    }
    
    /** 
     * @param dbName
     * @param dbConf
     * @param mpClass
     * @param tpClass
     * @param vmClass
     * @see com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseTrainable#BaseTrainable(java.lang.String, com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration, java.lang.Class, java.lang.Class)  
     */
    protected BaseLinearRegression(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass) {
        super(dbName, dbConf, mpClass, tpClass, vmClass, new LinearRegressionValidation<>());
    } 
    
    @Override
    protected VM validateModel(Dataframe validationData) {
        predictDataset(validationData);
        
        
        //create new validation metrics object
        VM validationMetrics = knowledgeBase.getEmptyValidationMetricsObject();
        
        int n = validationData.size();
        
        FlatDataList errorList = new FlatDataList();
        double Ybar = 0.0;
        for(Record r : validationData) {
            Ybar += TypeInference.toDouble(r.getY())/n;
            errorList.add(TypeInference.toDouble(r.getY())-TypeInference.toDouble(r.getYPredicted()));
        }

        validationMetrics.setDW(DurbinWatson.calculateScore(errorList)); //autocorrelation metric (around 2 no autocorrelation)
        
        double SSE = calculateSSE(validationData);
        validationMetrics.setSSE(SSE);
        
        boolean normalResiduals = Lilliefors.test(errorList.toFlatDataCollection(), "normalDistribution", 0.05);
        validationMetrics.setNormalResiduals( (normalResiduals)?0.0:1.0 ); //if the Lilliefors validate rejects the H0 means that the normality hypothesis is rejected thus the residuals are not normal
        errorList = null;
        
        double SSR = 0.0;
        for(Record r : validationData) {
            SSR += Math.pow(TypeInference.toDouble(r.getY()) - Ybar, 2);
        }
        validationMetrics.setSSR(SSR);
        
        double SST = SSR+SSE;
        validationMetrics.setSST(SST);
        
        double RSquare = SSR/SST;
        validationMetrics.setRSquare(RSquare);
        
        int d = knowledgeBase.getModelParameters().getD()+1;//add one for the constant
        int p = d - 1; //exclude constant
        
        double RSquareAdjusted = 1.0 - ((n-1.0)/(n-p-1.0))*(1.0-RSquare);
        validationMetrics.setRSquareAdjusted(RSquareAdjusted);
        
        //degrees of freedom
        double dfTotal = n-1.0;
        validationMetrics.setDfTotal(dfTotal);
        double dfRegression = d-1.0;
        validationMetrics.setDfRegression(dfRegression);
        double dfResidual = Math.max(n-d, 0.0);
        validationMetrics.setDfResidual(dfResidual);
        
        double F = (SSR/dfRegression)/(SSE/dfResidual);
        validationMetrics.setF(F);
        
        double FPValue = 1.0;
        if(n>d) {
            FPValue = ContinuousDistributions.fCdf(F, (int)dfRegression, (int)dfResidual);
        }
        validationMetrics.setFPValue(FPValue);
        
        Double StdErrorOfEstimate = null;
        if(dfResidual>0) {
            StdErrorOfEstimate = Math.sqrt(SSE/dfResidual);
        }
        validationMetrics.setStdErrorOfEstimate(StdErrorOfEstimate);
        
        return validationMetrics;
    }
    
    /**
     * Estimates the Sum of Squared Errors of the provided dataset.
     * 
     * @param validationData
     * @return 
     */
    protected double calculateSSE(Dataframe validationData) {
        double SSE = 0.0;
        for(Record r : validationData) {
            SSE += Math.pow(TypeInference.toDouble(r.getY())-TypeInference.toDouble(r.getYPredicted()), 2.0);
        }
        return SSE;
    }
    
}
