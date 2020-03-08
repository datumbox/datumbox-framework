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
package com.datumbox.framework.core.common.dataobjects;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.concurrency.ForkJoinStream;
import com.datumbox.framework.common.concurrency.StreamMethods;
import com.datumbox.framework.common.concurrency.ThreadMethods;
import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.common.dataobjects.FlatDataList;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.common.interfaces.Copyable;
import com.datumbox.framework.core.common.interfaces.Extractable;
import com.datumbox.framework.core.common.interfaces.Savable;
import com.datumbox.framework.common.storage.abstracts.BigMapHolder;
import com.datumbox.framework.common.storage.interfaces.BigMap;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.common.storage.interfaces.StorageEngine.MapType;
import com.datumbox.framework.common.storage.interfaces.StorageEngine.StorageHint;
import com.datumbox.framework.common.utilities.RandomGenerator;
import com.datumbox.framework.core.common.text.StringCleaner;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * The Dataframe class stores a list of Records Objects and several meta-data. All
 * Machine Learning algorithms get as argument Dataframe objects. The class has an
 * internal static Builder class which can be used to generate Dataframe objects 
 * from Text or CSV files.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Dataframe implements Collection<Record>, Copyable<Dataframe>, Savable {

    /**
     * Internal name of the response variable.
     */
    public static final String COLUMN_NAME_Y = "~Y";

    /**
     * Internal name of the constant.
     */
    public static final String COLUMN_NAME_CONSTANT = "~CONSTANT";

    /**
     * The Builder is a utility class which can help you build Dataframe from Text files, CSV files or load it from disk.
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
         * to extract the keywords from the documents and the Storage Configuration
         * Object.
         *
         * @param textFilesMap
         * @param textExtractor
         * @param configuration
         * @return
         */
        public static Dataframe parseTextFiles(Map<Object, URI> textFilesMap, Extractable textExtractor, Configuration configuration) {
            Dataframe dataset = new Dataframe(configuration);
            Logger logger = LoggerFactory.getLogger(Dataframe.Builder.class);

            for (Map.Entry<Object, URI> entry : textFilesMap.entrySet()) {
                Object theClass = entry.getKey();
                URI datasetURI = entry.getValue();

                logger.info("Dataset Parsing {} class", theClass);

                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(datasetURI)), "UTF8"))) {
                    final int baseCounter = dataset.size(); //because we read multiple files we need to keep track of all records added earlier
                    ThreadMethods.throttledExecution(StreamMethods.enumerate(br.lines()), e -> {
                        Integer rId = baseCounter + e.getKey();
                        String line = e.getValue();

                        AssociativeArray xData = new AssociativeArray(
                                textExtractor.extract(StringCleaner.clear(line))
                        );
                        Record r = new Record(xData, theClass);

                        //we call below the recalculateMeta()
                        dataset.set(rId, r);
                    }, configuration.getConcurrencyConfiguration());
                }
                catch (IOException ex) {
                    throw new RuntimeException(ex);
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
         * the string of the record/row separator. The Storage Configuration
         * object.
         *
         * @param reader
         * @param yVariable
         * @param headerDataTypes
         * @param delimiter
         * @param quote
         * @param recordSeparator
         * @param skip
         * @param limit
         * @param configuration
         * @return
         */
        public static Dataframe parseCSVFile(Reader reader, String yVariable, LinkedHashMap<String, TypeInference.DataType> headerDataTypes,
                                             char delimiter, char quote, String recordSeparator, Long skip, Long limit, Configuration configuration) {
            Logger logger = LoggerFactory.getLogger(Dataframe.Builder.class);

            if(skip == null) {
                skip = 0L;
            }

            if(limit == null) {
                limit = Long.MAX_VALUE;
            }

            logger.info("Parsing CSV file");

            if (!headerDataTypes.containsKey(yVariable)) {
                logger.warn("WARNING: The file is missing the response variable column {}.", yVariable);
            }

            TypeInference.DataType yDataType = headerDataTypes.get(yVariable);
            Map<String, TypeInference.DataType> xDataTypes = new HashMap<>(headerDataTypes); //copy header types
            xDataTypes.remove(yVariable); //remove the response variable from xDataTypes
            Dataframe dataset = new Dataframe(configuration, yDataType, xDataTypes); //use the private constructor to pass DataTypes directly and avoid updating them on the fly


            CSVFormat format = CSVFormat
                    .RFC4180
                    .withHeader()
                    .withDelimiter(delimiter)
                    .withQuote(quote)
                    .withRecordSeparator(recordSeparator);

            try (final CSVParser parser = new CSVParser(reader, format)) {
                ThreadMethods.throttledExecution(StreamMethods.enumerate(StreamMethods.stream(parser.spliterator(), false)).skip(skip).limit(limit), e -> {
                    Integer rId = e.getKey();
                    CSVRecord row = e.getValue();

                    if (!row.isConsistent()) {
                        logger.warn("WARNING: Skipping row {} because its size does not match the header size.", row.getRecordNumber());
                    }
                    else {
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

                        Record r = new Record(xData, y);

                        //use the internal unsafe methods to avoid the update of the Metas.
                        //The Metas are already set in the construction of the Dataframe.
                        dataset._unsafe_set(rId, r);
                    }
                }, configuration.getConcurrencyConfiguration());
            }
            catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return dataset;
        }

        /**
         * It loads a dataframe that has already been stored.
         *
         * @param storageName
         * @param configuration
         * @return
         */
        public static Dataframe load(String storageName, Configuration configuration) {
            return new Dataframe(storageName, configuration);
        }

    }

    /**
     * This class stores the data of the Dataframe.
     */
    private static class Data extends BigMapHolder {
        private TypeInference.DataType yDataType = null;
        private AtomicInteger atomicNextAvailableRecordId = new AtomicInteger();

        @BigMap(keyClass=Object.class, valueClass=TypeInference.DataType.class, mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=true)
        private Map<Object, TypeInference.DataType> xDataTypes;

        @BigMap(keyClass=Integer.class, valueClass=Record.class, mapType=MapType.TREEMAP, storageHint=StorageHint.IN_DISK, concurrent=true)
        private Map<Integer, Record> records;

        /**
         * Initializes the state of the Data object.
         *
         * @param storageEngine
         */
        private Data(StorageEngine storageEngine) {
            super(storageEngine);
        }
    }

    /**
     * Contains all the data of the dataframe.
     */
    private Data data;

    /**
     * Flag that indicates whether the trainer has been saved or loaded from disk.
     */
    private boolean stored;

    /**
     * The storage engine.
     */
    private final StorageEngine storageEngine;

    /**
     * The configuration object used to create the Dataframe. It is defined as protected to be accessible by classes
     * that extend the Dataframe or the DataframeMatrix class which is on the same package.
     */
    protected final Configuration configuration;

    /**
     * This executor is used for the parallel processing of streams with custom
     * Thread pool.
     */
    private final ForkJoinStream streamExecutor;

    /**
     * Public constructor of Dataframe.
     *
     * @param configuration
     */
    public Dataframe(Configuration configuration) {
        this.configuration = configuration;
        storageEngine = this.configuration.getStorageConfiguration().createStorageEngine("dts" + RandomGenerator.getThreadLocalRandomUnseeded().nextLong());
        streamExecutor = new ForkJoinStream(this.configuration.getConcurrencyConfiguration());

        data = new Data(storageEngine);
        stored = false;
    }

    /**
     * Private constructor used by the Builder inner static class.
     *
     * @param storageName
     * @param configuration
     */
    private Dataframe(String storageName, Configuration configuration) {
        this.configuration = configuration;
        storageEngine = this.configuration.getStorageConfiguration().createStorageEngine(storageName);
        streamExecutor = new ForkJoinStream(this.configuration.getConcurrencyConfiguration());

        data = storageEngine.loadObject("data", Data.class);
        stored = true;
    }

    /**
     * Private constructor used by the Builder inner static class.
     *
     * @param configuration
     * @param yDataType
     * @param xDataTypes
     */
    private Dataframe(Configuration configuration, TypeInference.DataType yDataType, Map<String, TypeInference.DataType> xDataTypes) {
        this(configuration);
        this.data.yDataType = yDataType;
        this.data.xDataTypes.putAll(xDataTypes);
    }


    //Storage Methods

    /**
     * Saves the Dataframe to disk.
     *
     * @param storageName
     */
    public void save(String storageName) {
        //store the objects on storage
        storageEngine.saveObject("data", data);

        //rename the storage
        storageEngine.rename(storageName);

        //reload the data of the object
        data = storageEngine.loadObject("data", Data.class);

        //mark it as stored
        stored = true;
    }

    /**
     * Deletes the Dataframe and removes all internal variables. Once you delete a
     * dataset, the instance can no longer be used.
     */
    public void delete() {
        storageEngine.clear();
        _close();
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        if(stored) {
            //if the dataset is stored in disk, just close the storage
            _close();
        }
        else {
            //if not try to delete it in case temporary files remained on disk
            delete();
        }
    }

    /**
     * Closes the storage engine.
     */
    private void _close() {
        try {
            storageEngine.close();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        finally {
            //Ensures that the Dataframe can't be used after _close() is called.
            data = null;
        }
    }


    //Mandatory Collection Methods

    /**
     * Returns the total number of Records of the Dataframe.
     *
     * @return
     */
    @Override
    public int size() {
        return data.records.size();
    }

    /**
     * Checks if the Dataframe is empty.
     *
     * @return
     */
    @Override
    public boolean isEmpty() {
        return data.records.isEmpty();
    }

    /**
     * Clears all the internal Records of the Dataframe. The Dataframe can be used
     * after you clear it.
     */
    @Override
    public void clear() {
        data.yDataType = null;
        data.atomicNextAvailableRecordId.set(0);
        data.xDataTypes.clear();
        data.records.clear();
    }

    /**
     * Adds a record in the Dataframe and updates the Meta data.
     *
     * @param r
     * @return
     */
    @Override
    public boolean add(Record r) {
        addRecord(r);
        return true;
    }

    /**
     * Checks if the Record exists in the Dataframe. Note that the Record is checked only
     * for its x and y components.
     *
     * @param o
     * @return
     */
    @Override
    public boolean contains(Object o) {
        return data.records.containsValue((Record)o);
    }

    /** {@inheritDoc} */
    @Override
    public boolean addAll(Collection<? extends Record> c) {
        c.stream().forEach(r -> {
            add(r);
        });
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsAll(Collection<?> c) {
        return data.records.values().containsAll(c);
    }

    /** {@inheritDoc} */
    @Override
    public Object[] toArray() {
        Object[] array = new Object[size()];
        int i = 0;
        for(Record r : values()) {
            array[i++] = r;
        }
        return array;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
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
        return values().iterator();
    }

    /** {@inheritDoc} */
    @Override
    public Stream<Record> stream() {
        return StreamMethods.stream(values(), false);
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
    public boolean remove(Object o) {
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
    public boolean removeAll(Collection<?> c) {
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
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        for(Map.Entry<Integer, Record> e : entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            if(!c.contains(r)) {
                remove(rId);
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
    public Record remove(Integer id) {
        return data.records.remove(id);
    }

    /**
     * Returns the index of the first occurrence of the specified element in this
     * Dataframe, or null if this Dataframe does not contain the element.
     * WARNING: The Records are checked only for their X and Y values, not for
     * the yPredicted and yPredictedProbabilities values.
     *
     * @param o
     * @return
     */
    public Integer indexOf(Record o) {
        if(o!=null) {
            for(Map.Entry<Integer, Record> e : entries()) {
                Integer rId = e.getKey();
                Record r = e.getValue();
                if(o.equals(r)) {
                    return rId;
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
    public Record get(Integer id) {
        return data.records.get(id);
    }

    /**
     * Adds a Record in the Dataframe and returns its id.
     *
     * @param r
     * @return
     */
    public Integer addRecord(Record r) {
        Integer rId = _unsafe_add(r);
        updateMeta(r);
        return rId;
    }

    /**
     * Sets the record of a particular id in the dataset. If the record does not
     * exist it will be added with the specific id and the next added record will
     * have as id the next integer.
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
        _unsafe_set(rId, r);
        updateMeta(r);
        return rId;
    }

    /**
     * Returns the total number of X columns in the Dataframe.
     *
     * @return
     */
    public int xColumnSize() {
        return data.xDataTypes.size();
    }

    /**
     * Returns the type of the response variable y.
     *
     * @return
     */
    public TypeInference.DataType getYDataType() {
        return data.yDataType;
    }

    /**
     * Returns an Map with column names as index and DataTypes as values.
     *
     * @return
     */
    public Map<Object, TypeInference.DataType> getXDataTypes() {
        return Collections.unmodifiableMap(data.xDataTypes);
    }

    /**
     * It extracts the values of a particular column from all records and
     * stores them into an FlatDataList.
     *
     * @param column
     * @return
     */
    public FlatDataList getXColumn(Object column) {
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
    public FlatDataList getYColumn() {
        FlatDataList flatDataList = new FlatDataList();

        for(Record r : values()) {
            flatDataList.add(r.getY());
        }

        return flatDataList;
    }

    /**
     * Removes completely a list of columns from the dataset. The meta-data of
     * the Dataframe are updated. The method internally uses threads.
     *
     * @param columnSet
     */
    public void dropXColumns(Set<Object> columnSet) {
        columnSet.retainAll(data.xDataTypes.keySet()); //keep only those columns that are already known to the Meta data of the Dataframe

        if(columnSet.isEmpty()) {
            return;
        }

        //remove all the columns from the Meta data
        data.xDataTypes.keySet().removeAll(columnSet);

        streamExecutor.forEach(StreamMethods.stream(entries(), true), e -> {
            Integer rId = e.getKey();
            Record r = e.getValue();

            AssociativeArray xData = r.getX().copy();
            boolean modified = xData.keySet().removeAll(columnSet);

            if(modified) {
                Record newR = new Record(xData, r.getY(), r.getYPredicted(), r.getYPredictedProbabilities());

                //safe to call in this context. we already updated the meta when we modified the xDataTypes
                _unsafe_set(rId, newR);
            }
        });

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
    public Dataframe getSubset(FlatDataList idsCollection) {
        Dataframe d = new Dataframe(configuration);

        for(Object id : idsCollection) {
            d.add(get((Integer)id));
        }
        return d;
    }

    /**
     * It forces the recalculation of Meta data using the Records of the dataset.
     */
    public void recalculateMeta() {
        data.yDataType = null;
        data.xDataTypes.clear();
        for(Record r : values()) {
            updateMeta(r);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Dataframe copy() {
        Dataframe d = new Dataframe(configuration);

        for(Map.Entry<Integer, Record> e : entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            d.set(rId, r);
        }
        return d;
    }

    /**
     * Returns a read-only Iterable on the keys and Records of the Dataframe.
     *
     * @return
     */
    public Iterable<Map.Entry<Integer, Record>> entries() {
        return () -> new Iterator<Map.Entry<Integer, Record>>() {
            private final Iterator<Map.Entry<Integer, Record>> it = data.records.entrySet().iterator();

            /** {@inheritDoc} */
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            /** {@inheritDoc} */
            @Override
            public Map.Entry<Integer, Record> next() {
                return it.next();
            }

            /** {@inheritDoc} */
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
            private final Iterator<Integer> it = data.records.keySet().iterator();

            /** {@inheritDoc} */
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            /** {@inheritDoc} */
            @Override
            public Integer next() {
                return it.next();
            }

            /** {@inheritDoc} */
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
        return () -> new Iterator<Record>(){
            private final Iterator<Record> it = data.records.values().iterator();

            /** {@inheritDoc} */
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            /** {@inheritDoc} */
            @Override
            public Record next() {
                return it.next();
            }

            /** {@inheritDoc} */
            @Override
            public void remove() {
                throw new UnsupportedOperationException("This is a read-only iterator, remove operation is not supported.");
            }
        };
    }

    /**
     * Sets the record in a particular position in the dataset, WITHOUT updating
     * the internal meta-info and returns the previous value (null if not existed).
     * This method is similar to set() and it allows quick updates
     * on the dataset. Nevertheless it is not advised to use this method because
     * unless you explicitly call the recalculateMeta() method, the meta data
     * will be corrupted. If you do use this method, MAKE sure you perform the
     * recalculation after you are done with the updates.
     *
     * @param rId
     * @param r
     * @return
     */
    public Record _unsafe_set(Integer rId, Record r) {
        //move ahead the next id
        data.atomicNextAvailableRecordId.updateAndGet(x -> (x<rId)?Math.max(x+1,rId+1):x);

        return data.records.put(rId, r);
    }

    /**
     * Adds the record in the dataset without updating the Meta. The add method
     * returns the id of the new record.
     *
     * @param r
     * @return
     */
    private Integer _unsafe_add(Record r) {
        Integer newId = data.atomicNextAvailableRecordId.getAndIncrement();
        data.records.put(newId, r);

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

            if(value!=null) {
                data.xDataTypes.putIfAbsent(column, TypeInference.getDataType(value));
            }
        }

        if(data.yDataType == null) {
            Object value = r.getY();
            if(value!=null) {
                data.yDataType = TypeInference.getDataType(r.getY());
            }
        }
    }

}