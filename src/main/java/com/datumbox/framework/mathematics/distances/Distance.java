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
package com.datumbox.framework.mathematics.distances;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.utilities.TypeConversions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Distance {
    
    public static double euclidean(AssociativeArray a1, AssociativeArray a2) {
        Map<Object, Double> columnDistances = columnDistances(a1, a2, null);
        
        double distance = 0.0;
        for(double columnDistance : columnDistances.values()) {
            distance+=(columnDistance*columnDistance);
        }
        
        return Math.sqrt(distance);
    }
    
    public static double euclideanWeighhted(AssociativeArray a1, AssociativeArray a2, Map<Object, Double> columnWeights) {
        Map<Object, Double> columnDistances = columnDistances(a1, a2, columnWeights.keySet());
        
        double distance = 0.0;
        for(Map.Entry<Object, Double> entry : columnDistances.entrySet()) {
            double columnDistance = entry.getValue();
            distance+=(columnDistance*columnDistance)*columnWeights.get(entry.getKey());
        }
        return Math.sqrt(distance);
    }
    
    public static double manhattan(AssociativeArray a1, AssociativeArray a2) {
        Map<Object, Double> columnDistances = columnDistances(a1, a2, null);
        
        double distance = 0.0;
        for(double columnDistance : columnDistances.values()) {
            distance+=Math.abs(columnDistance);
        }
        
        return distance;
    }
    
    public static double manhattanWeighhted(AssociativeArray a1, AssociativeArray a2, Map<Object, Double> columnWeights) {
        Map<Object, Double> columnDistances = columnDistances(a1, a2, columnWeights.keySet());
        
        double distance = 0.0;
        for(Map.Entry<Object, Double> entry : columnDistances.entrySet()) {
            distance+=Math.abs(entry.getValue())*columnWeights.get(entry.getKey());
        }
        
        return distance;
    }
    
    public static double maximum(AssociativeArray a1, AssociativeArray a2) {
        Map<Object, Double> columnDistances = columnDistances(a1, a2, null);
        
        double distance=0.0;
        for(double columnDistance : columnDistances.values()) {
            columnDistance = Math.abs(columnDistance);
            if(distance<columnDistance) {
                distance = columnDistance;
            }
        }
        
        return distance;
    }
    
    protected static Map<Object, Double> columnDistances(AssociativeArray a1, AssociativeArray a2, Set<Object> comparingColumns) {
        if(comparingColumns==null) {
            //if the list of comparing columns is not set, build it from the data
            comparingColumns = new HashSet<>(a1.keySet());
            comparingColumns.addAll(a2.keySet());
        }
        
        Map<Object, Double> columnDistances = new HashMap<>(); 
        
        for(Object column : comparingColumns) {
            Object v1 = a1.get(column);
            Object v2 = a2.get(column);
            
            if(v1==null || v2==null) {
                if(v1==v2) {
                    columnDistances.put(column, 0.0);
                }
                else {
                    Object nonNullObject = (v1!=null)?v1:v2;
                    
                    Dataset.ColumnType type = Dataset.value2ColumnType(nonNullObject);
                    if(type!=Dataset.ColumnType.NUMERICAL && type!=Dataset.ColumnType.DUMMYVAR) {
                        //max distance is set to 1.0
                        columnDistances.put(column, 1.0);
                    }
                    else {
                        //set the value of the non-null object
                        columnDistances.put(column, TypeConversions.toDouble(nonNullObject));
                    }
                } 
            }
            else if(v1.equals(v2)) { //this handles non numeric values too
                columnDistances.put(column, 0.0);
            }
            else {
                //The type check is performed as a safe-check in case the dataset
                //is not passed through a DataTransfomer to convert data to numeric
                //types. If they are already converted then we will correctly
                //use only their numeric values.
                Dataset.ColumnType type = Dataset.value2ColumnType(v1);
                
                if(type!=Dataset.ColumnType.NUMERICAL && type!=Dataset.ColumnType.DUMMYVAR) {
                    //if the type is not numerical we set as maximym distance the 1
                    //we are certain that those two are not equal due to the first if.
                    //it does not matter if it is boolean, ordinal or categorical
                    //since they don't match their distance is 1.0
                    columnDistances.put(column, 1.0);
                }
                else {
                    //the values are numbers
                    columnDistances.put(column, TypeConversions.toDouble(v1)-TypeConversions.toDouble(v2));
                }
            }
        }
        
        return columnDistances;
    }
}
