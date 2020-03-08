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
package com.datumbox.framework.core.mathematics.distances;

import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.common.dataobjects.TypeInference;

import java.util.*;

/**
 * The Distance class provides methods to estimate various types of distances
 * between Associative Arrays.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Distance {
    
    /**
     * Estimates the euclidean distance of two Associative Arrays.
     * 
     * @param a1
     * @param a2
     * @return 
     */
    public static double euclidean(AssociativeArray a1, AssociativeArray a2) {
        Map<Object, Double> columnDistances = columnDistances(a1, a2, null);
        
        double distance = 0.0;
        for(double columnDistance : columnDistances.values()) {
            distance+=(columnDistance*columnDistance);
        }
        
        return Math.sqrt(distance);
    }
    
    /**
     * Estimates the weighted euclidean distance of two Associative Arrays.
     * 
     * @param a1
     * @param a2
     * @param columnWeights
     * @return 
     */
    public static double euclideanWeighted(AssociativeArray a1, AssociativeArray a2, Map<Object, Double> columnWeights) {
        Map<Object, Double> columnDistances = columnDistances(a1, a2, columnWeights.keySet());
        
        double distance = 0.0;
        for(Map.Entry<Object, Double> entry : columnDistances.entrySet()) {
            double columnDistance = entry.getValue();
            distance+=(columnDistance*columnDistance)*columnWeights.get(entry.getKey());
        }
        return Math.sqrt(distance);
    }
    
    /**
     * Estimates the manhattan distance of two Associative Arrays.
     * 
     * @param a1
     * @param a2
     * @return 
     */
    public static double manhattan(AssociativeArray a1, AssociativeArray a2) {
        Map<Object, Double> columnDistances = columnDistances(a1, a2, null);
        
        double distance = 0.0;
        for(double columnDistance : columnDistances.values()) {
            distance+=Math.abs(columnDistance);
        }
        
        return distance;
    }
    
    /**
     * Estimates the weighted manhattan distance of two Associative Arrays.
     * 
     * @param a1
     * @param a2
     * @param columnWeights
     * @return 
     */
    public static double manhattanWeighted(AssociativeArray a1, AssociativeArray a2, Map<Object, Double> columnWeights) {
        Map<Object, Double> columnDistances = columnDistances(a1, a2, columnWeights.keySet());
        
        double distance = 0.0;
        for(Map.Entry<Object, Double> entry : columnDistances.entrySet()) {
            distance+=Math.abs(entry.getValue())*columnWeights.get(entry.getKey());
        }
        
        return distance;
    }
    
    /**
     * Estimates the maximum distance of two Associative Arrays.
     * 
     * @param a1
     * @param a2
     * @return 
     */
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
    
    private static Map<Object, Double> columnDistances(AssociativeArray a1, AssociativeArray a2, Set<Object> comparingColumns) {
        if(comparingColumns==null) {
            //if the list of comparing columns is not set, build it from the data
            comparingColumns = new HashSet<>(a1.keySet());
            comparingColumns.addAll(a2.keySet());
        }
        
        Map<Object, Double> columnDistances = new HashMap<>(); 
        
        for(Object column : comparingColumns) {
            Object v1 = a1.get(column);
            Object v2 = a2.get(column);
            
            if(Objects.equals(v1,v2)) { //equal or both null
                columnDistances.put(column, 0.0);
            }
            else if(v1==null || v2==null) { //one of them is null
                Object nonNullObject = (v1!=null)?v1:v2;

                TypeInference.DataType type = TypeInference.getDataType(nonNullObject);
                if(type==TypeInference.DataType.NUMERICAL || type==TypeInference.DataType.BOOLEAN) {
                    //if numeric then set its value. if boolean then set equal to
                    //1.0 only if the boolean feature is true.
                    columnDistances.put(column, TypeInference.toDouble(nonNullObject));
                }
                else {
                    //max distance is set to 1.0
                    columnDistances.put(column, 1.0);
                }
            }
            else { //none of them is null and they are not equal
                TypeInference.DataType type = TypeInference.getDataType(v1);
                
                if(type==TypeInference.DataType.NUMERICAL || type==TypeInference.DataType.BOOLEAN) {
                    //if numerics then subtract their values. if booleans do the same. Sometimes we
                    //deal with mixed data (in clustering: centroids vs points) and thus in this case 
                    //numeric estimation is required to estimate the distance between a probability and 
                    //a boolean value.
                    columnDistances.put(column, TypeInference.toDouble(v1)-TypeInference.toDouble(v2));
                }
                else {
                    //if the type is not numerical we set as maximum distance the 1
                    //we are certain that those two are not equal due to the first if.
                    //it does not matter if it is boolean, ordinal or categorical
                    //since they don't match their distance is 1.0
                    columnDistances.put(column, 1.0);
                }
            }
        }
        
        return columnDistances;
    }
}
