/**
 * Copyright (C) 2013-2020 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.core.machinelearning.modelselection.metrics;

import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelselection.AbstractMetrics;

import java.util.*;

/**
 * Estimates validation metrics for Classifiers.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ClassificationMetrics extends AbstractMetrics {
    private static final long serialVersionUID = 1L;

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

    //validation metrics
    private double accuracy = 0.0;

    private double macroPrecision = 0.0;
    private double macroRecall = 0.0;
    private double macroF1 = 0.0;

    private final Map<Object, Double> microPrecision = new HashMap<>(); //this is small. Size equal to 4*class numbers

    private final Map<Object, Double> microRecall = new HashMap<>(); //this is small. Size equal to 4*class numbers

    private final Map<Object, Double> microF1 = new HashMap<>(); //this is small. Size equal to 4*class numbers

    private final Map<List<Object>, Double> contingencyTable = new HashMap<>(); //this is small. Size equal to 4*class numbers

    /**
     * Getter for Accuracy.
     *
     * @return
     */
    public double getAccuracy() {
        return accuracy;
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
     * Getter for Macro Recall.
     *
     * @return
     */
    public double getMacroRecall() {
        return macroRecall;
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
     * Getter for Micro Precision.
     *
     * @return
     */
    public Map<Object, Double> getMicroPrecision() {
        return microPrecision;
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
     * Getter for Micro F1.
     *
     * @return
     */
    public Map<Object, Double> getMicroF1() {
        return microF1;
    }

    /**
     * Getter for Contingency Table.
     *
     * @return
     */
    public Map<List<Object>, Double> getContingencyTable() {
        return contingencyTable;
    }

    /**
     * @param predictedData
     * @see AbstractMetrics#AbstractMetrics(Dataframe)
     */
    public ClassificationMetrics(Dataframe predictedData) {
        super(predictedData);

        //retrieve the classes from the dataset
        Set<Object> classesSet = new HashSet<>();
        for(Record r : predictedData) {
            classesSet.add(r.getY());
            classesSet.add(r.getYPredicted());
        }

        for(Object theClass : classesSet) {
            contingencyTable.put(Arrays.asList(theClass, SensitivityRates.TRUE_POSITIVE), 0.0); //true possitive
            contingencyTable.put(Arrays.asList(theClass, SensitivityRates.FALSE_POSITIVE), 0.0); //false possitive
            contingencyTable.put(Arrays.asList(theClass, SensitivityRates.TRUE_NEGATIVE), 0.0); //true negative
            contingencyTable.put(Arrays.asList(theClass, SensitivityRates.FALSE_NEGATIVE), 0.0); //false negative
        }

        int n = predictedData.size();
        int c = classesSet.size();

        int correctCount=0;
        for(Record r : predictedData) {
            Object yPred = r.getYPredicted();
            if(yPred.equals(r.getY())) {
                ++correctCount;

                for(Object cl : classesSet) {
                    if(cl.equals(yPred)) {
                        List<Object> tpk = Arrays.asList(cl, SensitivityRates.TRUE_POSITIVE);
                        contingencyTable.put(tpk, contingencyTable.get(tpk) + 1.0);
                    }
                    else {
                        List<Object> tpk = Arrays.asList(cl, SensitivityRates.TRUE_NEGATIVE);
                        contingencyTable.put(tpk, contingencyTable.get(tpk) + 1.0);
                    }
                }
            }
            else {
                for(Object cl : classesSet) {
                    if(cl.equals(yPred)) {
                        List<Object> tpk = Arrays.asList(cl, SensitivityRates.FALSE_POSITIVE);
                        contingencyTable.put(tpk, contingencyTable.get(tpk) + 1.0);
                    }
                    else if(cl.equals(r.getY())) {
                        List<Object> tpk = Arrays.asList(cl, SensitivityRates.FALSE_NEGATIVE);
                        contingencyTable.put(tpk, contingencyTable.get(tpk) + 1.0);
                    }
                    else {
                        List<Object> tpk = Arrays.asList(cl, SensitivityRates.TRUE_NEGATIVE);
                        contingencyTable.put(tpk, contingencyTable.get(tpk) + 1.0);
                    }
                }
            }
        }

        accuracy = correctCount/(double)n;

        //Average Precision, Recall and F1: http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.104.8244&rep=rep1&type=pdf
        int activeClasses = c;
        for(Object theClass : classesSet) {
            double tp = contingencyTable.get(Arrays.asList(theClass, SensitivityRates.TRUE_POSITIVE));
            double fp = contingencyTable.get(Arrays.asList(theClass, SensitivityRates.FALSE_POSITIVE));
            double fn = contingencyTable.get(Arrays.asList(theClass, SensitivityRates.FALSE_NEGATIVE));


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


            microPrecision.put(theClass, classPrecision);
            microRecall.put(theClass, classRecall);
            microF1.put(theClass, classF1);

            macroPrecision += classPrecision;
            macroRecall += classRecall;
            macroF1 += classF1;
        }

        macroPrecision /= activeClasses;
        macroRecall /= activeClasses;
        macroF1 /= activeClasses;
    }

    /**
     * @param validationMetricsList
     * @see AbstractMetrics#AbstractMetrics(List)
     */
    public ClassificationMetrics(List<ClassificationMetrics> validationMetricsList) {
        super(validationMetricsList);
        
        int k = validationMetricsList.size(); //number of samples

        for(ClassificationMetrics vmSample : validationMetricsList) {
            
            //fetch the classes from the keys of one of the micro metrics. This way if a class is not included in a fold, we don't get null exceptions
            Set<Object> classesSet = vmSample.getMicroPrecision().keySet();
            
            for(Object theClass : classesSet) {
                
                Map<List<Object>, Double> ctEntryMap = vmSample.getContingencyTable();
                
                //get the values of all SensitivityRates and average them
                for(SensitivityRates sr : SensitivityRates.values()) {
                    List<Object> tpk = Arrays.asList(theClass, sr);
                    
                    Double previousValue = contingencyTable.getOrDefault(tpk, 0.0);
                    contingencyTable.put(tpk, previousValue + ctEntryMap.get(tpk)/k);
                }
                
                //update micro metrics of class
                Double previousPrecision = microPrecision.getOrDefault(theClass, 0.0);
                microPrecision.put(theClass, previousPrecision + vmSample.getMicroPrecision().get(theClass)/k);
                
                
                Double previousRecall = microRecall.getOrDefault(theClass, 0.0);
                microRecall.put(theClass, previousRecall + vmSample.getMicroRecall().get(theClass)/k);
                
                
                Double previousF1 = microF1.getOrDefault(theClass, 0.0);
                microF1.put(theClass, previousF1 + vmSample.getMicroF1().get(theClass)/k);

            }
            
            //update macro metrics
            accuracy += vmSample.getAccuracy()/k;
            macroPrecision += vmSample.getMacroPrecision()/k;
            macroRecall += vmSample.getMacroRecall()/k;
            macroF1 += vmSample.getMacroF1()/k;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        String sep = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(":").append(sep);
        sb.append("accuracy=").append(accuracy).append(sep);
        sb.append("macroPrecision=").append(macroPrecision).append(sep);
        sb.append("macroRecall=").append(macroRecall).append(sep);
        sb.append("macroF1=").append(macroF1).append(sep);
        sb.append("microPrecision=").append(microPrecision).append(sep);
        sb.append("microRecall=").append(microRecall).append(sep);
        sb.append("microF1=").append(microF1).append(sep);
        sb.append("contingencyTable=").append(contingencyTable).append(sep);
        return sb.toString();
    }
}
