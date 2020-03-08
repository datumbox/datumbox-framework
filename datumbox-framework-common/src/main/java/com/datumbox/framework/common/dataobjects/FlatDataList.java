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

import com.datumbox.framework.common.interfaces.Copyable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * The FlatDataList is a data structure that stores internally a {@literal List<Object>}. 
 * The class provides a set of useful methods to access and modify the contents 
 * of the collection.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class FlatDataList extends AbstractDataStructureList<List<Object>> implements Iterable<Object>, Copyable<FlatDataList> {
    private static final long serialVersionUID = 1L;
    
    /**
     * Default constructor which initializes the internal data with an ArrayList.
     */
    public FlatDataList() {
        super(new ArrayList<>());
    }
    
    /**
     * Constructor that initializes the internal data with the provided list.
     * 
     * @param internalData 
     */
    public FlatDataList(List<Object> internalData) {
        super(internalData);
    }
    
    /** {@inheritDoc} */
    @Override
    public FlatDataList copy() {
        FlatDataList copy = new FlatDataList();
        copy.internalData.addAll(this.internalData);
        return copy;
    }
    
    /**
     * Removes an element at the specified position in the internal list and returns it.
     * 
     * @param index
     * @return 
     */
    public final Object remove(int index) {
        return internalData.remove(index);
    }
    
    /**
     * Gets an element at the specified position in the internal list.
     * 
     * @param index
     * @return 
     */
    public final Object get(int index) {
        return internalData.get(index);
    }
    
    /**
     * Gets an element at the specified position in this list, converts it into
     * Double and returns its value. The value must be numeric or boolean or else
     * an exception is thrown.
     * 
     * @param index
     * @return 
     */
    public final Double getDouble(int index) {
        return TypeInference.toDouble(internalData.get(index));
    }
    
    /**
     * Appends the specified element to the end of the internal list.
     * 
     * @param e
     * @return 
     */
    public final boolean add(Object e) {
        return internalData.add(e);
    }
    
    /**
     * Replaces the element at the specified position in the internal list with the
     * specified element and returns the previous value.
     * 
     * @param index
     * @param element
     * @return 
     */
    public final Object set(int index, Object element) {
        return internalData.set(index, element);
    }
    
    /**
     * Adds all the elements of the provided collection in the internal list.
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
     * Returns a FlatDataCollection with the values of the internal list without
     * coping the data.
     * 
     * @return 
     */
    public final FlatDataCollection toFlatDataCollection() {
        return new FlatDataCollection(internalData);
    }
    
    /*
    public final boolean remove(Object o) {
        return internalData.remove(o);
    }
    
    public final void add(int index, Object element) {
        internalData.add(index, element);
    }
    
    public final boolean addAll(int index, Collection<Object> c) {
        return internalData.addAll(index, c);
    }
    */
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( !(o instanceof FlatDataList) ) return false;        
        return internalData.equals( ((FlatDataList)o).internalData );
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return internalData.hashCode();
    }
}