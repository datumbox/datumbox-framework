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
package com.datumbox.framework.machinelearning.common.bases.datatransformation;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.TypeConversions;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public abstract class BaseDummyMinMaxTransformer extends DataTransformer<BaseDummyMinMaxTransformer.ModelParameters, BaseDummyMinMaxTransformer.TrainingParameters> {
    
    public static class ModelParameters extends DataTransformer.ModelParameters {
            
        @BigMap
        protected Map<Object, Object> referenceLevels;
        
        @BigMap
        protected Map<Object, Double> minColumnValues;

        @BigMap
        protected Map<Object, Double> maxColumnValues;

        public ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }

        public Map<Object, Object> getReferenceLevels() {
            return referenceLevels;
        }

        public void setReferenceLevels(Map<Object, Object> referenceLevels) {
            this.referenceLevels = referenceLevels;
        }

        public Map<Object, Double> getMinColumnValues() {
            return minColumnValues;
        }

        public void setMinColumnValues(Map<Object, Double> minColumnValues) {
            this.minColumnValues = minColumnValues;
        }

        public Map<Object, Double> getMaxColumnValues() {
            return maxColumnValues;
        }

        public void setMaxColumnValues(Map<Object, Double> maxColumnValues) {
            this.maxColumnValues = maxColumnValues;
        }
        
        
    }
    
    public static class TrainingParameters extends DataTransformer.TrainingParameters {
        
    }

    protected BaseDummyMinMaxTransformer(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, BaseDummyMinMaxTransformer.ModelParameters.class, BaseDummyMinMaxTransformer.TrainingParameters.class);
    }
    
    protected static void fitX(Dataset data, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        
        for(Map.Entry<Object, Dataset.ColumnType> entry : data.getColumns().entrySet()) {
            Object column = entry.getKey();
            Dataset.ColumnType columnType = entry.getValue();

            if(columnType==Dataset.ColumnType.NUMERICAL) {
                FlatDataList columnValues = data.extractColumnValues(column);
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
    
    protected static void normalizeX(Dataset data, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        for(Integer rId : data) {
            Record r = data.get(rId);
            AssociativeArray xData = new AssociativeArray(r.getX());
            
            boolean modified = false;
            for(Object column : minColumnValues.keySet()) {
                Double value = xData.getDouble(column);
                if(value==null) { //if we have a missing value don't perform any normalization
                    continue;
                }
                
                Double min = minColumnValues.get(column);
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
                r = new Record(xData, r.getY(), r.getYPredicted(), r.getYPredictedProbabilities());
                data.set(rId, r);
            }
        }
    }
    
    protected static void denormalizeX(Dataset data, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        for(Integer rId : data) {
            Record r = data.get(rId);
            AssociativeArray xData = new AssociativeArray(r.getX());
            
            boolean modified = false;
            for(Object column : minColumnValues.keySet()) {
                Double value = xData.getDouble(column);
                if(value==null) { //if we have a missing value don't perform any denormalization
                    continue;
                }
                
                Double min = minColumnValues.get(column);
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
                r = new Record(xData, r.getY(), r.getYPredicted(), r.getYPredictedProbabilities());
                data.set(rId, r);
            }
        }
    }
    
    protected static void fitY(Dataset data, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        if(data.isEmpty()) {
            return;
        }
        
        //check if the first record has numeric value on response variable Y
        Dataset.ColumnType columnType = Dataset.value2ColumnType(data.get(data.iterator().next()).getY());

        if(columnType==Dataset.ColumnType.NUMERICAL) {
            //if this is numeric normalize it

            FlatDataList columnValues = data.extractYValues();
            Double max = Descriptives.max(columnValues.toFlatDataCollection());
            Double min = Descriptives.min(columnValues.toFlatDataCollection());

            minColumnValues.put(Dataset.YColumnName, min);
            maxColumnValues.put(Dataset.YColumnName, max);
        }
    }
    
    protected static void normalizeY(Dataset data, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        if(data.isEmpty()) {
            return;
        }
        
        Dataset.ColumnType columnType = Dataset.value2ColumnType(data.get(data.iterator().next()).getY());
        
        if(columnType==Dataset.ColumnType.NUMERICAL) {
            
            for(Integer rId : data) {
                Record r = data.get(rId);
                Double value = TypeConversions.toDouble(r.getY());
                if(value==null) { //if we have a missing value don't perform any normalization
                    continue;
                }
                
                //do the same for the response variable Y
                Double min = minColumnValues.get(Dataset.YColumnName);
                Double max = maxColumnValues.get(Dataset.YColumnName);
                
                //it is important how we will handle 0 normalized values because
                //0-valued features are considered inactive.
                double normalizedValue;
                if(min.equals(max)) {
                    normalizedValue = (min!=0.0)?1.0:0.0; //set it 0.0 ONLY if the feature is always inactive and 1.0 if it has a non-zero value
                }
                else {
                    normalizedValue = (value-min)/(max-min);
                }
                
                data.set(rId, new Record(r.getX(), normalizedValue, r.getYPredicted(), r.getYPredictedProbabilities()));
            }
        }
    }
    
    protected static void denormalizeY(Dataset data, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        
        if(data.isEmpty()) {
            return;
        }
        
        Dataset.ColumnType columnType = Dataset.value2ColumnType(data.get(data.iterator().next()).getY());
        
        if(columnType==Dataset.ColumnType.NUMERICAL) {
            
            for(Integer rId : data) {
                Record r = data.get(rId);
                
                //do the same for the response variable Y
                Double min = minColumnValues.get(Dataset.YColumnName);
                Double max = maxColumnValues.get(Dataset.YColumnName);
                
                Object denormalizedY = null;
                Object denormalizedYPredicted = null;
                if(min.equals(max)) {
                    denormalizedY = min;
                    if(r.getYPredicted()!=null) {
                        denormalizedYPredicted = min;
                    }
                }
                else {
                    denormalizedY = TypeConversions.toDouble(r.getY())*(max-min) + min;
                    
                    Double YPredicted = TypeConversions.toDouble(r.getYPredicted());
                    if(YPredicted!=null) {
                        denormalizedYPredicted = YPredicted*(max-min) + min;
                    }
                }
                
                data.set(rId, new Record(r.getX(), denormalizedY, denormalizedYPredicted, r.getYPredictedProbabilities()));
            }
        }
    }
    
    private static boolean covert2dummy(Dataset.ColumnType columnType) {
        return columnType==Dataset.ColumnType.CATEGORICAL || columnType==Dataset.ColumnType.ORDINAL;
    }
    
    protected static void extractDummies(Dataset data, Map<Object, Object> referenceLevels) {

        Map<Object, Dataset.ColumnType> columnTypes = data.getColumns();
        if(referenceLevels.isEmpty()) {
            //Training Mode
            
            //find the referenceLevels for each categorical variable
            for(Integer rId: data) {
                Record r = data.get(rId);
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
        
        //Replace variables with dummy versions
        for(Integer rId: data) {
            Record r = data.get(rId);
            
            AssociativeArray xData = new AssociativeArray(r.getX());
            
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
                r = new Record(xData, r.getY(), r.getYPredicted(), r.getYPredictedProbabilities());
                data.set(rId, r);
            }
        }
        
        //Reset Meta info
        data.resetMeta();
    }
    
}
