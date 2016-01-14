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

import com.datumbox.common.Configuration;
import com.datumbox.framework.machinelearning.common.abstracts.validators.AbstractValidator;

/**
 * Base Class for all the Topic Modeling algorithms.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public abstract class AbstractTopicModeler<MP extends AbstractTopicModeler.AbstractModelParameters, TP extends AbstractTopicModeler.AbstractTrainingParameters, VM extends AbstractTopicModeler.AbstractValidationMetrics> extends AbstractModeler<MP, TP, VM> {
    
    /** 
     * @param dbName
     * @param conf
     * @param mpClass
     * @param tpClass
     * @param vmClass
     * @param modelValidator
     * @see com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer#AbstractTrainer(java.lang.String, com.datumbox.common.Configuration, java.lang.Class, java.lang.Class...)  
     */
    protected AbstractTopicModeler(String dbName, Configuration conf, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass, AbstractValidator<MP, TP, VM> modelValidator) {
        super(dbName, conf, mpClass, tpClass, vmClass, modelValidator);
    } 
    
}