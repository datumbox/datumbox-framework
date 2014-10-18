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
package com.datumbox.common.dataobjects;

import java.util.Map;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
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
        
        
        int n = dataset.size();
        int d = dataset.getColumnSize();
        
        if(addConstantColumn) {
            ++d;
        }
        
        MatrixDataset m = new MatrixDataset(new ArrayRealVector(n), new BlockRealMatrix(n, d), featureIdsReference);
        
        
        if(dataset.isEmpty()) {
            return m;
        }
        
        boolean extractY=(Dataset.value2ColumnType(dataset.iterator().next().getY())==Dataset.ColumnType.NUMERICAL);
        
        int previousFeatureId=0; 
        if(addConstantColumn) {
            for(int row=0;row<n;++row) {
                m.X.setEntry(row, previousFeatureId, 1.0); //put the constant in evey row
            }
            m.feature2ColumnId.put(Dataset.constantColumnName, previousFeatureId);
            ++previousFeatureId; 
        }

        for(Record r : dataset) {
            int row = r.getId();
            
            if(extractY) {
                m.Y.setEntry(row, Dataset.toDouble(r.getY()));
            }
            
            
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                Integer featureId = m.feature2ColumnId.get(feature);
                if(featureId==null) {
                    featureId = previousFeatureId;
                    m.feature2ColumnId.put(feature, featureId);
                    ++previousFeatureId;
                }
                
                Double value = Dataset.toDouble(entry.getValue());
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
        
        int n = newDataset.size();
        int d = featureIdsReference.size();
        
        MatrixDataset m = new MatrixDataset(new ArrayRealVector(n), new BlockRealMatrix(n, d), featureIdsReference);
        
        if(newDataset.isEmpty()) {
            return m;
        }
        
        boolean extractY=(Dataset.value2ColumnType(newDataset.iterator().next().getY())==Dataset.ColumnType.NUMERICAL);
        
        boolean addConstantColumn = m.feature2ColumnId.containsKey(Dataset.constantColumnName);
        
        for(Record r : newDataset) {
            int row = r.getId();
            
            if(extractY) {
                m.Y.setEntry(row, Dataset.toDouble(r.getY()));
            }
            
            if(addConstantColumn) {
                m.X.setEntry(row, 0, 1.0); //add the constant column
            }
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                Double value = Dataset.toDouble(entry.getValue());
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
            Double value = Dataset.toDouble(entry.getValue());
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
