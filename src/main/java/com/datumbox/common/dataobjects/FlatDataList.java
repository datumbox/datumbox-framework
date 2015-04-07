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

import com.datumbox.common.utilities.TypeConversions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public final class FlatDataList extends DataStructureList<List<Object>> implements Iterable<Object> {
    
    public FlatDataList() {
        internalData = new ArrayList<>();
    }
    
    public FlatDataList(List<Object> internalData) {
        super(internalData);
    }
    /*
    public final boolean remove(Object o) {
        return internalData.remove(o);
    }
    */
    public final Object remove(int index) {
        return internalData.remove(index);
    }
    
    public final Object get(int index) {
        return internalData.get(index);
    }
    
    public final Double getDouble(int index) {
        return TypeConversions.toDouble(internalData.get(index));
    }
    
    public final boolean add(Object e) {
        return internalData.add(e);
    }
    
    public final Object set(int index, Object element) {
        return internalData.set(index, element);
    }
    
    /*
    public final void add(int index, Object element) {
        internalData.add(index, element);
    }
    */
    
    public final boolean addAll(Collection<Object> c) {
        return internalData.addAll(c);
    }
    /*
    public final boolean addAll(int index, Collection<Object> c) {
        return internalData.addAll(index, c);
    }
    */
    
    @Override
    public final Iterator<Object> iterator() {
        return internalData.iterator();
    }
    
    public final FlatDataCollection toFlatDataCollection() {
        return new FlatDataCollection(internalData);
    }
    
    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( !(o instanceof FlatDataList) ) return false;        
        return internalData.equals( ((FlatDataList)o).internalData );
    }

    @Override
    public int hashCode() {
        return internalData.hashCode();
    }
}