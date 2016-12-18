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
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;

/**
 * Base Class for Machine Learning algorithms.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class AbstractModeler<MP extends AbstractModeler.AbstractModelParameters, TP extends AbstractModeler.AbstractTrainingParameters> extends AbstractTrainer<MP, TP> {

    /**
     * @param baseDBname
     * @param conf
     * @param mpClass
     * @param tpClass
     * @see AbstractTrainer#AbstractTrainer(java.lang.String, Configuration, java.lang.Class, java.lang.Class)
     */
    protected AbstractModeler(String baseDBname, Configuration conf, Class<MP> mpClass, Class<TP> tpClass) {
        super(baseDBname, conf, mpClass, tpClass);
    }

    /**
     * Calculates the predictions for the newData and stores them in the provided
     * Dataframe.
     *
     * @param newData
     */
    public void predict(Dataframe newData) {
        logger.info("predict()");

        knowledgeBase.init();

        _predict(newData);
    }

    /**
     * Estimates the predictions for a new Dataframe.
     *
     * @param newData
     */
    protected abstract void _predict(Dataframe newData);
}
