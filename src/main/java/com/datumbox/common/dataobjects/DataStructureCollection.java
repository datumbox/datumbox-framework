/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
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

import com.datumbox.common.utilities.TypeInference;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <T>
 */
public abstract class DataStructureCollection<T extends Collection<?>> extends DataStructure<T> {
    
    public DataStructureCollection() {
        
    }
    
    public DataStructureCollection(T data) {
        super(data);
    }
    
    public final int size() {
        return internalData.size();
    }
    
    public final void clear() {
        internalData.clear();
    }
    
    public final boolean isEmpty() {
        return internalData.isEmpty();
    }
    
    public final Object[] toArray() {
        return internalData.toArray();
    }
    
    public final boolean contains(Object o) {
        return internalData.contains(o);
    }
    
    public final Iterator<Double> iteratorDouble() {
        return new Iterator<Double>() {
            private final Iterator<Object> objectIterator = (Iterator<Object>) internalData.iterator();
            @Override
            public boolean hasNext() {
                return objectIterator.hasNext();
            }

            @Override
            public Double next() {
                return TypeInference.toDouble(objectIterator.next());
            }

            @Override
            public void remove() {
                objectIterator.remove();
            }
        };
    }
}
