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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Data structure which stores internally a Map<Object, TransposeDataCollection>. This
 * data structure is usually used when we want to store the values of a particular
 * variable separately per each category or group combination. The class provides a number of 
 * methods to access and modify the internal map.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class TransposeDataCollection2D extends AbstractDataStructureMap<Map<Object, TransposeDataCollection>> {
    private static final long serialVersionUID = 1L;
    
    /**
     * Default constructor which initializes the internal data with a LinkedHashMap.
     */
    public TransposeDataCollection2D() {
        super(new LinkedHashMap<>());
    }
    
    /**
     * Constructor that initializes the internal data with the provided map.
     * 
     * @param internalData 
     */
    public TransposeDataCollection2D(Map<Object, TransposeDataCollection> internalData) {
        super(internalData);
    }
    
    /**
     * Removes a particular key from the internal map and returns the value 
     * associated with that key if present in the map.
     * 
     * @param key
     * @return 
     */
    public final TransposeDataCollection remove(Object key) {
        return internalData.remove(key);
    }
    
    /**
     * Returns the value which is associated with the provided key.
     * 
     * @param key
     * @return 
     */
    public final TransposeDataCollection get(Object key) {
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
    public final TransposeDataCollection put(Object key, TransposeDataCollection value) {
        return internalData.put(key, value);
    }
    
    /**
     * Returns the entrySet of the internal map.
     * 
     * @return 
     */
    public final Set<Map.Entry<Object, TransposeDataCollection>> entrySet() {
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
    public final Collection<TransposeDataCollection> values() {
        return internalData.values();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( !(o instanceof TransposeDataCollection2D) ) return false;
        return internalData.equals(((TransposeDataCollection2D)o).internalData);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return internalData.hashCode();
    }
}
