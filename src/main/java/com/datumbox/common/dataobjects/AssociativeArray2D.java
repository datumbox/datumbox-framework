/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Data structure which stores internally a Map<Object, Map<Object, Object>>.
 * 
 * @author bbriniotis
 */
public class AssociativeArray2D extends DataStructureMap<Map<Object, AssociativeArray>> {
    
    public AssociativeArray2D() {
        internalData = new LinkedHashMap<>();
    }
    
    public AssociativeArray2D(Map<Object, AssociativeArray> internalData) {
        super(internalData);
    }
    
    public AssociativeArray2D copy() {
        AssociativeArray2D aArray2D = new AssociativeArray2D();
        for(Map.Entry<Object, AssociativeArray> entry: internalData.entrySet()) {
            aArray2D.internalData.put(entry.getKey(), new AssociativeArray(entry.getValue()));
        }
        return aArray2D;
    }
    
    public final AssociativeArray remove(Object key) {
        return internalData.remove(key);
    }
    
    public final AssociativeArray get(Object key) {
        return internalData.get(key);
    }
    
    public final AssociativeArray put(Object key, AssociativeArray value) {
        return internalData.put(key, value);
    }
    
    public final Set<Map.Entry<Object, AssociativeArray>> entrySet() {
        return internalData.entrySet();
    }
    
    public final Set<Object> keySet() {
        return internalData.keySet();
    }
    
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
    
    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( !(o instanceof AssociativeArray2D) ) return false;
        return internalData.equals(((AssociativeArray2D)o).internalData);
    }

    @Override
    public int hashCode() {
        return internalData.hashCode();
    }
}
