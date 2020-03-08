/**
 * Copyright (C) 2013-2020 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.core.common.dataobjects;

import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.common.utilities.RandomGenerator;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The DataframeMatrix class is responsible for converting a Dataframe object to a
 Matrix representation. Some of the methods on framework require working with
 matrices and this class provides the tools to achieve the necessary conversions.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class DataframeMatrix {

    /**
     * We create a single storage engine for all DataframeMatrix and MapRealMatrix objects. It is necessary to define it static
     * and package protected to make it accessible to other classes such as the MapRealMatrix. This is because
     * some methods of the RealMatrix require generating new object without passing the configuration file. The engine
     * is created only once to avoid hurting performance. Thus the storageEngine is initialized once in a thread-safe manner.
     */
    static StorageEngine storageEngine;

    /**
     * Keeps record how many usages were made on the storageEngine to avoid conflicting names.
     */
    static final AtomicInteger storageId = new AtomicInteger();
    
    private final RealMatrix X;
    private final RealVector Y;
     
    /**
     * Getter for the X Matrix which contains the data of the Dataframe.
     * 
     * @return 
     */
    public RealMatrix getX() {
        return X;
    }

    /**
     * Getter for the Y vector with the values of the response variables.
     * 
     * @return 
     */
    public RealVector getY() {
        return Y;
    }
    
    
    /**
     * Private constructor which accepts as arguments the Y Vector with the values
     * of the response variables, the X matrix with the actual data and a
     * feature2ColumnId map which provides a mapping between the column name and
     * their column id in the data matrix.
     * 
     * @param Y
     * @param X
     */
    private DataframeMatrix(RealMatrix X, RealVector Y) {
        //this constructor must be private because it is used only internally
        this.Y = Y;
        this.X = X;
    }

    /**
     * Initializes the static storage engine if it's not already set.
     *
     * @param dataset
     */
    private static void setStorageEngine(Dataframe dataset) {
        //create a single storage engine for all the MapRealMatrixes
        if (storageEngine == null) {
            synchronized(DataframeMatrix.class) {
                if (storageEngine == null) {
                    String storageName = "mdf" + RandomGenerator.getThreadLocalRandomUnseeded().nextLong();
                    storageEngine = dataset.configuration.getStorageConfiguration().createStorageEngine(storageName);
                }
            }
        }
    }
    
    /**
     * Method used to generate a training Dataframe to a DataframeMatrix and extracts its contents
 to Matrixes. It populates the featureIdsReference map with the mappings
     * between the feature names and the column ids of the matrix. Typically used
     * to convert the training dataset.
     * 
     * @param dataset
     * @param addConstantColumn
     * @param recordIdsReference
     * @param featureIdsReference
     * @return 
     */
    public static DataframeMatrix newInstance(Dataframe dataset, boolean addConstantColumn, Map<Integer, Integer> recordIdsReference, Map<Object, Integer> featureIdsReference) {
        if(!featureIdsReference.isEmpty()) {
            throw new IllegalArgumentException("The featureIdsReference map should be empty.");
        }

        setStorageEngine(dataset);
        
        
        int n = dataset.size();
        int d = dataset.xColumnSize();
        
        if(addConstantColumn) {
            ++d;
        }

        DataframeMatrix m = new DataframeMatrix(new MapRealMatrix(n, d), new MapRealVector(n));
        
        if(dataset.isEmpty()) {
            return m;
        }
        
        boolean extractY=(dataset.getYDataType()== TypeInference.DataType.NUMERICAL);
        
        int featureId=0; 
        if(addConstantColumn) {
            for(int row=0;row<n;++row) {
                m.X.setEntry(row, featureId, 1.0); //put the constant in evey row
            }
            featureIdsReference.put(Dataframe.COLUMN_NAME_CONSTANT, featureId);
            ++featureId; 
        }
        
        int rowId = 0;
        for(Map.Entry<Integer, Record> e : dataset.entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            if(recordIdsReference != null) {
                recordIdsReference.put(rId, rowId);
            }
            
            if(extractY) {
                m.Y.setEntry(rowId, TypeInference.toDouble(r.getY()));
            }
            
            
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                Integer knownFeatureId = featureIdsReference.get(feature);
                if(knownFeatureId==null) {
                    featureIdsReference.put(feature, featureId);
                    knownFeatureId = featureId;
                    
                    ++featureId;
                }
                
                Double value = TypeInference.toDouble(entry.getValue());
                if(value != null) {
                    m.X.setEntry(rowId, knownFeatureId, value);
                }//else the X matrix maintains the 0.0 default value
            }
            ++rowId;
        }
        
        return m;
    }
    
    /**
     * Parses a testing dataset and converts it to DataframeMatrix by using an already
 existing mapping between feature names and column ids. Typically used
     * to parse the testing or validation dataset.
     * 
     * @param newData
     * @param recordIdsReference
     * @param featureIdsReference
     * @return 
     */
    public static DataframeMatrix parseDataset(Dataframe newData, Map<Integer, Integer> recordIdsReference, Map<Object, Integer> featureIdsReference) {
        if(featureIdsReference.isEmpty()) {
            throw new IllegalArgumentException("The featureIdsReference map should not be empty.");
        }

        setStorageEngine(newData);
        
        int n = newData.size();
        int d = featureIdsReference.size();

        DataframeMatrix m = new DataframeMatrix(new MapRealMatrix(n, d), new MapRealVector(n));
        
        if(newData.isEmpty()) {
            return m;
        }
        
        boolean extractY=(newData.getYDataType()==TypeInference.DataType.NUMERICAL);
        
        boolean addConstantColumn = featureIdsReference.containsKey(Dataframe.COLUMN_NAME_CONSTANT);
        
        int rowId = 0;
        for(Map.Entry<Integer, Record> e : newData.entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            if(recordIdsReference != null) {
                recordIdsReference.put(rId, rowId);
            }
            
            if(extractY) {
                m.Y.setEntry(rowId, TypeInference.toDouble(r.getY()));
            }
            
            if(addConstantColumn) {
                m.X.setEntry(rowId, 0, 1.0); //add the constant column
            }
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                Double value = TypeInference.toDouble(entry.getValue());
                if(value!=null) {
                    Integer featureId = featureIdsReference.get(feature);
                    if(featureId!=null) {//if the feature exists
                        m.X.setEntry(rowId, featureId, value);
                    }
                }//else the X matrix maintains the 0.0 default value
            }
            ++rowId;
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
            throw new IllegalArgumentException("The featureIdsReference map should not be empty.");
        }
        
        int d = featureIdsReference.size();

        //create an Map-backed vector only if we have available info about configuration.
        RealVector v = (storageEngine != null)?new MapRealVector(d):new OpenMapRealVector(d);
        
        boolean addConstantColumn = featureIdsReference.containsKey(Dataframe.COLUMN_NAME_CONSTANT);
        

        if(addConstantColumn) {
            v.setEntry(0, 1.0);  //add the constant column
        }
        for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
            Object feature = entry.getKey();
            Double value = TypeInference.toDouble(entry.getValue());
            if(value!=null) {
                Integer featureId = featureIdsReference.get(feature);
                if(featureId!=null) {//if the feature exists
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
