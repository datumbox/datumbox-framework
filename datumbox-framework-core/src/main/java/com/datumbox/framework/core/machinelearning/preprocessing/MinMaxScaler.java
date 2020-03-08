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
import com.datumbox.framework.core.machinelearning.common.abstracts.transformers.AbstractScaler;
import com.datumbox.framework.core.statistics.descriptivestatistics.Descriptives;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Rescales the numerical features of the dataset between 0 and 1.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MinMaxScaler extends AbstractScaler<MinMaxScaler.ModelParameters, MinMaxScaler.TrainingParameters> {

    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractScaler.AbstractModelParameters {
        private static final long serialVersionUID = 1L;

        /**
         * The minimum value of each numerical variable.
         */
        @BigMap(keyClass=Object.class, valueClass=Double.class, mapType= StorageEngine.MapType.HASHMAP, storageHint= StorageEngine.StorageHint.IN_MEMORY, concurrent=true)
        private Map<Object, Double> minColumnValues;

        /**
         * The maximum value of each numerical variable.
         */
        @BigMap(keyClass=Object.class, valueClass=Double.class, mapType= StorageEngine.MapType.HASHMAP, storageHint= StorageEngine.StorageHint.IN_MEMORY, concurrent=true)
        private Map<Object, Double> maxColumnValues;

        /**
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected ModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
        }

        /**
         * Getter for the minimum values of the columns.
         *
         * @return
         */
        public Map<Object, Double> getMinColumnValues() {
            return minColumnValues;
        }

        /**
         * Setter for the minimum values of the columns.
         *
         * @param minColumnValues
         */
        protected void setMinColumnValues(Map<Object, Double> minColumnValues) {
            this.minColumnValues = minColumnValues;
        }

        /**
         * Getter for the maximum values of the columns.
         *
         * @return
         */
        public Map<Object, Double> getMaxColumnValues() {
            return maxColumnValues;
        }

        /**
         * Setter for the maximum values of the columns.
         *
         * @param maxColumnValues
         */
        protected void setMaxColumnValues(Map<Object, Double> maxColumnValues) {
            this.maxColumnValues = maxColumnValues;
        }

    }

    /** {@inheritDoc} */
    public static class TrainingParameters extends AbstractScaler.AbstractTrainingParameters {
        private static final long serialVersionUID = 1L;

    }

    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainer.AbstractTrainingParameters, Configuration)
     */
    protected MinMaxScaler(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected MinMaxScaler(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }

    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        Map<Object, Double> minColumnValues = modelParameters.getMinColumnValues();
        Map<Object, Double> maxColumnValues = modelParameters.getMaxColumnValues();
        boolean scaleResponse = knowledgeBase.getTrainingParameters().getScaleResponse();

        Set<TypeInference.DataType> supportedXDataTypes = getSupportedXDataTypes();
        Stream<Object> transformedColumns = trainingData.getXDataTypes().entrySet().stream()
                .filter(e -> supportedXDataTypes.contains(e.getValue()))
                .map(e -> e.getKey());

        streamExecutor.forEach(StreamMethods.stream(transformedColumns, isParallelized()), column -> {
            FlatDataCollection columnValues = trainingData.getXColumn(column).toFlatDataCollection();

            minColumnValues.put(column, Descriptives.min(columnValues));
            maxColumnValues.put(column, Descriptives.max(columnValues));
        });

        if(scaleResponse && trainingData.getYDataType() == TypeInference.DataType.NUMERICAL) {
            FlatDataCollection columnValues = trainingData.getYColumn().toFlatDataCollection();

            minColumnValues.put(Dataframe.COLUMN_NAME_Y, Descriptives.min(columnValues));
            maxColumnValues.put(Dataframe.COLUMN_NAME_Y, Descriptives.max(columnValues));
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void _transform(Dataframe newData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        Map<Object, Double> minColumnValues = modelParameters.getMinColumnValues();
        Map<Object, Double> maxColumnValues = modelParameters.getMaxColumnValues();
        boolean scaleResponse = knowledgeBase.getTrainingParameters().getScaleResponse() && minColumnValues.containsKey(Dataframe.COLUMN_NAME_Y);

        streamExecutor.forEach(StreamMethods.stream(newData.entries(), isParallelized()), e -> {
            Record r = e.getValue();
            AssociativeArray xData = r.getX().copy();
            Object yData = r.getY();

            boolean modified = false;
            for(Object column : r.getX().keySet()) {
                Double min = minColumnValues.get(column);
                if(min == null) {
                    continue;
                }
                Object value = xData.remove(column);
                if(value != null) {
                    Double max = maxColumnValues.get(column);
                    xData.put(column, scale(TypeInference.toDouble(value), min, max));
                }
                modified = true;
            }

            if(scaleResponse && yData != null) {
                Double value = TypeInference.toDouble(yData);
                Double min = minColumnValues.get(Dataframe.COLUMN_NAME_Y);
                Double max = maxColumnValues.get(Dataframe.COLUMN_NAME_Y);

                yData = scale(value, min, max);
                modified = true;
            }

            if(modified) {
                Integer rId = e.getKey();
                Record newR = new Record(xData, yData, r.getYPredicted(), r.getYPredictedProbabilities());

                //no modification on the actual columns takes place, safe to do.
                newData._unsafe_set(rId, newR);
            }
        });
    }

    /**
     * Performs the actual rescaling handling corner cases.
     *
     * @param value
     * @param min
     * @param max
     * @return
     */
    private Double scale(Double value, Double min, Double max) {
        if(min.equals(max)) {
            if(value>max) {
                return 1.0;
            }
            else if(value==max && value!=0.0) {
                return 1.0;
            }
            else {
                return 0.0;
            }
        }
        else {
            return (value-min)/(max-min);
        }
    }

}
