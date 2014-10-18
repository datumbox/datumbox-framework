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
package com.datumbox.framework.machinelearning.common.bases.datatransformation;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureMarker;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.mongodb.morphia.annotations.Transient;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public abstract class BaseMinMaxNormalizer extends DataTransformer<BaseMinMaxNormalizer.ModelParameters, BaseMinMaxNormalizer.TrainingParameters> {
    
    public static class ModelParameters extends DataTransformer.ModelParameters {
            
        @BigDataStructureMarker
        @Transient
        protected Map<Object, Double> minColumnValues;

        @BigDataStructureMarker
        @Transient
        protected Map<Object, Double> maxColumnValues;
    
        @BigDataStructureMarker
        @Transient
        protected Map<Object, Object> referenceLevels;
        
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            super.bigDataStructureInitializer(bdsf, memoryConfiguration);
            
            BigDataStructureFactory.MapType mapType = memoryConfiguration.getMapType();
            int LRUsize = memoryConfiguration.getLRUsize();
            
            minColumnValues = bdsf.getMap("minColumnValues", mapType, LRUsize);
            maxColumnValues = bdsf.getMap("maxColumnValues", mapType, LRUsize);
            referenceLevels = bdsf.getMap("referenceLevels", mapType, LRUsize);
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

        public Map<Object, Object> getReferenceLevels() {
            return referenceLevels;
        }

        public void setReferenceLevels(Map<Object, Object> referenceLevels) {
            this.referenceLevels = referenceLevels;
        }
        
        
    }
    
    public static class TrainingParameters extends DataTransformer.TrainingParameters {
        
    }

    protected BaseMinMaxNormalizer(String dbName) {
        super(dbName, BaseMinMaxNormalizer.ModelParameters.class, BaseMinMaxNormalizer.TrainingParameters.class);
    }
    
    protected static void transformDummy(Dataset data, Map<Object, Object> referenceLevels, boolean trainingMode) {

        Map<Object, Dataset.ColumnType> newColumns = new HashMap<>();
        
        int n = data.size();
        
        Iterator<Map.Entry<Object, Dataset.ColumnType>> it = data.getColumns().entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Object, Dataset.ColumnType> entry = it.next();
            Object column = entry.getKey();
            Dataset.ColumnType columnType = entry.getValue();

            if(columnType==Dataset.ColumnType.CATEGORICAL ||
               columnType==Dataset.ColumnType.ORDINAL) { //ordinal and categorical are converted into dummyvars
                //WARNING: Afterwards we must reduce the number of levels to level-1 to avoid multicollinearity issues
                
                //Remove the old column from the column map
                it.remove();
                
                
                //create dummy variables for all the levels
                for(Record r : data) {
                    if(!r.getX().containsKey(column)) {
                        continue; //does not contain column
                    }
                    
                    Object value = r.getX().get(column);
                    
                    //remove the column from data
                    r.getX().remove(column); 
                    
                    
                    Object referenceLevelValue = referenceLevels.get(column);
                    
                    List<Object> newColumn = null;
                    if(trainingMode) {
                        if(referenceLevelValue==null) { //if we don't have a reference point add it
                            referenceLevels.put(column, value); //column/value dummy variables that are added as refernce points are ignored from the data to avoid overparametrization
                        }
                        else if(referenceLevelValue==value) { //if this is reference point ignore it
                            //do nothing
                        }
                        else {
                            //create a new column
                            newColumn = Arrays.<Object>asList(column,value);
                        }
                    }
                    else {
                        //include it in the data ONLY if it was spotted on the traning database and has value other than the reference
                        if(referenceLevelValue!=null && referenceLevelValue!=value) { 
                            newColumn = Arrays.<Object>asList(column,value);
                        }
                    }
                    
                    if(newColumn!=null) {
                        //add a new dummy variable for this column-value combination
                        r.getX().put(newColumn, true); 
                        
                        //add the new column in the list for insertion
                        newColumns.put(newColumn, Dataset.ColumnType.DUMMYVAR);
                    }
                    
                }
            }
        }
        
        //add the new columns in the dataset column map
        if(!newColumns.isEmpty()) {
            data.getColumns().putAll(newColumns);
        }
    }
    
    protected static void transformX(Dataset data, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        
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
        for(Record r : data) {
            for(Object column : minColumnValues.keySet()) {
                Double value = r.getX().getDouble(column);
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
                
                r.getX().put(column, normalizedValue);
            }
            
            //do nothing for the response variable Y
        }
    }
    
    protected static void denormalizeX(Dataset data, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        for(Record r : data) {
            for(Object column : minColumnValues.keySet()) {
                Double value = r.getX().getDouble(column);
                if(value==null) { //if we have a missing value don't perform any denormalization
                    continue;
                }
                
                Double min = minColumnValues.get(column);
                Double max = maxColumnValues.get(column);
                
                if(min.equals(max)) {
                    r.getX().put(column, min);
                }
                else {
                    r.getX().put(column, value*(max-min) + min);
                }
            }
            
            //do nothing for the response variable Y
        }
    }
    
    protected static void transformY(Dataset data, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        if(data.isEmpty()) {
            return;
        }
        
        //check if the first record has numeric value on response variable Y
        Dataset.ColumnType columnType = Dataset.value2ColumnType(data.iterator().next().getY());

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
        
        Dataset.ColumnType columnType = Dataset.value2ColumnType(data.iterator().next().getY());
        
        if(columnType==Dataset.ColumnType.NUMERICAL) {
            
            for(Record r : data) {
                Double value = Dataset.toDouble(r.getY());
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
                
                r.setY(normalizedValue);
            }
        }
    }
    
    protected static void denormalizeY(Dataset data, Map<Object, Double> minColumnValues, Map<Object, Double> maxColumnValues) {
        
        if(data.isEmpty()) {
            return;
        }
        
        Dataset.ColumnType columnType = Dataset.value2ColumnType(data.iterator().next().getY());
        
        if(columnType==Dataset.ColumnType.NUMERICAL) {
            
            for(Record r : data) {
                //do the same for the response variable Y
                Double min = minColumnValues.get(Dataset.YColumnName);
                Double max = maxColumnValues.get(Dataset.YColumnName);

                if(min.equals(max)) {
                    r.setY(min);
                    if(r.getYPredicted()!=null) {
                        r.setYPredicted(min);
                    }
                }
                else {
                    r.setY(Dataset.toDouble(r.getY())*(max-min) + min);
                    
                    Double YPredicted = Dataset.toDouble(r.getYPredicted());
                    if(YPredicted!=null) {
                        r.setYPredicted(YPredicted*(max-min) + min);
                    }
                }
            }
        }
    }
}
