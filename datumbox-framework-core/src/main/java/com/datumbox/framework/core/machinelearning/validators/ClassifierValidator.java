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

import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.Record;
import com.datumbox.framework.core.machinelearning.common.abstracts.validators.AbstractValidator;

import java.util.*;

/**
 * Estimates validation metrics for Classifiers.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ClassifierValidator extends AbstractValidator<ClassifierValidator.ValidationMetrics> {

    /**
     * Enum that stores the 4 possible Sensitivity Rates.
     */
    public enum SensitivityRates {
        /**
         * True Positive.
         */
        TRUE_POSITIVE,

        /**
         * True Negative.
         */
        TRUE_NEGATIVE,

        /**
         * False Positive.
         */
        FALSE_POSITIVE,

        /**
         * False Negative.
         */
        FALSE_NEGATIVE;
    }

    /** {@inheritDoc} */
    public static class ValidationMetrics extends AbstractValidator.AbstractValidationMetrics {

        //validation metrics
        private double accuracy = 0.0;

        private double macroPrecision = 0.0;
        private double macroRecall = 0.0;
        private double macroF1 = 0.0;

        private Map<Object, Double> microPrecision = new HashMap<>(); //this is small. Size equal to 4*class numbers

        private Map<Object, Double> microRecall = new HashMap<>(); //this is small. Size equal to 4*class numbers

        private Map<Object, Double> microF1 = new HashMap<>(); //this is small. Size equal to 4*class numbers

        private Map<List<Object>, Double> ContingencyTable = new HashMap<>(); //this is small. Size equal to 4*class numbers

        /**
         * Getter for Accuracy.
         *
         * @return
         */
        public double getAccuracy() {
            return accuracy;
        }

        /**
         * Setter for Accuracy.
         *
         * @param accuracy
         */
        public void setAccuracy(double accuracy) {
            this.accuracy = accuracy;
        }

        /**
         * Getter for Macro Precision.
         *
         * @return
         */
        public double getMacroPrecision() {
            return macroPrecision;
        }

        /**
         * Setter for Macro Precision.
         *
         * @param macroPrecision
         */
        public void setMacroPrecision(double macroPrecision) {
            this.macroPrecision = macroPrecision;
        }

        /**
         * Getter for Macro Recall.
         *
         * @return
         */
        public double getMacroRecall() {
            return macroRecall;
        }

        /**
         * Setter for Macro Recall.
         *
         * @param macroRecall
         */
        public void setMacroRecall(double macroRecall) {
            this.macroRecall = macroRecall;
        }

        /**
         * Getter for Macro F1.
         *
         * @return
         */
        public double getMacroF1() {
            return macroF1;
        }

        /**
         * Setter for Macro F1.
         *
         * @param macroF1
         */
        public void setMacroF1(double macroF1) {
            this.macroF1 = macroF1;
        }

        /**
         * Getter for Micro Precision.
         *
         * @return
         */
        public Map<Object, Double> getMicroPrecision() {
            return microPrecision;
        }

        /**
         * Setter for Micro Precision.
         *
         * @param microPrecision
         */
        public void setMicroPrecision(Map<Object, Double> microPrecision) {
            this.microPrecision = microPrecision;
        }

        /**
         * Getter for Micro Recall.
         *
         * @return
         */
        public Map<Object, Double> getMicroRecall() {
            return microRecall;
        }

        /**
         * Setter for Micro Recall.
         *
         * @param microRecall
         */
        public void setMicroRecall(Map<Object, Double> microRecall) {
            this.microRecall = microRecall;
        }

        /**
         * Getter for Micro F1.
         *
         * @return
         */
        public Map<Object, Double> getMicroF1() {
            return microF1;
        }

        /**
         * Setter for Micro F1.
         *
         * @param microF1
         */
        public void setMicroF1(Map<Object, Double> microF1) {
            this.microF1 = microF1;
        }

        /**
         * Getter for Contingency Table.
         *
         * @return
         */
        public Map<List<Object>, Double> getContingencyTable() {
            return ContingencyTable;
        }

        /**
         * Setter for Contingency Table.
         *
         * @param ContingencyTable
         */
        public void setContingencyTable(Map<List<Object>, Double> ContingencyTable) {
            this.ContingencyTable = ContingencyTable;
        }
    }

    /** {@inheritDoc} */
    @Override
    public ValidationMetrics validate(Dataframe predictedData) {
        //retrieve the classes from the dataset
        Set<Object> classesSet = new HashSet<>();
        for(Record r : predictedData) {
            classesSet.add(r.getY());
            classesSet.add(r.getYPredicted());
        }

        //create new validation metrics object
        ValidationMetrics validationMetrics = new ValidationMetrics();

        Map<List<Object>, Double> ctMap = validationMetrics.getContingencyTable();
        for(Object theClass : classesSet) {
            ctMap.put(Arrays.asList(theClass, SensitivityRates.TRUE_POSITIVE), 0.0); //true possitive
            ctMap.put(Arrays.asList(theClass, SensitivityRates.FALSE_POSITIVE), 0.0); //false possitive
            ctMap.put(Arrays.asList(theClass, SensitivityRates.TRUE_NEGATIVE), 0.0); //true negative
            ctMap.put(Arrays.asList(theClass, SensitivityRates.FALSE_NEGATIVE), 0.0); //false negative
        }

        int n = predictedData.size();
        int c = classesSet.size();

        int correctCount=0;
        for(Record r : predictedData) {
            if(r.getYPredicted().equals(r.getY())) {
                ++correctCount;

                for(Object cl : classesSet) {
                    if(cl.equals(r.getYPredicted())) {
                        List<Object> tpk = Arrays.asList(cl, SensitivityRates.TRUE_POSITIVE);
                        ctMap.put(tpk, ctMap.get(tpk) + 1.0);
                    }
                    else {
                        List<Object> tpk = Arrays.asList(cl, SensitivityRates.TRUE_NEGATIVE);
                        ctMap.put(tpk, ctMap.get(tpk) + 1.0);
                    }
                }
            }
            else {
                for(Object cl : classesSet) {
                    if(cl.equals(r.getYPredicted())) {
                        List<Object> tpk = Arrays.asList(cl, SensitivityRates.FALSE_POSITIVE);
                        ctMap.put(tpk, ctMap.get(tpk) + 1.0);
                    }
                    else if(cl.equals(r.getY())) {
                        List<Object> tpk = Arrays.asList(cl, SensitivityRates.FALSE_NEGATIVE);
                        ctMap.put(tpk, ctMap.get(tpk) + 1.0);
                    }
                    else {
                        List<Object> tpk = Arrays.asList(cl, SensitivityRates.TRUE_NEGATIVE);
                        ctMap.put(tpk, ctMap.get(tpk) + 1.0);
                    }
                }
            }
        }

        validationMetrics.setAccuracy(correctCount/(double)n);

        //Average Precision, Recall and F1: http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.104.8244&rep=rep1&type=pdf
        int activeClasses = c;
        for(Object theClass : classesSet) {


            double tp = ctMap.get(Arrays.asList(theClass, SensitivityRates.TRUE_POSITIVE));
            double fp = ctMap.get(Arrays.asList(theClass, SensitivityRates.FALSE_POSITIVE));
            double fn = ctMap.get(Arrays.asList(theClass, SensitivityRates.FALSE_NEGATIVE));


            double classPrecision=0.0;
            double classRecall=0.0;
            double classF1=0.0;
            if(tp>0.0) {
                classPrecision = tp/(tp+fp);
                classRecall = tp/(tp+fn);
                classF1 = 2.0*classPrecision*classRecall/(classPrecision+classRecall);
            }
            else if(tp==0.0 && fp==0.0 && fn==0.0) {
                //if this category did not appear in the dataset reduce the number of classes
                --activeClasses;
            }


            validationMetrics.getMicroPrecision().put(theClass, classPrecision);
            validationMetrics.getMicroRecall().put(theClass, classRecall);
            validationMetrics.getMicroF1().put(theClass, classF1);

            validationMetrics.setMacroPrecision(validationMetrics.getMacroPrecision() + classPrecision);
            validationMetrics.setMacroRecall(validationMetrics.getMacroRecall() + classRecall);
            validationMetrics.setMacroF1(validationMetrics.getMacroF1() + classF1);
        }

        validationMetrics.setMacroPrecision(validationMetrics.getMacroPrecision()/activeClasses);
        validationMetrics.setMacroRecall(validationMetrics.getMacroRecall()/activeClasses);
        validationMetrics.setMacroF1(validationMetrics.getMacroF1()/activeClasses);

        return validationMetrics;
    }

    /** {@inheritDoc} */
    @Override
    public ValidationMetrics average(List<ValidationMetrics> validationMetricsList) {
        if(validationMetricsList.isEmpty()) {
            return null;
        }
        
        int k = validationMetricsList.size(); //number of samples

        ValidationMetrics avgValidationMetrics = new ValidationMetrics();
        for(ValidationMetrics vmSample : validationMetricsList) {
            
            //fetch the classes from the keys of one of the micro metrics. This way if a class is not included in a fold, we don't get null exceptions
            Set<Object> classesSet = vmSample.getMicroPrecision().keySet();
            
            for(Object theClass : classesSet) {
                
                Map<List<Object>, Double> ctEntryMap = vmSample.getContingencyTable();
                
                //get the values of all SensitivityRates and average them
                for(SensitivityRates sr : SensitivityRates.values()) {
                    List<Object> tpk = Arrays.asList(theClass, sr);
                    
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
