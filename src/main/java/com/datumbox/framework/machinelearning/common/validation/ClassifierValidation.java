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

import com.datumbox.framework.machinelearning.common.enums.SensitivityRates;
import com.datumbox.framework.machinelearning.common.bases.validation.ModelValidation;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclassifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public class ClassifierValidation<MP extends BaseMLclassifier.ModelParameters, TP extends BaseMLclassifier.TrainingParameters, VM extends BaseMLclassifier.ValidationMetrics> extends ModelValidation<MP, TP, VM> {
    
    
    public ClassifierValidation() {
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
            
            
        for(VM vmSample : validationMetricsList) {
            
            //fetch the classes from the keys of one of the micro metrics. This way if a class is not included in a fold, we don't get null exceptions
            Set<Object> classesSet = vmSample.getMicroPrecision().keySet();
            
            for(Object theClass : classesSet) {
                
                Map<List<Object>, Double> ctEntryMap = vmSample.getContingencyTable();
                
                //get the values of all SensitivityRates and average them
                for(SensitivityRates sr : SensitivityRates.values()) {
                    List<Object> tpk = Arrays.<Object>asList(theClass, sr);
                    
                    Double previousValue = avgValidationMetrics.getContingencyTable().get(tpk);
                    if(previousValue==null) {
                        previousValue=0.0;
                    }
                    
                    avgValidationMetrics.getContingencyTable().put(tpk, previousValue + ctEntryMap.get(tpk)/k);
                }
                
                //update micro metrics of class
                Double previousPrecision = avgValidationMetrics.getMicroPrecision().get(theClass);
                if(previousPrecision==null) {
                    previousPrecision=0.0;
                }
                avgValidationMetrics.getMicroPrecision().put(theClass, previousPrecision + vmSample.getMicroPrecision().get(theClass)/k);
                
                
                Double previousRecall = avgValidationMetrics.getMicroRecall().get(theClass);
                if(previousRecall==null) {
                    previousRecall=0.0;
                }
                avgValidationMetrics.getMicroRecall().put(theClass, previousRecall + vmSample.getMicroRecall().get(theClass)/k);
                
                
                Double previousF1 = avgValidationMetrics.getMicroF1().get(theClass);
                if(previousF1==null) {
                    previousF1=0.0;
                }
                avgValidationMetrics.getMicroF1().put(theClass, previousF1 + vmSample.getMicroF1().get(theClass)/k);

            }
            
            //update macro metrics
            avgValidationMetrics.setAccuracy(avgValidationMetrics.getAccuracy() + vmSample.getAccuracy()/k);
            avgValidationMetrics.setMacroPrecision(avgValidationMetrics.getMacroPrecision() + vmSample.getMacroPrecision()/k);
            avgValidationMetrics.setMacroRecall(avgValidationMetrics.getMacroRecall() + vmSample.getMacroRecall()/k);
            avgValidationMetrics.setMacroF1(avgValidationMetrics.getMacroF1() + vmSample.getMacroF1()/k);
        }
        
        
        return avgValidationMetrics;
    }
}
