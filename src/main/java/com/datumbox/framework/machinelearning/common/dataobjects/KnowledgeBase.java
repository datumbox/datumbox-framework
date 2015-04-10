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
import com.datumbox.framework.machinelearning.common.bases.dataobjects.BaseModelParameters;
import com.datumbox.framework.machinelearning.common.bases.dataobjects.BaseTrainingParameters;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;


/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public class KnowledgeBase<MP extends BaseModelParameters, TP extends BaseTrainingParameters> implements Serializable {
    
    /*
        VARIABLES
        =========
    */
    protected String dbName; 
    
    
    protected transient DatabaseConnector dbc;
    protected transient DatabaseConfiguration dbConf;

    
    
    protected Class<MP> mpClass;
    protected Class<TP> tpClass;
    
    protected MP modelParameters;
    protected TP trainingParameters;
    
    
    
    
    
    /*
        EXTENDING INTERFACE
        ==================
    */

    public KnowledgeBase(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass) {
        this.dbName = dbName;
        this.dbConf = dbConf;
        
        //get an instance on the permanent storage handler
        dbc = dbConf.getConnector(dbName);
        
        this.mpClass = mpClass;
        this.tpClass = tpClass;
    }

    public DatabaseConnector getDbc() {
        return dbc;
    }

    public DatabaseConfiguration getDbConf() {
        return dbConf;
    }

    public void save() {
        if(modelParameters==null) {
            throw new IllegalArgumentException("Can not store an empty KnowledgeBase.");
        }
        
        dbc.save(this);
    }
    
    public void load() {
        if(modelParameters==null) {

            //NOTE: the kbObject was constructed with the default protected no-argument
            //constructor. As a result it does not have an initialized dbc object.
            //We don't care for that though because this instance has a valid dbc object
            //and the kbObject is only used to copy its values (we don't use it).
            KnowledgeBase kbObject = dbc.load(this.getClass());
            if(kbObject==null) {
                throw new IllegalArgumentException("The KnowledgeBase could not be loaded.");
            }
            
            trainingParameters = (TP) kbObject.trainingParameters;
            modelParameters = (MP) kbObject.modelParameters;
        }
    }
    
    public void erase() {
    	dbc.dropDatabase();
        
        modelParameters = null;
        trainingParameters = null;
    }
    
    public void reinitialize() {
        erase();
        
        try {
            modelParameters = mpClass.getConstructor(DatabaseConnector.class).newInstance(dbc);
            trainingParameters = tpClass.getConstructor().newInstance();
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
        
    }

    
    
        
    
    
    /*
        GETTER SETTERS
        ==============
    */
    
    public TP getTrainingParameters() {
        return trainingParameters;
    }

    public void setTrainingParameters(TP trainingParameters) {
        this.trainingParameters = trainingParameters;
    }

    public MP getModelParameters() {
        return modelParameters;
    }

    public void setModelParameters(MP modelParameters) {
        this.modelParameters = modelParameters;
    }
    
    
}
