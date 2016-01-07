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
package com.datumbox.framework.machinelearning.common.bases.baseobjects;

import com.datumbox.common.dataobjects.Dataframe;
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
    
    /**
     * The Logger of all algorithms.
     * We want this to be non-static in order to print the names of the inherited classes.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    /**
     * The name of the Database where we persist our data.
     */
    protected String dbName;
    
    /**
     * The KnowledgeBase instance of the algorithm. Do NOT access it directly,
     * use the kb() getter instead.
     */
    protected KB knowledgeBase;
    
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
        this.knowledgeBase = (KB) new KnowledgeBase(this.dbName, dbConf, mpClass, tpClass);
    }
    
    /** {@inheritDoc} */
    @Override
     public MP getModelParameters() {
       return kb().getModelParameters();

    } 
    
    /** {@inheritDoc} */
    @Override
    public TP getTrainingParameters() {
        return kb().getTrainingParameters();
    }
    
    /** {@inheritDoc} */
    @Override
    public void fit(Dataframe trainingData, TP trainingParameters) {
        logger.info("fit()");
        
        //reset knowledge base
        kb().clear();
        kb().setTrainingParameters(trainingParameters);
        
        MP modelParameters = kb().getModelParameters();
        modelParameters.setN(trainingData.size());
        modelParameters.setD(trainingData.xColumnSize());
        
        _fit(trainingData);
        
        logger.info("Saving model");
        kb().save();
    }
      
    /** {@inheritDoc} */
    @Override
    public void delete() {
        kb().delete();
    }
            
    /** {@inheritDoc} */
    @Override
    public void close() {
        kb().close();
    }
    
    /**
     * Getter for the KnowledgeBase instance.
     * 
     * @return 
     */
    protected KB kb() {
        return knowledgeBase;
    }
    
    /**
     * This method estimates the actual coefficients of the algorithm.
     * 
     * @param trainingData 
     */
    protected abstract void _fit(Dataframe trainingData);
    
}
