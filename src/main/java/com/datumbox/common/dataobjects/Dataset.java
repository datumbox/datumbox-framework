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

import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public final class Dataset implements Serializable, Iterable<Integer> {
    
    private final Map<Integer, Record> recordList;
    
    /* Stores columnName=> Class (ie Type) */
    private final Map<Object, ColumnType> columns;
    
    private transient String dbName;
    private transient DatabaseConnector dbc;
    private transient DatabaseConfiguration dbConf;
    
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
    
    
    
    public Dataset(DatabaseConfiguration dbConf) {
        //we dont need to have a unique name, because it is not used by the connector on the current implementations
        //dbName = "dts_"+new BigInteger(130, RandomValue.getRandomGenerator()).toString(32);
        dbName = "dts";
        
        this.dbConf = dbConf;
        dbc = this.dbConf.getConnector(dbName);
        recordList = dbc.getBigMap("tmp_recordList", true);
        columns = dbc.getBigMap("tmp_columns", true);
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
        Dataset d = new Dataset(dbConf);
        
        for(Object id : idsCollection) {
            d.add(recordList.get((Integer)id)); 
        }        
        return d;
    }
    
    /**
     * Returns a copy of the Dataset. 
     * 
     * @return 
     */
    public Dataset copy() {
        Dataset d = new Dataset(dbConf);
        
        for(Integer id : this) {
            d.add(recordList.get(id)); 
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
     * Remove completely a list of columns from the dataset.
     * 
     * @param columnSet
     * @return  
     */
    public void removeColumns(Set<Object> columnSet) {  
        columnSet.retainAll(columns.keySet()); //keep only those columns that are already known to the Meta data of the Dataset
        
        if(columnSet.isEmpty()) {
            return;
        }
        
        //remove all the columns from the Meta data
        for(Object column : columnSet) {
            columns.remove(column);
        }

        for(Integer rId : this) {
            Record r = recordList.get(rId);
            
            boolean modified = false;
            AssociativeArray xData = new AssociativeArray(r.getX());
            for(Object column: columnSet) {
                modified |= xData.remove(column)!=null;
            }
            
            if(modified) {
                r = new Record(xData, r.getY(), r.getYPredicted(), r.getYPredictedProbabilities());
                recordList.put(rId, r);
            }
        }
        
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
        if(recordList.containsKey(rId)==false) {
            throw new IndexOutOfBoundsException(); //ensure that the record has already be set with add()
        }
        recordList.put(rId, r);
    }
    
    /**
     * Clears the Dataset and removes the internal variables.
     */
    public void clear() {
        dbc.dropBigMap("tmp_recordList", recordList);
        dbc.dropBigMap("tmp_columns", columns);
        dbc.dropDatabase();
    }
    
    /**
     * Implementing read-only iterator on Dataset to use it in loops.
     * 
     * @return 
     */
    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            //private Iterator<Integer> it = recordList.keySet().iterator();
            private Integer counter = 0;
            private final int n = recordList.size();
            
            @Override
            public boolean hasNext() {
                //return it.hasNext();
                return counter<n;
            }

            @Override
            public Integer next() {
                //return it.next();
                return counter++;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
