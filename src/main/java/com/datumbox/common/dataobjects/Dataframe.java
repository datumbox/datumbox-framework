/**
 * Copyright (C) 2013-2016 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
import com.datumbox.framework.utilities.text.cleaners.StringCleaner;
import com.datumbox.framework.utilities.text.extractors.TextExtractor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Dataframe class stores a list of Records Objects and several meta-data. All
 * Machine Learning algorithms get as argument Dataframe objects. The class has an
 * internal static Builder class which can be used to generate Dataframe objects 
 * from Text or CSV files.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Dataframe implements Serializable, Collection<Record> {
    private static final long serialVersionUID = 1L;
    
    /**
     * Internal name of the response variable.
     */
    public static final String yColumnName = "~Y";
    
    /**
     * Internal name of the constant.
     */
    public static final String constantColumnName = "~CONSTANT";
    
    /**
     * The Builder is a utility class which can help you build Dataframe from
 Text files and CSV files.
     */
    public static class Builder {
        
        /**
         * It builds a Dataframe object from a provided list of text files. The data
         * map should have as index the names of each class and as values the URIs
         * of the training files. The files should contain one training example
         * per row. If we want to parse a Text File of unknown category then
         * pass a single URI with null as key.
         * 
         * The method requires as arguments a file with the category names and locations
         * of the training files, an instance of a TextExtractor which is used
         * to extract the keywords from the documents and the Database Configuration
         * Object.
         * 
         * @param textFilesMap
         * @param textExtractor
         * @param dbConf
         * @return 
         */
        public static Dataframe parseTextFiles(Map<Object, URI> textFilesMap, TextExtractor textExtractor, DatabaseConfiguration dbConf) {
            Dataframe dataset = new Dataframe(dbConf);
            Logger logger = LoggerFactory.getLogger(Dataframe.Builder.class);
            
            for (Map.Entry<Object, URI> entry : textFilesMap.entrySet()) {
                Object theClass = entry.getKey();
                URI datasetURI = entry.getValue();
                
                logger.info("Dataset Parsing {} class", theClass);
                
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(datasetURI)), "UTF8"))) {
                    for (String line; (line = br.readLine()) != null;) {
                        dataset.add(new Record(new AssociativeArray(textExtractor.extract(StringCleaner.clear(line))), theClass));
                    }
                } 
                catch (IOException ex) {
                    dataset.delete();
                    throw new UncheckedIOException(ex);
                }
            }
            
            return dataset;
        }
        
        /**
         * It builds a Dataframe object from a CSV file; the first line of the provided 
         * CSV file must have a header with the column names.
         * 
         * The method accepts the following arguments: A Reader object from where
         * we will read the contents of the csv file. The name column of the 
         * response variable y. A map with the column names and their respective
         * DataTypes. The char delimiter for the columns, the char for quotes and
         * the string of the record/row separator. The Database Configuration
         * object.
         * 
         * @param reader
         * @param yVariable
         * @param headerDataTypes
         * @param delimiter
         * @param quote
         * @param recordSeparator
         * @param dbConf
         * @return 
         */
        public static Dataframe parseCSVFile(Reader reader, String yVariable, Map<String, TypeInference.DataType> headerDataTypes, 
                                           char delimiter, char quote, String recordSeparator, DatabaseConfiguration dbConf) {
            Logger logger = LoggerFactory.getLogger(Dataframe.Builder.class);
            
            logger.info("Parsing CSV file");
            
            if (!headerDataTypes.containsKey(yVariable)) {
                logger.warn("WARNING: The file is missing the response variable column {}.", yVariable);
            }
            
            TypeInference.DataType yDataType = headerDataTypes.get(yVariable);
            Map<String, TypeInference.DataType> xDataTypes = new HashMap<>(headerDataTypes); //copy header types
            xDataTypes.remove(yVariable); //remove the response variable from xDataTypes
            Dataframe dataset = new Dataframe(dbConf, yDataType, xDataTypes); //use the private constructor to pass DataTypes directly and avoid updating them on the fly
            
            
            CSVFormat format = CSVFormat
                                .RFC4180
                                .withHeader()
                                .withDelimiter(delimiter)
                                .withQuote(quote)
                                .withRecordSeparator(recordSeparator);
            
            try (final CSVParser parser = new CSVParser(reader, format)) {                    
                for (CSVRecord row : parser) {
                    
                    if (!row.isConsistent()) {
                        logger.warn("WARNING: Skipping row {} because its size does not match the header size.", row.getRecordNumber());
                        continue;
                    }
                    
                    Object y = null;
                    AssociativeArray xData = new AssociativeArray();
                    for (Map.Entry<String, TypeInference.DataType> entry : headerDataTypes.entrySet()) {
                        String column = entry.getKey();
                        TypeInference.DataType dataType = entry.getValue();
                        
                        Object value = TypeInference.DataType.parse(row.get(column), dataType); //parse the string value according to the DataType
                        if (yVariable != null && yVariable.equals(column)) {
                            y = value;
                        } 
                        else {
                            xData.put(column, value);
                        }
                    }
                    dataset._add(new Record(xData, y)); //use the internal _add() to avoid the update of the Metas. The Metas are already set in the construction of the Dataframe.
                }
            } 
            catch (IOException ex) {
                dataset.delete();
                throw new UncheckedIOException(ex);
            }
            return dataset;
        }

    }    
    
    private TypeInference.DataType yDataType; 
    private Map<Object, TypeInference.DataType> xDataTypes;
    private List<Integer> index;
    private Map<Integer, Record> records;
    
    private String dbName;
    private transient DatabaseConnector dbc;
    private transient DatabaseConfiguration dbConf;
    
    /**
     * Public constructor.
     * 
     * @param dbConf 
     */
    public Dataframe(DatabaseConfiguration dbConf) {
        //we dont need to have a unique name, because it is not used by the connector on the current implementations
        //dbName = "dts_"+new BigInteger(130, RandomGenerator.getThreadLocalRandom()).toString(32);
        dbName = "dts";
        
        this.dbConf = dbConf;
        dbc = this.dbConf.getConnector(dbName);
        index = new LinkedList<>();
        records = dbc.getBigMap("tmp_records", true);
        
        yDataType = null;
        xDataTypes = new HashMap<>();
    }
    
    /**
     * Private constructor used by the Builder inner static class.
     * 
     * @param dbConf
     * @param yDataType
     * @param xDataTypes 
     */
    private Dataframe(DatabaseConfiguration dbConf, TypeInference.DataType yDataType, Map<String, TypeInference.DataType> xDataTypes) {
        this(dbConf);
        this.yDataType = yDataType;
        this.xDataTypes.putAll(xDataTypes);
    }
    
    
    //Mandatory Collection Methods
    
    /**
     * Returns the total number of Records of the Dataframe.
     * 
     * @return 
     */
    @Override
    public synchronized int size() {
        return index.size();
    }
    
    /**
     * Checks if the Dataframe is empty.
     * 
     * @return 
     */
    @Override
    public synchronized boolean isEmpty() {
        return index.isEmpty();
    }
    
    /**
     * Clears all the internal Records of the Dataframe. The Dataframe can be used
     * after you clear it.
     */
    @Override
    public synchronized void clear() {
        yDataType = null;
        
        xDataTypes.clear();
        index.clear();
        records.clear();
    }

    /**
     * Adds a record in the Dataframe and updates the Meta data. 
     * 
     * @param r
     * @return 
     */
    @Override
    public synchronized boolean add(Record r) {
        addRecord(r);
        return true;
    }
    
    /**
     * Checks if the object exists in the Dataframe.
     * 
     * @param o
     * @return 
     */
    @Override
    public synchronized boolean contains(Object o) {
        return records.containsValue((Record)o);
    }
    
    /**
     * Adds all of the elements in the specified collection to this collection.
     * 
     * @param c
     * @return 
     */
    @Override
    public synchronized boolean addAll(Collection<? extends Record> c) {
        for(Record r : c) {
            add(r);
        }
        return true;
    }
    
    /**
     * Returns true if this collection contains all of the elements
     * in the specified collection.
     * 
     * @param c
     * @return 
     */
    @Override
    public synchronized boolean containsAll(Collection<?> c) {
        return records.values().containsAll(c);
    }
    
    /**
     * Returns the Records of the Dataframe in an Object Array.
     * 
     * @return 
     */
    @Override
    public synchronized Object[] toArray() {
        Object[] array = new Object[size()];
        int i = 0;
        for(Record r : values()) {
            array[i++] = r;
        }
        return array;
    }
    
    /**
     * Returns the Records of the Dataframe in an Array of specific type.
     * 
     * @param <T>
     * @param a
     * @return 
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T> T[] toArray(T[] a) {
        int size = size();
        if (a.length < size) {
            a = (T[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }
        int i = 0;
        for (Record r : values()) {
            a[i++] = (T) r;
        }
        return a;
    }
    
    /**
     * Returns a read-only iterator on the values of the Dataframe.
     * 
     * @return 
     */
    @Override
    public Iterator<Record> iterator() {
        return new Iterator<Record>() {
            private final Iterator<Integer> it = index().iterator();
             
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Record next() {
                return get(it.next());
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("This is a read-only iterator, remove operation is not supported.");
            }
        };
    }
    
    
    //Optional Collection Methods
    
    /**
     * Removes the first occurrence of the specified element from this Dataframe, 
     * if it is present and it does not update the metadata.
     * 
     * @param o
     * @return 
     */
    @Override
    public synchronized boolean remove(Object o) {
        Integer id = indexOf((Record) o);
        if(id == null) {
            return false;
        }
        remove(id);
        return true;
    }
    
    /**
     * Removes all of this collection's elements that are also contained in the
     * specified collection and updates the metadata.
     * 
     * @param c
     * @return 
     */
    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for(Object o : c) {
            modified |= remove((Record)o);
        }
        if(modified) {
            recalculateMeta();
        }
        return modified;
    }

    /**
     * Retains only the elements in this collection that are contained in the
     * specified collection and updates the meta data.
     * 
     * @param c
     * @return 
     */
    @Override
    public synchronized boolean retainAll(Collection<?> c) {
        boolean modified = false;
        for(Map.Entry<Integer, Record> e : entries()) {
            if(!c.contains(e.getValue())) {
                remove(e.getKey());
                modified = true;
            }
        }
        if(modified) {
            recalculateMeta();
        }
        return modified;
    }
    
    
    //Other methods

    /**
     * Removes a record with a particular id from the Dataframe but does not update
     * the metadata.
     * 
     * @param id
     * @return 
     */
    public synchronized Record remove(Integer id) {
        if(index.remove(id)) {
            return records.remove(id);
        }
        return null;
    }
    
    /**
     * Returns the index of the first occurrence of the specified element in this 
     * Dataframe, or null if this Dataframe does not contain the element.
     * WARNING: The Recordsare checked only for their X and Y values, not for 
     * the yPredicted and yPredictedProbabilities values.
     * 
     * @param r
     * @return 
     */
    public synchronized Integer indexOf(Record r) {
        if(r!=null) {
            for(Map.Entry<Integer, Record> e : entries()) {
                if(r.equals(e.getValue())) {
                    return e.getKey();
                }
            }
        }
        return null;
    }
    
    /**
     * Returns a particular Record using its id.
     * 
     * @param id
     * @return 
     */
    public synchronized Record get(Integer id) {
        return records.get(id);
    }
    
    /**
     * Adds a Record in the Dataframe and returns its id.
     * 
     * @param r
     * @return 
     */
    public synchronized Integer addRecord(Record r) {
        Integer rId = _add(r);
        updateMeta(r);
        return rId;
    }
    
    /**
     * Sets the record of a particular id in the dataset. The record must already
     * exists within the dataset or an IllegalArgumentException is thrown.
     * 
     * Note that the meta-data are partially updated. This means that if the replaced 
     * Record contained a column which is now no longer available in the dataset,
     * then the meta-data will not refect this update (the column will continue to exist
     * in the meta data). If this is a problem, you should call the recalculateMeta()
     * method to force them being recalculated.
     * 
     * @param rId
     * @param r
     * @return 
     */
    public synchronized Integer set(Integer rId, Record r) {
        _set(rId, r);
        updateMeta(r);
        return rId;
    }
    
    /**
     * Returns the total number of X columns in the Dataframe.
     * 
     * @return 
     */
    public synchronized int xColumnSize() {
        return xDataTypes.size();
    }
    
    /**
     * Returns the type of the response variable y.
     * 
     * @return 
     */
    public synchronized TypeInference.DataType getYDataType() {
        return yDataType;
    }
    
    /**
     * Returns an Map with column names as index and DataTypes as values.
     * 
     * @return 
     */
    public synchronized Map<Object, TypeInference.DataType> getXDataTypes() {
        return Collections.unmodifiableMap(xDataTypes);
    }
    
    /**
     * It extracts the values of a particular column from all records and
     * stores them into an FlatDataList.
     * 
     * @param column
     * @return 
     */
    public synchronized FlatDataList getXColumn(Object column) {
        FlatDataList flatDataList = new FlatDataList();
        
        for(Record r : values()) {
            flatDataList.add(r.getX().get(column));
        }
        
        return flatDataList;
    }
    
    /**
     * It extracts the values of the response variables from all observations and
     * stores them into an FlatDataList.
     * 
     * @return 
     */
    public synchronized FlatDataList getYColumn() {
        FlatDataList flatDataList = new FlatDataList();
        
        for(Record r : values()) {
            flatDataList.add(r.getY());
        }
        
        return flatDataList;
    }
    
    /**
     * Removes completely a list of columns from the dataset. The meta-data of 
     * the Dataframe are updated.
     * 
     * @param columnSet
     */
    public synchronized void dropXColumns(Set<Object> columnSet) {  
        columnSet.retainAll(xDataTypes.keySet()); //keep only those columns that are already known to the Meta data of the Dataframe
        
        if(columnSet.isEmpty()) {
            return;
        }
        
        //remove all the columns from the Meta data
        xDataTypes.keySet().removeAll(columnSet);

        for(Map.Entry<Integer, Record> e : entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            
            AssociativeArray xData = r.getX().copy();
            int d = xData.size();
            xData.keySet().removeAll(columnSet);
            
            if(xData.size()!=d) {
                r = new Record(xData, r.getY(), r.getYPredicted(), r.getYPredictedProbabilities());
                _set(rId, r);
            }
        }
        
    }
    
    /**
     * It generates and returns a new Dataframe which contains a subset of this Dataframe. 
     * All the Records of the returned Dataframe are copies of the original Records. 
     * The method is used for k-fold cross validation and sampling. Note that the 
     * Records in the new Dataframe have DIFFERENT ids from the original ones.
     * 
     * @param idsCollection
     * @return 
     */
    public synchronized Dataframe getSubset(FlatDataList idsCollection) {
        Dataframe d = new Dataframe(dbConf);
        
        for(Object id : idsCollection) {
            d.add(get((Integer)id)); 
        }        
        return d;
    }
    
    /**
     * It forces the recalculation of Meta data using the Records of the dataset.
     */
    public synchronized void recalculateMeta() {
        yDataType = null;
        xDataTypes.clear();
        for(Record r : values()) {
            updateMeta(r);
        }
    }
    
    /**
     * Returns a deep copy of the Dataframe. 
     * 
     * @return 
     */
    public synchronized Dataframe copy() {
        Dataframe d = new Dataframe(dbConf);
        
        for(Record r : values()) {
            d.add(r); 
        }        
        return d;
    }
    
    /**
     * Deletes the Dataframe and removes all internal variables. Once you delete a
     * dataset, the instance can no longer be used.
     */
    public synchronized void delete() {
        dbc.dropBigMap("tmp_records", records);
        dbc.dropDatabase();
        dbc.close();
        
        //Ensures that the Dataframe can't be used after delete() is called.
        yDataType = null;
        xDataTypes = null;
        index = null;
        records = null;
    }
    
    /**
     * Returns a read-only Iterable on the keys and Records of the Dataframe.
     * 
     * @return 
     */
    public Iterable<Map.Entry<Integer, Record>> entries() {
        return () -> new Iterator<Map.Entry<Integer, Record>>() {
            private final Iterator<Integer> it = index().iterator();
            
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }
            
            @Override
            public Map.Entry<Integer, Record> next() {
                Integer rId = it.next();
                return new AbstractMap.SimpleEntry<>(rId, get(rId));
                
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException("This is a read-only iterator, remove operation is not supported.");
            }
        };
    }
    
    /**
     * Returns a read-only Iterable on the keys of the Dataframe.
     * 
     * @return 
     */
    public Iterable<Integer> index() {
        return () -> new Iterator<Integer>() {
            private final Iterator<Integer> it = index.iterator();
            
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
                throw new UnsupportedOperationException("This is a read-only iterator, remove operation is not supported.");
            }
        };
    }
    
    /**
     * Returns a read-only Iterable on the values of the Dataframe.
     * 
     * @return 
     */
    public Iterable<Record> values() {
        return Dataframe.this::iterator;
    }
    
    /**
     * Sets the record in a particular position in the dataset, WITHOUT updating
     * the internal meta-info. This method is similar to set() and it allows quick updates 
     * on the dataset. Nevertheless it is not advised to use this method because 
     * unless you explicitly call the recalculateMeta() method, the meta data
     * will be corrupted. If you do use this method, MAKE sure you perform the
     * recalculation after you are done with the updates.
     * 
     * @param rId
     * @param r 
     */
    public synchronized void _set(Integer rId, Record r) {
        if(records.containsKey(rId)==false) {
            throw new IllegalArgumentException("Setting an id which does not exist is not permitted.");
        }
        records.put(rId, r);
    }
    
    /**
     * Adds the record in the dataset without updating the Meta. The add method 
     * returns the id of the new record.
     * 
     * @param r
     * @return 
     */
    private Integer _add(Record r) {
        Integer newId = size();
        index.add(newId);
        records.put(newId, r);
        return newId;
    }
    
    /**
     * Updates the meta data of the Dataframe using the provided Record. 
     * The Meta-data include the supported columns and their DataTypes.
     * 
     * @param r 
     */
    private void updateMeta(Record r) {
        for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
            Object column = entry.getKey();
            Object value = entry.getValue();
            
            if(xDataTypes.containsKey(column) == false) {
                xDataTypes.put(column, TypeInference.getDataType(value));
            }
        }
        
        if(yDataType == null) {
            yDataType = TypeInference.getDataType(r.getY());
        }
    }
    
}
