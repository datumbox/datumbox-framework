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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author bbriniotis
 */
public class AssociativeArray2D extends DataStructureMap<Map<Object, AssociativeArray>> {
    
    public AssociativeArray2D() {
        internalData = new HashMap<>();
    }
    
    public AssociativeArray2D(Map<Object, AssociativeArray> internalData) {
        super(internalData);
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
     * Convenience function used to put a value in a particular key positions; 
     * it should be used only when the internal AssociativeArray is declared
     * as HashMap.
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
