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

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLregressor;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureMarker;
import com.datumbox.framework.machinelearning.common.validation.LinearRegressionValidation;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;
import com.datumbox.framework.statistics.nonparametrics.onesample.Lilliefors;
import com.datumbox.framework.statistics.parametrics.onesample.DurbinWatson;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.mongodb.morphia.annotations.Transient;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public abstract class BaseLinearRegression<MP extends BaseLinearRegression.ModelParameters, TP extends BaseLinearRegression.TrainingParameters, VM extends BaseLinearRegression.ValidationMetrics> extends BaseMLregressor<MP, TP, VM> {

    
    public static abstract class ModelParameters extends BaseMLregressor.ModelParameters {

        /**
         * Thita weights
         */
        @BigDataStructureMarker
        @Transient
        private Map<Object, Double> thitas; //the thita parameters of the model

        
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            super.bigDataStructureInitializer(bdsf, memoryConfiguration);
            
            BigDataStructureFactory.MapType mapType = memoryConfiguration.getMapType();
            int LRUsize = memoryConfiguration.getLRUsize();
            
            thitas = bdsf.getMap("thitas", mapType, LRUsize);
        }
        
        public Map<Object, Double> getThitas() {
            return thitas;
        }

        public void setThitas(Map<Object, Double> thitas) {
            this.thitas = thitas;
        }
    } 

    
    public static abstract class TrainingParameters extends BaseMLregressor.TrainingParameters {     

    } 
    
    
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

        public double getRSquare() {
            return RSquare;
        }

        public void setRSquare(double RSquare) {
            this.RSquare = RSquare;
        }

        public double getRSquareAdjusted() {
            return RSquareAdjusted;
        }

        public void setRSquareAdjusted(double RSquareAdjusted) {
            this.RSquareAdjusted = RSquareAdjusted;
        }

        public double getSSE() {
            return SSE;
        }

        public void setSSE(double SSE) {
            this.SSE = SSE;
        }

        public double getSSR() {
            return SSR;
        }

        public void setSSR(double SSR) {
            this.SSR = SSR;
        }

        public double getSST() {
            return SST;
        }

        public void setSST(double SST) {
            this.SST = SST;
        }

        public double getDfRegression() {
            return dfRegression;
        }

        public void setDfRegression(double dfRegression) {
            this.dfRegression = dfRegression;
        }

        public double getDfResidual() {
            return dfResidual;
        }

        public void setDfResidual(double dfResidual) {
            this.dfResidual = dfResidual;
        }

        public double getDfTotal() {
            return dfTotal;
        }

        public void setDfTotal(double dfTotal) {
            this.dfTotal = dfTotal;
        }

        public double getF() {
            return F;
        }

        public void setF(double F) {
            this.F = F;
        }

        public double getFPValue() {
            return FPValue;
        }

        public void setFPValue(double FPValue) {
            this.FPValue = FPValue;
        }

        public Double getStdErrorOfEstimate() {
            return StdErrorOfEstimate;
        }

        public void setStdErrorOfEstimate(Double StdErrorOfEstimate) {
            this.StdErrorOfEstimate = StdErrorOfEstimate;
        }

        public double getDW() {
            return DW;
        }

        public void setDW(double DW) {
            this.DW = DW;
        }

        public double getNormalResiduals() {
            return NormalResiduals;
        }

        public void setNormalResiduals(double NormalResiduals) {
            this.NormalResiduals = NormalResiduals;
        }
        
    }

    
    protected BaseLinearRegression(String dbName, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass) {
        super(dbName, mpClass, tpClass, vmClass, new LinearRegressionValidation<>());
    } 
    

    @Override
    protected VM validateModel(Dataset validationData) {
        predictDataset(validationData);
        
        
        //create new validation metrics object
        VM validationMetrics = knowledgeBase.getEmptyValidationMetricsObject();
        
        int n = validationData.size();
        
        FlatDataList errorList = new FlatDataList();
        double Ybar = 0.0;
        for(Record r : validationData) {
            Ybar += Dataset.toDouble(r.getY())/n;
            errorList.add(Dataset.toDouble(r.getY())-Dataset.toDouble(r.getYPredicted()));
        }

        validationMetrics.setDW(DurbinWatson.calculateScore(errorList)); //autocorrelation metric (around 2 no autocorrelation)
        
        double SSE = calculateSSE(validationData);
        validationMetrics.setSSE(SSE);
        
        boolean normalResiduals;
        try {
            normalResiduals = Lilliefors.test(errorList.toFlatDataCollection(), "normalDistribution", 0.05);
        } catch (IllegalArgumentException | SecurityException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
        validationMetrics.setNormalResiduals( (normalResiduals)?0.0:1.0 ); //if the Lilliefors test rejects the H0 means that the normality hypothesis is rejected thus the residuals are not normal
        errorList = null;
        
        double SSR = 0.0;
        for(Record r : validationData) {
            SSR += Math.pow(Dataset.toDouble(r.getY()) - Ybar, 2);
        }
        validationMetrics.setSSR(SSR);
        
        double SST = SSR+SSE;
        validationMetrics.setSST(SST);
        
        double RSquare = SSR/SST;
        validationMetrics.setRSquare(RSquare);
        
        int d = knowledgeBase.getModelParameters().getD();
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
            FPValue = ContinuousDistributions.FCdf(F, (int)dfRegression, (int)dfResidual);
        }
        validationMetrics.setFPValue(FPValue);
        
        Double StdErrorOfEstimate = null;
        if(dfResidual>0) {
            Math.sqrt(SSE/dfResidual);
        }
        validationMetrics.setStdErrorOfEstimate(StdErrorOfEstimate);
        
        return validationMetrics;
    }
    
    protected double calculateSSE(Dataset validationData) {
        double SSE = 0.0;
        for(Record r : validationData) {
            SSE += Math.pow(Dataset.toDouble(r.getY())-Dataset.toDouble(r.getYPredicted()), 2.0);
        }
        return SSE;
    }
    
}
