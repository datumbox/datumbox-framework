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

import com.google.common.collect.HashMultiset;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public final class FlatDataCollection extends DataStructureCollection<Collection<Object>> implements Iterable<Object> {

    /**
     * Converts to Object[] the original FlatDataCollection. The method is used to
     * generate a deep copy of the flatDataCollection and it is called in order to
     * avoid modifying the original array.
     *
     * @param <T>
     * @param c
     * @return
     * @throws IllegalArgumentException
     */
    public <T> T[] copyCollection2Array(Class<T> c) throws IllegalArgumentException {
        int n = internalData.size();
        if (n == 0) {
            throw new IllegalArgumentException();
        }
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
    public Double[] copyCollection2DoubleArray() {
        int n = internalData.size();
        Double[] doubleArray = new Double[n];
        int i = 0;
        Iterator<Double> it = this.iteratorDouble();
        while (it.hasNext()) {
            doubleArray[i++] = it.next();
        }
        return doubleArray;
    }
    
    public FlatDataCollection() throws IllegalArgumentException {
        throw new IllegalArgumentException();
    }
    
    public FlatDataCollection(Collection<Object> internalData) {
        super(internalData);
    }
    
    public final boolean remove(Object o) {
        return internalData.remove(o);
    }
    
    public final boolean add(Object e) {
        return internalData.add(e);
    }
    
    public final boolean addAll(Collection<Object> c) {
        return internalData.addAll(c);
    }
    
    @Override
    public final Iterator<Object> iterator() {
        return internalData.iterator();
    }
    
    @SuppressWarnings("unchecked")
    public FlatDataList toFlatDataList() {
        List<Object> list;
        if (internalData instanceof List<?>) {
            list = (List<Object>)internalData;
        }
        else {
            list = new ArrayList(internalData);
        }
        return new FlatDataList(list);
    }
    
    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( !(o instanceof FlatDataCollection) ) return false;
        
        if (internalData.size() != ((FlatDataCollection)o).internalData.size()) {
            return false;
        }
        
        //we should not care about the order of the elements in the collections. We care about duplicates, that's why we use HashMultisets
        return HashMultiset.create(internalData).equals(HashMultiset.create(((FlatDataCollection)o).internalData));
    }

    @Override
    public int hashCode() {
        /*
        int hash = 5;
        for( Object value : internalData ) {
            hash = hash * 31 + ((value == null) ? 0 : value.hashCode());
        }
        return hash;
        */
        return internalData.hashCode();
    }
    
    
}
