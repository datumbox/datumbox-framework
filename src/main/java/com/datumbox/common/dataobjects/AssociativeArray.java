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
package com.datumbox.common.dataobjects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Data structure which stores internally a Map<Object, Object>.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public final class AssociativeArray extends DataStructureMap<Map<Object, Object>> {
    
    public AssociativeArray() {
        internalData = new LinkedHashMap<>();
    }
    
    public AssociativeArray(Map<Object, Object> internalData) {
        super(internalData);
    }
    
    public AssociativeArray(AssociativeArray aArray) {
        this();
        if(aArray != null) {
            internalData.putAll(aArray.internalData);
        }
    }
    
    public final void overwrite(Map<Object, Object> data) {
        internalData.clear();
        internalData.putAll(data);
    }
    
    public final void addValues(AssociativeArray array) {
        addRemoveValues(array, +1);
    }
    
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
    
    public final void multiplyValues(double multiplier) {
        for(Map.Entry<Object, Object> entry : internalData.entrySet()) {
            Double previousValue = TypeInference.toDouble(entry.getValue());
            if(previousValue==null) {
                continue;
            }
            internalData.put(entry.getKey(), previousValue*multiplier);
        } 
    }
    
    public final Object remove(Object key) {
        return internalData.remove(key);
    }
    
    public final Object get(Object key) {
        return internalData.get(key);
    }
    
    public final Double getDouble(Object key) {
        return TypeInference.toDouble(internalData.get(key));
    }
    
    public final Object put(Object key, Object value) {
        return internalData.put(key, value);
    }
    
    public void putAll(Map<? extends Object,? extends Object> m) {
        internalData.putAll(m);
    }
    
    public final Set<Map.Entry<Object, Object>> entrySet() {
        return internalData.entrySet();
    }
    
    public final Set<Object> keySet() {
        return internalData.keySet();
    }
    
    public final Collection<Object> values() {
        return internalData.values();
    }
    
    public FlatDataCollection toFlatDataCollection() {
        return new FlatDataCollection(internalData.values());
    }
    
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
    
    
    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( !(o instanceof AssociativeArray) ) return false;
        return internalData.equals(((AssociativeArray)o).internalData);
    }

    @Override
    public int hashCode() {
        return internalData.hashCode();
    }
    
    @Override
    public String toString() {
        return internalData.toString();
    }
}
