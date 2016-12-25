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
import com.datumbox.framework.common.storageengines.interfaces.BigMap;
import com.datumbox.framework.common.storageengines.interfaces.StorageEngine;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.transformers.AbstractCategoricalEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Encodes the categorical columns of the dataset into booleans using the Corner Constraints encoding (also known
 * as set-to-zero).
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class CornerConstraintsEncoder extends AbstractCategoricalEncoder<CornerConstraintsEncoder.ModelParameters, CornerConstraintsEncoder.TrainingParameters> {

    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractCategoricalEncoder.AbstractModelParameters {
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
    public static class TrainingParameters extends AbstractCategoricalEncoder.AbstractTrainingParameters {
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

        Map<Object, TypeInference.DataType> columnTypes = trainingData.getXDataTypes();

        //find the referenceLevels for each categorical variable
        for(Record r : trainingData) {
            for(Map.Entry<Object, Object> entry: r.getX().entrySet()) {
                Object column = entry.getKey();
                if(covert2dummy(columnTypes.get(column))) {
                    referenceLevels.putIfAbsent(column, entry.getValue()); //This Map is an implementation of ConcurrentHashMap and we don't need a synchronized is needed.
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
                if(covert2dummy(columnTypes.get(column))==false) {
                    continue;
                }
                Object value = xData.remove(column); //remove the original column
                modified = true;

                Object referenceLevel= referenceLevels.get(column);

                if(referenceLevel != null && //not unknown variable
                        !referenceLevel.equals(value)) { //not equal to reference level

                    //create a new column
                    List<Object> newColumn = Arrays.asList(column,value);

                    //add a new dummy variable for this column-value combination
                    xData.put(newColumn, true);
                }
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

    /**
     * Checks whether the variable should be converted into dummy (boolean). Only
     * categorical and ordinal values are converted.
     *
     * @param columnType
     * @return
     */
    private boolean covert2dummy(TypeInference.DataType columnType) {
        return columnType==TypeInference.DataType.CATEGORICAL || columnType==TypeInference.DataType.ORDINAL;
    }

}
