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
package com.datumbox.framework.common.dataobjects;

import java.util.*;

/**
 * Data structure which stores internally a Map<Object, Object>. The class provides
 * a number of methods to access and modify the internal map.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class AssociativeArray extends AbstractDataStructureMap<Map<Object, Object>> {
    private static final long serialVersionUID = 1L;
    
    /**
     * Copies the provided AssociativeArray and builds a new which is unmodifiable.
     * 
     * @param original
     * @return 
     */
    public static AssociativeArray copy2Unmodifiable(AssociativeArray original) {
        Map<Object, Object> internalData = new LinkedHashMap<>();
        internalData.putAll(original.internalData);
        internalData = Collections.unmodifiableMap(internalData);
        return new AssociativeArray(internalData);
    }
    
    /**
     * Converts the internal data of the provided AssociativeArray to unmondifiable
     * but it does not copy its values. This means that if the original AssociativeArray
     * gets modified, the data of the new object will be modified too. This method 
     * is not as safe as copy2Unmodifiable() but it should be preferred when speed
     * is crucial.
     * 
     * @param original
     * @return 
     */
    public static AssociativeArray convert2Unmodifiable(AssociativeArray original) {
        return new AssociativeArray(Collections.unmodifiableMap(original.internalData));
    }
    
    /**
     * Default constructor which initializes the internal data with a LinkedHashMap.
     */
    public AssociativeArray() {
        super(new LinkedHashMap<>());
    }
    
    /**
     * Constructor that initializes the internal data with the provided map.
     * 
     * @param internalData 
     */
    public AssociativeArray(Map<Object, Object> internalData) {
        super(internalData);
    }
    
    /** {@inheritDoc} */
    public AssociativeArray copy() {
        AssociativeArray copy = new AssociativeArray();
        copy.internalData.putAll(this.internalData);
        return copy;
    }
    
    /**
     * Overwrites the contents of the internal data of this object with the ones
     * of the provided map.
     * 
     * @param data 
     */
    public final void overwrite(Map<Object, Object> data) {
        internalData.clear();
        internalData.putAll(data);
    }
    
    /**
     * Adds the provided associative array to the current object. All the columns
     * of this object should be numeric or boolean or else an exception is thrown.
     * 
     * @param array 
     */
    public final void addValues(AssociativeArray array) {
        addRemoveValues(array, +1);
    }
    
    /**
     * Subtracts the provided associative array to the current object. All the columns
     * of this object should be numeric or boolean or else an exception is thrown.
     * 
     * @param array 
     */
    public final void subtractValues(AssociativeArray array) {
        addRemoveValues(array, -1);
    }
    
    private void addRemoveValues(AssociativeArray array, int sign) {
        //assumes that the AssociativeArray stores only numerical fields, meaning
        //that an dataTransformation algorithm was run before calling the method
        
        //sign should be -1 or 1
        for(Map.Entry<Object, Object> entry : array.entrySet()) {
            Object column = entry.getKey();
            Double previousValue = TypeInference.toDouble(internalData.get(column));
            if(previousValue==null) {
                previousValue=0.0;
            }
            internalData.put(column, previousValue+ sign*TypeInference.toDouble(entry.getValue()));
        }
    }
    
    /**
     * Multiplies the values of the object with a particular multiplier. All the columns
     * of this object should be numeric or boolean or else an exception is thrown.
     * 
     * @param multiplier 
     */
    public final void multiplyValues(double multiplier) {
        for(Map.Entry<Object, Object> entry : internalData.entrySet()) {
            Double previousValue = TypeInference.toDouble(entry.getValue());
            if(previousValue==null) {
                continue;
            }
            internalData.put(entry.getKey(), previousValue*multiplier);
        } 
    }
    
    /**
     * Removes a particular key from the internal map and returns the value 
     * associated with that key if present in the map.
     * 
     * @param key
     * @return 
     */
    public final Object remove(Object key) {
        return internalData.remove(key);
    }
    
    /**
     * Returns the value which is associated with the provided key.
     * 
     * @param key
     * @return 
     */
    public final Object get(Object key) {
        return internalData.get(key);
    }
    
    /**
     * Gets value of the particular key from the map and converts it into a Double.
     * If not found null is returned. The value must be numeric or boolean or else
     * an exception is thrown.
     * 
     * @param key
     * @return 
     */
    public final Double getDouble(Object key) {
        return TypeInference.toDouble(internalData.get(key));
    }
    
    /**
     * Adds a particular key-value into the internal map. It returns the previous
     * value which was associated with that key.
     * 
     * @param key
     * @param value
     * @return 
     */
    public final Object put(Object key, Object value) {
        return internalData.put(key, value);
    }
    
    /**
     * Adds all the key-value combinations of the provided map in the internal map.
     * 
     * @param m 
     */
    public void putAll(Map<? extends Object,? extends Object> m) {
        internalData.putAll(m);
    }
    
    /**
     * Returns the entrySet of the internal map.
     * 
     * @return 
     */
    public final Set<Map.Entry<Object, Object>> entrySet() {
        return internalData.entrySet();
    }
    
    /**
     * Returns the keySet of the internal map.
     * 
     * @return 
     */
    public final Set<Object> keySet() {
        return internalData.keySet();
    }
    
    /**
     * Returns the values of the internal map.
     * 
     * @return 
     */
    public final Collection<Object> values() {
        return internalData.values();
    }
    
    /**
     * Returns a FlatDataCollection with the values of the internal map. The method
     * does not copy the data.
     * 
     * @return 
     */
    public FlatDataCollection toFlatDataCollection() {
        return new FlatDataCollection(internalData.values());
    }
    
    /**
     * Returns a FlatDataList with the values of the internal map. The method
     * might require to copy the data.
     * 
     * @return 
     */
    @SuppressWarnings("unchecked")
    public FlatDataList toFlatDataList() {
        Collection<Object> values = internalData.values();
        List<Object> list;
        if (values instanceof List<?>) {
            list = (List<Object>)values;
        }
        else {
            list = new ArrayList(values);
        }
        return new FlatDataList(list);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( !(o instanceof AssociativeArray) ) return false;
        return internalData.equals(((AssociativeArray)o).internalData);
    }
    
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return internalData.hashCode();
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return internalData.toString();
    }
}
