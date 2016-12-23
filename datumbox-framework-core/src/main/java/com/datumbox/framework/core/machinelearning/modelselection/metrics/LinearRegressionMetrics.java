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
package com.datumbox.framework.core.machinelearning.modelselection.metrics;

import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.FlatDataList;
import com.datumbox.framework.common.dataobjects.Record;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelselection.AbstractMetrics;
import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;
import com.datumbox.framework.core.statistics.nonparametrics.onesample.Lilliefors;
import com.datumbox.framework.core.statistics.parametrics.onesample.DurbinWatson;

import java.util.List;

/**
 * Validation class for Linear Regression.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class LinearRegressionMetrics extends AbstractMetrics {
    private static final long serialVersionUID = 1L;

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
     * Getter for the R Square Adjusted.
     *
     * @return
     */
    public double getRSquareAdjusted() {
        return RSquareAdjusted;
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
     * Getter for the Sum of Squared due to Regression.
     *
     * @return
     */
    public double getSSR() {
        return SSR;
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
     * Getter for the degrees of freedom of Regression.
     *
     * @return
     */
    public double getDfRegression() {
        return dfRegression;
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
     * Getter for the degrees of freedom of Total.
     *
     * @return
     */
    public double getDfTotal() {
        return dfTotal;
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
     * Getter for F p-value.
     *
     * @return
     */
    public double getFPValue() {
        return FPValue;
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
     * Getter of Durbin Watson statistic.
     *
     * @return
     */
    public double getDW() {
        return DW;
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
     * @param predictedData
     * @see AbstractMetrics#AbstractMetrics(Dataframe)
     */
    public LinearRegressionMetrics(Dataframe predictedData) {
        super(predictedData);

        int n = predictedData.size();

        FlatDataList errorList = new FlatDataList();
        double Ybar = 0.0;
        for(Record r : predictedData) {
            Ybar += TypeInference.toDouble(r.getY())/n;
            errorList.add(TypeInference.toDouble(r.getY())-TypeInference.toDouble(r.getYPredicted()));
        }

        DW = DurbinWatson.calculateScore(errorList);

        for(Record r : predictedData) {
            SSE += Math.pow(TypeInference.toDouble(r.getY())-TypeInference.toDouble(r.getYPredicted()), 2.0);
        }

        boolean normalResiduals = Lilliefors.test(errorList.toFlatDataCollection(), "normalDistribution", 0.05);
        NormalResiduals = (normalResiduals)?0.0:1.0; //if the Lilliefors validate rejects the H0 means that the normality hypothesis is rejected thus the residuals are not normal
        //errorList = null;

        for(Record r : predictedData) {
            SSR += Math.pow(TypeInference.toDouble(r.getY()) - Ybar, 2);
        }

        SST = SSR+SSE;
        RSquare = SSR/SST;

        int d = predictedData.xColumnSize()+1;//add one for the constant
        int p = d - 1; //exclude constant

        RSquareAdjusted = 1.0 - ((n-1.0)/(n-p-1.0))*(1.0-RSquare);

        //degrees of freedom
        dfTotal = n-1.0;
        dfRegression = d-1.0;
        dfResidual = Math.max(n-d, 0.0);

        F = (SSR/dfRegression)/(SSE/dfResidual);

        FPValue = 1.0;
        if(n>d) {
            FPValue = ContinuousDistributions.fCdf(F, (int)dfRegression, (int)dfResidual);
        }

        StdErrorOfEstimate = null;
        if(dfResidual > 0.0) {
            StdErrorOfEstimate = Math.sqrt(SSE/dfResidual);
        }
    }

    /**
     * @param validationMetricsList
     * @see AbstractMetrics#AbstractMetrics(List)
     */
    public LinearRegressionMetrics(List<LinearRegressionMetrics> validationMetricsList) {
        super(validationMetricsList);

        if(!validationMetricsList.isEmpty()) {
            int k = validationMetricsList.size(); //number of samples
            for (LinearRegressionMetrics vmSample : validationMetricsList) {
                RSquare += vmSample.getRSquare() / k;
                RSquareAdjusted += vmSample.getRSquareAdjusted() / k;
                SSE += vmSample.getSSE() / k;
                SSR += vmSample.getSSR() / k;
                SST += vmSample.getSST() / k;
                dfRegression += vmSample.getDfRegression() / k;
                dfResidual += vmSample.getDfResidual() / k;
                dfTotal += vmSample.getDfTotal() / k;
                F += vmSample.getF() / k;
                FPValue += vmSample.getFPValue() / k;
                Double stdErrorOfEstimate = vmSample.getStdErrorOfEstimate();
                if (stdErrorOfEstimate == null) {
                    stdErrorOfEstimate = 0.0;
                }
                StdErrorOfEstimate += stdErrorOfEstimate / k;
                DW += vmSample.getDW() / k;
                NormalResiduals += vmSample.getNormalResiduals() / k; //percentage of samples that found the residuals to be normal
            }
        }
    }
}
