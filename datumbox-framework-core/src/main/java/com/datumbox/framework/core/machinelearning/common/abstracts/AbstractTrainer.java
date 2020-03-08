/**
 * Copyright (C) 2013-2020 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.machinelearning.common.interfaces.Trainable;
import com.datumbox.framework.common.storage.abstracts.BigMapHolder;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.common.utilities.RandomGenerator;
import com.datumbox.framework.core.machinelearning.common.dataobjects.KnowledgeBase;
import com.datumbox.framework.core.machinelearning.common.interfaces.ModelParameters;
import com.datumbox.framework.core.machinelearning.common.interfaces.TrainingParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     */
    public static abstract class AbstractModelParameters extends BigMapHolder implements ModelParameters {

        /**
         * Constructor of the ModelParameters that accepts a Storage Engine.
         * 
         * @param storageEngine
         */
        protected AbstractModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
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
     * Flag that indicates whether the trainer has been saved or loaded from disk.
     */
    private boolean stored;

    /**
     * Constructor which is called on model initialization before training.
     *
     * @param trainingParameters
     * @param configuration
     */
    protected AbstractTrainer(TP trainingParameters, Configuration configuration) {
        String knowledgeBaseName = createKnowledgeBaseName("kb" + RandomGenerator.getThreadLocalRandomUnseeded().nextLong(), configuration.getStorageConfiguration().getStorageNameSeparator());
        knowledgeBase = new KnowledgeBase<>(knowledgeBaseName, configuration, trainingParameters);
        stored = false;
    }

    /**
     * Constructor which is called when we pre-trained load stored models.
     *
     * @param storageName
     * @param configuration
     */
    protected AbstractTrainer(String storageName, Configuration configuration) {
        String knowledgeBaseName = createKnowledgeBaseName(storageName, configuration.getStorageConfiguration().getStorageNameSeparator());
        knowledgeBase = new KnowledgeBase<>(knowledgeBaseName, configuration);
        stored = true;
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
    public void save(String storageName) {
        logger.info("save()");

        String knowledgeBaseName = createKnowledgeBaseName(storageName, knowledgeBase.getConfiguration().getStorageConfiguration().getStorageNameSeparator());
        knowledgeBase.save(knowledgeBaseName);
        stored = true;
    }

    /** {@inheritDoc} */
    @Override
    public void delete() {
        logger.info("delete()");

        knowledgeBase.delete();
    }
            
    /** {@inheritDoc} */
    @Override
    public void close() {
        logger.info("close()");

        if(stored) {
            //if the trainer is stored in disk, just close the storage
            knowledgeBase.close();
        }
        else {
            //if not try to delete it in case temporary files remained on disk
            knowledgeBase.delete();
        }
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
     * @param storageName
     * @param separator
     * @return
     */
    protected final String createKnowledgeBaseName(String storageName, String separator) {
        return storageName + separator + getClass().getSimpleName();
    }
}
