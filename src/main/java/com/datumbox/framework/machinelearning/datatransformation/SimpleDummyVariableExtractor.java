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
package com.datumbox.framework.machinelearning.datatransformation;

import com.datumbox.framework.machinelearning.common.bases.datatransformation.DataTransformer;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Simple Dummy Variable Extractor: Removes the categorical and ordinal features 
 * and creates new dummy variables that are combinations of feature-value. It does not
 * take into consideration reference levels. It is used only by BayesianEnsembleMethod
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class SimpleDummyVariableExtractor extends DataTransformer<SimpleDummyVariableExtractor.ModelParameters, SimpleDummyVariableExtractor.TrainingParameters> {


    public static final String SHORT_METHOD_NAME = "SDExt";
        
    public static class ModelParameters extends DataTransformer.ModelParameters {
            
    }
    
    public static class TrainingParameters extends DataTransformer.TrainingParameters {
        
    }

    public SimpleDummyVariableExtractor(String dbName) {
        super(dbName, SimpleDummyVariableExtractor.ModelParameters.class, SimpleDummyVariableExtractor.TrainingParameters.class);
    }



    @Override
    public String shortMethodName() {
        return SHORT_METHOD_NAME;
    }
    
    @Override
    protected void _transform(Dataset data, boolean trainingMode) {
        //handle non-numeric types, extract columns to dummy variables
        
        Map<Object, Dataset.ColumnType> newColumns = new HashMap<>();
        
        int n = data.size();
        
        Iterator<Map.Entry<Object, Dataset.ColumnType>> it = data.getColumns().entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Object, Dataset.ColumnType> entry = it.next();
            Object column = entry.getKey();
            Dataset.ColumnType columnType = entry.getValue();
            
            if(columnType==Dataset.ColumnType.CATEGORICAL ||
               columnType==Dataset.ColumnType.ORDINAL) { //ordinal and categorical are converted into dummyvars
                
                
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
                    

                    
                    List<Object> newColumn = Arrays.<Object>asList(column,value);
                    
                    //add a new boolean feature with combination of column and value
                    r.getX().put(newColumn, true);
                    
                    //add the new column in the list for insertion
                    newColumns.put(newColumn, Dataset.ColumnType.DUMMYVAR);
                }
            }
        }
        
        //add the new columns in the dataset column map
        if(!newColumns.isEmpty()) {
            data.getColumns().putAll(newColumns);
        }
    }

    @Override
    protected void _normalize(Dataset data) {
        //no normalization performed
    }

    @Override
    protected void _denormalize(Dataset data) {
        //no denormalization required
    }
    
}
