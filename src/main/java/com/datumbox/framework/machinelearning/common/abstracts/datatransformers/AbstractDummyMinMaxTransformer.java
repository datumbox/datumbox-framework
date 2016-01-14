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
package com.datumbox.framework.machinelearning.common.abstracts.datatransformers;

import com.datumbox.common.concurrency.ForkJoinStream;
import com.datumbox.common.concurrency.StreamMethods;
import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.MapType;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.StorageHint;
import com.datumbox.development.switchers.SynchronizedBlocks;
import com.datumbox.framework.machinelearning.common.interfaces.Parallelizable;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Base class for Dummy and MinMax Transformers.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public abstract class AbstractDummyMinMaxTransformer extends AbstractTransformer<AbstractDummyMinMaxTransformer.ModelParameters, AbstractDummyMinMaxTransformer.TrainingParameters> implements Parallelizable {
    
    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractTransformer.AbstractModelParameters {
        private static final long serialVersionUID = 1L;
        
        /**
         * The reference levels of each categorical variable.
         */
        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=true)
        private Map<Object, Object> referenceLevels;
        
        /**
         * The minimum value of each numerical variable.
         */
        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=true)
        private Map<Object, Double> minColumnValues;
        
        /**
         * The maximum value of each numerical variable.
         */
        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=true)
        private Map<Object, Double> maxColumnValues;

        /** 
         * @param dbc
         * @see com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer.AbstractModelParameters#AbstractModelParameters(com.datumbox.common.persistentstorage.interfaces.DatabaseConnector) 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }

        /**
         * Getter for the reference levels of the categorical variables.
         * 
         * @return 
         */
        public Map<Object, Object> getReferenceLevels() {
            return referenceLevels;
        }
        
        /**
         * Setter for the reference levels of the categorical variables.
         * 
         * @param referenceLevels 
         */
        protected void setReferenceLevels(Map<Object, Object> referenceLevels) {
            this.referenceLevels = referenceLevels;
        }
        
        /**
         * Getter for the minimum values of the columns.
         * 
         * @return 
         */
        public Map<Object, Double> getMinColumnValues() {
            return minColumnValues;
        }
        
        /**
         * Setter for the minimum values of the columns.
         * 
         * @param minColumnValues 
         */
        protected void setMinColumnValues(Map<Object, Double> minColumnValues) {
            this.minColumnValues = minColumnValues;
        }
        
        /**
         * Getter for the maximum values of the columns.
         * 
         * @return 
         */
        public Map<Object, Double> getMaxColumnValues() {
            return maxColumnValues;
        }

        /**
         * Setter for the maximum values of the columns.
         * 
         * @param maxColumnValues 
         */
        protected void setMaxColumnValues(Map<Object, Double> maxColumnValues) {
            this.maxColumnValues = maxColumnValues;
        }
        
    }
    
    /** {@inheritDoc} */
    public static class TrainingParameters extends AbstractTransformer.AbstractTrainingParameters {
        private static final long serialVersionUID = 1L;
        
    }
    
    /** 
     * @param dbName
     * @param dbConf
     * @see com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer#AbstractTrainer(java.lang.String, com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration, java.lang.Class, java.lang.Class...)  
     */
    protected AbstractDummyMinMaxTransformer(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, AbstractDummyMinMaxTransformer.ModelParameters.class, AbstractDummyMinMaxTransformer.TrainingParameters.class);
        streamExecutor = new ForkJoinStream();
    }
    
    private boolean parallelized = true;
    protected final ForkJoinStream streamExecutor;
    
    /** {@inheritDoc} */
    @Override
    public boolean isParallelized() {
        return parallelized;
    }

    /** {@inheritDoc} */
    @Override
    public void setParallelized(boolean parallelized) {
        this.parallelized = parallelized;
    }
    
    /**
     * Learns the normalization parameters for the X data.
     * 
     * @param dataset
     * @param minColumnValues
     * @param maxColumnValues 
     */
    protected void fitX(Dataframe dataset, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        streamExecutor.forEach(StreamMethods.stream(dataset.getXDataTypes().entrySet().stream(), isParallelized()), entry -> {
            Object column = entry.getKey();
            TypeInference.DataType columnType = entry.getValue();

            if(columnType==TypeInference.DataType.NUMERICAL) {
                FlatDataList columnValues = dataset.getXColumn(column);
                Double max = Descriptives.max(columnValues.toFlatDataCollection());
                Double min = Descriptives.min(columnValues.toFlatDataCollection());

                minColumnValues.put(column, min); //each tread takes a unique key and the map is concurrent
                maxColumnValues.put(column, max); //each tread takes a unique key and the map is concurrent
            } //do nothing for non-numeric columns
        }); //do nothing for the response variable Y
    }
    
    /**
     * Normalizes the X data.
     * 
     * @param dataset
     * @param minColumnValues
     * @param maxColumnValues 
     */
    protected void normalizeX(Dataframe dataset, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        streamExecutor.forEach(StreamMethods.stream(dataset.entries(), isParallelized()), e -> {
            Integer rId = e.getKey();
            Record r = e.getValue();
            AssociativeArray xData = r.getX().copy();
            
            boolean modified = false;
            for(Map.Entry<Object,Double> entry : minColumnValues.entrySet()) {
                Object column = entry.getKey();
                Double value = xData.getDouble(column);
                if(value==null) { //if we have a missing value don't perform any normalization
                    continue;
                }
                
                Double min = entry.getValue();
                Double max = maxColumnValues.get(column);
                
                //it is important how we will handle 0 normalized values because
                //0-valued features are considered inactive.
                double normalizedValue;
                if(min.equals(max)) {
                    normalizedValue = (min>0.0)?1.0:0.0; //set it 0.0 ONLY if the feature is always inactive and 1.0 if it has a non-zero value
                }
                else {
                    normalizedValue = (value-min)/(max-min);
                }
                
                xData.put(column, normalizedValue);
                modified = true;
            }
            
            if(modified) {
                Record newR = new Record(xData, r.getY(), r.getYPredicted(), r.getYPredictedProbabilities());
                
                //no modification on the actual columns takes place, safe to do.
                if(SynchronizedBlocks.WITHOUT_SYNCHRONIZED.isActivated()) {
                    dataset._unsafe_set(rId, newR); 
                }
                else {
                    synchronized(dataset) {
                        dataset._unsafe_set(rId, newR); 
                    }                    
                }
            }
        });
    }
    
    /**
     * Denormalizes the X data.
     * 
     * @param dataset
     * @param minColumnValues
     * @param maxColumnValues 
     */
    protected void denormalizeX(Dataframe dataset, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        streamExecutor.forEach(StreamMethods.stream(dataset.entries(), isParallelized()), e -> {
            Integer rId = e.getKey();
            Record r = e.getValue();
            AssociativeArray xData = r.getX().copy();
            
            boolean modified = false;
            for(Map.Entry<Object,Double> entry : minColumnValues.entrySet()) {
                Object column = entry.getKey();
                Double value = xData.getDouble(column);
                if(value==null) { //if we have a missing value don't perform any denormalization
                    continue;
                }
                
                Double min = entry.getValue();
                Double max = maxColumnValues.get(column);
                
                if(min.equals(max)) {
                    xData.put(column, min);
                }
                else {
                    xData.put(column, value*(max-min) + min);
                }
                modified = true;
            }
            
            if(modified) {
                Record newR = new Record(xData, r.getY(), r.getYPredicted(), r.getYPredictedProbabilities());
                
                //no modification on the actual columns takes place, safe to do.
                if(SynchronizedBlocks.WITHOUT_SYNCHRONIZED.isActivated()) {
                    dataset._unsafe_set(rId, newR); 
                }
                else {
                    synchronized(dataset) {
                        dataset._unsafe_set(rId, newR); 
                    }                    
                }
            }
        });
    }
    
    /**
     * Learns the normalization parameters for the Y variable.
     * 
     * @param dataset
     * @param minColumnValues
     * @param maxColumnValues 
     */
    protected void fitY(Dataframe dataset, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        if(dataset.getYDataType()==TypeInference.DataType.NUMERICAL) {
            //if this is numeric normalize it

            FlatDataList columnValues = dataset.getYColumn();
            Double max = Descriptives.max(columnValues.toFlatDataCollection());
            Double min = Descriptives.min(columnValues.toFlatDataCollection());

            minColumnValues.put(Dataframe.COLUMN_NAME_Y, min);
            maxColumnValues.put(Dataframe.COLUMN_NAME_Y, max);
        }
    }
    
    /**
     * Normalizes the Y variable.
     * 
     * @param dataset
     * @param minColumnValues
     * @param maxColumnValues 
     */
    protected void normalizeY(Dataframe dataset, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        if(dataset.getYDataType()==TypeInference.DataType.NUMERICAL) {
            
            streamExecutor.forEach(StreamMethods.stream(dataset.entries(), isParallelized()), e -> {
                Integer rId = e.getKey();
                Record r = e.getValue();
                Double value = TypeInference.toDouble(r.getY());
                
                if(value!=null) { //if we have a missing value don't perform any normalization
                    //do the same for the response variable Y
                    Double min = minColumnValues.get(Dataframe.COLUMN_NAME_Y);
                    Double max = maxColumnValues.get(Dataframe.COLUMN_NAME_Y);

                    //it is important how we will handle 0 normalized values because
                    //0-valued features are considered inactive.
                    double normalizedValue;
                    if(min.equals(max)) {
                        normalizedValue = (min!=0.0)?1.0:0.0; //set it 0.0 ONLY if the feature is always inactive and 1.0 if it has a non-zero value
                    }
                    else {
                        normalizedValue = (value-min)/(max-min);
                    }

                    Record newR = new Record(r.getX(), normalizedValue, r.getYPredicted(), r.getYPredictedProbabilities());

                    //no modification on the actual columns takes place, safe to do.
                    if(SynchronizedBlocks.WITHOUT_SYNCHRONIZED.isActivated()) {
                        dataset._unsafe_set(rId, newR); 
                    }
                    else {
                        synchronized(dataset) {
                            dataset._unsafe_set(rId, newR); 
                        }                    
                    }
                }
                
            });
        }
    }
    
    /**
     * Denormalizes the Y variable.
     * 
     * @param dataset
     * @param minColumnValues
     * @param maxColumnValues 
     */
    protected void denormalizeY(Dataframe dataset, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        TypeInference.DataType dataType = dataset.getYDataType();
        if(dataType==TypeInference.DataType.NUMERICAL || dataType==null) {

            streamExecutor.forEach(StreamMethods.stream(dataset.entries(), isParallelized()), e -> {
                Integer rId = e.getKey();
                Record r = e.getValue();
                
                //do the same for the response variable Y
                Double min = minColumnValues.get(Dataframe.COLUMN_NAME_Y);
                Double max = maxColumnValues.get(Dataframe.COLUMN_NAME_Y);
                
                Object denormalizedY = null;
                Object denormalizedYPredicted = null;
                if(min.equals(max)) {
                    if(r.getY()!=null) {
                        denormalizedY = min;
                    }
                    if(r.getYPredicted()!=null) {
                        denormalizedYPredicted = min;
                    }
                }
                else {
                    if(r.getY()!=null) {
                        denormalizedY = TypeInference.toDouble(r.getY())*(max-min) + min;
                    }
                    
                    Double YPredicted = TypeInference.toDouble(r.getYPredicted());
                    if(YPredicted!=null) {
                        denormalizedYPredicted = YPredicted*(max-min) + min;
                    }
                }

                Record newR = new Record(r.getX(), denormalizedY, denormalizedYPredicted, r.getYPredictedProbabilities());

                //no modification on the actual columns takes place, safe to do.
                if(SynchronizedBlocks.WITHOUT_SYNCHRONIZED.isActivated()) {
                    dataset._unsafe_set(rId, newR); 
                }
                else {
                    synchronized(dataset) {
                        dataset._unsafe_set(rId, newR); 
                    }                    
                }
            });
        }
    }
    
    /**
     * Learns the reference levels of the categorical variables.
     * 
     * @param dataset
     * @param referenceLevels 
     */
    protected void fitDummy(Dataframe dataset, Map<Object, Object> referenceLevels) {
        Map<Object, TypeInference.DataType> columnTypes = dataset.getXDataTypes();
        
        //find the referenceLevels for each categorical variable
        for(Record r : dataset) {
            for(Map.Entry<Object, Object> entry: r.getX().entrySet()) {
                Object column = entry.getKey();
                if(covert2dummy(columnTypes.get(column))) { 
                    referenceLevels.putIfAbsent(column, entry.getValue()); //This Map is an implementation of ConcurrentHashMap and we don't need a synchronized is needed.
                }
            }
        }
    }
    
    /**
     * Transforms the categorical variables into dummy (boolean) variables.
     * 
     * @param dataset
     * @param referenceLevels 
     */
    protected void transformDummy(Dataframe dataset, Map<Object, Object> referenceLevels) {

        Map<Object, TypeInference.DataType> columnTypes = dataset.getXDataTypes();
        
        //Replace variables with dummy versions
        streamExecutor.forEach(StreamMethods.stream(dataset.entries(), isParallelized()), e -> {
            Integer rId = e.getKey();
            Record r = e.getValue();
            
            AssociativeArray xData = r.getX().copy();
            
            boolean modified = false;
            for(Object column : r.getX().keySet()) {
                if(covert2dummy(columnTypes.get(column))==false) { 
                    continue;
                }
                Object value = xData.remove(column); //remove the original column
                modified = true;
                
                Object referenceLevel= referenceLevels.get(column);
                
                if(referenceLevel != null && //not unknown variable
                   !referenceLevel.equals(value)) { //not equal to reference level
                    
                    //create a new column
                    List<Object> newColumn = Arrays.<Object>asList(column,value);
                    
                    //add a new dummy variable for this column-value combination
                    xData.put(newColumn, true); 
                }
            }
            
            if(modified) {
                Record newR = new Record(xData, r.getY(), r.getYPredicted(), r.getYPredictedProbabilities());

                //we call below the recalculateMeta()
                if(SynchronizedBlocks.WITHOUT_SYNCHRONIZED.isActivated()) {
                    dataset._unsafe_set(rId, newR); 
                }
                else {
                    synchronized(dataset) {
                        dataset._unsafe_set(rId, newR); 
                    }                    
                }
            }
        });
        
        //Reset Meta info
        dataset.recalculateMeta(); 
    }
    
    /**
     * Checks whether the variable should be converted into dummy (boolean). Only
     * categorical and ordinal values are converted.
     * 
     * @param columnType
     * @return 
     */
    private boolean covert2dummy(TypeInference.DataType columnType) {
        return columnType==TypeInference.DataType.CATEGORICAL || columnType==TypeInference.DataType.ORDINAL;
    }
    
}
