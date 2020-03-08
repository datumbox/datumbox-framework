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
package com.datumbox.framework.core.machinelearning.ensemblelearning;

import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.common.dataobjects.DataTable2D;
import com.datumbox.framework.common.dataobjects.FlatDataCollection;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.common.utilities.MapMethods;
import com.datumbox.framework.core.statistics.descriptivestatistics.Descriptives;

import java.util.ArrayList;
import java.util.Map;

/**
 * Implementation of various Fixed combination rules which can be used in order
 * to combine the responses of multiple classifiers.
 * 
 * References:
 * http://www2.cs.uh.edu/~ceick/ML/Topic12.ppt
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class FixedCombinationRules {
    
    /**
     * Combines the responses of the classifiers by using estimating the sum
     * of the probabilities of their responses.
     * 
     * @param classifierClassProbabilityMatrix
     * @return 
     */
    public static AssociativeArray sum(DataTable2D classifierClassProbabilityMatrix) {
        AssociativeArray combinedClassProbabilities = new AssociativeArray(); 
        
        for(Map.Entry<Object, AssociativeArray> entry : classifierClassProbabilityMatrix.entrySet()) {
            //Object classifier = entry.getKey();
            AssociativeArray listOfClassProbabilities = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : listOfClassProbabilities.entrySet()) {
                Object theClass = entry2.getKey();
                Double probability = TypeInference.toDouble(entry2.getValue());
                
                Double previousValue = combinedClassProbabilities.getDouble(theClass);
                if(previousValue==null) { 
                    previousValue=0.0;
                }
                
                combinedClassProbabilities.put(theClass, previousValue+probability);
            }
        }
        
        return combinedClassProbabilities;
    }
    
    /**
     * Combines the responses of the classifiers by using estimating the average
     * of the probabilities of their responses.
     * 
     * @param classifierClassProbabilityMatrix
     * @return 
     */
    public static AssociativeArray average(DataTable2D classifierClassProbabilityMatrix) {
        AssociativeArray combinedClassProbabilities = new AssociativeArray(); 
        
        int numberOfClasses = classifierClassProbabilityMatrix.size();
        
        for(Map.Entry<Object, AssociativeArray> entry : classifierClassProbabilityMatrix.entrySet()) {
            //Object classifier = entry.getKey();
            AssociativeArray listOfClassProbabilities = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : listOfClassProbabilities.entrySet()) {
                Object theClass = entry2.getKey();
                Double probability = TypeInference.toDouble(entry2.getValue());
                
                Double previousValue = combinedClassProbabilities.getDouble(theClass);
                if(previousValue==null) {
                    previousValue=0.0;
                }
                
                combinedClassProbabilities.put(theClass, previousValue+probability/numberOfClasses);
            }
        }
        
        return combinedClassProbabilities;
    }
    
    /**
     * Combines the responses of the classifiers by estimating the weighted
     * average of the probabilities of their responses.
     * 
     * @param classifierClassProbabilityMatrix
     * @param classifierWeights
     * @return 
     */
    public static AssociativeArray weightedAverage(DataTable2D classifierClassProbabilityMatrix, AssociativeArray classifierWeights) {
        AssociativeArray combinedClassProbabilities = new AssociativeArray(); 
        
        for(Map.Entry<Object, AssociativeArray> entry : classifierClassProbabilityMatrix.entrySet()) {
            Object classifier = entry.getKey();
            AssociativeArray listOfClassProbabilities = entry.getValue();
            Double classifierWeight = classifierWeights.getDouble(classifier);
            for(Map.Entry<Object, Object> entry2 : listOfClassProbabilities.entrySet()) {
                Object theClass = entry2.getKey();
                Double probability = TypeInference.toDouble(entry2.getValue());
                
                Double previousValue = combinedClassProbabilities.getDouble(theClass);
                if(previousValue==null) {  
                    previousValue=0.0;
                }
                
                combinedClassProbabilities.put(theClass, previousValue+probability*classifierWeight);
            }
        }
        
        return combinedClassProbabilities;
    }
    
    /**
     * Combines the responses of the classifiers by using estimating the median
     * of the probabilities of their responses.
     * 
     * @param classifierClassProbabilityMatrix
     * @return 
     */
    public static AssociativeArray median(DataTable2D classifierClassProbabilityMatrix) {
        AssociativeArray combinedClassProbabilities = new AssociativeArray(); 
        
        //extract all the classes first
        for(Map.Entry<Object, AssociativeArray> entry : classifierClassProbabilityMatrix.entrySet()) {
            //Object classifier = entry.getKey();
            AssociativeArray listOfClassProbabilities = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : listOfClassProbabilities.entrySet()) {
                Object theClass = entry2.getKey();
                //Double probability = Dataset.toDouble(entry2.getValue());
                combinedClassProbabilities.put(theClass, 0.0);
            }
        }
        
        for(Map.Entry<Object, Object> entry : combinedClassProbabilities.entrySet()) {
            Object theClass = entry.getKey();
            FlatDataCollection listOfProbabilities = new FlatDataCollection(new ArrayList<>());
            for(Map.Entry<Object, AssociativeArray> entry2 : classifierClassProbabilityMatrix.entrySet()) {
                //Object classifier = entry2.getKey();
                AssociativeArray listOfClassProbabilities = entry2.getValue();
                Double probability = listOfClassProbabilities.getDouble(theClass);
                if(probability!=null) {
                    listOfProbabilities.add(probability);
                }
            }
            combinedClassProbabilities.put(theClass, Descriptives.median(listOfProbabilities));
            //listOfProbabilities=null;
        }
                
        return combinedClassProbabilities;
    }
    
    /**
     * Combines the responses of the classifiers by using selecting the maximum
     * probability of each class.
     * 
     * @param classifierClassProbabilityMatrix
     * @return 
     */
    public static AssociativeArray maximum(DataTable2D classifierClassProbabilityMatrix) {
        AssociativeArray combinedClassProbabilities = new AssociativeArray(); 
        
        for(Map.Entry<Object, AssociativeArray> entry : classifierClassProbabilityMatrix.entrySet()) {
            //Object classifier = entry.getKey();
            AssociativeArray listOfClassProbabilities = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : listOfClassProbabilities.entrySet()) {
                Object theClass = entry2.getKey();
                Double probability = TypeInference.toDouble(entry2.getValue());
                
                Double previousValue = combinedClassProbabilities.getDouble(theClass);
                if(previousValue==null || probability>previousValue) { 
                    combinedClassProbabilities.put(theClass, probability);
                }
                
            }
        }
        
        return combinedClassProbabilities;
    }    
    
    /**
     * Combines the responses of the classifiers by using selecting the minimum
     * probability of each class.
     * 
     * @param classifierClassProbabilityMatrix
     * @return 
     */
    public static AssociativeArray minimum(DataTable2D classifierClassProbabilityMatrix) {
        AssociativeArray combinedClassProbabilities = new AssociativeArray(); 
        
        for(Map.Entry<Object, AssociativeArray> entry : classifierClassProbabilityMatrix.entrySet()) {
            //Object classifier = entry.getKey();
            AssociativeArray listOfClassProbabilities = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : listOfClassProbabilities.entrySet()) {
                Object theClass = entry2.getKey();
                Double probability = TypeInference.toDouble(entry2.getValue());
                
                Double previousValue = combinedClassProbabilities.getDouble(theClass);
                if(previousValue==null || probability<previousValue) { 
                    combinedClassProbabilities.put(theClass, probability);
                }
                
            }
        }
        
        return combinedClassProbabilities;
    }    
    
    /**
     * Combines the responses of the classifiers by using estimating the product
     * of the probabilities of their responses.
     * 
     * @param classifierClassProbabilityMatrix
     * @return 
     */
    public static AssociativeArray product(DataTable2D classifierClassProbabilityMatrix) {
        AssociativeArray combinedClassProbabilities = new AssociativeArray(); 
        
        for(Map.Entry<Object, AssociativeArray> entry : classifierClassProbabilityMatrix.entrySet()) {
            //Object classifier = entry.getKey();
            AssociativeArray listOfClassProbabilities = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : listOfClassProbabilities.entrySet()) {
                Object theClass = entry2.getKey();
                Double probability = TypeInference.toDouble(entry2.getValue());
                
                Double previousValue = combinedClassProbabilities.getDouble(theClass);
                if(previousValue==null) { 
                    previousValue=1.0;
                }
                
                combinedClassProbabilities.put(theClass, previousValue*probability);
            }
        }
        
        return combinedClassProbabilities;
    }
    
    /**
     * Combines the responses of the classifiers by summing the votes of each
     * winner class.
     * 
     * @param classifierClassProbabilityMatrix
     * @return 
     */
    public static AssociativeArray majorityVote(DataTable2D classifierClassProbabilityMatrix) {
        AssociativeArray combinedClassProbabilities = new AssociativeArray(); 
        
        //extract all the classes first
        for(Map.Entry<Object, AssociativeArray> entry : classifierClassProbabilityMatrix.entrySet()) {
            //Object classifier = entry.getKey();
            AssociativeArray listOfClassProbabilities = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : listOfClassProbabilities.entrySet()) {
                Object theClass = entry2.getKey();
                //Double probability = Dataset.toDouble(entry2.getValue());
                combinedClassProbabilities.put(theClass, 0.0);
            }
        }
        
        for(Map.Entry<Object, AssociativeArray> entry : classifierClassProbabilityMatrix.entrySet()) {
            //Object classifier = entry.getKey();
            AssociativeArray listOfClassProbabilities = entry.getValue();
            
            Map.Entry<Object, Object> selectedClassEntry = MapMethods.selectMaxKeyValue(listOfClassProbabilities);
            
            Object theClass = selectedClassEntry.getKey();
            
            Double previousValue = combinedClassProbabilities.getDouble(theClass);
            if(previousValue==null) {
                previousValue=0.0;
            }
            
            combinedClassProbabilities.put(theClass, previousValue+1.0);
        }
        
        return combinedClassProbabilities;
    }
    
}
