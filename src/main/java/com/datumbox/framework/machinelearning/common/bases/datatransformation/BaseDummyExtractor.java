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
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class BaseDummyExtractor<MP extends BaseDummyExtractor.ModelParameters, TP extends BaseDummyExtractor.TrainingParameters> extends DataTransformer<MP, TP> {

    public static class ModelParameters extends DataTransformer.ModelParameters {

        public ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
            
    }
    
    public static class TrainingParameters extends DataTransformer.TrainingParameters {
        
    }

    protected BaseDummyExtractor(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass) {
        super(dbName, dbConf, mpClass, tpClass);
    }
    
    protected static void extractDummies(Dataset data, Map<Object, Object> referenceLevels, boolean trainingMode) {

        Map<Object, Dataset.ColumnType> newColumns = new HashMap<>();
        
        //TODO: rewrite this to avoid accessing getColumns() directly
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
                    
                    
                    List<Object> newColumn = null;
                    if (referenceLevels != null) {
                        //Mode that uses the references levels
                        Object referenceLevelValue = referenceLevels.get(column);

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
                    }
                    else {
                        //Mode that extracts every variable without taking into account reference levels
                        newColumn = Arrays.<Object>asList(column,value);
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
    
}
