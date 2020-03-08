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
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.transformers.AbstractScaler;

import java.util.Map;
import java.util.Set;

/**
 * Rescales the numerical features of the dataset between -1 and 1.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class BinaryScaler extends AbstractScaler<BinaryScaler.ModelParameters, BinaryScaler.TrainingParameters> {

    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractScaler.AbstractModelParameters {
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
    public static class TrainingParameters extends AbstractScaler.AbstractTrainingParameters {
        private static final long serialVersionUID = 1L;

        private double threshold = 0.0;

        /**
         * Getter for the threhold.
         *
         * @return
         */
        public double getThreshold() {
            return threshold;
        }

        /**
         * Setter for the threshold. Values less or equal to the threhold are turned into false and those greater
         * are turned into true.
         *
         * @param threshold
         */
        public void setThreshold(double threshold) {
            this.threshold = threshold;
        }
    }

    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainer.AbstractTrainingParameters, Configuration)
     */
    protected BinaryScaler(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected BinaryScaler(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }

    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {

    }

    /** {@inheritDoc} */
    @Override
    protected void _transform(Dataframe newData) {
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        boolean scaleResponse = trainingParameters.getScaleResponse() && newData.getYDataType() == TypeInference.DataType.NUMERICAL;
        double threshold = trainingParameters.getThreshold();

        Set<TypeInference.DataType> supportedXDataTypes = getSupportedXDataTypes();
        Map<Object, TypeInference.DataType> xDataTypes = newData.getXDataTypes();

        streamExecutor.forEach(StreamMethods.stream(newData.entries(), isParallelized()), e -> {
            Record r = e.getValue();
            AssociativeArray xData = r.getX().copy();
            Object yData = r.getY();

            boolean modified = false;
            for(Object column : r.getX().keySet()) {
                if(!supportedXDataTypes.contains(xDataTypes.get(column))) {
                    continue;
                }
                Object value = xData.remove(column);
                if(value != null) {
                    xData.put(column, scale(TypeInference.toDouble(value), threshold));
                }
                modified = true;
            }

            if(scaleResponse && yData != null) {
                Double value = TypeInference.toDouble(yData);

                yData = scale(value, threshold);
                modified = true;
            }

            if(modified) {
                Integer rId = e.getKey();
                Record newR = new Record(xData, yData, r.getYPredicted(), r.getYPredictedProbabilities());

                //we call below the recalculateMeta()
                newData._unsafe_set(rId, newR);
            }
        });

        //Reset Meta info
        newData.recalculateMeta();
    }

    /**
     * Performs the actual rescaling handling corner cases.
     *
     * @param value
     * @param threshold
     * @return
     */
    private Boolean scale(Double value, double threshold) {
        return value>threshold;
    }
}
