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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author bbriniotis
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
