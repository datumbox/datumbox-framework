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

import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;

/**
 * Abstract class which is the base of every Continuous Feature Selection algorithm.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class AbstractContinuousFeatureSelector<MP extends AbstractContinuousFeatureSelector.ModelParameters, TP extends AbstractContinuousFeatureSelector.TrainingParameters> extends AbstractFeatureSelector<MP, TP> {

    /** {@inheritDoc} */
    public static abstract class ModelParameters extends AbstractFeatureSelector.ModelParameters {
        
        /** 
         * @param dbc
         * @see com.datumbox.framework.machinelearning.common.abstracts.AbstractModelParameters#AbstractModelParameters(com.datumbox.common.persistentstorage.interfaces.DatabaseConnector) 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
    }
    
    /** {@inheritDoc} */
    public static abstract class TrainingParameters extends AbstractFeatureSelector.TrainingParameters {
        
    }
    
    /** 
     * @param dbName
     * @param dbConf
     * @param mpClass
     * @param tpClass
     * @see com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer#AbstractTrainer(java.lang.String, com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration, java.lang.Class, java.lang.Class...)  
     */
    protected AbstractContinuousFeatureSelector(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass) {
        super(dbName, dbConf, mpClass, tpClass);
    }
    
}
