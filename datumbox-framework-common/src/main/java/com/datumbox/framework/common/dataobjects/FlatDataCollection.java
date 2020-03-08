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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * The FlatDataCollection is a data structure that stores internally a {@literal Collection<Object>}. 
 * The actual implementation of the collection can be Sets, Lists etc and
 * they must store Objects internally. The class provides a set of useful methods 
 * to access and modify the contents of the collection.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class FlatDataCollection extends AbstractDataStructureCollection<Collection<Object>> implements Collection<Object> {
    private static final long serialVersionUID = 1L;
    
    /**
     * Public constructor which accepts as argument a Collection of Objects.
     * 
     * @param internalData 
     */
    public FlatDataCollection(Collection<Object> internalData) {
        super(internalData);
    }
    
    /**
     * Removes a particular object from the internal data. It returns a boolean
     * which indicates whether the object was found the collection.
     * 
     * @param o
     * @return 
     */
    public final boolean remove(Object o) {
        return internalData.remove(o);
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsAll(Collection<?> c) {
        return internalData.containsAll(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean addAll(Collection<?> c) {
        return internalData.addAll(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeAll(Collection<?> c) {
        return internalData.removeAll(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean retainAll(Collection<?> c) {
        return internalData.retainAll(c);
    }

    /**
     * It adds an object in the collection. It returns a boolean which indicates 
     * whether the collection changed as a result of the call.
     * 
     * @param e
     * @return 
     */
    public final boolean add(Object e) {
        return internalData.add(e);
    }
    
    /** {@inheritDoc} */
    @Override
    public final Iterator<Object> iterator() {
        return internalData.iterator();
    }

    /** {@inheritDoc} */
    @Override
    public <T> T[] toArray(T[] a) {
        return internalData.toArray(a);
    }

    /**
     * Converts the FlatDataCollection to a FlatDataList trying (if possible)
     * not to copy the data.
     * 
     * @return 
     */
    @SuppressWarnings("unchecked")
    public final FlatDataList toFlatDataList() {
        List<Object> list;
        if (internalData instanceof List<?>) {
            list = (List<Object>)internalData;
        }
        else {
            list = new ArrayList(internalData);
        }
        return new FlatDataList(list);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( !(o instanceof FlatDataCollection) ) return false;
        return internalData.equals(((FlatDataCollection)o).internalData);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return internalData.hashCode();
    }
    
    
}
