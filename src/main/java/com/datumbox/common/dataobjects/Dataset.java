/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
/**
 *
 * @author bbriniotis
 */
public final class Dataset implements Serializable, Iterable<Integer> {
    
    private final Map<Integer, Record> recordList;
    
    /* Stores columnName=> Class (ie Type) */
    private final Map<Object, ColumnType> columns;
    
    public static final Object constantColumnName = "~constant";
    public static final Object YColumnName = "~Y";
    
    public enum ColumnType {
        ORDINAL, //ordinal - the key is a String, Integer or List<Object> and the value is short
        NUMERICAL, //number field - the key is a String, Integer or List<Object> and the value is double
        DUMMYVAR, //dummy variable - the key is a List<Object> and the value is true false
        CATEGORICAL; //variable with multiple levels
    }
    
    public static ColumnType value2ColumnType(Object o) {
        if(o instanceof Double || 
           o instanceof Integer ||
           o instanceof Long ||
           o instanceof Float) {
            return ColumnType.NUMERICAL;
        }
        else if(o instanceof Boolean) {
            return ColumnType.DUMMYVAR;
        }
        else if(o instanceof Short) {
            return ColumnType.ORDINAL;
        }
        else if(o instanceof Number) {
            return ColumnType.NUMERICAL;
        }
        else { //string
            return ColumnType.CATEGORICAL;
        }
    }
    
    
    
    public Dataset() {
        recordList = new TreeMap<>();
        columns = new HashMap<>();
    }
    
    /**
     * Returns an Map with columns as keys and types are values.
     * 
     * @return 
     */
    public Map<Object, ColumnType> getColumns() {
        return Collections.unmodifiableMap(columns);
    }
    
    /**
     * Returns the number of columns of the internalDataset.
     * 
     * @return 
     */
    public int getColumnSize() {
        return columns.size();
    }
    
    /**
     * Returns the number of Records in the internalDataset.
     * 
     * @return 
     */
    public int size() {
        return recordList.size();
    }
    
    /**
     * Checks if the Dataset is empty.
     * 
     * @return 
     */
    public boolean isEmpty() {
        return recordList.isEmpty();
    }
    
    /**
     * It extracts the values of a particular column from all observations and
     * stores them into an array. It basically extracts the "flatDataCollection".
     * 
     * @param column
     * @return 
     */
    public FlatDataList extractColumnValues(Object column) {
        FlatDataList flatDataList = new FlatDataList();
        
        for(Integer rId : this) {
            Record r = recordList.get(rId);
            flatDataList.add(r.getX().get(column));
        }
        
        return flatDataList;
    }
    
    /**
     * It extracts the values of a Y values from all observations and
     * stores them into an array. It basically extracts the "flatDataCollection".
     * 
     * @return 
     */
    public FlatDataList extractYValues() {
        FlatDataList flatDataList = new FlatDataList();
        
        for(Integer rId : this) {
            Record r = recordList.get(rId);
            flatDataList.add(r.getY());
        }
        
        return flatDataList;
    }
    
    /**
     * For each Response variable Y found in the internalDataset, it extracts the 
     * values of column and stores it in a list. This method is used usually when we 
     * have categories in Y and we want the values of a particular column to be
     * extracted for each category. It basically extracts the "transposeDataList".
     * 
     * @param column
     * @return 
     */
    public TransposeDataList extractColumnValuesByY(Object column) {
        TransposeDataList transposeDataList = new TransposeDataList();
        
        for(Integer rId : this) {
            Record r = recordList.get(rId);   
            if(!transposeDataList.containsKey(r.getY())) {
                transposeDataList.put(r.getY(), new FlatDataList(new ArrayList<>()) );
            }
            
            transposeDataList.get(r.getY()).add(r.getX().get(column));
        }
        
        return transposeDataList;
    }
    
    /**
     * Returns a subset of the Dataset. It is used for k-fold cross validation
     * and sampling and he Records in the new Dataset have DIFFERENT ids from the
     * original.
     * 
     * @param idsCollection
     * @return 
     */
    public Dataset generateNewSubset(FlatDataList idsCollection) {
        Dataset d = new Dataset();
        
        for(Object id : idsCollection) {
            d.add(recordList.get((Integer)id)); 
        }        
        return d;
    }
    
    /**
     * Retrieves from the Dataset a particular Record by its id.
     * 
     * @param id
     * @return 
     */
    public Record get(Integer id) {
        return recordList.get(id);
    }
    
    /**
     * Remove completely a column from the dataset.
     * 
     * @param column 
     * @return  
     */
    public boolean removeColumn(Object column) {        
        if(columns.remove(column)!=null) { //try to remove it from the columns and it if it removed remove it from the list too
            for(Integer rId : this) {
                Record r = recordList.get(rId);
                if(r.getX().containsKey(column)) {
                    AssociativeArray xData = new AssociativeArray(r.getX());
                    xData.remove(column);
                    r = new Record(xData, r.getY(), r.getYPredicted(), r.getYPredictedProbabilities());
                    recordList.put(rId, r);
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Updates the meta information of the Dataset such as the supported columns.
     * 
     * @param r 
     */
    private void updateMeta(Record r) {
        for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
            Object column = entry.getKey();
            Object value = entry.getValue();
            
            if(columns.get(column) == null) {
                columns.put(column, value2ColumnType(value));
            }
        }
    }
    
    public void resetMeta() {
        columns.clear();
        for(Integer id: this) {
            updateMeta(recordList.get(id));
        }
    }
    
    /**
     * Adds the record in the dataset. The add method returns the id of the new record.
     * 
     * @param r
     * @return 
     */
    public Integer add(Record r) {
        Integer newId=(Integer) recordList.size();
        recordList.put(newId, r);
        updateMeta(r);
        
        return newId;
    }
    
    /**
     * Sets the record in a particular id in dataset.
     * 
     * @param rId
     * @param r
     * @return 
     */
    public Integer set(Integer rId, Record r) {
        _set(rId, r);
        updateMeta(r);
        
        return rId;
    }
    
    /**
     * Sets the record in a particular position in the dataset, WITHOUT updating
     * the internal meta-info. This method allows quick updates on the dataset 
     * but it is essential to call the resetMeta() immediately afterwards to
     * rebuild the meta info.
     * 
     * @param rId
     * @param r 
     */
    public void _set(Integer rId, Record r) {
        recordList.put(rId, r);
    }
    
    /**
     * Clears the Dataset and removes the internal variables.
     */
    public void clear() {
        //TODO: delete the bigdata appropriately
        recordList.clear();
        columns.clear();
    }
    
    /**
     * Implementing read-only iterator on Dataset to use it in loops.
     * 
     * @return 
     */
    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            private Iterator<Integer> it = recordList.keySet().iterator();
            
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Integer next() {
                return it.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
