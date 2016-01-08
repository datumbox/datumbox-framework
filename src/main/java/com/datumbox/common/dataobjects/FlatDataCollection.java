/**
 * Copyright (C) 2013-2016 Vasilis Vryniotis <bbriniotis@datumbox.com>
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

import java.lang.reflect.Array;
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
public class FlatDataCollection extends AbstractDataStructureCollection<Collection<Object>> implements Iterable<Object> {
    private static final long serialVersionUID = 1L;
    
    /**
     * Converts to Object[] the original FlatDataCollection. The method is used to
     * generate a copy of the flatDataCollection and it is called in order to
     * avoid modifying the original array.
     *
     * @param <T>
     * @param c
     * @return
     */
    public final <T> T[] copyCollection2Array(Class<T> c) {
        int n = internalData.size();
        T[] copy = (T[]) Array.newInstance(c, n);
        int i = 0;
        for (Object value : internalData) {
            copy[i++] = c.cast(value);
        }
        return copy;
    }

    /**
     * Converts to Double[] safely the original FlatDataCollection by using the
     * iteratorDouble.
     *
     * @return
     */
    public final Double[] copyCollection2DoubleArray() {
        int n = internalData.size();
        Double[] doubleArray = new Double[n];
        int i = 0;
        Iterator<Double> it = this.iteratorDouble();
        while (it.hasNext()) {
            doubleArray[i++] = it.next();
        }
        return doubleArray;
    }
    
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
    
    /**
     * Adds all the objects of the provided collection to the internal data. It 
     * returns a boolean which indicates whether the collection changed as a 
     * result of the call.
     * 
     * @param c
     * @return 
     */
    public final boolean addAll(Collection<Object> c) {
        return internalData.addAll(c);
    }
    
    /** {@inheritDoc} */
    @Override
    public final Iterator<Object> iterator() {
        return internalData.iterator();
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
