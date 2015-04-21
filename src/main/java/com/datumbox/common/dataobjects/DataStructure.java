/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
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

import java.io.Serializable;

/**
 * Common DataStructures used internally by the Framework.
 * 
 * The Abstract class used by the commonly used internalDatastructures of the framework.
 *
 * Class Name                   Data Structure
 * ----------                   --------------
 * FlatDataList			List<Object>
 * FlatDataCollection		Collection<Object>
 * AssociativeArray		Map<Object, Object>
 * TransposeDataList		Map<Object, FlatDataList>
 * TransposeDataCollection	Map<Object, FlatDataCollection>
 * TransposeDataCollection2D    Map<Object, TransposeDataCollection>
 * AssociativeArray2D		Map<Object, AssociativeArray>
 * DataTable2D                  Map<Object, AssociativeArray>
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
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
