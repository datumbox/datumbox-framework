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
package com.datumbox.framework.core.machinelearning.common.dataobjects;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.core.common.interfaces.Savable;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.core.machinelearning.common.interfaces.ModelParameters;
import com.datumbox.framework.core.machinelearning.common.interfaces.TrainingParameters;


/**
 * The KnowledgeBase stores internally the training and model parameters of the algorithm.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public class KnowledgeBase<MP extends ModelParameters, TP extends TrainingParameters> implements Savable {

    /**
     * The Configuration of the Storage Engine.
     */
    private final Configuration configuration;

    /**
     * The storage engine.
     */
    private final StorageEngine storageEngine;

    /**
     * The ModelParameters object of the algorithm.
     */
    private MP modelParameters;

    /**
     * The TrainingParameters object of the algorithm.
     */
    private final TP trainingParameters;

    /**
     * Constructor which is called on model initialization before training.
     *
     * @param storageName
     * @param configuration
     * @param trainingParameters
     */
    public KnowledgeBase(String storageName, Configuration configuration, TP trainingParameters) {
        this.configuration = configuration;
        storageEngine = this.configuration.getStorageConfiguration().createStorageEngine(storageName);

        this.trainingParameters = trainingParameters;
        modelParameters = ModelParameters.newInstance(trainingParameters.getMPClass(), storageEngine);
    }

    /**
     * Constructor which is called when we pre-trained load stored models.
     *
     * @param storageName
     * @param configuration
     */
    @SuppressWarnings("unchecked")
    public KnowledgeBase(String storageName, Configuration configuration) {
        this.configuration = configuration;
        storageEngine = this.configuration.getStorageConfiguration().createStorageEngine(storageName);

        trainingParameters = (TP) storageEngine.loadObject("trainingParameters", TrainingParameters.class);
        modelParameters = (MP) storageEngine.loadObject("modelParameters", ModelParameters.class);
    }

    /**
     * Getter for the Storage Engine.
     *
     * @return
     */
    public StorageEngine getStorageEngine() {
        return storageEngine;
    }

    /**
     * Getter for the Configuration.
     *
     * @return
     */
    public Configuration getConfiguration() {
        return configuration;
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
     * Getter for the Model Parameters.
     *
     * @return
     */
    public MP getModelParameters() {
        return modelParameters;
    }

    /**
     * Saves the KnowledgeBase using the storage engine.
     */
    public void save(String storageName) {
        //store the objects on storage
        storageEngine.saveObject("modelParameters", modelParameters);
        storageEngine.saveObject("trainingParameters", trainingParameters);

        //rename the storage
        storageEngine.rename(storageName);

        //reload the model parameters, necessary for the maps to point to the new location
        modelParameters = (MP) storageEngine.loadObject("modelParameters", ModelParameters.class);
    }

    /**
     * Deletes the storage of the algorithm and closes the storage engine.
     */
    public void delete() {
        storageEngine.clear();
        close();
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        try {
            storageEngine.close();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Clears the KnowledgeBase object by deleting all its data, while keeping
     * open the connection to the storage engine.
     */
    public void clear() {
        storageEngine.clear();
        modelParameters = ModelParameters.newInstance(trainingParameters.getMPClass(), storageEngine);
    }
}
