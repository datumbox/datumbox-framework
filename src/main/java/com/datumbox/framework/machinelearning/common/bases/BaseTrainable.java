/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
package com.datumbox.framework.machinelearning.common.bases;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.objecttypes.Trainable;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;

import com.datumbox.framework.machinelearning.common.bases.dataobjects.BaseModelParameters;
import com.datumbox.framework.machinelearning.common.bases.dataobjects.BaseTrainingParameters;
import com.datumbox.framework.machinelearning.common.dataobjects.KnowledgeBase;


import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <KB>
 */
public abstract class BaseTrainable<MP extends BaseModelParameters, TP extends BaseTrainingParameters, KB extends KnowledgeBase<MP, TP>> implements Trainable<MP, TP> {
    
    protected final Logger logger;
    
    protected KB knowledgeBase;
    protected String dbName;
    
    
    public static <BT extends BaseTrainable> BT newInstance(Class<BT> aClass, String dbName, DatabaseConfiguration dbConfig) {
        BT algorithm = null;
        try {
            algorithm = aClass.getConstructor(String.class, DatabaseConfiguration.class).newInstance(dbName, dbConfig);
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
        
        return algorithm;
    }

    protected BaseTrainable(String dbName, DatabaseConfiguration dbConf) {
        String methodName = this.getClass().getSimpleName();
        if(!dbName.contains(methodName)) { //patch for the K-fold cross validation which already contains the name of the algorithm in the dbname
            dbName += dbConf.getDBnameSeparator() + methodName;
        }
        
        this.dbName = dbName;
        
        logger = LoggerFactory.getLogger(this.getClass());
    }
    
    protected BaseTrainable(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass) {
        this(dbName, dbConf);
        
        knowledgeBase = (KB) new KnowledgeBase(this.dbName, dbConf, mpClass, tpClass);
    } 
    
    @Override
    public TP getTrainingParameters() {
        return knowledgeBase.getTrainingParameters();
    }
    
    @Override
    public void erase() {
        knowledgeBase.erase();
    }
    
    @Override
     public MP getModelParameters() {
       return knowledgeBase.getModelParameters();

    }
    
    
    @Override
    public void fit(Dataset trainingData, TP trainingParameters) {
        logger.info("fit()");
        
        initializeTrainingConfiguration(trainingParameters);
        _fit(trainingData);
        
        //store database
        knowledgeBase.save();
    }
    
    @Override
    public boolean modifiesData() {
        //check if the algorithm itself modifies the data
        try { 
            Boolean dataSafe = (Boolean) this.getClass().getDeclaredField("DATA_SAFE_CALL_BY_REFERENCE").get(this);
            //see if the data are safe mearning that algorithm does not modify the data internally.
            //if the data are not safe, mark it for deep copy
            if(dataSafe!=true) {
                return true;
            }
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
            return true; //if no information available play it safe and mark it as true
        }
        
        return false;
    }
    
    protected void initializeTrainingConfiguration(TP trainingParameters) {
        //reset knowledge base
        knowledgeBase.reinitialize();
        knowledgeBase.setTrainingParameters(trainingParameters);
    }
    
    protected abstract void _fit(Dataset trainingData);
    
}
