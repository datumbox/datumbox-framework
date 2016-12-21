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
import com.datumbox.framework.common.utilities.RandomGenerator;
import com.datumbox.framework.common.utilities.ReflectionMethods;
import com.datumbox.framework.core.machinelearning.common.dataobjects.KnowledgeBase;
import com.datumbox.framework.core.machinelearning.common.interfaces.ModelParameters;
import com.datumbox.framework.core.machinelearning.common.interfaces.TrainingParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * Base class for every Trainable Algorithm of the Framework. This includes Machine Learning
 * Models, Data Transformers, Feature Selectors etc.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class AbstractTrainer<MP extends AbstractTrainer.AbstractModelParameters, TP extends AbstractTrainer.AbstractTrainingParameters> implements Trainable<MP, TP> {
       
    /**
     * Base class for every ModelParameter class in the framework. It automatically
     * initializes all the BidMap fields by using reflection.
     * 
     * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
     */
    public static abstract class AbstractModelParameters implements ModelParameters {

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
     * The KnowledgeBase instance of the algorithm. 
     */
    protected final KnowledgeBase<MP, TP> knowledgeBase;

    /**
     * Constructor which is called on model initialization before training.
     *
     * @param trainingParameters
     * @param conf
     */
    protected AbstractTrainer(TP trainingParameters, Configuration conf) {
        String knowledgeBaseName = createKnowledgeBaseName("kb_" + RandomGenerator.getThreadLocalRandomUnseeded().nextLong());
        knowledgeBase = new KnowledgeBase<>(knowledgeBaseName, conf, trainingParameters);
    }

    /**
     * Constructor which is called when we pre-trained load persisted models.
     *
     * @param dbName
     * @param conf
     */
    protected AbstractTrainer(String dbName, Configuration conf) {
        String knowledgeBaseName = createKnowledgeBaseName(dbName);
        knowledgeBase = new KnowledgeBase<>(knowledgeBaseName, conf);
    }
    
    /** {@inheritDoc} */
    @Override
    public MP getModelParameters() {
        return knowledgeBase.getModelParameters();
    } 
    
    /** {@inheritDoc} */
    @Override
    public TP getTrainingParameters() {
        return knowledgeBase.getTrainingParameters();
    }
    
    /** {@inheritDoc} */
    @Override
    public void fit(Dataframe trainingData) {
        logger.info("fit()");
        
        //reset knowledge base
        knowledgeBase.clear();

        _fit(trainingData);
    }

    /** {@inheritDoc} */
    @Override
    public void save(String dbName) {
        String knowledgeBaseName = createKnowledgeBaseName(dbName);
        knowledgeBase.save(knowledgeBaseName);
    }

    /** {@inheritDoc} */
    @Override
    public void delete() {
        knowledgeBase.delete();
    }
            
    /** {@inheritDoc} */
    @Override
    public void close() {
        knowledgeBase.close();
    }
    
    /**
     * This method estimates the actual coefficients of the algorithm.
     * 
     * @param trainingData 
     */
    protected abstract void _fit(Dataframe trainingData);

    /**
     * Generates a name for the KnowledgeBase.
     *
     * @param dbName
     * @return
     */
    protected final String createKnowledgeBaseName(String dbName) {
        return dbName + "_" + getClass().getSimpleName();
    }
}
