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
package com.datumbox.framework.core.machinelearning.common.abstracts;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.interfaces.Trainable;
import com.datumbox.framework.common.persistentstorage.interfaces.BigMap;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.common.utilities.ReflectionMethods;
import com.datumbox.framework.core.machinelearning.common.dataobjects.DoubleKnowledgeBase;
import com.datumbox.framework.core.machinelearning.common.interfaces.KnowledgeBase;
import com.datumbox.framework.core.machinelearning.common.interfaces.ModelParameters;
import com.datumbox.framework.core.machinelearning.common.interfaces.TrainingParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * Base class for every Trainable Algorithm of the Framework. This includes Machine Learning
 * Models, Data Transformers, Feature Selectors etc.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <KB>
 */
public abstract class AbstractTrainer<MP extends AbstractTrainer.AbstractModelParameters, TP extends AbstractTrainer.AbstractTrainingParameters, KB extends DoubleKnowledgeBase<MP, TP>> implements Trainable<MP, TP> {
       
    /**
     * Base class for every ModelParameter class in the framework. It automatically
     * initializes all the BidMap fields by using reflection.
     * 
     * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
     */
    public static abstract class AbstractModelParameters implements ModelParameters {
        //number of data points used for training
        private Integer n = 0;

        //number of features in data points used for training
        private Integer d = 0;

        /**
         * Protected constructor which accepts as argument the DatabaseConnector.
         * 
         * @param dbc 
         */
        public AbstractModelParameters(DatabaseConnector dbc) {
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
         * Getter for the dimension of the dataset used in training.
         * 
         * @return 
         */
        public Integer getD() {
            return d;
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
            for(Field field : ReflectionMethods.getAllFields(new LinkedList<>(), this.getClass())){
                //if the field is annotated with BigMap
                if (field.isAnnotationPresent(BigMap.class)) {
                    initializeBigMapField(dbc, field);
                }
            }
        }

        /**
         * Initializes a field which is marked as BigMap.
         *
         * @param dbc
         * @param field
         */
        private void initializeBigMapField(DatabaseConnector dbc, Field field) {
            field.setAccessible(true);

            try {
                BigMap a = field.getAnnotation(BigMap.class);
                field.set(this, dbc.getBigMap(field.getName(), a.keyClass(), a.valueClass(), a.mapType(), a.storageHint(), a.concurrent(), false));
            }
            catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * The AbstractTrainingParameters class stores the parameters that can be 
     * changed  before training the algorithm.
     */
    public static abstract class AbstractTrainingParameters implements TrainingParameters {
                     
    }
    
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
     * The basic Constructor of all BaseTrainable classes.
     * 
     * @param baseDBname
     * @param conf 
     * @param kbClass 
     * @param kbSubtypeClasses 
     */
    protected AbstractTrainer(String baseDBname, Configuration conf, Class<? extends DoubleKnowledgeBase> kbClass, Class<? extends Serializable>... kbSubtypeClasses) {
        String methodName = this.getClass().getSimpleName();
        String dbNameSeparator = conf.getDbConfig().getDBnameSeparator();
        if(!baseDBname.contains(methodName+dbNameSeparator)) { //patch for the K-fold cross validation which already contains the name of the algorithm in the dbname
            baseDBname += dbNameSeparator + methodName;
        }
        
        dbName = baseDBname;
        
        knowledgeBase = (KB) KnowledgeBase.newInstance(kbClass, dbName, conf, kbSubtypeClasses);
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
        
        AbstractModelParameters modelParameters = (AbstractModelParameters) kb().getModelParameters();
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
