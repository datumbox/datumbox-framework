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
import com.datumbox.framework.core.machinelearning.common.abstracts.transformers.AbstractNumericalScaler;
import com.datumbox.framework.core.statistics.descriptivestatistics.Descriptives;

import java.util.Map;

/**
 * Rescales the numerical features of the dataset between 0 and 1.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MinMaxScaler extends AbstractNumericalScaler<MinMaxScaler.ModelParameters, MinMaxScaler.TrainingParameters> {

    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractNumericalScaler.AbstractModelParameters {
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
    public static class TrainingParameters extends AbstractNumericalScaler.AbstractTrainingParameters {
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

        streamExecutor.forEach(StreamMethods.stream(trainingData.getXDataTypes().entrySet().stream().filter(entry -> entry.getValue() == TypeInference.DataType.NUMERICAL), isParallelized()), entry -> {
            Object column = entry.getKey();

            FlatDataList columnValues = trainingData.getXColumn(column);
            Double max = Descriptives.max(columnValues.toFlatDataCollection());
            Double min = Descriptives.min(columnValues.toFlatDataCollection());

            minColumnValues.put(column, min);
            maxColumnValues.put(column, max);
        });

        if(scaleResponse && trainingData.getYDataType() == TypeInference.DataType.NUMERICAL) {
            FlatDataList columnValues = trainingData.getYColumn();
            Double max = Descriptives.max(columnValues.toFlatDataCollection());
            Double min = Descriptives.min(columnValues.toFlatDataCollection());

            minColumnValues.put(Dataframe.COLUMN_NAME_Y, min);
            maxColumnValues.put(Dataframe.COLUMN_NAME_Y, max);
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
            for(Map.Entry<Object,Double> entry : minColumnValues.entrySet()) {
                Object column = entry.getKey();
                Double value = xData.getDouble(column);
                if(value==null) { //if we have a missing value don't perform any scaling
                    continue;
                }

                Double min = entry.getValue();
                Double max = maxColumnValues.get(column);

                double normalizedValue;
                if(min.equals(max)) {
                    normalizedValue = (min>0.0)?1.0:0.0; //set it 0.0 ONLY if the feature is always inactive and 1.0 if it has a non-zero value
                }
                else {
                    normalizedValue = (value-min)/(max-min);
                }

                if(normalizedValue != 0.0) { //don't store zero values for memory reasons
                    xData.put(column, normalizedValue);
                    modified = true;
                }
            }

            if(scaleResponse && yData != null) {
                Double min = minColumnValues.get(Dataframe.COLUMN_NAME_Y);
                Double max = maxColumnValues.get(Dataframe.COLUMN_NAME_Y);

                if(min.equals(max)) {
                    yData = (min!=0.0)?1.0:0.0; //set it 0.0 ONLY if the feature is always inactive and 1.0 if it has a non-zero value
                }
                else {
                    yData = (TypeInference.toDouble(yData) -min)/(max-min);
                }

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

}
