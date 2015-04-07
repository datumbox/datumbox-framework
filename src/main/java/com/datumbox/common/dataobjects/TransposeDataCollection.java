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
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public final class TransposeDataCollection extends DataStructureMap<Map<Object, FlatDataCollection>> {
    
    public TransposeDataCollection() {
        internalData = new LinkedHashMap<>();
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
