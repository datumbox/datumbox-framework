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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author bbriniotis
 */
public final class TransposeDataList extends DataStructureMap<Map<Object, FlatDataList>> {
    
    public TransposeDataList() {
        internalData = new HashMap<>();
    }
    
    public TransposeDataList(Map<Object, FlatDataList> internalData) {
        super(internalData);
    }
    
    public final FlatDataList remove(Object key) {
        return internalData.remove(key);
    }
    
    public final FlatDataList get(Object key) {
        return internalData.get(key);
    }
    
    public final FlatDataList put(Object key, FlatDataList value) {
        return internalData.put(key, value);
    }
    
    public final Set<Map.Entry<Object, FlatDataList>> entrySet() {
        return internalData.entrySet();
    }
    
    public final Set<Object> keySet() {
        return internalData.keySet();
    }
    
    public final Collection<FlatDataList> values() {
        return internalData.values();
    }
    
    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( !(o instanceof TransposeDataList) ) return false;
        return internalData.equals(((TransposeDataList)o).internalData);
    }

    @Override
    public int hashCode() {
        return internalData.hashCode();
    }
}