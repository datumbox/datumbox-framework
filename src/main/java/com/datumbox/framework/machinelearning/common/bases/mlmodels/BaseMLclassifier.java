/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.machinelearning.common.bases.mlmodels;

import com.datumbox.framework.machinelearning.common.bases.validation.ModelValidation;
import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.utilities.MapFunctions;
import com.datumbox.framework.machinelearning.common.enums.SensitivityRates;
import com.datumbox.framework.machinelearning.common.validation.ClassifierValidation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public abstract class BaseMLclassifier<MP extends BaseMLclassifier.ModelParameters, TP extends BaseMLclassifier.TrainingParameters, VM extends BaseMLclassifier.ValidationMetrics> extends BaseMLmodel<MP, TP, VM> {
    
    public static abstract class ModelParameters extends BaseMLmodel.ModelParameters {
        
        //Set with all the supported classes. Use Linked Hash Set to ensure that the order of classes will be maintained. Some method requires that (ordinal regression)
        private Set<Object> classes = new LinkedHashSet<>(); //this is small. Size equal to class numbers;

        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
        public Integer getC() {
            return classes.size();
        }

        public Set<Object> getClasses() {
            return classes;
        }

        protected void setClasses(Set<Object> classes) {
            this.classes = classes;
        }
        
        
        
    } 
    
    
    public static abstract class TrainingParameters extends BaseMLmodel.TrainingParameters {    

    } 

    
    public static abstract class ValidationMetrics extends BaseMLmodel.ValidationMetrics {
        
        //validation metrics
        private double accuracy = 0.0;

        private double macroPrecision = 0.0;
        private double macroRecall = 0.0;
        private double macroF1 = 0.0;
        
        private Map<Object, Double> microPrecision = new HashMap<>(); //this is small. Size equal to 4*class numbers
        
        private Map<Object, Double> microRecall = new HashMap<>(); //this is small. Size equal to 4*class numbers
        
        private Map<Object, Double> microF1 = new HashMap<>(); //this is small. Size equal to 4*class numbers
        
        private Map<List<Object>, Double> ContingencyTable = new HashMap<>(); //this is small. Size equal to 4*class numbers
        
        public double getAccuracy() {
            return accuracy;
        }

        public void setAccuracy(double accuracy) {
            this.accuracy = accuracy;
        }

        public double getMacroPrecision() {
            return macroPrecision;
        }

        public void setMacroPrecision(double macroPrecision) {
            this.macroPrecision = macroPrecision;
        }

        public double getMacroRecall() {
            return macroRecall;
        }

        public void setMacroRecall(double macroRecall) {
            this.macroRecall = macroRecall;
        }

        public double getMacroF1() {
            return macroF1;
        }

        public void setMacroF1(double macroF1) {
            this.macroF1 = macroF1;
        }

        public Map<Object, Double> getMicroPrecision() {
            return microPrecision;
        }

        public void setMicroPrecision(Map<Object, Double> microPrecision) {
            this.microPrecision = microPrecision;
        }

        public Map<Object, Double> getMicroRecall() {
            return microRecall;
        }

        public void setMicroRecall(Map<Object, Double> microRecall) {
            this.microRecall = microRecall;
        }

        public Map<Object, Double> getMicroF1() {
            return microF1;
        }

        public void setMicroF1(Map<Object, Double> microF1) {
            this.microF1 = microF1;
        }

        public Map<List<Object>, Double> getContingencyTable() {
            return ContingencyTable;
        }

        public void setContingencyTable(Map<List<Object>, Double> ContingencyTable) {
            this.ContingencyTable = ContingencyTable;
        }
    }
    
    protected BaseMLclassifier(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass) {
        super(dbName, dbConf, mpClass, tpClass, vmClass, new ClassifierValidation<>());
    } 
    
    protected BaseMLclassifier(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass, ModelValidation<MP, TP, VM> modelValidator) {
        super(dbName, dbConf, mpClass, tpClass, vmClass, modelValidator);
    } 
    
    @Override
    protected VM validateModel(Dataset validationData) {
        predictDataset(validationData);
        
        Set<Object> classesSet = knowledgeBase.getModelParameters().getClasses();
        
        //create new validation metrics object
        VM validationMetrics = knowledgeBase.getEmptyValidationMetricsObject();
        
        //short notation
        Map<List<Object>, Double> ctMap = validationMetrics.getContingencyTable();
        for(Object theClass : classesSet) {
            ctMap.put(Arrays.<Object>asList(theClass, SensitivityRates.TP), 0.0); //true possitive
            ctMap.put(Arrays.<Object>asList(theClass, SensitivityRates.FP), 0.0); //false possitive
            ctMap.put(Arrays.<Object>asList(theClass, SensitivityRates.TN), 0.0); //true negative
            ctMap.put(Arrays.<Object>asList(theClass, SensitivityRates.FN), 0.0); //false negative
        }
        
        int n = validationData.getRecordNumber();
        int c = classesSet.size();
        
        int correctCount=0;
        for(Integer rId : validationData) {
            Record r = validationData.get(rId);
            if(r.getYPredicted().equals(r.getY())) {
                ++correctCount;
                
                for(Object cl : classesSet) {
                    if(cl.equals(r.getYPredicted())) {
                        List<Object> tpk = Arrays.<Object>asList(cl, SensitivityRates.TP);
                        ctMap.put(tpk, ctMap.get(tpk) + 1.0);
                    }
                    else {
                        List<Object> tpk = Arrays.<Object>asList(cl, SensitivityRates.TN);
                        ctMap.put(tpk, ctMap.get(tpk) + 1.0);
                    }
                }
            }
            else {
                for(Object cl : classesSet) {
                    if(cl.equals(r.getYPredicted())) {
                        List<Object> tpk = Arrays.<Object>asList(cl, SensitivityRates.FP);
                        ctMap.put(tpk, ctMap.get(tpk) + 1.0);
                    }
                    else if(cl.equals(r.getY())) {
                        List<Object> tpk = Arrays.<Object>asList(cl, SensitivityRates.FN);
                        ctMap.put(tpk, ctMap.get(tpk) + 1.0);
                    }
                    else {
                        List<Object> tpk = Arrays.<Object>asList(cl, SensitivityRates.TN);
                        ctMap.put(tpk, ctMap.get(tpk) + 1.0);
                    }
                }
            }
        }
        
        validationMetrics.setAccuracy(correctCount/(double)n);
        
        //Average Precision, Recall and F1: http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.104.8244&rep=rep1&type=pdf
        
        for(Object theClass : classesSet) {

            
            double tp = ctMap.get(Arrays.<Object>asList(theClass, SensitivityRates.TP));
            double fp = ctMap.get(Arrays.<Object>asList(theClass, SensitivityRates.FP));
            double fn = ctMap.get(Arrays.<Object>asList(theClass, SensitivityRates.FN));
            

            double classPrecision=0.0;
            double classRecall=0.0;
            double classF1=0.0;
            if(tp>0.0) {
                classPrecision = tp/(tp+fp);
                classRecall = tp/(tp+fn);
                classF1 = 2.0*classPrecision*classRecall/(classPrecision+classRecall);                
            }
            else if(tp==0.0 && fp==0.0 && fn==0.0) {
                //if this category did not appear in the dataset then set the metrics to 1
                classPrecision=1.0;
                classRecall=1.0;
                classF1=1.0;
            }
        
            
            validationMetrics.getMicroPrecision().put(theClass, classPrecision);
            validationMetrics.getMicroRecall().put(theClass, classRecall);
            validationMetrics.getMicroF1().put(theClass, classF1);
            
            validationMetrics.setMacroPrecision(validationMetrics.getMacroPrecision() + classPrecision/c);
            validationMetrics.setMacroRecall(validationMetrics.getMacroRecall() + classRecall/c);
            validationMetrics.setMacroF1(validationMetrics.getMacroF1() + classF1/c);
        }
        
        return validationMetrics;
    }
    
    protected Object getSelectedClassFromClassScores(AssociativeArray predictionScores) {
        Map.Entry<Object, Object> maxEntry = MapFunctions.selectMaxKeyValue(predictionScores);
        
        return maxEntry.getKey();
    }
    
}