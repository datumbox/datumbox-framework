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
package com.datumbox.framework.machinelearning.common.abstracts;

import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.interfaces.Trainable;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.framework.machinelearning.common.dataobjects.DoubleKnowledgeBase;
import com.datumbox.framework.machinelearning.common.interfaces.KnowledgeBase;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for every Trainable Algorithm of the Framework. This includes Machine Learning
 * Models, Data Transformers, Feature Selectors etc.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <KB>
 */
public abstract class AbstractTrainer<MP extends AbstractModelParameters, TP extends AbstractTrainingParameters, KB extends DoubleKnowledgeBase<MP, TP>> implements Trainable<MP, TP> {
    
    /**
     * The Logger of all algorithms.
     * We want this to be non-static in order to print the names of the inherited classes.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    /**
     * The name of the Database where we persist our data.
     */
    protected final String dbName;
    
    /**
     * The KnowledgeBase instance of the algorithm. 
     */
    private final KB knowledgeBase;
    
    /**
     * Generates a new instance of a AbstractTrainer by providing the Class of the
 algorithm.
     * 
     * @param <BT>
     * @param aClass
     * @param dbName
     * @param dbConf
     * @return 
     */
    public static <BT extends AbstractTrainer> BT newInstance(Class<BT> aClass, String dbName, DatabaseConfiguration dbConf) {
        BT algorithm = null;
        try {
            algorithm = aClass.getConstructor(String.class, DatabaseConfiguration.class).newInstance(dbName, dbConf);
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
        
        return algorithm;
    }

    /**
     * The basic Constructor of all BaseTrainable classes.
     * 
     * @param baseDBname
     * @param dbConf 
     * @param kbClass 
     * @param kbSubtypeClasses 
     */
    protected AbstractTrainer(String baseDBname, DatabaseConfiguration dbConf, Class<? extends DoubleKnowledgeBase> kbClass, Class<? extends Serializable>... kbSubtypeClasses) {
        String methodName = this.getClass().getSimpleName();
        String dbNameSeparator = dbConf.getDBnameSeparator();
        if(!baseDBname.contains(methodName+dbNameSeparator)) { //patch for the K-fold cross validation which already contains the name of the algorithm in the dbname
            baseDBname += dbNameSeparator + methodName;
        }
        
        dbName = baseDBname;
        
        knowledgeBase = (KB) KnowledgeBase.newInstance(kbClass, dbName, dbConf, kbSubtypeClasses);
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
