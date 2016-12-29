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
package com.datumbox.framework.core.machinelearning.common.abstracts.transformers;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.concurrency.ForkJoinStream;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.common.storageengines.interfaces.StorageEngine;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.interfaces.Parallelizable;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Base class for all the Data Transformers of the framework.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class AbstractTransformer<MP extends AbstractTransformer.AbstractModelParameters, TP extends AbstractTransformer.AbstractTrainingParameters> extends AbstractTrainer<MP, TP> implements Parallelizable {

    /** {@inheritDoc} */
    public abstract static class AbstractModelParameters extends AbstractTrainer.AbstractModelParameters {

        /**
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected AbstractModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
        }

    }

    /** {@inheritDoc} */
    public static abstract class AbstractTrainingParameters extends AbstractTrainer.AbstractTrainingParameters {
        private Set<Object> transformedColumns;

        /**
         * Getter for the transformed columns.
         *
         * @return
         */
        public Set<Object> getTransformedColumns() {
            return transformedColumns;
        }

        /**
         * Setter for the set of transformed columns. This option limits the columns on which we apply the transformation.
         * If this is null then the transformation is applied to all the eligible columns of the Dataset.
         *
         * @param transformedColumns
         */
        public void setTransformedColumns(Set<Object> transformedColumns) {
            this.transformedColumns = transformedColumns;
        }
    }

    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainer.AbstractTrainingParameters, Configuration)
     */
    protected AbstractTransformer(TP trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
        streamExecutor = new ForkJoinStream(knowledgeBase.getConfiguration().getConcurrencyConfiguration());
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected AbstractTransformer(String storageName, Configuration configuration) {
        super(storageName, configuration);
        streamExecutor = new ForkJoinStream(knowledgeBase.getConfiguration().getConcurrencyConfiguration());
    }

    private boolean parallelized = true;

    /**
     * This executor is used for the parallel processing of streams with custom
     * Thread pool.
     */
    protected final ForkJoinStream streamExecutor;

    /** {@inheritDoc} */
    @Override
    public boolean isParallelized() {
        return parallelized;
    }

    /** {@inheritDoc} */
    @Override
    public void setParallelized(boolean parallelized) {
        this.parallelized = parallelized;
    }

    /**
     * Returns a set with the supported DataTypes of the transformer.
     *
     * @return
     */
    protected abstract Set<TypeInference.DataType> getSupportedTypes();

    /**
     * Returns a Stream with the columns that should be transformed.
     *
     * @param data
     * @return
     */
    protected Stream<Object> getTransformedColumns(Dataframe data) {
        Set<Object> transformedColumns = knowledgeBase.getTrainingParameters().getTransformedColumns();
        Map<Object, TypeInference.DataType> xDataTypes = data.getXDataTypes();
        Set<TypeInference.DataType> supportedTypes = getSupportedTypes();

        if(transformedColumns == null) {
            return xDataTypes.entrySet().stream()
                .filter(e -> supportedTypes.contains(e.getValue()))
                .map(e -> e.getKey());
        }
        else {
            return transformedColumns.stream()
                .filter(c -> supportedTypes.contains(xDataTypes.get(c)));
        }
    }

    /**
     * Fits, transforms and normalizes the data of the provided dataset.
     * 
     * @param trainingData
     */
    public void fit_transform(Dataframe trainingData) {
        fit(trainingData);
        transform(trainingData);
    }
    
    /**
     * Applies an irreversible trasformation to the the provided dataset.
     * 
     * @param newData 
     */
    public void transform(Dataframe newData) {
        logger.info("transform()");

        _transform(newData);
    }

    /**
     * The actual implementation of the transformation.
     *
     * @param newData
     */
    protected abstract void _transform(Dataframe newData);

}
