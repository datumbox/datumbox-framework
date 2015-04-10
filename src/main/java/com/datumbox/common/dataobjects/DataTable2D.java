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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public final class DataTable2D extends AssociativeArray2D {

    
    public DataTable2D() {
        super();
    }
    
    public DataTable2D(Map<Object, AssociativeArray> internalData) {
        super(internalData);
    }
    
    /**
     * Checks whether all the DataTable cells are set.
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
    
    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( !(o instanceof DataTable2D) ) return false;
        return internalData.equals(((DataTable2D)o).internalData);
    }

    @Override
    public int hashCode() {
        return internalData.hashCode();
    }
}
