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
package com.datumbox.framework.machinelearning.common.bases.mlmodels;

import com.datumbox.framework.machinelearning.common.bases.validation.ModelValidation;
import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.utilities.MapFunctions;
import com.datumbox.framework.machinelearning.common.enums.SensitivityRates;
import com.datumbox.framework.machinelearning.common.validation.ClassifierValidation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
public abstract class BaseMLclassifier<MP extends BaseMLclassifier.ModelParameters, TP extends BaseMLclassifier.TrainingParameters, VM extends BaseMLclassifier.ValidationMetrics> extends BaseMLmodel<MP, TP, VM> {
    
    public static abstract class ModelParameters extends BaseMLmodel.ModelParameters {
        //number of observations used for training
        private Integer n =0;
        
        //number of features in data. IN DATA not in the algorithm. Typically the features of the algortihm is d*c
        private Integer d =0;
        
        //number of classes in data
        private Integer c =0;

        //Set with all the supported classes. Use Linked Hash Set to ensure that the order of classes will be maintained. Some method requires that (ordinal regression)
        private Set<Object> classes = new LinkedHashSet<>(); //this is small. Size equal to class numbers;
        
        /*
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            super.bigDataStructureInitializer(bdsf, memoryConfiguration);
        }
        */
        
        public Integer getN() {
            return n;
        }

        public void setN(Integer n) {
            this.n = n;
        }

        public Integer getD() {
            return d;
        }

        public void setD(Integer d) {
            this.d = d;
        }

        public Integer getC() {
            return c;
        }

        public void setC(Integer c) {
            this.c = c;
        }

        public Set<Object> getClasses() {
            return classes;
        }

        public void setClasses(Set<Object> classes) {
            this.classes = classes;
        }
        
        
        
    } 
    
    
    public static abstract class TrainingParameters extends BaseMLmodel.TrainingParameters {    

    } 

    
    public static abstract class ValidationMetrics extends BaseMLmodel.ValidationMetrics {
        
        //validation metrics
        private double accuracy=0.0;

        private double macroPrecision=0.0;
        private double macroRecall=0.0;
        private double macroF1=0.0;
        
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
    
    protected BaseMLclassifier(String dbName, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass) {
        super(dbName, mpClass, tpClass, vmClass, new ClassifierValidation<>());
    } 
    
    protected BaseMLclassifier(String dbName, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass, ModelValidation<MP, TP, VM> modelValidator) {
        super(dbName, mpClass, tpClass, vmClass, modelValidator);
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
        
        int n = validationData.size();
        int c = classesSet.size();
        
        int correctCount=0;
        for(Record r : validationData) {
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