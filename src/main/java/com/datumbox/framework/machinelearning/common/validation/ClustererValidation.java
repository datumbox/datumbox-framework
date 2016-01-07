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
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclusterer;
import java.util.List;

/**
 * Validation class for the Clustering models.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public class ClustererValidation<MP extends BaseMLclusterer.ModelParameters, TP extends BaseMLclusterer.TrainingParameters, VM extends BaseMLclusterer.ValidationMetrics> extends ModelValidation<MP, TP, VM> {
    
    @Override
    protected VM calculateAverageValidationMetrics(List<VM> validationMetricsList) {
        
        if(validationMetricsList.isEmpty()) {
            return null;
        }
        
        int k = validationMetricsList.size(); //number of samples

        
        //create a new empty ValidationMetrics Object
        VM avgValidationMetrics = (VM) validationMetricsList.iterator().next().getEmptyObject();
        
        //estimate average values
        for(VM vmSample : validationMetricsList) {
            if(vmSample.getNMI()==null) { //it is null when we don't have goldStandardClass information
                continue; //
            }
            
            //update metrics
            Double NMI = avgValidationMetrics.getNMI();
            if(NMI==null) {
                NMI = 0.0;
            }
            avgValidationMetrics.setNMI(NMI+ vmSample.getNMI()/k);
            Double purity = avgValidationMetrics.getPurity();
            if(purity==null) {
                purity = 0.0;
            }
            avgValidationMetrics.setPurity(purity+ vmSample.getPurity()/k);
        }
        
        
        return avgValidationMetrics;
    }
}
