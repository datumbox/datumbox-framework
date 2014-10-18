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
package com.datumbox.framework.machinelearning.ensemblelearning;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.DataTable2D;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.utilities.MapFunctions;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class FixedCombinationRules {
    //References: www2.cs.uh.edu/~ceick/ML/Topic12.ppt‎
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    public static AssociativeArray sum(DataTable2D classifierClassProbabilityMatrix) {
        AssociativeArray combinedClassProbabilities = new AssociativeArray(); //new TreeMap<>(Collections.reverseOrder())
        
        for(Map.Entry<Object, AssociativeArray> entry : classifierClassProbabilityMatrix.entrySet()) {
            //Object classifier = entry.getKey();
            AssociativeArray listOfClassProbabilities = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : listOfClassProbabilities.entrySet()) {
                Object theClass = entry2.getKey();
                Double probability = Dataset.toDouble(entry2.getValue());
                
                Double previousValue = combinedClassProbabilities.getDouble(theClass);
                if(previousValue==null) { 
                    previousValue=0.0;
                }
                
                combinedClassProbabilities.put(theClass, previousValue+probability);
            }
        }
        
        return combinedClassProbabilities;
    }
    
    public static AssociativeArray average(DataTable2D classifierClassProbabilityMatrix) {
        AssociativeArray combinedClassProbabilities = new AssociativeArray(); //new TreeMap<>(Collections.reverseOrder())
        
        int numberOfClasses = classifierClassProbabilityMatrix.size();
        
        for(Map.Entry<Object, AssociativeArray> entry : classifierClassProbabilityMatrix.entrySet()) {
            //Object classifier = entry.getKey();
            AssociativeArray listOfClassProbabilities = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : listOfClassProbabilities.entrySet()) {
                Object theClass = entry2.getKey();
                Double probability = Dataset.toDouble(entry2.getValue());
                
                Double previousValue = combinedClassProbabilities.getDouble(theClass);
                if(previousValue==null) {
                    previousValue=0.0;
                }
                
                combinedClassProbabilities.put(theClass, previousValue+probability/numberOfClasses);
            }
        }
        
        return combinedClassProbabilities;
    }
    
    public static AssociativeArray weightedAverage(DataTable2D classifierClassProbabilityMatrix, AssociativeArray classifierWeights) {
        AssociativeArray combinedClassProbabilities = new AssociativeArray(); //new TreeMap<>(Collections.reverseOrder())
        
        for(Map.Entry<Object, AssociativeArray> entry : classifierClassProbabilityMatrix.entrySet()) {
            Object classifier = entry.getKey();
            AssociativeArray listOfClassProbabilities = entry.getValue();
            Double classifierWeight = classifierWeights.getDouble(classifier);
            for(Map.Entry<Object, Object> entry2 : listOfClassProbabilities.entrySet()) {
                Object theClass = entry2.getKey();
                Double probability = Dataset.toDouble(entry2.getValue());
                
                Double previousValue = combinedClassProbabilities.getDouble(theClass);
                if(previousValue==null) {  
                    previousValue=0.0;
                }
                
                combinedClassProbabilities.put(theClass, previousValue+probability*classifierWeight);
            }
        }
        
        return combinedClassProbabilities;
    }
    
    public static AssociativeArray median(DataTable2D classifierClassProbabilityMatrix) {
        AssociativeArray combinedClassProbabilities = new AssociativeArray(); //new TreeMap<>(Collections.reverseOrder())
        
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
            listOfProbabilities=null;
        }
                
        return combinedClassProbabilities;
    }
    
    public static AssociativeArray maximum(DataTable2D classifierClassProbabilityMatrix) {
        AssociativeArray combinedClassProbabilities = new AssociativeArray(); //new TreeMap<>(Collections.reverseOrder())
        
        for(Map.Entry<Object, AssociativeArray> entry : classifierClassProbabilityMatrix.entrySet()) {
            //Object classifier = entry.getKey();
            AssociativeArray listOfClassProbabilities = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : listOfClassProbabilities.entrySet()) {
                Object theClass = entry2.getKey();
                Double probability = Dataset.toDouble(entry2.getValue());
                
                Double previousValue = combinedClassProbabilities.getDouble(theClass);
                if(previousValue==null || probability>previousValue) { 
                    combinedClassProbabilities.put(theClass, probability);
                }
                
            }
        }
        
        return combinedClassProbabilities;
    }    
    
    public static AssociativeArray minimum(DataTable2D classifierClassProbabilityMatrix) {
        AssociativeArray combinedClassProbabilities = new AssociativeArray(); //new TreeMap<>(Collections.reverseOrder())
        
        for(Map.Entry<Object, AssociativeArray> entry : classifierClassProbabilityMatrix.entrySet()) {
            //Object classifier = entry.getKey();
            AssociativeArray listOfClassProbabilities = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : listOfClassProbabilities.entrySet()) {
                Object theClass = entry2.getKey();
                Double probability = Dataset.toDouble(entry2.getValue());
                
                Double previousValue = combinedClassProbabilities.getDouble(theClass);
                if(previousValue==null || probability<previousValue) { 
                    combinedClassProbabilities.put(theClass, probability);
                }
                
            }
        }
        
        return combinedClassProbabilities;
    }    
    
    public static AssociativeArray product(DataTable2D classifierClassProbabilityMatrix) {
        AssociativeArray combinedClassProbabilities = new AssociativeArray(); //new TreeMap<>(Collections.reverseOrder())
        
        for(Map.Entry<Object, AssociativeArray> entry : classifierClassProbabilityMatrix.entrySet()) {
            //Object classifier = entry.getKey();
            AssociativeArray listOfClassProbabilities = entry.getValue();
            for(Map.Entry<Object, Object> entry2 : listOfClassProbabilities.entrySet()) {
                Object theClass = entry2.getKey();
                Double probability = Dataset.toDouble(entry2.getValue());
                
                Double previousValue = combinedClassProbabilities.getDouble(theClass);
                if(previousValue==null) { 
                    previousValue=1.0;
                }
                
                combinedClassProbabilities.put(theClass, previousValue*probability);
            }
        }
        
        return combinedClassProbabilities;
    }
    
    public static AssociativeArray majorityVote(DataTable2D classifierClassProbabilityMatrix) {
        AssociativeArray combinedClassProbabilities = new AssociativeArray(); //new TreeMap<>(Collections.reverseOrder())
        
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
            
            Map.Entry<Object, Object> selectedClassEntry = MapFunctions.selectMaxKeyValue(listOfClassProbabilities);
            
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
