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

import com.datumbox.framework.common.interfaces.Copyable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Data structure which stores internally a Map<Object, Map<Object, Object>>. The 
 * class provides a number of methods to access and modify the internal map.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class AssociativeArray2D extends AbstractDataStructureMap<Map<Object, AssociativeArray>> implements Copyable<AssociativeArray2D> {
    private static final long serialVersionUID = 1L;
    
    /**
     * Default constructor which initializes the internal data with a LinkedHashMap.
     */
    public AssociativeArray2D() {
        super(new LinkedHashMap<>());
    }
    
    /**
     * Constructor that initializes the internal data with the provided map.
     * 
     * @param internalData 
     */
    public AssociativeArray2D(Map<Object, AssociativeArray> internalData) {
        super(internalData);
    }
    
    /** {@inheritDoc} */
    @Override
    public AssociativeArray2D copy() {
        AssociativeArray2D aArray2D = new AssociativeArray2D();
        for(Map.Entry<Object, AssociativeArray> entry: internalData.entrySet()) {
            aArray2D.internalData.put(entry.getKey(), entry.getValue().copy());
        }
        return aArray2D;
    }
    
    /**
     * Removes a particular key from the internal map and returns the value 
     * associated with that key if present in the map.
     * 
     * @param key
     * @return 
     */
    public final AssociativeArray remove(Object key) {
        return internalData.remove(key);
    }
    
    /**
     * Returns the value which is associated with the provided key.
     * 
     * @param key
     * @return 
     */
    public final AssociativeArray get(Object key) {
        return internalData.get(key);
    }
    
    /**
     * Adds a particular key-value into the internal map. It returns the previous
     * value which was associated with that key.
     * 
     * @param key
     * @param value
     * @return 
     */
    public final AssociativeArray put(Object key, AssociativeArray value) {
        return internalData.put(key, value);
    }
    
    /**
     * Returns the entrySet of the internal map.
     * 
     * @return 
     */
    public final Set<Map.Entry<Object, AssociativeArray>> entrySet() {
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
    public final Collection<AssociativeArray> values() {
        return internalData.values();
    }
    
    /**
     * Convenience function to get the value by using both keys.
     * 
     * @param key1
     * @param key2
     * @return 
     */
    public final Object get2d(Object key1, Object key2) {
        AssociativeArray tmp = internalData.get(key1);
        if(tmp == null) {
            return null;
        }
        
        return tmp.internalData.get(key2);
    }
    
    /**
     * Convenience function used to put a value in a particular key positions.
     * 
     * @param key1
     * @param key2
     * @param value
     * @return 
     */
    public final Object put2d(Object key1, Object key2, Object value) {
        AssociativeArray tmp = internalData.get(key1);
        if(tmp == null) {
            internalData.put(key1, new AssociativeArray());
        }
        
        return internalData.get(key1).internalData.put(key2, value);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if ( this == o ) return true;
        if ( !(o.getClass().equals(this.getClass())) ) return false;
        return internalData.equals(((AssociativeArray2D)o).internalData);
    }
    
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return internalData.hashCode();
    }
}
