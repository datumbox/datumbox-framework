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
package com.datumbox.framework.machinelearning.common.dataobjects;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseModelParameters;
import com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseTrainingParameters;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 * The KnowledgeBase represents the "database" that the algorithm learned during  
 * training. It is a wrapper of the 2 classes: the model parameters and the 
 * training parameters.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public class KnowledgeBase<MP extends BaseModelParameters, TP extends BaseTrainingParameters> implements Serializable {

    protected String dbName; 
    
    protected transient DatabaseConnector dbc;
    protected transient DatabaseConfiguration dbConf;

    protected Class<MP> mpClass;
    protected Class<TP> tpClass;
    
    protected MP modelParameters;
    protected TP trainingParameters;
    
    /**
     * Public constructor of the object.
     * 
     * @param dbName
     * @param dbConf 
     * @param mpClass 
     * @param tpClass 
     */
    public KnowledgeBase(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass) {
        this.dbName = dbName;
        this.dbConf = dbConf;
        
        dbc = dbConf.getConnector(dbName);
        
        this.mpClass = mpClass;
        this.tpClass = tpClass;
    }
    
    /**
     * Getter for the Database Connector.
     * 
     * @return 
     */
    public DatabaseConnector getDbc() {
        return dbc;
    }
    
    /**
     * Getter for the Database Configuration.
     * 
     * @return 
     */
    public DatabaseConfiguration getDbConf() {
        return dbConf;
    }

    /**
     * Saves a KnowledgeBase to the permanent storage.
     */
    public void save() {
        if(modelParameters==null) {
            throw new IllegalArgumentException("Can not store an empty KnowledgeBase.");
        }
        
        dbc.save("KnowledgeBase", this);
    }
    
    /**
     * Loads a KnowledgeBase from the permanent storage.
     */
    public void load() {
        if(modelParameters==null) {
            KnowledgeBase kbObject = dbc.load("KnowledgeBase", this.getClass());
            if(kbObject==null) {
                throw new IllegalArgumentException("The KnowledgeBase could not be loaded.");
            }
            
            trainingParameters = (TP) kbObject.trainingParameters;
            modelParameters = (MP) kbObject.modelParameters;
        }
    }
    
    /**
     * Deletes the database of the algorithm. 
     */
    public void erase() {
    	dbc.dropDatabase();
        dbc.close();
        
        modelParameters = null;
        trainingParameters = null;
    }
    
    /**
     * Closes all the resources of the algorithm. 
     */
    public void close() {
        dbc.close();
    }
    
    /**
     * Deletes and re-initializes KnowledgeBase object. It erases all data from 
     * storage, it releases all resources, reinitializes the internal objects and
     * opens new connection to the permanent storage.
     */
    public void reinitialize() {
        erase();
        dbc = dbConf.getConnector(dbName); //re-open connector
        
        try {
            Constructor<MP> c = mpClass.getDeclaredConstructor(DatabaseConnector.class);
            c.setAccessible(true);
            modelParameters = c.newInstance(dbc);
            trainingParameters = tpClass.getConstructor().newInstance();
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
        
    }

    /**
     * Getter for the Training Parameters.
     * 
     * @return 
     */
    public TP getTrainingParameters() {
        return trainingParameters;
    }

    /**
     * Setter for the Training Parameters.
     * 
     * @param trainingParameters 
     */
    public void setTrainingParameters(TP trainingParameters) {
        this.trainingParameters = trainingParameters;
    }

    /**
     * Getter for the Model Parameters.
     * 
     * @return 
     */
    public MP getModelParameters() {
        return modelParameters;
    }
    
    /**
     * Setter for the Model Parameters.
     * 
     * @param modelParameters 
     */
    public void setModelParameters(MP modelParameters) {
        this.modelParameters = modelParameters;
    }
    
}
