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
package com.datumbox.framework.machinelearning.common.validation;

import com.datumbox.framework.machinelearning.common.bases.validation.ModelValidation;
import com.datumbox.framework.machinelearning.recommendersystem.CollaborativeFiltering;
import java.util.List;

/**
 * Validation class for the Collaborative Filtering algorithm.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public class CollaborativeFilteringValidation<MP extends CollaborativeFiltering.ModelParameters, TP extends CollaborativeFiltering.TrainingParameters, VM extends CollaborativeFiltering.ValidationMetrics> extends ModelValidation<MP, TP, VM> {
 
    @Override
    protected VM calculateAverageValidationMetrics(List<VM> validationMetricsList) {
        
        if(validationMetricsList.isEmpty()) {
            return null;
        }
        
        //create a new empty ValidationMetrics Object
        VM avgValidationMetrics = (VM) validationMetricsList.iterator().next().getEmptyObject();
        
        int k = validationMetricsList.size(); //number of samples
        for(VM vmSample : validationMetricsList) {
            avgValidationMetrics.setRMSE(avgValidationMetrics.getRMSE() + vmSample.getRMSE()/k);
        }
        
        return avgValidationMetrics;
    }
}
