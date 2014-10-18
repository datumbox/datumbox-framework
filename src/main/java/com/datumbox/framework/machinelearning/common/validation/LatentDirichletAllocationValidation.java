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
import com.datumbox.framework.machinelearning.topicmodeling.LatentDirichletAllocation;
import java.util.List;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class LatentDirichletAllocationValidation extends ModelValidation<LatentDirichletAllocation.ModelParameters, LatentDirichletAllocation.TrainingParameters, LatentDirichletAllocation.ValidationMetrics> {
    
    
    public LatentDirichletAllocationValidation() {
        super();
    }
        
    @Override
    public LatentDirichletAllocation.ValidationMetrics calculateAverageValidationMetrics(List<LatentDirichletAllocation.ValidationMetrics> validationMetricsList) {

        if(validationMetricsList.isEmpty()) {
            return null;
        }
        
        int k = validationMetricsList.size(); //number of samples
        
        //create a new empty ValidationMetrics Object
        LatentDirichletAllocation.ValidationMetrics avgValidationMetrics = (LatentDirichletAllocation.ValidationMetrics) validationMetricsList.iterator().next().getEmptyObject();
        
        for(LatentDirichletAllocation.ValidationMetrics vmSample : validationMetricsList) {
            avgValidationMetrics.setPerplexity(avgValidationMetrics.getPerplexity()+ vmSample.getPerplexity()/k);
        }
        
        return avgValidationMetrics;
    }
}
