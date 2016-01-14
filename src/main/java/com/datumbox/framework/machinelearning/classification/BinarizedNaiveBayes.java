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
package com.datumbox.framework.machinelearning.classification;

import com.datumbox.common.Configuration;
import com.datumbox.framework.machinelearning.common.abstracts.algorithms.AbstractNaiveBayes;
import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;

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
public class BinarizedNaiveBayes extends AbstractNaiveBayes<BinarizedNaiveBayes.ModelParameters, BinarizedNaiveBayes.TrainingParameters, BinarizedNaiveBayes.ValidationMetrics> {
    
    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractNaiveBayes.AbstractModelParameters {
        private static final long serialVersionUID = 1L;
        
        /** 
         * @param dbc
         * @see com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer.AbstractModelParameters#AbstractModelParameters(com.datumbox.common.persistentstorage.interfaces.DatabaseConnector) 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }

    } 
    
    /** {@inheritDoc} */
    public static class TrainingParameters extends AbstractNaiveBayes.AbstractTrainingParameters {    
        private static final long serialVersionUID = 1L;

    }
    
    /** {@inheritDoc} */
    public static class ValidationMetrics extends AbstractNaiveBayes.AbstractValidationMetrics {
        private static final long serialVersionUID = 1L;

    }
    
    /**
     * Public constructor of the algorithm.
     * 
     * @param dbName
     * @param conf 
     */
    public BinarizedNaiveBayes(String dbName, Configuration conf) {
        super(dbName, conf, BinarizedNaiveBayes.ModelParameters.class, BinarizedNaiveBayes.TrainingParameters.class, BinarizedNaiveBayes.ValidationMetrics.class, true);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        kb().getTrainingParameters().setMultiProbabilityWeighted(false);
        super._fit(trainingData);
    }
    
}