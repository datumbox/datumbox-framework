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
import java.util.Iterator;

/**
 * Abstract class for every AbstractDataStructure which internally uses a Collection
 Object.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <T>
 */
public abstract class AbstractDataStructureCollection<T extends Collection<?>> extends AbstractDataStructure<T> {
    
    /**
     * Public constructor which takes as argument the appropriate Java collection.
     * 
     * @param data 
     */
    public AbstractDataStructureCollection(T data) {
        super(data);
    }
    
    /**
     * Returns the size of the collection.
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
     * Returns a copy of the internal data as an Array. Note that this method
     * copies the data, so its use must be avoided.
     * 
     * @return 
     */
    public final Object[] toArray() {
        return internalData.toArray();
    }
    
    /**
     * Checks whether the provided object is contained in the internal data.
     * 
     * @param o
     * @return 
     */
    public final boolean contains(Object o) {
        return internalData.contains(o);
    }
    
    /**
     * Iterator which casts the values of the Data Structure from Object to Double.
     * This iterator should be used only when the underling Data Structure contains
     * Numeric or Boolean values. Accessing this iterator when other data types
     * are stored will lead to an Exception.
     * 
     * @return 
     */
    public final Iterator<Double> iteratorDouble() {
        return new Iterator<Double>() {
            private final Iterator<Object> objectIterator = (Iterator<Object>) internalData.iterator();
            
            /** {@inheritDoc} */
            @Override
            public boolean hasNext() {
                return objectIterator.hasNext();
            }
            
            /** {@inheritDoc} */
            @Override
            public Double next() {
                return TypeInference.toDouble(objectIterator.next());
            }
            
            /** {@inheritDoc} */
            @Override
            public void remove() {
                objectIterator.remove();
            }
        };
    }
}
