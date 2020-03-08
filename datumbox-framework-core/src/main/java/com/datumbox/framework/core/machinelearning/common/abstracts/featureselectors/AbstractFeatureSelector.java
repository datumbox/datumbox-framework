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
package com.datumbox.framework.core.machinelearning.common.abstracts.featureselectors;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.concurrency.ForkJoinStream;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.interfaces.Parallelizable;

import java.util.Set;

/**
 * Base class for all the Feature Selectors of the framework.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class AbstractFeatureSelector<MP extends AbstractFeatureSelector.AbstractModelParameters, TP extends AbstractFeatureSelector.AbstractTrainingParameters> extends AbstractTrainer<MP, TP> implements Parallelizable {

    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainingParameters, Configuration)
     */
    protected AbstractFeatureSelector(TP trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
        streamExecutor = new ForkJoinStream(knowledgeBase.getConfiguration().getConcurrencyConfiguration());
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected AbstractFeatureSelector(String storageName, Configuration configuration) {
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
     * Fits and transforms the data of the provided dataset. 
     * 
     * @param trainingData
     */
    public void fit_transform(Dataframe trainingData) {
        fit(trainingData);
        transform(trainingData);
    }

    /** {@inheritDoc} */
    @Override
    public void fit(Dataframe trainingData) {
        Set<TypeInference.DataType> supportedYDataTypes = getSupportedYDataTypes();
        if(supportedYDataTypes != null && !supportedYDataTypes.contains(trainingData.getYDataType())) {
            throw new IllegalArgumentException("The response variable DataType of the Dataframe is not supported by this method.");
        }
        super.fit(trainingData);
    }

    /**
     * Performs feature selection on the provided dataset.
     * 
     * @param newData
     */
    public void transform(Dataframe newData) {
        logger.info("transform()");
        
        _transform(newData);
    }
    
    /**
     * Performs the filtering of the features.
     * 
     * @param newData
     */
    protected abstract void _transform(Dataframe newData);

    /**
     * Returns a set with the supported DataTypes of X (features).
     *
     * @return
     */
    protected abstract Set<TypeInference.DataType> getSupportedXDataTypes();

    /**
     * Returns a set with the supported DataTypes of Y (response variable) or null if the method is unsupervised.
     *
     * @return
     */
    protected abstract Set<TypeInference.DataType> getSupportedYDataTypes();

}
