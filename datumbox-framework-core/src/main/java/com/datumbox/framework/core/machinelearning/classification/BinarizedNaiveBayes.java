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
package com.datumbox.framework.core.machinelearning.classification;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.algorithms.AbstractNaiveBayes;

/**
 * The BinarizedNaiveBayes class implements an alternative version of Multinomial
 * Naive Bayes, in which the total number of counts of each activated feature
 * is clipped to 1.
 * 
 * References: 
 * http://blog.datumbox.com/machine-learning-tutorial-the-naive-bayes-text-classifier/
 * http://www.stanford.edu/class/cs124/lec/sentiment.pptx
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class BinarizedNaiveBayes extends AbstractNaiveBayes<BinarizedNaiveBayes.ModelParameters, BinarizedNaiveBayes.TrainingParameters> {
    
    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractNaiveBayes.AbstractModelParameters {
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
    public static class TrainingParameters extends AbstractNaiveBayes.AbstractTrainingParameters {    
        private static final long serialVersionUID = 1L;

    }

    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainer.AbstractTrainingParameters, Configuration)
     */
    protected BinarizedNaiveBayes(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected BinarizedNaiveBayes(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }

    /** {@inheritDoc} */
    protected boolean isBinarized() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        knowledgeBase.getTrainingParameters().setMultiProbabilityWeighted(false);
        super._fit(trainingData);
    }
}