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

import java.util.Map;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <T>
 */
public abstract class DataStructureMap<T extends Map<?,?>> extends DataStructure<T> {
    
    public DataStructureMap() {
        
    }
    
    public DataStructureMap(T data) {
        super(data);
    }
    
    public final int size() {
        return internalData.size();
    }
    
    public final boolean containsKey(Object key) {
        return internalData.containsKey(key);
    }
    
    public final void clear() {
        internalData.clear();
    }
    
    public final boolean isEmpty() {
        return internalData.isEmpty();
    }
}
