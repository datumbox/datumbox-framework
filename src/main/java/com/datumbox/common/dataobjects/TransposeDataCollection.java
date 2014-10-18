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
public final class TransposeDataCollection extends DataStructureMap<Map<Object, FlatDataCollection>> {
    
    public TransposeDataCollection() {
        internalData = new HashMap<>();
    }
    
    public TransposeDataCollection(Map<Object, FlatDataCollection> internalData) {
        super(internalData);
    }
    
    public final FlatDataCollection remove(Object key) {
        return internalData.remove(key);
    }
    
    public final FlatDataCollection get(Object key) {
        return internalData.get(key);
    }
    
    public final FlatDataCollection put(Object key, FlatDataCollection value) {
        return internalData.put(key, value);
    }
    
    public final Set<Map.Entry<Object, FlatDataCollection>> entrySet() {
        return internalData.entrySet();
    }
    
    public final Set<Object> keySet() {
        return internalData.keySet();
    }
    
    public final Collection<FlatDataCollection> values() {
        return internalData.values();
    }
    
    
    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( !(o instanceof TransposeDataCollection) ) return false;
        /*
        TransposeDataCollection otherObject = ((TransposeDataCollection)o);
        
        if(internalData.size() != otherObject.internalData.size()) {
            return false;
        }
        
        //first ensure that the keys are the same. IMPORTANT: we ignore order since we use sets here
        if(internalData.keySet().equals(otherObject.internalData.keySet())==false) {
            return false;
        }
        
        //now compare the FlatDataCollections if they are equal
        for(Object key : internalData.keySet()) {            
            if(internalData.get(key).equals(otherObject.internalData.get(key))==false) {
                return false;
            }
        }
        return true;
        */
        return internalData.equals(((TransposeDataCollection)o).internalData);
    }

    @Override
    public int hashCode() {
        return internalData.hashCode();
    }
}
