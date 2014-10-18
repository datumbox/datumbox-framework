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
package com.datumbox.common.utilities;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author bbriniotis
 */
public class MapFunctions {
    
     /**
     * Selects the key-value entry with the largest value.
     * 
     * @param keyValueMap
     * @return 
     */
    public static Map.Entry<Object, Object> selectMaxKeyValue(AssociativeArray keyValueMap) {
        Double maxValue=Double.NEGATIVE_INFINITY;
        Object maxValueKey = null;
        
        for(Map.Entry<Object, Object> entry : keyValueMap.entrySet()) {
            Double value = Dataset.toDouble(entry.getValue());
            if(value!=null && value>maxValue) {
                maxValue=value;
                maxValueKey=entry.getKey();
            }
        }
        
        return new AbstractMap.SimpleEntry<>(maxValueKey, keyValueMap.get(maxValueKey));
    }
    
    public static Map.Entry<Object, Double> selectMaxKeyValue(Map<Object, Double> keyValueMap) {
        Double maxValue=Double.NEGATIVE_INFINITY;
        Object maxValueKey = null;
        
        for(Map.Entry<Object, Double> entry : keyValueMap.entrySet()) {
            Double value = entry.getValue();
            if(value!=null && value>maxValue) {
                maxValue=value;
                maxValueKey=entry.getKey();
            }
        }
        
        return new AbstractMap.SimpleEntry<>(maxValueKey, keyValueMap.get(maxValueKey));
    }
    
     /**
     * Selects the key-value entry with the smallest value.
     * 
     * @param keyValueMap
     * @return 
     */
    public static Map.Entry<Object, Object> selectMinKeyValue(AssociativeArray keyValueMap) {
        Double minValue=Double.POSITIVE_INFINITY;
        Object minValueKey = null;
        
        for(Map.Entry<Object, Object> entry : keyValueMap.entrySet()) {
            Double value = Dataset.toDouble(entry.getValue());
            if(value!=null && value<minValue) {
                minValue=value;
                minValueKey=entry.getKey();
            }
        }
        
        return new AbstractMap.SimpleEntry<>(minValueKey, keyValueMap.get(minValueKey));
    }
    
    public static Map.Entry<Object, Double> selectMinKeyValue(Map<Object, Double> keyValueMap) {
        Double minValue=Double.POSITIVE_INFINITY;
        Object minValueKey = null;
        
        for(Map.Entry<Object, Double> entry : keyValueMap.entrySet()) {
            Double value = entry.getValue();
            if(value!=null && value<minValue) {
                minValue=value;
                minValueKey=entry.getKey();
            }
        }
        
        return new AbstractMap.SimpleEntry<>(minValueKey, keyValueMap.get(minValueKey));
    }
    
    /**
     * Sorts by Key a Map in ascending order. 
     * 
     * @param <K>
     * @param <V>
     * @param map
     * @return 
     */
    public static <K, V> Map<K, V> sortNumberMapByKeyAscending(Map<K, V> map) {
        ArrayList<Map.Entry<K, V>> entries = new ArrayList<>(map.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
          @Override
          public int compare(Map.Entry<K, V> a, Map.Entry<K, V> b){
              Double va = Dataset.toDouble(a.getKey());
              Double vb = Dataset.toDouble(b.getKey());
              return va.compareTo(vb);
          }
        });
        
        Map<K, V> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : entries) {
          sortedMap.put(entry.getKey(), entry.getValue());
        }
        
        return sortedMap;
    }
    
    /**
     * Sorts by Key a Map in descending order. 
     * 
     * @param <K>
     * @param <V>
     * @param map
     * @return 
     */
    public static <K, V> Map<K, V> sortNumberMapByKeyDescending(Map<K, V> map) {
        ArrayList<Map.Entry<K, V>> entries = new ArrayList<>(map.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
          @Override
          public int compare(Map.Entry<K, V> a, Map.Entry<K, V> b){
              Double va = Dataset.toDouble(a.getKey());
              Double vb = Dataset.toDouble(b.getKey());
              return -va.compareTo(vb);
          }
        });
        
        Map<K, V> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : entries) {
          sortedMap.put(entry.getKey(), entry.getValue());
        }
        
        return sortedMap;
    }
    
    /**
     * Sorts by Value a Map in ascending order. 
     * 
     * @param <K>
     * @param <V>
     * @param map
     * @return 
     */
    public static <K, V> Map<K, V> sortNumberMapByValueAscending(Map<K, V> map) {
        ArrayList<Map.Entry<K, V>> entries = new ArrayList<>(map.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
          @Override
          public int compare(Map.Entry<K, V> a, Map.Entry<K, V> b){
              Double va = Dataset.toDouble(a.getValue());
              Double vb = Dataset.toDouble(b.getValue());
              return va.compareTo(vb);
          }
        });
        
        Map<K, V> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : entries) {
          sortedMap.put(entry.getKey(), entry.getValue());
        }
        
        return sortedMap;
    }
    
    /**
     * Sorts by Value a Map in descending order. 
     * 
     * @param <K>
     * @param <V>
     * @param map
     * @return 
     */
    public static <K, V> Map<K, V> sortNumberMapByValueDescending(Map<K, V> map) {
        ArrayList<Map.Entry<K, V>> entries = new ArrayList<>(map.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
          @Override
          public int compare(Map.Entry<K, V> a, Map.Entry<K, V> b){
              Double va = Dataset.toDouble(a.getValue());
              Double vb = Dataset.toDouble(b.getValue());
              return -va.compareTo(vb);
          }
        });
        
        Map<K, V> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : entries) {
          sortedMap.put(entry.getKey(), entry.getValue());
        }
        
        return sortedMap;
    }
    
    /**
     * Sorts by Value a Associative Array in ascending order. 
     * 
     * @param associativeArray
     * @return 
     */
    public static AssociativeArray sortAssociativeArrayByValueAscending(AssociativeArray associativeArray) {
        ArrayList<Map.Entry<Object, Object>> entries = new ArrayList<>(associativeArray.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<Object,Object>>() {
          @Override
          public int compare(Map.Entry<Object, Object> a, Map.Entry<Object, Object> b){
              Double va = Dataset.toDouble(a.getValue());
              Double vb = Dataset.toDouble(b.getValue());
              return va.compareTo(vb);
          }
        });
        
        AssociativeArray sortedAssociativeArray = new AssociativeArray(new LinkedHashMap<>());
        for (Map.Entry<Object, Object> entry : entries) {
          sortedAssociativeArray.put(entry.getKey(), entry.getValue());
        }
        
        return sortedAssociativeArray;
    }
    
    /**
     * Sorts by Value a Associative Array in descending order. 
     * 
     * @param associativeArray
     * @return 
     */
    public static AssociativeArray sortAssociativeArrayByValueDescending(AssociativeArray associativeArray) {
        ArrayList<Map.Entry<Object, Object>> entries = new ArrayList<>(associativeArray.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<Object,Object>>() {
          @Override
          public int compare(Map.Entry<Object, Object> a, Map.Entry<Object, Object> b){
              Double va = Dataset.toDouble(a.getValue());
              Double vb = Dataset.toDouble(b.getValue());
              return -va.compareTo(vb);
          }
        });
        
        AssociativeArray sortedAssociativeArray = new AssociativeArray(new LinkedHashMap<>());
        for (Map.Entry<Object, Object> entry : entries) {
          sortedAssociativeArray.put(entry.getKey(), entry.getValue());
        }
        
        return sortedAssociativeArray;
    }
}
