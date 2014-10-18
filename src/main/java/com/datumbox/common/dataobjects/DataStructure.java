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

import java.io.Serializable;

/**
 * Common DataStructures used internally by the Framework.
 * 
 * The Abstract class used by the commonly used internalDatastructures of the framework.
 
 Class Name                   Data Structure
 ----------                   --------------
 
 FlatDataCollection	=>	Collection<Object>
 * FlatDataList		=>	List<Object>
 * AssociativeArray	=>	Map<Object, Object>
 * transposeDataCollection	=>	Map<Object, FlatDataCollection>
 * TransposeDataList	=>	Map<Object, FlatDataList>
 * AssociativeArray2D	=>	Map<Object, AssociativeArray>
 * 
 * @author bbriniotis
 * @param <T>
 */
public abstract class DataStructure<T> implements Serializable {
    protected T internalData;
    
    public DataStructure() {
        
    }
    
    public DataStructure(T data) {
        this.internalData = data;
    }
}
