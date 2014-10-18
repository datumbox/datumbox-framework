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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author bbriniotis
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
        return Dataset.toDouble(internalData.get(index));
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