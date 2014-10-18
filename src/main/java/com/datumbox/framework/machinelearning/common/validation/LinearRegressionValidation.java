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

import com.datumbox.framework.machinelearning.common.bases.validation.ModelValidation;
import com.datumbox.framework.machinelearning.common.bases.basemodels.BaseLinearRegression;
import java.util.List;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
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
