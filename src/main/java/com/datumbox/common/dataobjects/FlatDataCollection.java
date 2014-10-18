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

import com.google.common.collect.HashMultiset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author bbriniotis
 */
public final class FlatDataCollection extends DataStructureCollection<Collection<Object>> implements Iterable<Object> {
    
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
