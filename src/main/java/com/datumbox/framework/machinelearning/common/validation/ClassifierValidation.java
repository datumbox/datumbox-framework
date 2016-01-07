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

import com.datumbox.framework.machinelearning.common.enums.SensitivityRates;
import com.datumbox.framework.machinelearning.common.bases.validation.ModelValidation;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclassifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validation class for the Classifier models.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public class ClassifierValidation<MP extends BaseMLclassifier.ModelParameters, TP extends BaseMLclassifier.TrainingParameters, VM extends BaseMLclassifier.ValidationMetrics> extends ModelValidation<MP, TP, VM> {
    
    @Override
    protected VM calculateAverageValidationMetrics(List<VM> validationMetricsList) {
        
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
