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
package com.datumbox.common.dataobjects;

import java.util.Map;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * The MatrixDataset class is responsible for converting a Dataset object to a
 * Matrix representation. Some of the methods on framework require working with
 * matrices and this class provides the tools to achieve the necessary conversions.
 * The major drawback of using this class is that all the data from the Dataset
 * object are brought in memory and this limits the amount of data that we can
 * use.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MatrixDataset {
    
    private final RealVector Y;
    private final RealMatrix X;
    private final Map<Object, Integer> feature2ColumnId;
    
    /**
     * Getter for the Y vector with the values of the response variables.
     * 
     * @return 
     */
    public RealVector getY() {
        return Y;
    }
    
    /**
     * Getter for the X Matrix which contains the data of the Dataset.
     * 
     * @return 
     */
    public RealMatrix getX() {
        return X;
    }
    
    /**
     * Private constructor which accepts as arguments the Y Vector with the values
     * of the response variables, the X matrix with the actual data and a
     * feature2ColumnId map which provides a mapping between the column name and
     * their column id in the data matrix.
     * 
     * @param Y
     * @param X
     * @param feature2ColumnId 
     */
    private MatrixDataset(RealVector Y, RealMatrix X, Map<Object, Integer> feature2ColumnId) {
        //this constructor must be private because it is used only internally
        this.Y = Y;
        this.X = X;
        this.feature2ColumnId = feature2ColumnId;
    }
    
    /**
     * Method used to generate a training Dataset to a MatrixDataset and extracts its contents
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
        
        
        int n = dataset.size();
        int d = dataset.xColumnSize();
        
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

        for(Integer rId : dataset) {
            Record r = dataset.get(rId);
            
            if(extractY) {
                m.Y.setEntry(rId, TypeInference.toDouble(r.getY()));
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
                    m.X.setEntry(rId, featureId, value);
                }
                else {
                    //else the X matrix maintains the 0.0 default value
                }
            }
        }
        
        return m;
    }
    
    /**
     * Parses a testing dataset and converts it to MatrixDataset by using an already
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
        
        int n = newDataset.size();
        int d = featureIdsReference.size();
        
        MatrixDataset m = new MatrixDataset(new ArrayRealVector(n), new BlockRealMatrix(n, d), featureIdsReference);
        
        if(newDataset.isEmpty()) {
            return m;
        }
        
        boolean extractY=(newDataset.getYDataType()==TypeInference.DataType.NUMERICAL);
        
        boolean addConstantColumn = m.feature2ColumnId.containsKey(Dataset.constantColumnName);
        
        //Assummes that the ids start from 0 and go up to n
        for(Integer rId : newDataset) {
            Record r = newDataset.get(rId);
            
            if(extractY) {
                m.Y.setEntry(rId, TypeInference.toDouble(r.getY()));
            }
            
            if(addConstantColumn) {
                m.X.setEntry(rId, 0, 1.0); //add the constant column
            }
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                Double value = TypeInference.toDouble(entry.getValue());
                if(value!=null) {
                    Integer featureId = m.feature2ColumnId.get(feature);
                    if(featureId!=null) {//if the feature exists in our database
                        m.X.setEntry(rId, featureId, value);
                    }
                }
                else {
                    //else the X matrix maintains the 0.0 default value
                }
            }
        }
        
        return m;
    }
    
    /**
     * Parses a single Record and converts it to RealVector by using an already
     * existing mapping between feature names and column ids. 
     * 
     * @param r
     * @param featureIdsReference
     * @return 
     */
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
