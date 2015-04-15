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

import com.datumbox.common.utilities.TypeInference;
import java.util.Map;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MatrixDataset {
    
    private final RealVector Y;
    private final RealMatrix X;
    private final Map<Object, Integer> feature2ColumnId;
    
    public RealVector getY() {
        return Y;
    }

    public RealMatrix getX() {
        return X;
    }
    
    private MatrixDataset(RealVector Y, RealMatrix X, Map<Object, Integer> feature2ColumnId) {
        //this constructor must be private because it is used only internally
        this.Y = Y;
        this.X = X;
        this.feature2ColumnId = feature2ColumnId;
    }
    
    /**
     * Method used to generate a Dataset to a MatrixDataset and extracts its contents
     * to Matrixes. It populates the featureIdsReference map with the mappings
     * between the feature names and the column ids of the matrix. Typically used
     * to convert the training dataset.
     * 
     * @param dataset
     * @param addConstantColumn
     * @param featureIdsReference
     * @return 
     */
    public static MatrixDataset newInstance(Dataset dataset, boolean addConstantColumn, Map<Object, Integer> featureIdsReference) {
        if(!featureIdsReference.isEmpty()) {
            throw new RuntimeException("The featureIdsReference map should be empty.");
        }
        
        
        int n = dataset.getRecordNumber();
        int d = dataset.getVariableNumber();
        
        if(addConstantColumn) {
            ++d;
        }
        
        MatrixDataset m = new MatrixDataset(new ArrayRealVector(n), new BlockRealMatrix(n, d), featureIdsReference);
        
        
        if(dataset.isEmpty()) {
            return m;
        }
        
        boolean extractY=(dataset.getYDataType()==TypeInference.DataType.NUMERICAL);
        
        int previousFeatureId=0; 
        if(addConstantColumn) {
            for(int row=0;row<n;++row) {
                m.X.setEntry(row, previousFeatureId, 1.0); //put the constant in evey row
            }
            m.feature2ColumnId.put(Dataset.constantColumnName, previousFeatureId);
            ++previousFeatureId; 
        }

        for(Integer id : dataset) {
            Record r = dataset.get(id);
            int row = id;
            
            if(extractY) {
                m.Y.setEntry(row, TypeInference.toDouble(r.getY()));
            }
            
            
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                Integer featureId = m.feature2ColumnId.get(feature);
                if(featureId==null) {
                    featureId = previousFeatureId;
                    m.feature2ColumnId.put(feature, featureId);
                    ++previousFeatureId;
                }
                
                Double value = TypeInference.toDouble(entry.getValue());
                if(value != null) {
                    m.X.setEntry(row, featureId, value);
                }
                else {
                    //else the X matrix maintains the 0.0 default value
                }
            }
        }
        
        return m;
    }
    
    /**
     * Parses a dataset and converts it to MatrixDataset by using an already
     * existing mapping between feature names and column ids. Typically used
     * to parse the testing or validation dataset.
     * 
     * @param newDataset
     * @param featureIdsReference
     * @return 
     */
    public static MatrixDataset parseDataset(Dataset newDataset, Map<Object, Integer> featureIdsReference) {
        if(featureIdsReference.isEmpty()) {
            throw new RuntimeException("The featureIdsReference map should not be empty.");
        }
        
        int n = newDataset.getRecordNumber();
        int d = featureIdsReference.size();
        
        MatrixDataset m = new MatrixDataset(new ArrayRealVector(n), new BlockRealMatrix(n, d), featureIdsReference);
        
        if(newDataset.isEmpty()) {
            return m;
        }
        
        boolean extractY=(newDataset.getYDataType()==TypeInference.DataType.NUMERICAL);
        
        boolean addConstantColumn = m.feature2ColumnId.containsKey(Dataset.constantColumnName);
        
        for(Integer id : newDataset) {
            Record r = newDataset.get(id);
            int row = id;
            
            if(extractY) {
                m.Y.setEntry(row, TypeInference.toDouble(r.getY()));
            }
            
            if(addConstantColumn) {
                m.X.setEntry(row, 0, 1.0); //add the constant column
            }
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                Double value = TypeInference.toDouble(entry.getValue());
                if(value!=null) {
                    Integer featureId = m.feature2ColumnId.get(feature);
                    if(featureId!=null) {//if the feature exists in our database
                        m.X.setEntry(row, featureId, value);
                    }
                }
                else {
                    //else the X matrix maintains the 0.0 default value
                }
            }
        }
        
        return m;
    }
    
    public static RealVector parseRecord(Record r, Map<Object, Integer> featureIdsReference) {
        if(featureIdsReference.isEmpty()) {
            throw new RuntimeException("The featureIdsReference map should not be empty.");
        }
        
        int d = featureIdsReference.size();
        
        RealVector v = new ArrayRealVector(d);
        
        boolean addConstantColumn = featureIdsReference.containsKey(Dataset.constantColumnName);
        

        if(addConstantColumn) {
            v.setEntry(0, 1.0);  //add the constant column
        }
        for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
            Object feature = entry.getKey();
            Double value = TypeInference.toDouble(entry.getValue());
            if(value!=null) {
                Integer featureId = featureIdsReference.get(feature);
                if(featureId!=null) {//if the feature exists in our database
                    v.setEntry(featureId, value);
                }
            }
            else {
                //else the X matrix maintains the 0.0 default value
            }
        }
        
        return v;
    }
}
