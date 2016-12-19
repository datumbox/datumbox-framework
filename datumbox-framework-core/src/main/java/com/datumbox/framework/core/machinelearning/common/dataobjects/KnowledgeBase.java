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
    protected final DatabaseConnector dbc;

    /**
     * The class of the ModelParameters class of the algorithm.
     */
    private final Class<MP> mpClass;

    /**
     * The class of the TrainingParameters class of the algorithm.
     */
    private final Class<TP> tpClass;

    /**
     * The ModelParameters object of the algorithm.
     */
    private MP modelParameters;

    /**
     * The TrainingParameters object of the algorithm.
     */
    private TP trainingParameters;

    /**
     * Public constructor of the object.
     *
     * @param dbName
     * @param conf
     * @param mpClass
     * @param tpClass
     */
    public KnowledgeBase(String dbName, Configuration conf, Class<MP> mpClass, Class<TP> tpClass) {
        this.conf = conf;

        dbc = this.conf.getDbConfig().getConnector(dbName);

        this.mpClass = mpClass;
        this.tpClass = tpClass;

        if(dbc.existsObject("modelParameters") && dbc.existsObject("trainingParameters")) {
            modelParameters = dbc.loadObject("modelParameters", mpClass);
            trainingParameters = dbc.loadObject("trainingParameters", tpClass);
        }
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
     * Saves the KnowledgeBase to the permanent storage.
     */
    public void save() {
        if(modelParameters == null || trainingParameters == null) {
            throw new IllegalArgumentException("Can't save an empty KnowledgeBase.");
        }

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

    /**
     * Clears the KnowledgeBase object by deleting all its data, while keeping
     * open the connection to the permanent storage.
     */
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

    /**
     * Getter for the Training Parameters.
     *
     * @return
     */
    public TP getTrainingParameters() {
        return trainingParameters;
    }

    /**
     * Setter for the Training Parameters.
     *
     * @param trainingParameters
     */
    public void setTrainingParameters(TP trainingParameters) {
        this.trainingParameters = trainingParameters;
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
     * Setter for the Model Parameters.
     *
     * @param modelParameters
     */
    public void setModelParameters(MP modelParameters) {
        this.modelParameters = modelParameters;
    }

}
