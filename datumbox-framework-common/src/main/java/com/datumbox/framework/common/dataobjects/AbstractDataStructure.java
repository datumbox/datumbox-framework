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

import java.io.Serializable;

/**
 * Common DataStructures used internally by the Framework. The framework requires
 * a variety of different DataStructures such as Lists, Arrays, Sets, Maps, Maps
 * of Maps etc. Their use depends on the method or algorithm. While designing
 * the framework we had the option to use either native collections (such as
 * {@literal Lists<Double>}) or build wrapper classes for each supported data structure. 
 
 We follow the latter approach. The benefit of this is that we limit the number
 of total Data Structures used internally in the framework, making the methods
 and utility classes easier to reuse. Moreover the wrapped classes make possible
 the conversion between different Data Structures without copying the data and
 they provide a number of extra convenience methods. You should note that all
 DataStructures store values as Objects. This is done deliberately because
 the types of the data that we store is not always known at compile time but
 rather on runtime. Moreover keeping everything stored as Objects helps us to 
 avoid copying the data while converting one collection type to the other. The
 framework takes care of any conversion on the fly, so you don't need to worry
 about it.
 
 
 Note that this Abstract class is the parent class for all the common 
 internalDatastructures of the framework. Below we provide a mapping between the
 AbstractDataStructure classes of the framework and the wrapped Java Collection:

 AbstractDataStructure Class          Wrapped Java Collection        
 -------------------          -----------------------
 FlatDataList			{@literal List<Object>}
 * FlatDataCollection		{@literal Collection<Object>}
 * AssociativeArray		{@literal Map<Object, Object>}
 * TransposeDataList		{@literal Map<Object, FlatDataList>}
 * TransposeDataCollection	{@literal Map<Object, FlatDataCollection>}
 * TransposeDataCollection2D    {@literal Map<Object, TransposeDataCollection>}
 * AssociativeArray2D		{@literal Map<Object, AssociativeArray>}
 * DataTable2D                  {@literal Map<Object, AssociativeArray>}
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <T>
 */
public abstract class AbstractDataStructure<T> implements Serializable {
    /**
     * Internal Wrapped Java Collection.
     */
    protected final T internalData;
    
    /**
     * Public constructor which takes as argument the appropriate Java collection
     * for each DataStructure.
     * 
     * @param data 
     */
    public AbstractDataStructure(T data) {
        this.internalData = data;
    }
    
}
