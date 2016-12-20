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
package com.datumbox.framework.core.machinelearning.common.abstracts.datatransformers;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;

/**
 * Base class for all the Data Transformers of the framework.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class AbstractTransformer<MP extends AbstractTransformer.AbstractModelParameters, TP extends AbstractTransformer.AbstractTrainingParameters> extends AbstractTrainer<MP, TP> {

    /**
     * @param dbName
     * @param conf
     * @param trainingParameters
     * @see AbstractTrainer#AbstractTrainer(java.lang.String, Configuration, TP)
     */
    protected AbstractTransformer(String dbName, Configuration conf, TP trainingParameters) {
        super(dbName, conf, trainingParameters);
    }

    /**
     * @param dbName
     * @param conf
     * @see AbstractTrainer#AbstractTrainer(java.lang.String, Configuration)
     */
    protected AbstractTransformer(String dbName, Configuration conf) {
        super(dbName, conf);
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
     * Transforms and Normalizes the data of the provided dataset. The transformations are
     * non-reversible operations of on the dataset, while normalizations are 
     * are reversible.
     * 
     * @param newData 
     */
    public void transform(Dataframe newData) {
        logger.info("transform()");

        _convert(newData); 
        _normalize(newData);
    }
    
    /**
     * Denormalizes the data of the provided dataset.
     * 
     * @param data 
     */
    public void denormalize(Dataframe data) {
        logger.info("denormalize()");

        _denormalize(data);
    }
    
    /**
     * Converts the data (adding/modifying/removing columns). The conversions 
     * are not possible to be rolledback.
     * 
     * @param data 
     */
    protected abstract void _convert(Dataframe data);
    
    /**
     * Normalizes the data by modifying the columns. The changes should be 
     * possible to be rolledback (denormalized). 
     * 
     * @param data 
     */
    protected abstract void _normalize(Dataframe data);
    
    /**
     * Denormalizes the data by undoing the modifications performed by normilize().
     * 
     * @param data 
     */
    protected abstract void _denormalize(Dataframe data);
    
}
