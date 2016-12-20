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
package com.datumbox.framework.core.machinelearning.datatransformation;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.datatransformers.AbstractDummyMinMaxTransformer;

import java.util.Map;

/**
 * Dummy XY MinMax Normalizer: Transforms the X vector and Y values of the 
 * Records to the 0.0-1.0 scale and builds dummy variables when non-numerics are
 * detected. This normalizer can be used in continuous regression models.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class DummyXYMinMaxNormalizer extends AbstractDummyMinMaxTransformer<DummyXYMinMaxNormalizer.ModelParameters, DummyXYMinMaxNormalizer.TrainingParameters> {

    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractDummyMinMaxTransformer.AbstractModelParameters {
        private static final long serialVersionUID = 1L;

        /**
         * @param dbc
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(DatabaseConnector)
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }

    }

    /** {@inheritDoc} */
    public static class TrainingParameters extends AbstractDummyMinMaxTransformer.AbstractTrainingParameters {
        private static final long serialVersionUID = 1L;

    }

    /**
     * @param dbName
     * @param conf
     * @param trainingParameters
     * @see AbstractTrainer#AbstractTrainer(String, Configuration, AbstractTrainer.AbstractTrainingParameters)
     */
    protected DummyXYMinMaxNormalizer(String dbName, Configuration conf, TrainingParameters trainingParameters) {
        super(dbName, conf, trainingParameters);
    }

    /**
     * @param dbName
     * @param conf
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected DummyXYMinMaxNormalizer(String dbName, Configuration conf) {
        super(dbName, conf);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        Map<Object, Double> minColumnValues = knowledgeBase.getModelParameters().getMinColumnValues();
        Map<Object, Double> maxColumnValues = knowledgeBase.getModelParameters().getMaxColumnValues();
        
        fitX(trainingData, minColumnValues, maxColumnValues);
        fitY(trainingData, minColumnValues, maxColumnValues);
        
        fitDummy(trainingData, knowledgeBase.getModelParameters().getReferenceLevels());
    }

    /** {@inheritDoc} */
    @Override
    protected void _convert(Dataframe data) {
        transformDummy(data, knowledgeBase.getModelParameters().getReferenceLevels());
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _normalize(Dataframe data) {
        Map<Object, Double> minColumnValues = knowledgeBase.getModelParameters().getMinColumnValues();
        Map<Object, Double> maxColumnValues = knowledgeBase.getModelParameters().getMaxColumnValues();

        normalizeX(data, minColumnValues, maxColumnValues);
        normalizeY(data, minColumnValues, maxColumnValues);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _denormalize(Dataframe data) {
        Map<Object, Double> minColumnValues = knowledgeBase.getModelParameters().getMinColumnValues();
        Map<Object, Double> maxColumnValues = knowledgeBase.getModelParameters().getMaxColumnValues();

        denormalizeX(data, minColumnValues, maxColumnValues);
        denormalizeY(data, minColumnValues, maxColumnValues);
    }
       
}
