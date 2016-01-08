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
package com.datumbox.framework.machinelearning.common.interfaces;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.abstracts.AbstractModelParameters;
import com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainingParameters;
import com.datumbox.framework.machinelearning.common.dataobjects.TripleKnowledgeBase;
import com.datumbox.framework.machinelearning.common.dataobjects.DoubleKnowledgeBase;
import java.io.Serializable;


/**
 * The KnowledgeBase stores internally everything that the algorithm learned during  
 * training. This interface is implemented by any KnowledgeBase object.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public interface KnowledgeBase<MP extends AbstractModelParameters, TP extends AbstractTrainingParameters> extends AutoCloseable {
    
    /**
     * Generates a new KnowledgeBase object based on the provided input.
     * 
     * @param <KB>
     * @param kbClass
     * @param dbName
     * @param dbConf
     * @param kbSubtypeClasses
     * @return 
     */
    public static <KB extends KnowledgeBase> KB newInstance(Class<KB> kbClass, String dbName, DatabaseConfiguration dbConf, Class<? extends Serializable>[] kbSubtypeClasses) {
        if(DoubleKnowledgeBase.class.equals(kbClass) && kbSubtypeClasses.length == 2) {
            return (KB) new DoubleKnowledgeBase(dbName, dbConf, kbSubtypeClasses[0], kbSubtypeClasses[1]);
        }
        else if(TripleKnowledgeBase.class.equals(kbClass) && kbSubtypeClasses.length == 3) {
            return (KB) new TripleKnowledgeBase(dbName, dbConf, kbSubtypeClasses[0], kbSubtypeClasses[1], kbSubtypeClasses[2]);
        }
        else {
            throw new IllegalArgumentException("Unsupported KnowledgeBase class.");
        }
    }
    
    /**
     * Getter for the Database Connector.
     * 
     * @return 
     */
    public DatabaseConnector getDbc();
    
    /**
     * Getter for the Database Configuration.
     * 
     * @return 
     */
    public DatabaseConfiguration getDbConf();

    /**
     * Saves the KnowledgeBase to the permanent storage.
     */
    public void save();
    
    /**
     * Loads the KnowledgeBase from the permanent storage.
     */
    public void load();
    
    /**
     * Deletes the database of the algorithm and closes the connection to the
     * permanent storage. 
     */
    public void delete();
    
    /**
     * Clears the KnowledgeBase object by deleting all its data, while keeping 
     * open the connection to the permanent storage. 
     */
    public void clear();

    /**
     * Getter for the Training Parameters.
     * 
     * @return 
     */
    public TP getTrainingParameters();

    /**
     * Setter for the Training Parameters.
     * 
     * @param trainingParameters 
     */
    public void setTrainingParameters(TP trainingParameters);

    /**
     * Getter for the Model Parameters.
     * 
     * @return 
     */
    public MP getModelParameters();
    
    /**
     * Setter for the Model Parameters.
     * 
     * @param modelParameters 
     */
    public void setModelParameters(MP modelParameters);
    
    /** {@inheritDoc} */
    @Override
    void close();
}
