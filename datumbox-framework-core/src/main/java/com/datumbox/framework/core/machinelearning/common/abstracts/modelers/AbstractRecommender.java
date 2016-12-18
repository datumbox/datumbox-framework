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
import com.datumbox.framework.core.machinelearning.modelselection.metrics.RecommendationMetrics;
import com.datumbox.framework.core.machinelearning.modelselection.splitters.TemporaryKFold;

/**
 * Abstract Class for recommender algorithms.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class AbstractRecommender<MP extends AbstractRecommender.AbstractModelParameters, TP extends AbstractRecommender.AbstractTrainingParameters> extends AbstractModeler<MP, TP> {
    
    /** 
     * @param dbName
     * @param conf
     * @param mpClass
     * @param tpClass
     * @see AbstractModeler#AbstractModeler(java.lang.String, Configuration, java.lang.Class, java.lang.Class)
     */
    protected AbstractRecommender(String dbName, Configuration conf, Class<MP> mpClass, Class<TP> tpClass) {
        super(dbName, conf, mpClass, tpClass);
    }

    //TODO: remove this once we create the save/load
    public RecommendationMetrics validate(Dataframe testingData) {
        logger.info("validate()");

        predict(testingData);

        return new RecommendationMetrics(testingData);
    }
    //TODO: remove this once we create the save/load
    public RecommendationMetrics kFoldCrossValidation(Dataframe trainingData, TP trainingParameters, int k) {
        logger.info("validate()");

        return new TemporaryKFold<>(RecommendationMetrics.class).validate(trainingData, k, dbName, knowledgeBase.getConf(), this.getClass(), trainingParameters);
    }
}
