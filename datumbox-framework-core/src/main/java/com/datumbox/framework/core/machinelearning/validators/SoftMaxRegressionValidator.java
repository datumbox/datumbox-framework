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

import com.datumbox.framework.core.machinelearning.classification.SoftMaxRegression;

import java.util.List;

/**
 * Validation class for SoftMax Regression.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class SoftMaxRegressionValidator extends ClassifierValidator<SoftMaxRegression.ModelParameters, SoftMaxRegression.TrainingParameters, SoftMaxRegression.ValidationMetrics> {
    
    /** {@inheritDoc} */
    @Override
    protected SoftMaxRegression.ValidationMetrics calculateAverageValidationMetrics(List<SoftMaxRegression.ValidationMetrics> validationMetricsList) {
        SoftMaxRegression.ValidationMetrics avgValidationMetrics = super.calculateAverageValidationMetrics(validationMetricsList);
        if(avgValidationMetrics==null) {
            return null;
        }
        
        int k = validationMetricsList.size(); //number of samples
        for(SoftMaxRegression.ValidationMetrics vmSample : validationMetricsList) {
            avgValidationMetrics.setCountRSquare(avgValidationMetrics.getCountRSquare() + vmSample.getCountRSquare()/k);
            avgValidationMetrics.setSSE(avgValidationMetrics.getSSE() + vmSample.getSSE()/k);
        }
        
        return avgValidationMetrics;
    }
}
