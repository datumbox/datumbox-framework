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

import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.Record;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.machinelearning.common.abstracts.validators.AbstractValidator;
import com.datumbox.framework.core.machinelearning.recommendersystem.CollaborativeFiltering;

import java.util.*;

/**
 * Estimates RMSE for ML models.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class RMSEValidator extends AbstractValidator<RMSEValidator.ValidationMetrics> {

    /** {@inheritDoc} */
    public static class ValidationMetrics extends AbstractValidator.AbstractValidationMetrics {

        //validation metrics
        private double RMSE = 0.0;

        /**
         * Getter for RMSE.
         *
         * @return
         */
        public double getRMSE() {
            return RMSE;
        }

        /**
         * Setter for RMSE.
         *
         * @param RMSE
         */
        public void setRMSE(double RMSE) {
            this.RMSE = RMSE;
        }

    }

    /** {@inheritDoc} */
    @Override
    public ValidationMetrics validate(Dataframe predictedData) {
        ValidationMetrics validationMetrics = new ValidationMetrics();

        double RMSE = 0.0;
        int i = 0;
        for(Record r : predictedData) {
            AssociativeArray predictions = r.getYPredictedProbabilities();
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object column = entry.getKey();
                Object value = entry.getValue();
                RMSE += Math.pow(TypeInference.toDouble(value)-TypeInference.toDouble(predictions.get(column)), 2.0);
                ++i;
            }
        }

        RMSE = Math.sqrt(RMSE/i);
        validationMetrics.setRMSE(RMSE);

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
            avgValidationMetrics.setRMSE(avgValidationMetrics.getRMSE() + vmSample.getRMSE()/k);
        }

        return avgValidationMetrics;
    }
}
