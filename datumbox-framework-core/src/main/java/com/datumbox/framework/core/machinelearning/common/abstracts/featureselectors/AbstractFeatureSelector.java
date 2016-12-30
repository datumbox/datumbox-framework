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
import com.datumbox.framework.common.concurrency.ForkJoinStream;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.utilities.SelectKth;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.interfaces.Parallelizable;

import java.util.Iterator;
import java.util.Map;

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


    /**
     * This method keeps the highest scoring features of the provided feature map
     * and removes all the others.
     *
     * @param featureScores
     * @param maxFeatures
     */
    protected void selectTopFeatures(Map<Object, Double> featureScores, Integer maxFeatures) {
        logger.debug("selectTopFeatures()");

        logger.debug("Estimating the minPermittedScore");
        Double minPermittedScore = SelectKth.largest(featureScores.values().iterator(), maxFeatures);

        //remove any entry with score less than the minimum permitted one
        logger.debug("Removing features with scores less than threshold");
        Iterator<Map.Entry<Object, Double>> it = featureScores.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Object, Double> entry = it.next();
            if(entry.getValue()<minPermittedScore) {
                it.remove();
            }
        }

        //if some extra features still exist (due to ties on the scores) remove some of those extra features
        int numOfExtraFeatures = featureScores.size()-maxFeatures;
        if(numOfExtraFeatures>0) {
            logger.debug("Removing extra features caused by ties");
            it = featureScores.entrySet().iterator();
            while(it.hasNext() && numOfExtraFeatures>0) {
                Map.Entry<Object, Double> entry = it.next();
                if(entry.getValue()-minPermittedScore<=0.0) { //DO NOT COMPARE THEM DIRECTLY USE SUBTRACTION!
                    it.remove();
                    --numOfExtraFeatures;
                }
            }
        }
    }
}
