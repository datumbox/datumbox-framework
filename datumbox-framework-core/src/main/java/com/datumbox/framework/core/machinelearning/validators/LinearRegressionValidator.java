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
package com.datumbox.framework.core.machinelearning.validators;

import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.FlatDataList;
import com.datumbox.framework.common.dataobjects.Record;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.machinelearning.common.abstracts.validators.AbstractValidator;
import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;
import com.datumbox.framework.core.statistics.nonparametrics.onesample.Lilliefors;
import com.datumbox.framework.core.statistics.parametrics.onesample.DurbinWatson;

import java.util.List;

/**
 * Validation class for Linear Regression.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class LinearRegressionValidator extends AbstractValidator<LinearRegressionValidator.ValidationMetrics> {

    /** {@inheritDoc} */
    public static class ValidationMetrics extends AbstractValidator.AbstractValidationMetrics {
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

    /** {@inheritDoc} */
    @Override
    public ValidationMetrics validate(Dataframe predictedData) {
        //create new validation metrics object
        ValidationMetrics validationMetrics = new ValidationMetrics();

        int n = predictedData.size();

        FlatDataList errorList = new FlatDataList();
        double Ybar = 0.0;
        for(Record r : predictedData) {
            Ybar += TypeInference.toDouble(r.getY())/n;
            errorList.add(TypeInference.toDouble(r.getY())-TypeInference.toDouble(r.getYPredicted()));
        }

        validationMetrics.setDW(DurbinWatson.calculateScore(errorList)); //autocorrelation metric (around 2 no autocorrelation)

        double SSE = 0.0;
        for(Record r : predictedData) {
            SSE += Math.pow(TypeInference.toDouble(r.getY())-TypeInference.toDouble(r.getYPredicted()), 2.0);
        }
        validationMetrics.setSSE(SSE);

        boolean normalResiduals = Lilliefors.test(errorList.toFlatDataCollection(), "normalDistribution", 0.05);
        validationMetrics.setNormalResiduals( (normalResiduals)?0.0:1.0 ); //if the Lilliefors validate rejects the H0 means that the normality hypothesis is rejected thus the residuals are not normal
        //errorList = null;

        double SSR = 0.0;
        for(Record r : predictedData) {
            SSR += Math.pow(TypeInference.toDouble(r.getY()) - Ybar, 2);
        }
        validationMetrics.setSSR(SSR);

        double SST = SSR+SSE;
        validationMetrics.setSST(SST);

        double RSquare = SSR/SST;
        validationMetrics.setRSquare(RSquare);

        int d = predictedData.xColumnSize()+1;//add one for the constant
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

    /** {@inheritDoc} */
    @Override
    public ValidationMetrics average(List<ValidationMetrics> validationMetricsList) {
        
        if(validationMetricsList.isEmpty()) {
            return null;
        }

        ValidationMetrics avgValidationMetrics = new ValidationMetrics();
        
        int k = validationMetricsList.size(); //number of samples
        for(ValidationMetrics vmSample : validationMetricsList) {
            avgValidationMetrics.setRSquare(avgValidationMetrics.getRSquare() + vmSample.getRSquare()/k);
            avgValidationMetrics.setRSquareAdjusted(avgValidationMetrics.getRSquareAdjusted() + vmSample.getRSquareAdjusted()/k);
            avgValidationMetrics.setSSE(avgValidationMetrics.getSSE() + vmSample.getSSE()/k);
            avgValidationMetrics.setSSR(avgValidationMetrics.getSSR() + vmSample.getSSR()/k);
            avgValidationMetrics.setSST(avgValidationMetrics.getSST() + vmSample.getSST()/k);
            avgValidationMetrics.setDfRegression(avgValidationMetrics.getDfRegression() + vmSample.getDfRegression()/k);
            avgValidationMetrics.setDfResidual(avgValidationMetrics.getDfResidual() + vmSample.getDfResidual()/k);
            avgValidationMetrics.setDfTotal(avgValidationMetrics.getDfTotal() + vmSample.getDfTotal()/k);
            avgValidationMetrics.setF(avgValidationMetrics.getF() + vmSample.getF()/k);
            avgValidationMetrics.setFPValue(avgValidationMetrics.getFPValue() + vmSample.getFPValue()/k);
            Double stdErrorOfEstimate = vmSample.getStdErrorOfEstimate();
            if(stdErrorOfEstimate==null) {
                stdErrorOfEstimate=0.0;
            }
            avgValidationMetrics.setStdErrorOfEstimate(avgValidationMetrics.getStdErrorOfEstimate() + stdErrorOfEstimate/k);
            avgValidationMetrics.setDW(avgValidationMetrics.getDW() + vmSample.getDW()/k);
            avgValidationMetrics.setNormalResiduals(avgValidationMetrics.getNormalResiduals() + vmSample.getNormalResiduals()/k); //percentage of samples that found the residuals to be normal
        }
        
        return avgValidationMetrics;
    }
}
