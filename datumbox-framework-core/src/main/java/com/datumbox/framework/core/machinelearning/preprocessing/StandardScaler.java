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
 * Rescales the numerical features of the dataset by subtracting the mean and dividing by the standard deviation.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class StandardScaler extends AbstractScaler<StandardScaler.ModelParameters, StandardScaler.TrainingParameters> {

    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractScaler.AbstractModelParameters {
        private static final long serialVersionUID = 1L;

        /**
         * The mean value of each numerical variable.
         */
        @BigMap(keyClass=Object.class, valueClass=Double.class, mapType= StorageEngine.MapType.HASHMAP, storageHint= StorageEngine.StorageHint.IN_MEMORY, concurrent=true)
        private Map<Object, Double> meanColumnValues;

        /**
         * The standard deviation value of each numerical variable.
         */
        @BigMap(keyClass=Object.class, valueClass=Double.class, mapType= StorageEngine.MapType.HASHMAP, storageHint= StorageEngine.StorageHint.IN_MEMORY, concurrent=true)
        private Map<Object, Double> stdColumnValues;

        /**
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected ModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
        }

        /**
         * Getter for the mean values of the columns.
         *
         * @return
         */
        public Map<Object, Double> getMeanColumnValues() {
            return meanColumnValues;
        }

        /**
         * Setter for the mean values of the columns.
         *
         * @param meanColumnValues
         */
        protected void setMeanColumnValues(Map<Object, Double> meanColumnValues) {
            this.meanColumnValues = meanColumnValues;
        }

        /**
         * Getter for the std values of the columns.
         *
         * @return
         */
        public Map<Object, Double> getStdColumnValues() {
            return stdColumnValues;
        }

        /**
         * Setter for the std values of the columns.
         *
         * @param stdColumnValues
         */
        protected void setStdColumnValues(Map<Object, Double> stdColumnValues) {
            this.stdColumnValues = stdColumnValues;
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
    protected StandardScaler(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected StandardScaler(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }

    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        Map<Object, Double> meanColumnValues = modelParameters.getMeanColumnValues();
        Map<Object, Double> stdColumnValues = modelParameters.getStdColumnValues();
        boolean scaleResponse = knowledgeBase.getTrainingParameters().getScaleResponse();

        Set<TypeInference.DataType> supportedXDataTypes = getSupportedXDataTypes();
        Stream<Object> transformedColumns = trainingData.getXDataTypes().entrySet().stream()
                .filter(e -> supportedXDataTypes.contains(e.getValue()))
                .map(e -> e.getKey());

        streamExecutor.forEach(StreamMethods.stream(transformedColumns, isParallelized()), column -> {
            FlatDataCollection columnValues = trainingData.getXColumn(column).toFlatDataCollection();

            meanColumnValues.put(column, Descriptives.mean(columnValues));
            stdColumnValues.put(column, Descriptives.std(columnValues, true));
        });

        if(scaleResponse && trainingData.getYDataType() == TypeInference.DataType.NUMERICAL) {
            FlatDataCollection columnValues = trainingData.getYColumn().toFlatDataCollection();

            meanColumnValues.put(Dataframe.COLUMN_NAME_Y, Descriptives.mean(columnValues));
            stdColumnValues.put(Dataframe.COLUMN_NAME_Y, Descriptives.std(columnValues, true));
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void _transform(Dataframe newData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        Map<Object, Double> meanColumnValues = modelParameters.getMeanColumnValues();
        Map<Object, Double> stdColumnValues = modelParameters.getStdColumnValues();
        boolean scaleResponse = knowledgeBase.getTrainingParameters().getScaleResponse() && meanColumnValues.containsKey(Dataframe.COLUMN_NAME_Y);

        streamExecutor.forEach(StreamMethods.stream(newData.entries(), isParallelized()), e -> {
            Record r = e.getValue();
            AssociativeArray xData = r.getX().copy();
            Object yData = r.getY();

            boolean modified = false;
            for(Object column : r.getX().keySet()) {
                Double mean = meanColumnValues.get(column);
                if(mean == null) {
                    continue;
                }
                Object value = xData.remove(column);
                if(value != null) {
                    Double std = stdColumnValues.get(column);
                    xData.put(column, scale(TypeInference.toDouble(value), mean, std));
                }
                modified = true;
            }

            if(scaleResponse && yData != null) {
                Double value = TypeInference.toDouble(yData);
                Double mean = meanColumnValues.get(Dataframe.COLUMN_NAME_Y);
                Double std = stdColumnValues.get(Dataframe.COLUMN_NAME_Y);

                yData = scale(value, mean, std);
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
     * @param mean
     * @param std
     * @return
     */
    private Double scale(Double value, Double mean, Double std) {
        if(std.equals(0.0)) {
            if(value > mean) {
                return 1.0;
            }
            else if(value < mean) {
                return -1.0;
            }
            else {
                return Math.signum(value);
            }
        }
        else {
            return (value-mean)/std;
        }
    }

}
