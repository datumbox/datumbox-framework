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
package com.datumbox.framework.machinelearning.common.bases.datatransformation;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.TypeConversions;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
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
        for(Integer rId : data) {
            Record r = data.get(rId);
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
                
                r.setY(normalizedValue);
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

                if(min.equals(max)) {
                    r.setY(min);
                    if(r.getYPredicted()!=null) {
                        r.setYPredicted(min);
                    }
                }
                else {
                    r.setY(TypeConversions.toDouble(r.getY())*(max-min) + min);
                    
                    Double YPredicted = TypeConversions.toDouble(r.getYPredicted());
                    if(YPredicted!=null) {
                        r.setYPredicted(YPredicted*(max-min) + min);
                    }
                }
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
            Set<Object> columns = new HashSet<>(r.getX().keySet()); //TODO: remove this once we make Record immutable
            for(Object column : columns) {
                if(covert2dummy(columnTypes.get(column))==false) { 
                    continue;
                }
                Object value = r.getX().get(column);
                
                r.getX().remove(column); //remove the original column
                
                
                Object referenceLevel= referenceLevels.get(column);
                
                if(referenceLevel != null && //not unknown variable
                   !referenceLevel.equals(value)) { //not equal to reference level
                    
                    //create a new column
                    List<Object> newColumn = Arrays.<Object>asList(column,value);
                    
                    //add a new dummy variable for this column-value combination
                    r.getX().put(newColumn, true); 
                }
            }
        }
        
        //Reset Meta info
        data.resetMeta();
    }
    
}
