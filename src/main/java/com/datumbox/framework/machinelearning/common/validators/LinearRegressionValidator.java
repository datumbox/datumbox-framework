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
package com.datumbox.framework.machinelearning.common.validators;

import com.datumbox.framework.machinelearning.common.abstracts.validators.AbstractValidator;
import com.datumbox.framework.machinelearning.common.abstracts.algorithms.AbstractLinearRegression;
import java.util.List;

/**
 * Validation class for Linear Regression.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public class LinearRegressionValidator<MP extends AbstractLinearRegression.ModelParameters, TP extends AbstractLinearRegression.TrainingParameters, VM extends AbstractLinearRegression.ValidationMetrics> extends AbstractValidator<MP, TP, VM> {
    
    /** {@inheritDoc} */
    @Override
    protected VM calculateAverageValidationMetrics(List<VM> validationMetricsList) {
        
        if(validationMetricsList.isEmpty()) {
            return null;
        }
        
        //create a new empty ValidationMetrics Object
        VM avgValidationMetrics = (VM) validationMetricsList.iterator().next().getEmptyObject();
        
        int k = validationMetricsList.size(); //number of samples
        for(VM vmSample : validationMetricsList) {
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
