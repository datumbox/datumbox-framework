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

import com.datumbox.common.utilities.MapFunctions;
import com.datumbox.common.utilities.RandomValue;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 *
 * @author bbriniotis
 */
public final class Dataset implements Serializable, Iterable<Record> {
    private boolean sparce = true;
    
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
    
    /**
     * Returns a subset of the Dataset. It is used for k-fold cross validation
     * and sampling and he Records in the new Dataset have DIFFERENT ids from the
     * original.
     * 
     * @param idsCollection
     * @return 
     */
    public Dataset generateNewSubset(Collection<Integer> idsCollection) {
        Dataset d = new Dataset();
        
        for(Integer id : idsCollection) {
            d.add(recordList.get(id)); 
        }        
        return d;
    }
    
    //Retrieves from the Dataset a particular Record by its id.
    public Record get(Integer id) {
        return recordList.get(id);
    }
    
    
    /**
     * Converts safely any Number to Double.
     * @param o
     * @return 
     */
    public static Double toDouble(Object o) {
        if(o==null) {
            return null;
        }
        if(o instanceof Boolean) {
            return ((Boolean)o)?1.0:0.0;
        }
        return ((Number)o).doubleValue();
    }
    
    /**
     * Converts safely any Number to Integer.
     * @param o
     * @return 
     */
    public static Integer toInteger(Object o) {
        if(o==null) {
            return null;
        }
        if(o instanceof Boolean) {
            return ((Boolean)o)?1:0;
        }
        return ((Number)o).intValue();
    }
    
    /**
     * Converts to Double[] safely the original FlatDataCollection by using the
     * iteratorDouble.
     * 
     * @param flatDataCollection
     * @return 
     */
    public static Double[] copyCollection2DoubleArray(FlatDataCollection flatDataCollection) {
        int n = flatDataCollection.size();
        Double[] doubleArray = new Double[n];
        int i=0;
        
        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            doubleArray[i++] = it.next();
        }
        
        return doubleArray;
    }
    
    /**
     * Converts to Object[] the original FlatDataCollection. The method is used to 
     * generate a deep copy of the flatDataCollection and it is called in order to
     * avoid modifying the original array.
     * 
     * @param <T>
     * @param c
     * @param flatDataCollection
     * @return
     * @throws IllegalArgumentException 
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] copyCollection2Array(Class<T> c, FlatDataCollection flatDataCollection) throws IllegalArgumentException {
        int n = flatDataCollection.size();
        if(n==0) {
            throw new IllegalArgumentException();
        }
        
        T[] copy = (T[]) Array.newInstance(c, n);
        
        int i=0;
        for (Object value : flatDataCollection) {
            copy[i++]=c.cast(value);
        }
        
        return copy;
    }
    
    /**
     * Replaces the actual values of the flatDataCollection with their ranks and
     * returns in the tieCounter the keys that occur more than once and the 
     * number of occurrences. The tieCounter does not store the list and ranks
     * of the actual ties as in the PHP implementation because we never use them. 
     * 
     * @param flatDataCollection
     * @return 
     */
    public static AssociativeArray getRanksFromValues(FlatDataList flatDataCollection) {
        
        AssociativeArray tiesCounter = new AssociativeArray(new LinkedHashMap<>()); //ConcurrentSkipListMap
        Map<Object, Double> key2AvgRank = new LinkedHashMap<>();
        
        _buildRankArrays(flatDataCollection.internalData, tiesCounter, key2AvgRank); //tiesCounter and key2AvgRank are modified
        
        int i = 0;
        for (Object value : flatDataCollection) {
            flatDataCollection.set(i++, key2AvgRank.get(value));
        }
        
        return tiesCounter; 
    }

    /**
     * Replaces the actual values of the associativeArray with their ranks and
     * returns in the tieCounter the keys that occur more than once and the 
     * number of occurrences. The tieCounter does not store the list and ranks
     * of the actual ties as in the PHP implementation because we never use them. 
     * 
     * @param associativeArray
     * @return 
     */
    public static AssociativeArray getRanksFromValues(AssociativeArray associativeArray) {
        
        AssociativeArray tiesCounter = new AssociativeArray(new LinkedHashMap<>()); //ConcurrentSkipListMap
        Map<Object, Double> key2AvgRank = new LinkedHashMap<>();
        
        _buildRankArrays(associativeArray.values(), tiesCounter, key2AvgRank); //tiesCounter and key2AvgRank are modified
        
        for (Map.Entry<Object, Object> entry : associativeArray.entrySet()) {
            associativeArray.put(entry.getKey(), key2AvgRank.get(entry.getValue()));
        }
        
        return tiesCounter; 
    }
    
    /**
     * Internal method used by getRanksFromValues() to produce the tiesCounter
     * and key2AvgRank arrays.
     * 
     * @param dataCollection
     * @param tiesCounter
     * @param key2AvgRank 
     */
    private static void _buildRankArrays(Collection<Object> dataCollection, AssociativeArray tiesCounter, Map<Object, Double> key2AvgRank) {
        //unnecessary already empty
        //tiesCounter.clear();
        //key2AvgRank.clear();
        
        for (Object value : dataCollection) {
            Object count = tiesCounter.get(value);
            if(count==null) {
                count=0;
            }
            tiesCounter.put(value,((Number)count).intValue() + 1);
        }
        
        tiesCounter.internalData = MapFunctions.<Object, Object>sortNumberMapByKeyAscending(tiesCounter.internalData);
        
        int itemCounter=0;
        
        //for(Map.Entry<Object, Object> entry : tiesCounter.entrySet()) {
        Iterator<Map.Entry<Object, Object>> it = tiesCounter.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Object, Object> entry = it.next();
            Object key = entry.getKey();
            double count = Dataset.toDouble(entry.getValue());
            if(count<=1.0) {
                //keep as ties only keys that occur more than once.
                //tiesCounter.remove(key); 
                it.remove();
            }
            
            //Arithmetic progression: http://en.wikipedia.org/wiki/Arithmetic_progression
            //double sumOfRanks = (double)occurrences/2.0 * ((itemCounter+1) +  (itemCounter+occurrences)); 
            //double avgRank= sumOfRanks/occurrences;
            double avgRank = ((itemCounter+1) +  (itemCounter+count))/2.0; //same as above but faster
            
            key2AvgRank.put(key, avgRank); //now the tmpMap stores the value => avgRank
            itemCounter+=count;
        }
    }
    
    public Dataset() {
        recordList = new LinkedHashMap<>();
        columns = new HashMap<>();
    }
    
    /**
     * Returns true if the internalDataset is sparce (all columns appear in the internalData) 
 and false if it is not sparce.
     * 
     * @return 
     */
    public boolean isSparce() {
        return sparce;
    }
    
    /**
     * Returns an Map with columns as keys and types are values.
     * 
     * @return 
     */
    public Map<Object, ColumnType> getColumns() {
        return columns;
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
     * Converts the Dataset to Sparse by adding the missing columns.
     */
    public void convert2Sparse() {
        if(sparce==true) {
            return;
        }
        
        for(Record r : recordList.values()) {
            for(Object column : columns.keySet()) {
                if(r.getX().containsKey(column)==false) {
                    r.getX().put(column, null);
                }
            }
        }
        
        sparce=true;
    }
    
    /**
     * Remove completely a column from the dataset.
     * 
     * @param column 
     * @return  
     */
    public boolean removeColumn(Object column) {        
        if(columns.remove(column)!=null) { //try to remove it from the columns and it if it removed remove it from the list too
            for(Record r : recordList.values()) {
                r.getX().remove(column);
            }
            
            return true;
        }
        
        return false;
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
        
        for(Record r : recordList.values()) {
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
        
        for(Record r : recordList.values()) {
            flatDataList.add(r.getY());
        }
        
        return flatDataList;
    }
    
    /**
     * For each Response variable Y found in the internalDataset, it extracts the values
 of column and stores it in a list. This method is used usually when we 
     * have categories in Y and we want the values of a particular column to be
     * extracted for each category. It basically extracts the "transposeDataList".
     * 
     * @param column
     * @return 
     */
    public TransposeDataList extractColumnValuesByY(Object column) {
        TransposeDataList transposeDataList = new TransposeDataList(new LinkedHashMap<>());
        
        for(Record r : recordList.values()) {    
            if(!transposeDataList.containsKey(r.getY())) {
                transposeDataList.put(r.getY(), new FlatDataList(new ArrayList<>()) );
            }
            
            transposeDataList.get(r.getY()).add(r.getX().get(column));
        }
        
        return transposeDataList;
    }
    
    /**
     * Updates the meta information of the Dataset such as whether it is sparce
     * and the supported columns.
     * 
     * @param r 
     */
    private void updateMeta(Record r) {
        boolean foundNewColumn = false;
        for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
            Object column = entry.getKey();
            Object value = entry.getValue();
            
            if(columns.get(column) == null) {
                columns.put(column, value2ColumnType(value));
                foundNewColumn=true;
            }
        }
        
        /* 
            If new columns are found and it is not the first record added
            then the internalData are not sparse.
        */
        if(sparce == true && foundNewColumn==true && recordList.size()>1) {
            sparce = false;
        }
    }

    /**
     * Merge the d dataset to the current one.
     * 
     * @param d 
     */
    public void merge(Dataset d) {
        //does not modify the ids of the records of the Dataset d
        for(Record r : d) {
            this.add(r);
        }
    } 
    
    
    /**
     * Adds the record in the dataset. The original record is shallow copied 
     * and its id is updated (this does not affect the id of the original record).
     * The add method returns the id of the new record.
     * 
     * @param original
     * @return 
     */
    public Integer add(Record original) {
        Record newRecord = original.quickCopy();
        
        Integer newId=(Integer) recordList.size();
        newRecord.setId(newId);
        recordList.put(newId, newRecord);
        updateMeta(newRecord);
        
        return newRecord.getId();
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
     * Clears the Dataset and removes the internal variables.
     */
    public void clear() {
        recordList.clear();
        columns.clear();
    }
    
    /**
     * Randomizes the order of the records in the recordList. This method DOES NOT 
     * change the ids of the records or the keys of the recordList. It just modifies
     * the their order in the list.
     */
    public void shuffle() {
        List<Integer> idList = new ArrayList<>(recordList.keySet());
        Collections.shuffle(idList, RandomValue.randomGenerator);
        
        Map<Integer, Record> newRecordList = new LinkedHashMap<>();
        for(Integer id : idList) {
            newRecordList.put(id, recordList.get(id));
        }
        recordList.clear();
        recordList.putAll(newRecordList);
        //TODO: this method does not work appropriately because the iterator of dataset fetches records by their ID. I need to check when the shuffle is called and correct this logic
    }
    
    /**
     * Implementing read-only iterator on Dataset to use it in loops.
     * 
     * @return 
     */
    @Override
    public Iterator<Record> iterator() { 
        return new Iterator<Record>() {
            private int index=0;
            @Override
            public boolean hasNext() {
                return index < recordList.size();
            }

            @Override
            public Record next() {
                return recordList.get(index++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
