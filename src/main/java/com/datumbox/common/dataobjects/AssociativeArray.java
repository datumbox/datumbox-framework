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
package com.datumbox.common.dataobjects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author bbriniotis
 */
public final class AssociativeArray extends DataStructureMap<Map<Object, Object>> {
    
    public AssociativeArray() {
        internalData = new HashMap<>();
    }
    
    public AssociativeArray(Map<Object, Object> internalData) {
        super(internalData);
    }
    
    public final void addValues(AssociativeArray array) {
        addRemoveValues(array, +1);
    }
    
    public final void removeValues(AssociativeArray array) {
        addRemoveValues(array, -1);
    }
    
    private void addRemoveValues(AssociativeArray array, int sign) {
        //assumes that the AssociativeArray stores only numerical fields, meaning
        //that an dataTransformation algorithm was run before calling the method
        
        //sign should be -1 or 1
        for(Map.Entry<Object, Object> entry : array.entrySet()) {
            Object column = entry.getKey();
            Double previousValue = Dataset.toDouble(internalData.get(column));
            if(previousValue==null) {
                previousValue=0.0;
            }
            internalData.put(column, previousValue+ sign*Dataset.toDouble(entry.getValue()));
        }
    }
    
    public final void multiplyValues(double multiplier) {
        for(Map.Entry<Object, Object> entry : internalData.entrySet()) {
            Double previousValue = Dataset.toDouble(entry.getValue());
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
        return Dataset.toDouble(internalData.get(key));
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
