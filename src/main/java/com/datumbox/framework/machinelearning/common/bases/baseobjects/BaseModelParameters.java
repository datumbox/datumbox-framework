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
package com.datumbox.framework.machinelearning.common.bases.baseobjects;

import com.datumbox.common.objecttypes.Learnable;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Base class for every ModelParameter class in the framework. It automatically
 * initializes all the BidMap fields by using reflection.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public abstract class BaseModelParameters implements Learnable {
    //number of data points used for training
    private Integer n = 0;

    //number of features in data points used for training
    private Integer d = 0;
        
    /**
     * Protected constructor which accepts as argument the DatabaseConnector.
     * 
     * @param dbc 
     */
    public BaseModelParameters(DatabaseConnector dbc) {
        //Initialize all the BigMap fields
        bigMapInitializer(dbc);
    }

    /**
     * Getter for the total number of records used in training.
     * 
     * @return 
     */
    public Integer getN() {
        return n;
    }

    /**
     * Setter for the total number of records used in training.
     * 
     * @param n 
     */
    protected void setN(Integer n) {
        this.n = n;
    }

    /**
     * Getter for the dimension of the dataset used in training.
     * 
     * @return 
     */
    public Integer getD() {
        return d;
    }

    /**
     * Setter for the dimension of the dataset used in training.
     * 
     * @param d 
     */
    protected void setD(Integer d) {
        this.d = d;
    }

    /**
     * Initializes all the fields of the class which are marked with the BigMap
     * annotation automatically.
     * 
     * @param dbc 
     */
    private void bigMapInitializer(DatabaseConnector dbc) {
        //get all the fields from all the inherited classes
        for(Field field : getAllFields(new LinkedList<>(), this.getClass())){
            
            //if the field is annotated with BigMap
            if (field.isAnnotationPresent(BigMap.class)) {
                field.setAccessible(true);
                
                try {
                    //call the getBigMap method to load it
                    field.set(this, dbc.getBigMap(field.getName(), false));
                } 
                catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
                
            }
        }
    }
    
    /**
     * Gets all the fields recursively from all the parent classes.
     * 
     * @param fields
     * @param type
     * @return 
     */
    private List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }
}
