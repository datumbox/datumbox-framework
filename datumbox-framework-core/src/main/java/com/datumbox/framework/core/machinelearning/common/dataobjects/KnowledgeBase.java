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
package com.datumbox.framework.core.machinelearning.common.dataobjects;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.core.machinelearning.common.interfaces.ModelParameters;
import com.datumbox.framework.core.machinelearning.common.interfaces.TrainingParameters;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 * The KnowledgeBase stores internally the training and model parameters of the algorithm.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public class KnowledgeBase<MP extends ModelParameters, TP extends TrainingParameters> implements AutoCloseable {

    /**
     * The database configuration of the Permanent Storage.
     */
    private final Configuration conf;

    /**
     * The connector to the Permanent Storage.
     */
    private final DatabaseConnector dbc;

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
     * @param dbName
     * @param conf
     * @param trainingParameters
     */
    //FIXME: Perhaps this version will not get dbName and use a tmp instead. The other constructor will take the name used in the save().
    public KnowledgeBase(String dbName, Configuration conf, TP trainingParameters) {
        this.conf = conf;
        dbc = this.conf.getDbConfig().getConnector(dbName);

        this.trainingParameters = trainingParameters;
        modelParameters = ModelParameters.newInstance(trainingParameters.getMPClass(), dbc);
    }

    /**
     * Constructor which is called when we pre-trained load persisted models.
     *
     * @param dbName
     * @param conf
     */
    @SuppressWarnings("unchecked")
    public KnowledgeBase(String dbName, Configuration conf) {
        this.conf = conf;
        dbc = this.conf.getDbConfig().getConnector(dbName);

        trainingParameters = (TP) dbc.loadObject("trainingParameters", TrainingParameters.class);
        modelParameters = (MP) dbc.loadObject("modelParameters", ModelParameters.class);
    }

    /**
     * Getter for the Database Connector.
     *
     * @return
     */
    public DatabaseConnector getDbc() {
        return dbc;
    }

    /**
     * Getter for the Configuration.
     *
     * @return
     */
    public Configuration getConf() {
        return conf;
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
     * Saves the KnowledgeBase to the permanent storage.
     */
    public void save() {
        dbc.saveObject("modelParameters", modelParameters);
        dbc.saveObject("trainingParameters", trainingParameters);
    }

    /**
     * Deletes the database of the algorithm and closes the connection to the
     * permanent storage.
     */
    public void delete() {
        dbc.clear();
        close();
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

    /**
     * Clears the KnowledgeBase object by deleting all its data, while keeping
     * open the connection to the permanent storage.
     */
    public void clear() {
        dbc.clear();
        modelParameters = ModelParameters.newInstance(trainingParameters.getMPClass(), dbc);
    }
}
