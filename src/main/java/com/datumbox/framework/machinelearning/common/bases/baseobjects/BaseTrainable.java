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

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.objecttypes.Trainable;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.framework.machinelearning.common.dataobjects.KnowledgeBase;
import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for every Model of the Framework. This includes Machine Learning
 * Models, Data Transformers, Feature Selectors etc.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <KB>
 */
public abstract class BaseTrainable<MP extends BaseModelParameters, TP extends BaseTrainingParameters, KB extends KnowledgeBase<MP, TP>> implements Trainable<MP, TP> {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    protected KB knowledgeBase;
    protected String dbName;
    
    /**
     * Generates a new instance of a BaseTrainable by providing the Class of the
     * algorithm.
     * 
     * @param <BT>
     * @param aClass
     * @param dbName
     * @param dbConfig
     * @return 
     */
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

    /**
     * Protected Constructor which does not include the initialization of the
     * KnowledgeBase.
     * 
     * @param dbName
     * @param dbConf 
     */
    protected BaseTrainable(String dbName, DatabaseConfiguration dbConf) {
        String methodName = this.getClass().getSimpleName();
        String dbNameSeparator = dbConf.getDBnameSeparator();
        if(!dbName.contains(methodName+dbNameSeparator)) { //patch for the K-fold cross validation which already contains the name of the algorithm in the dbname
            dbName += dbNameSeparator + methodName;
        }
        
        this.dbName = dbName;
    }
    
    /**
     * Protected Constructor which includes the initialization of the
     * KnowledgeBase.
     * 
     * @param dbName
     * @param dbConf
     * @param mpClass
     * @param tpClass 
     */
    protected BaseTrainable(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass) {
        this(dbName, dbConf);
        
        knowledgeBase = (KB) new KnowledgeBase(this.dbName, dbConf, mpClass, tpClass);
    }
    
    /**
     * Returns the model parameters that were estimated after training.
     * 
     * @return 
     */
    @Override
     public MP getModelParameters() {
       return knowledgeBase.getModelParameters();

    } 
    
    /**
     * It returns the training parameters that configure the algorithm.
     * 
     * @return 
     */
    @Override
    public TP getTrainingParameters() {
        return knowledgeBase.getTrainingParameters();
    }
    
    /**
     * Trains a Machine Learning model using the provided training data. This
     * method is responsible for initializing appropriately the algorithm and then
     * calling the _fit() method which performs the learning.
     * 
     * @param trainingData
     * @param trainingParameters 
     */
    @Override
    public void fit(Dataset trainingData, TP trainingParameters) {
        logger.info("fit()");
        
        //reset knowledge base
        knowledgeBase.reinitialize();
        knowledgeBase.setTrainingParameters(trainingParameters);
        
        MP modelParameters = knowledgeBase.getModelParameters();
        modelParameters.setN(trainingData.getRecordNumber());
        modelParameters.setD(trainingData.getVariableNumber());
        
        _fit(trainingData);
        
        logger.info("Saving model");
        knowledgeBase.save();
    }
      
    /**
     * Deletes the database of the algorithm. 
     */
    @Override
    public void erase() {
        knowledgeBase.erase();
    }
            
    /**
     * Closes all the resources of the algorithm. 
     */
    @Override
    public void close() {
        knowledgeBase.close();
    }
    
    /**
     * This method estimates the actual coefficients of the algorithm.
     * 
     * @param trainingData 
     */
    protected abstract void _fit(Dataset trainingData);
    
}
