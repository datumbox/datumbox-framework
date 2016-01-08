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
package com.datumbox.framework.machinelearning.common.abstracts.featureselectors;

import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;

import com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.machinelearning.common.abstracts.AbstractModelParameters;
import com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainingParameters;
import com.datumbox.framework.machinelearning.common.dataobjects.DoubleKnowledgeBase;

/**
 * Base class for all the Feature Selectors of the framework.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class AbstractFeatureSelector<MP extends AbstractFeatureSelector.ModelParameters, TP extends AbstractFeatureSelector.TrainingParameters> extends AbstractTrainer<MP, TP, DoubleKnowledgeBase<MP, TP>> {

    /**
     * The ModelParameters class stores the coefficients that were learned during
     * the training of the algorithm.
     */
    public static abstract class ModelParameters extends AbstractModelParameters {

        /** 
         * @param dbc
         * @see com.datumbox.framework.machinelearning.common.abstracts.AbstractModelParameters#AbstractModelParameters(com.datumbox.common.persistentstorage.interfaces.DatabaseConnector) 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
    }
    
    /**
     * The TrainingParameters class stores the parameters that can be changed
     * before training the algorithm.
     */
    public static abstract class TrainingParameters extends AbstractTrainingParameters {
        
    }
    
    /** 
     * @param dbName
     * @param dbConf
     * @param mpClass
     * @param tpClass
     * @see com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer#AbstractTrainer(java.lang.String, com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration, java.lang.Class, java.lang.Class...)  
     */
    protected AbstractFeatureSelector(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass) {
        super(dbName, dbConf, DoubleKnowledgeBase.class, mpClass, tpClass);
    }
    
    
    /**
     * Fits and transforms the data of the provided dataset. 
     * 
     * @param trainingData
     * @param trainingParameters 
     */
    public void fit_transform(Dataframe trainingData, TP trainingParameters) {
        fit(trainingData, trainingParameters);
        transform(trainingData);
    }
    
    /**
     * Performs feature selection on the provided dataset.
     * 
     * @param newData 
     */
    public void transform(Dataframe newData) {
        logger.info("transform()");
        
        kb().load();
        
        filterFeatures(newData);
    }
    
    /**
     * Performs the filtering of the features.
     * 
     * @param newdata 
     */
    protected abstract void filterFeatures(Dataframe newdata);
}
