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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Data structure which extends the AssociativeArray2D. The only difference of this
 * class with AssociativeArray2D is that all the cells within the internal map
 * should be set. In other words the internal map should have a rectangular 
 * format. The class provides a number of methods to access and modify the internal map.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class DataTable2D extends AssociativeArray2D {
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public DataTable2D() {
        super();
    }
    
    /**
     * Constructor that initializes the internal data with the provided map.
     * 
     * @param internalData 
     */
    public DataTable2D(Map<Object, AssociativeArray> internalData) {
        super(internalData);
    }
    
    /**
     * Returns if the DataTable2D is valid. This data structure is considered
     * valid it all the DataTable cells are set and as a result the DataTable
     * has a rectangular format.
     * 
     * @return 
     */
    public boolean isValid() {
        int totalNumberOfColumns = 0;
        Set<Object> columns = new HashSet<>();
        for(Map.Entry<Object, AssociativeArray> entry : internalData.entrySet()) {
            AssociativeArray row = entry.getValue();
            if(columns.isEmpty()) {
                //this is executed only for the first row
                for(Object column : row.internalData.keySet()) {
                    columns.add(column);
                }
                totalNumberOfColumns = columns.size();
            }
            else {
                //validating the columns of next rows based on first column
                if(totalNumberOfColumns!=row.size()) {
                    return false; //if the number of rows do not much then it is invalid
                }
                
                for(Object column : columns) {
                    if(row.containsKey(column)==false) {
                        return false; //if one of the detected columns do not exist, it is invalid
                    }
                }
            }
        }
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public DataTable2D copy() {
        return (DataTable2D) super.copy();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( !(o instanceof DataTable2D) ) return false;
        return internalData.equals(((DataTable2D)o).internalData);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return internalData.hashCode();
    }
}
