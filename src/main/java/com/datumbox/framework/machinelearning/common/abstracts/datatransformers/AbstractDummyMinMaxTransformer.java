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
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Base class for Dummy and MinMax Transformers.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public abstract class AbstractDummyMinMaxTransformer extends AbstractTransformer<AbstractDummyMinMaxTransformer.ModelParameters, AbstractDummyMinMaxTransformer.TrainingParameters> {
    
    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractTransformer.AbstractModelParameters {
        private static final long serialVersionUID = 1L;
        
        /**
         * The reference levels of each categorical variable.
         */
        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=false)
        private Map<Object, Object> referenceLevels;
        
        /**
         * The minimum value of each numerical variable.
         */
        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=false)
        private Map<Object, Double> minColumnValues;
        
        /**
         * The maximum value of each numerical variable.
         */
        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=false)
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
    }
    
    /**
     * Learns the normalization parameters for the X data.
     * 
     * @param data
     * @param minColumnValues
     * @param maxColumnValues 
     */
    protected static void fitX(Dataframe data, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        
        for(Map.Entry<Object, TypeInference.DataType> entry : data.getXDataTypes().entrySet()) {
            Object column = entry.getKey();
            TypeInference.DataType columnType = entry.getValue();

            if(columnType==TypeInference.DataType.NUMERICAL) {
                FlatDataList columnValues = data.getXColumn(column);
                Double max = Descriptives.max(columnValues.toFlatDataCollection());
                Double min = Descriptives.min(columnValues.toFlatDataCollection());

                minColumnValues.put(column, min);
                maxColumnValues.put(column, max);
            }
            else {
                //do nothing for non-numeric columns
            }
        }

        //do nothing for the response variable Y
    }
    
    /**
     * Normalizes the X data.
     * 
     * @param data
     * @param minColumnValues
     * @param maxColumnValues 
     */
    protected static void normalizeX(Dataframe data, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        for(Map.Entry<Integer, Record> e : data.entries()) {
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
                data._unsafe_set(rId, new Record(xData, r.getY(), r.getYPredicted(), r.getYPredictedProbabilities())); //no modification on the actula columns takes place, safe to do.
            }
        }
    }
    
    /**
     * Denormalizes the X data.
     * 
     * @param data
     * @param minColumnValues
     * @param maxColumnValues 
     */
    protected static void denormalizeX(Dataframe data, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        for(Map.Entry<Integer, Record> e : data.entries()) {
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
                data._unsafe_set(rId, new Record(xData, r.getY(), r.getYPredicted(), r.getYPredictedProbabilities())); //no modification on the actula columns takes place, safe to do.
            }
        }
    }
    
    /**
     * Learns the normalization parameters for the Y variable.
     * 
     * @param data
     * @param minColumnValues
     * @param maxColumnValues 
     */
    protected static void fitY(Dataframe data, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        if(data.getYDataType()==TypeInference.DataType.NUMERICAL) {
            //if this is numeric normalize it

            FlatDataList columnValues = data.getYColumn();
            Double max = Descriptives.max(columnValues.toFlatDataCollection());
            Double min = Descriptives.min(columnValues.toFlatDataCollection());

            minColumnValues.put(Dataframe.COLUMN_NAME_Y, min);
            maxColumnValues.put(Dataframe.COLUMN_NAME_Y, max);
        }
    }
    
    /**
     * Normalizes the Y variable.
     * 
     * @param data
     * @param minColumnValues
     * @param maxColumnValues 
     */
    protected static void normalizeY(Dataframe data, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        if(data.isEmpty()) {
            return;
        }
        
        if(data.getYDataType()==TypeInference.DataType.NUMERICAL) {
            
            for(Map.Entry<Integer, Record> e : data.entries()) {
                Integer rId = e.getKey();
                Record r = e.getValue();
                Double value = TypeInference.toDouble(r.getY());
                if(value==null) { //if we have a missing value don't perform any normalization
                    continue;
                }
                
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
                
                data._unsafe_set(rId, new Record(r.getX(), normalizedValue, r.getYPredicted(), r.getYPredictedProbabilities()));
            }
        }
    }
    
    /**
     * Denormalizes the Y variable.
     * 
     * @param data
     * @param minColumnValues
     * @param maxColumnValues 
     */
    protected static void denormalizeY(Dataframe data, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        
        if(data.isEmpty()) {
            return;
        }
        
        TypeInference.DataType dataType = data.getYDataType();
        if(dataType==TypeInference.DataType.NUMERICAL || dataType==null) {

            for(Map.Entry<Integer, Record> e : data.entries()) {
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
                
                data._unsafe_set(rId, new Record(r.getX(), denormalizedY, denormalizedYPredicted, r.getYPredictedProbabilities()));
            }
        }
    }
    
    /**
     * Learns the reference levels of the categorical variables.
     * 
     * @param data
     * @param referenceLevels 
     */
    protected static void fitDummy(Dataframe data, Map<Object, Object> referenceLevels) {
        Map<Object, TypeInference.DataType> columnTypes = data.getXDataTypes();

        //find the referenceLevels for each categorical variable
        for(Record r : data) {
            for(Map.Entry<Object, Object> entry: r.getX().entrySet()) {
                Object column = entry.getKey();
                if(referenceLevels.containsKey(column)==false) { //already set?
                    if(covert2dummy(columnTypes.get(column))==false) { 
                        continue; //only ordinal and categorical are converted into dummyvars
                    }
                    Object value = entry.getValue();
                    referenceLevels.put(column, value);
                }
            }
        }
    }
    
    /**
     * Transforms the categorical variables into dummy (boolean) variables.
     * 
     * @param data
     * @param referenceLevels 
     */
    protected static void transformDummy(Dataframe data, Map<Object, Object> referenceLevels) {

        Map<Object, TypeInference.DataType> columnTypes = data.getXDataTypes();
        
        //Replace variables with dummy versions
        for(Map.Entry<Integer, Record> e : data.entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            
            AssociativeArray xData = r.getX().copy();
            
            boolean modified = false;
            for(Object column : r.getX().keySet()) {
                if(covert2dummy(columnTypes.get(column))==false) { 
                    continue;
                }
                Object value = xData.get(column);
                
                xData.remove(column); //remove the original column
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
                data._unsafe_set(rId, new Record(xData, r.getY(), r.getYPredicted(), r.getYPredictedProbabilities()));
            }
        }
        
        //Reset Meta info
        data.recalculateMeta(); //call the recalculate because we used _unsafe_set()
    }
    
    /**
     * Checks whether the variable should be converted into dummy (boolean).
     * 
     * @param columnType
     * @return 
     */
    private static boolean covert2dummy(TypeInference.DataType columnType) {
        return columnType==TypeInference.DataType.CATEGORICAL || columnType==TypeInference.DataType.ORDINAL;
    }
    
}
