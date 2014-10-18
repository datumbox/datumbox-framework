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
import java.util.Iterator;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
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
                return Dataset.toDouble(objectIterator.next());
            }

            @Override
            public void remove() {
                objectIterator.remove();
            }
        };
    }
}
