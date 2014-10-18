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
package com.datumbox.framework.machinelearning.common.validation;

import com.datumbox.framework.machinelearning.classification.OrdinalRegression;
import java.util.List;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class OrdinalRegressionValidation extends ClassifierValidation<OrdinalRegression.ModelParameters, OrdinalRegression.TrainingParameters, OrdinalRegression.ValidationMetrics> {
    
    
    public OrdinalRegressionValidation() {
        super();
    }
        
    @Override
    public OrdinalRegression.ValidationMetrics calculateAverageValidationMetrics(List<OrdinalRegression.ValidationMetrics> validationMetricsList) {
        OrdinalRegression.ValidationMetrics avgValidationMetrics = super.calculateAverageValidationMetrics(validationMetricsList);
        if(avgValidationMetrics==null) {
            return null;
        }
        
        int k = validationMetricsList.size(); //number of samples
        for(OrdinalRegression.ValidationMetrics vmSample : validationMetricsList) {
            avgValidationMetrics.setCountRSquare(avgValidationMetrics.getCountRSquare() + vmSample.getCountRSquare()/k);
            avgValidationMetrics.setSSE(avgValidationMetrics.getSSE() + vmSample.getSSE()/k);
        }
        
        return avgValidationMetrics;
    }
}
