/**
 * Copyright (C) 2013-2020 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
import com.datumbox.framework.common.storage.interfaces.BigMap;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.transformers.AbstractEncoder;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * Encodes the categorical columns of the dataset into booleans using the Corner Constraints encoding (also known
 * as set-to-zero).
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class CornerConstraintsEncoder extends AbstractEncoder<CornerConstraintsEncoder.ModelParameters, CornerConstraintsEncoder.TrainingParameters> {

    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractEncoder.AbstractModelParameters {
        private static final long serialVersionUID = 1L;

        /**
         * The reference levels of each categorical variable.
         */
        @BigMap(keyClass=Object.class, valueClass=Object.class, mapType= StorageEngine.MapType.HASHMAP, storageHint= StorageEngine.StorageHint.IN_MEMORY, concurrent=true)
        private Map<Object, Object> referenceLevels;

        /**
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected ModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
        }

        /**
         * Getter for the reference levels of the categorical variables.
         *
         * @return
         */
        public Map<Object, Object> getReferenceLevels() {
            return referenceLevels;
        }

        /**
         * Setter for the reference levels of the categorical variables.
         *
         * @param referenceLevels
         */
        protected void setReferenceLevels(Map<Object, Object> referenceLevels) {
            this.referenceLevels = referenceLevels;
        }
    }

    /** {@inheritDoc} */
    public static class TrainingParameters extends AbstractEncoder.AbstractTrainingParameters {
        private static final long serialVersionUID = 1L;

    }

    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainer.AbstractTrainingParameters, Configuration)
     */
    protected CornerConstraintsEncoder(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected CornerConstraintsEncoder(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }

    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        Map<Object, Object> referenceLevels = modelParameters.getReferenceLevels();

        Set<TypeInference.DataType> supportedXDataTypes = getSupportedXDataTypes();
        Map<Object, TypeInference.DataType> xDataTypes = trainingData.getXDataTypes();

        //find the referenceLevels for each supported variable
        for(Record r : trainingData) {
            for(Map.Entry<Object, Object> entry: r.getX().entrySet()) {
                Object column = entry.getKey();
                Object value = entry.getValue();
                if(value != null && supportedXDataTypes.contains(xDataTypes.get(column)) ) {
                    referenceLevels.putIfAbsent(column, value);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void _transform(Dataframe newData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        Map<Object, Object> referenceLevels = modelParameters.getReferenceLevels();

        Map<Object, TypeInference.DataType> columnTypes = newData.getXDataTypes();

        //Replace variables with dummy versions
        streamExecutor.forEach(StreamMethods.stream(newData.entries(), isParallelized()), e -> {
            Integer rId = e.getKey();
            Record r = e.getValue();

            AssociativeArray xData = r.getX().copy();

            boolean modified = false;
            for(Object column : r.getX().keySet()) {
                Object referenceLevel = referenceLevels.get(column);
                if(referenceLevel == null) {
                    continue;
                }
                Object value = xData.remove(column);
                if(value!= null && !referenceLevel.equals(value)) {
                    //add a new dummy variable for this column-value combination
                    xData.put(Arrays.asList(column,value), true);
                }
                modified = true;
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
