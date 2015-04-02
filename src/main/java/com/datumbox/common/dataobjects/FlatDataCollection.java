/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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

import com.google.common.collect.HashMultiset;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author bbriniotis
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
