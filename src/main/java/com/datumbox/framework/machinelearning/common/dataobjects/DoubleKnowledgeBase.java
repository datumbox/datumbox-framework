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
package com.datumbox.framework.machinelearning.common.dataobjects;

import com.datumbox.framework.machinelearning.common.interfaces.KnowledgeBase;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.abstracts.AbstractModelParameters;
import com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainingParameters;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 * The basic implementation of KnowledgeBase with two main internal parameters. 
 * This class is used by the majority of algorithms excluding the ML models 
 * and any other technique that requires Model Parameters and Training Parameters
 * but has no need for Validation Metrics.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public class DoubleKnowledgeBase<MP extends AbstractModelParameters, TP extends AbstractTrainingParameters> implements KnowledgeBase<MP, TP> {
    
    /**
     * The database configuration of the Permanent Storage.
     */
    protected final DatabaseConfiguration dbConf;
    
    /**
     * The connector to the Permanent Storage.
     */
    protected final DatabaseConnector dbc;
    
    /**
     * The class of the ModelParameters class of the algorithm.
     */
    protected final Class<MP> mpClass;
    
    /**
     * The class of the TrainingParameters class of the algorithm.
     */
    protected final Class<TP> tpClass;
    
    /**
     * The ModelParameters object of the algorithm.
     */
    protected MP modelParameters;
    
    /**
     * The TrainingParameters object of the algorithm.
     */
    protected TP trainingParameters;
    
    /**
     * Public constructor of the object.
     * 
     * @param dbName
     * @param dbConf 
     * @param mpClass 
     * @param tpClass 
     */
    public DoubleKnowledgeBase(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass) {
        this.dbConf = dbConf;
        
        dbc = dbConf.getConnector(dbName);
        
        this.mpClass = mpClass;
        this.tpClass = tpClass;
    }
    
    /** {@inheritDoc} */
    @Override
    public DatabaseConnector getDbc() {
        return dbc;
    }
    
    /** {@inheritDoc} */
    @Override
    public DatabaseConfiguration getDbConf() {
        return dbConf;
    }

    /** {@inheritDoc} */
    @Override
    public void save() {
        if(isInitialized()==false) {
            throw new IllegalArgumentException("Can't save an empty KnowledgeBase.");
        }
        
        dbc.saveObject("modelParameters", modelParameters);
        dbc.saveObject("trainingParameters", trainingParameters);
    }
    
    /** {@inheritDoc} */
    @Override
    public void load() {
        if(!isInitialized()) {
            modelParameters = dbc.loadObject("modelParameters", mpClass);
            trainingParameters = dbc.loadObject("trainingParameters", tpClass);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void delete() {
    	dbc.clear();
        close();
        
        modelParameters = null;
        trainingParameters = null;
    }
    
    /** {@inheritDoc} */
    @Override
    public void close() {
        try {
            dbc.close();
        } 
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void clear() {
        dbc.clear();
        modelParameters = null;
        trainingParameters = null;
        
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

    /** {@inheritDoc} */
    @Override
    public TP getTrainingParameters() {
        return trainingParameters;
    }

    /** {@inheritDoc} */
    @Override
    public void setTrainingParameters(TP trainingParameters) {
        this.trainingParameters = trainingParameters;
    }

    /** {@inheritDoc} */
    @Override
    public MP getModelParameters() {
        return modelParameters;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setModelParameters(MP modelParameters) {
        this.modelParameters = modelParameters;
    }
    
    /**
     * Checks if the KnowledgeBase has not been initialized.
     * 
     * @return 
     */
    protected boolean isInitialized() {
        return modelParameters != null && trainingParameters != null;
    }
}
