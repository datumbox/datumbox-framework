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

import java.util.Map;

/**
 * Abstract class for every AbstractDataStructure which internally uses a Map
 Object.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <T>
 */
public abstract class AbstractDataStructureMap<T extends Map<?,?>> extends AbstractDataStructure<T> {
    
    /**
     * Public constructor which takes as argument the appropriate Java collection.
     * 
     * @param data 
     */
    public AbstractDataStructureMap(T data) {
        super(data);
    }
    
    /**
     * Returns the size of the map.
     * 
     * @return 
     */
    public final int size() {
        return internalData.size();
    }
    
    /**
     * Clears the internal data.
     */
    public final void clear() {
        internalData.clear();
    }
    
    /**
     * Checks if the internal data are empty.
     * 
     * @return 
     */
    public final boolean isEmpty() {
        return internalData.isEmpty();
    }
    
    /**
     * Checks if the provided key exists in the map.
     * 
     * @param key
     * @return 
     */
    public final boolean containsKey(Object key) {
        return internalData.containsKey(key);
    }
}
