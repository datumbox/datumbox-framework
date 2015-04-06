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
 * @author bbriniotis
 */
public final class TransposeDataList extends DataStructureMap<Map<Object, FlatDataList>> {
    
    public TransposeDataList() {
        internalData = new LinkedHashMap<>();
    }
    
    public TransposeDataList(Map<Object, FlatDataList> internalData) {
        super(internalData);
    }
    
    public final FlatDataList remove(Object key) {
        return internalData.remove(key);
    }
    
    public final FlatDataList get(Object key) {
        return internalData.get(key);
    }
    
    public final FlatDataList put(Object key, FlatDataList value) {
        return internalData.put(key, value);
    }
    
    public final Set<Map.Entry<Object, FlatDataList>> entrySet() {
        return internalData.entrySet();
    }
    
    public final Set<Object> keySet() {
        return internalData.keySet();
    }
    
    public final Collection<FlatDataList> values() {
        return internalData.values();
    }
    
    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( !(o instanceof TransposeDataList) ) return false;
        return internalData.equals(((TransposeDataList)o).internalData);
    }

    @Override
    public int hashCode() {
        return internalData.hashCode();
    }
}