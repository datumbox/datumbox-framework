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
package com.datumbox.framework.core.machinelearning.preprocessing;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.concurrency.StreamMethods;
import com.datumbox.framework.common.dataobjects.*;
import com.datumbox.framework.common.storageengines.interfaces.StorageEngine;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.transformers.AbstractCategoricalEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Encodes the categorical columns of the dataset into booleans using the One Hot Encoding method.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class OneHotEncoder extends AbstractCategoricalEncoder<OneHotEncoder.ModelParameters, OneHotEncoder.TrainingParameters> {

    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractCategoricalEncoder.AbstractModelParameters {
        private static final long serialVersionUID = 1L;

        /**
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected ModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
        }

    }

    /** {@inheritDoc} */
    public static class TrainingParameters extends AbstractCategoricalEncoder.AbstractTrainingParameters {
        private static final long serialVersionUID = 1L;

    }

    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainer.AbstractTrainingParameters, Configuration)
     */
    protected OneHotEncoder(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected OneHotEncoder(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }

    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        //does not learn anything
    }

    /** {@inheritDoc} */
    @Override
    protected void _transform(Dataframe newData) {
        Map<Object, TypeInference.DataType> columnTypes = newData.getXDataTypes();

        //Replace variables with dummy versions
        streamExecutor.forEach(StreamMethods.stream(newData.entries(), isParallelized()), e -> {
            Integer rId = e.getKey();
            Record r = e.getValue();

            AssociativeArray xData = r.getX().copy();

            boolean modified = false;
            for(Object column : r.getX().keySet()) {
                if(covert2dummy(columnTypes.get(column))==false) {
                    continue;
                }
                Object value = xData.remove(column); //remove the original column
                modified = true;

                //create a new column
                List<Object> newColumn = Arrays.asList(column,value);

                //add a new dummy variable for this column-value combination
                xData.put(newColumn, true);
            }

            if(modified) {
                Record newR = new Record(xData, r.getY(), r.getYPredicted(), r.getYPredictedProbabilities());

                //we call below the recalculateMeta()
                newData._unsafe_set(rId, newR);
            }
        });

        //Reset Meta info
        newData.recalculateMeta();
    }

}