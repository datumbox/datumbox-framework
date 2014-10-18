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
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclusterer;
import java.util.List;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public class ClustererValidation<MP extends BaseMLclusterer.ModelParameters, TP extends BaseMLclusterer.TrainingParameters, VM extends BaseMLclusterer.ValidationMetrics> extends ModelValidation<MP, TP, VM> {
    
    
    public ClustererValidation() {
        super();
    }
    
    @Override
    public VM calculateAverageValidationMetrics(List<VM> validationMetricsList) {
        
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
