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
package com.datumbox.framework.machinelearning.common.abstracts.modelers;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.abstracts.validators.AbstractValidator;

/**
 * Abstract Class for recommender algorithms.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public abstract class AbstractRecommender<MP extends AbstractRecommender.ModelParameters, TP extends AbstractRecommender.TrainingParameters, VM extends AbstractRecommender.ValidationMetrics> extends AbstractAlgorithm<MP, TP, VM> {
    
    /** {@inheritDoc} */
    public static abstract class ModelParameters extends AbstractAlgorithm.ModelParameters {

        /** 
         * @param dbc
         * @see com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseModelParameters#BaseModelParameters(com.datumbox.common.persistentstorage.interfaces.DatabaseConnector) 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
    }
    
    /** {@inheritDoc} */
    public static abstract class TrainingParameters extends AbstractAlgorithm.TrainingParameters {
        
    } 
    
    /** {@inheritDoc} */
    public static abstract class ValidationMetrics extends AbstractAlgorithm.ValidationMetrics {
  
    }
    
    /** 
     * @param dbName
     * @param dbConf
     * @param mpClass
     * @param tpClass
     * @param vmClass
     * @param modelValidator
     * @see com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseTrainable#BaseTrainable(java.lang.String, com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration, java.lang.Class, java.lang.Class)  
     */
    protected AbstractRecommender(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass, AbstractValidator<MP, TP, VM> modelValidator) {
        super(dbName, dbConf, mpClass, tpClass, vmClass, modelValidator);
    } 
    
}
