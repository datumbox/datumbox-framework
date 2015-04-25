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
import com.datumbox.framework.utilities.text.cleaners.StringCleaner;
import com.datumbox.framework.utilities.text.extractors.TextExtractor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Dataset class stores a list of Records Objects and several meta-data. All
 * Machine Learning algorithms get as argument Dataset objects. The class has an
 * internal static Builder class which can be used to generate Dataset objects 
 * from Text or CSV files.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public final class Dataset implements Serializable, Iterable<Integer> {
    
    public static final String yColumnName = "~Y";
    public static final String constantColumnName = "~CONSTANT";
    
    /**
     * The Builder is a utility class which can help you build Dataset from
     * Text files and CSV files.
     */
    public static final class Builder {
        
        /**
         * It builds a Dataset object from a provided list of text files. The data
         * map should have as keys the names of each class and as values the URIs
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
        public static Dataset parseTextFiles(Map<Object, URI> textFilesMap, TextExtractor textExtractor, DatabaseConfiguration dbConf) {
            Dataset dataset = new Dataset(dbConf);
            Logger logger = LoggerFactory.getLogger(Dataset.Builder.class);
            
            for (Map.Entry<Object, URI> entry : textFilesMap.entrySet()) {
                Object theClass = entry.getKey();
                URI datasetURI = entry.getValue();
                
                logger.info("Dataset Parsing {} class", theClass);
                
                try (final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(datasetURI)), "UTF8"))) {
                    for (String line; (line = br.readLine()) != null;) {
                        dataset.add(new Record(new AssociativeArray(textExtractor.extract(StringCleaner.clear(line))), theClass));
                    }
                } 
                catch (IOException ex) {
                    dataset.erase();
                    throw new RuntimeException(ex);
                }
            }
            
            return dataset;
        }
        
        /**
         * It builds a Dataset object from a CSV file; the first line of the provided 
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
        public static Dataset parseCSVFile(Reader reader, String yVariable, Map<String, TypeInference.DataType> headerDataTypes, 
                                           char delimiter, char quote, String recordSeparator, DatabaseConfiguration dbConf) {
            Logger logger = LoggerFactory.getLogger(Dataset.Builder.class);
            
            logger.info("Parsing CSV file");
            
            if (!headerDataTypes.containsKey(yVariable)) {
                logger.warn("WARNING: The file is missing the response variable column {}.", yVariable);
            }
            
            TypeInference.DataType yDataType = headerDataTypes.get(yVariable);
            Map<String, TypeInference.DataType> xDataTypes = new HashMap<>(headerDataTypes); //copy header types
            xDataTypes.remove(yVariable); //remove the response variable from xDataTypes
            Dataset dataset = new Dataset(dbConf, yDataType, xDataTypes); //use the private constructor to pass DataTypes directly and avoid updating them on the fly
            
            
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
                    dataset._add(new Record(xData, y)); //use the internal _add() to avoid the update of the Metas. The Metas are already set in the construction of the Dataset.
                }
            } 
            catch (IOException ex) {
                dataset.erase();
                throw new RuntimeException(ex);
            }
            return dataset;
        }

    }    
    
    private Map<Integer, Record> recordList;
    
    private TypeInference.DataType yDataType; 
    /* Stores columnName=> DataType */
    private Map<Object, TypeInference.DataType> xDataTypes;
    
    private transient String dbName;
    private transient DatabaseConnector dbc;
    private transient DatabaseConfiguration dbConf;
    
    /**
     * Public constructor.
     * 
     * @param dbConf 
     */
    public Dataset(DatabaseConfiguration dbConf) {
        //we dont need to have a unique name, because it is not used by the connector on the current implementations
        //dbName = "dts_"+new BigInteger(130, RandomGenerator.getThreadLocalRandom()).toString(32);
        dbName = "dts";
        
        this.dbConf = dbConf;
        dbc = this.dbConf.getConnector(dbName);
        recordList = dbc.getBigMap("tmp_recordList", true);
        
        yDataType = null;
        xDataTypes = dbc.getBigMap("tmp_xColumnTypes", true);
    }
    
    /**
     * Private constructor used by the Builder inner static class.
     * 
     * @param dbConf
     * @param yDataType
     * @param xDataTypes 
     */
    private Dataset(DatabaseConfiguration dbConf, TypeInference.DataType yDataType, Map<String, TypeInference.DataType> xDataTypes) {
        this(dbConf);
        this.yDataType = yDataType;
        this.xDataTypes.putAll(xDataTypes);
    }
    
    /**
     * Returns the type of the response variable y.
     * 
     * @return 
     */
    public TypeInference.DataType getYDataType() {
        return yDataType;
    }
    
    /**
     * Returns an Map with column names as keys and DataTypes as values.
     * 
     * @return 
     */
    public Map<Object, TypeInference.DataType> getXDataTypes() {
        return Collections.unmodifiableMap(xDataTypes);
    }
    
    /**
     * Returns the total number of columns on the internalDataset.
     * 
     * @return 
     */
    public int getVariableNumber() {
        return xDataTypes.size();
    }
    
    /**
     * Returns the total number of Records in the internalDataset.
     * 
     * @return 
     */
    public int getRecordNumber() {
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
     * It extracts the values of a particular column from all records and
     * stores them into an FlatDataList.
     * 
     * @param column
     * @return 
     */
    public FlatDataList extractXColumnValues(Object column) {
        FlatDataList flatDataList = new FlatDataList();
        
        for(Integer rId : this) {
            Record r = recordList.get(rId);
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
    public FlatDataList extractYValues() {
        FlatDataList flatDataList = new FlatDataList();
        
        for(Integer rId : this) {
            Record r = recordList.get(rId);
            flatDataList.add(r.getY());
        }
        
        return flatDataList;
    }
    
    /**
     * It extracts the values of a particular column and groups them by the 
     * Response variable Y. This method is usually used when we 
     * have categories in Y and we want the values of a particular column to be
     * extracted for each category.
     * 
     * @param column
     * @return 
     */
    public TransposeDataList extractXColumnValuesByY(Object column) {
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
     * It generates and returns a new Dataset which contains a subset of this Dataset. 
     * All the Records of the returned Dataset are copies of the original Records. 
     * The method is used for k-fold cross validation and sampling. Note that the 
     * Records in the new Dataset have DIFFERENT ids from the original ones.
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
     * Returns a deep copy of the Dataset. 
     * 
     * @return 
     */
    public Dataset copy() {
        Dataset d = new Dataset(dbConf);
        
        for(Integer rId : this) {
            d.add(recordList.get(rId)); 
        }        
        return d;
    }
    
    /**
     * Returns a particular Record using its id.
     * 
     * @param id
     * @return 
     */
    public Record get(Integer id) {
        return recordList.get(id);
    }
    
    /**
     * Removes completely a list of columns from the dataset. The meta-data of the 
     * Dataset are updated.
     * 
     * @param columnSet
     */
    public void removeColumns(Set<Object> columnSet) {  
        columnSet.retainAll(xDataTypes.keySet()); //keep only those columns that are already known to the Meta data of the Dataset
        
        if(columnSet.isEmpty()) {
            return;
        }
        
        //remove all the columns from the Meta data
        xDataTypes.keySet().removeAll(columnSet);

        for(Integer rId : this) {
            Record r = recordList.get(rId);
            
            AssociativeArray xData = r.getX().copy();
            int d = xData.size();
            xData.keySet().removeAll(columnSet);
            
            if(xData.size()!=d) {
                r = new Record(xData, r.getY(), r.getYPredicted(), r.getYPredictedProbabilities());
                recordList.put(rId, r);
            }
        }
        
    }
    
    /**
     * Updates the meta data of the Dataset using the provided Record. 
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
    
    /**
     * It forces the recalculation of Meta data using the Records of the dataset.
     */
    public void recalculateMeta() {
        yDataType = null;
        xDataTypes.clear();
        for(Integer rId: this) {
            updateMeta(recordList.get(rId));
        }
    }
    
    /**
     * Adds a record in the Dataset and updates the Meta data. The method returns 
     * the id of the record.
     * 
     * @param r
     * @return 
     */
    public Integer add(Record r) {
        Integer newId=_add(r);
        updateMeta(r);
        return newId;
    }
    
    /**
     * Adds the record in the dataset without updating the Meta. The add method 
     * returns the id of the new record.
     * 
     * @param r
     * @return 
     */
    private Integer _add(Record r) {
        Integer newId=(Integer) recordList.size();
        recordList.put(newId, r);
        return newId;
    }
    
    /**
     * Sets the record of a particular id in the dataset. The record must already
     * exists within the dataset or an IndexOutOfBoundsException is thrown.
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
    public Integer set(Integer rId, Record r) {
        _set(rId, r);
        updateMeta(r);
        return rId;
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
    public void _set(Integer rId, Record r) {
        if(recordList.containsKey(rId)==false) {
            throw new IndexOutOfBoundsException(); //ensure that the record has already be set with add()
        }
        recordList.put(rId, r);
    }
    
    /**
     * Erases the Dataset and removes all internal variables. Once you erase a
     * dataset, the instance can no longer be used.
     */
    public void erase() {
        dbc.dropBigMap("tmp_xColumnTypes", xDataTypes);
        dbc.dropBigMap("tmp_recordList", recordList);
        dbc.dropDatabase();
        
        dbName = null;
        dbc = null;
        dbConf = null;
        
        //Ensures that the Dataset can't be used after erase() is called.
        yDataType = null;
        xDataTypes = null;
        recordList = null;
    }
    
    /**
     * Implementing read-only iterator on the Record IDs to use it in loops.
     * 
     * @return 
     */
    @Override
    public Iterator<Integer> iterator() {
        //Instead of looping through the recordList keyset we exploit the way
        //that the Dataset builds the Ids and instead we loop through them using
        //a counter. If the construction of the Dataset changes, this optimization
        //should be removed.
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
