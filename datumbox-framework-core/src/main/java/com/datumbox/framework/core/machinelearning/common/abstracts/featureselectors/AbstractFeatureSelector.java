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
package com.datumbox.framework.core.machinelearning.common.abstracts.featureselectors;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;

/**
 * Base class for all the Feature Selectors of the framework.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class AbstractFeatureSelector<MP extends AbstractFeatureSelector.AbstractModelParameters, TP extends AbstractFeatureSelector.AbstractTrainingParameters> extends AbstractTrainer<MP, TP> {

    /**
     * @param trainingParameters
     * @param conf
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainingParameters, Configuration)
     */
    protected AbstractFeatureSelector(TP trainingParameters, Configuration conf) {
        super(trainingParameters, conf);
    }

    /**
     * @param dbName
     * @param conf
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected AbstractFeatureSelector(String dbName, Configuration conf) {
        super(dbName, conf);
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
     * @param newdata 
     */
    protected abstract void _transform(Dataframe newdata);
}
