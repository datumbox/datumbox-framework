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
package com.datumbox.framework.machinelearning.common.validation;

import com.datumbox.framework.machinelearning.common.bases.validation.ModelValidation;
import com.datumbox.framework.machinelearning.common.bases.basemodels.BaseLinearRegression;
import java.util.List;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public class LinearRegressionValidation<MP extends BaseLinearRegression.ModelParameters, TP extends BaseLinearRegression.TrainingParameters, VM extends BaseLinearRegression.ValidationMetrics> extends ModelValidation<MP, TP, VM> {

    public LinearRegressionValidation() {
        super();
    }
       
    @Override
    public VM calculateAverageValidationMetrics(List<VM> validationMetricsList) {
        
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
