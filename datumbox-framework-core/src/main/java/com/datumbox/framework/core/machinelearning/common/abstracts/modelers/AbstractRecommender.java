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
package com.datumbox.framework.core.machinelearning.common.abstracts.modelers;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.validators.AbstractValidator;

/**
 * Abstract Class for recommender algorithms.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public abstract class AbstractRecommender<MP extends AbstractRecommender.AbstractModelParameters, TP extends AbstractRecommender.AbstractTrainingParameters, VM extends AbstractRecommender.AbstractValidationMetrics> extends AbstractModeler<MP, TP, VM> {
    
    /** 
     * @param dbName
     * @param conf
     * @param mpClass
     * @param tpClass
     * @param vmClass
     * @param modelValidator
     * @see AbstractTrainer#AbstractTrainer(java.lang.String, Configuration, java.lang.Class, java.lang.Class...)
     */
    protected AbstractRecommender(String dbName, Configuration conf, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass, AbstractValidator<MP, TP, VM> modelValidator) {
        super(dbName, conf, mpClass, tpClass, vmClass, modelValidator);
    } 
    
}
