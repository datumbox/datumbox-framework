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
package com.datumbox.framework.core.machinelearning.ensemblelearning;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.algorithms.AbstractNaiveBayes;

/** 
 * Implementation of Bayesian Ensemble Method. This algorithm can be used to 
 * combine the responses of multiple different classifiers into one strong 
 * classifier.
 * 
 * References: 
 * http://www.stanford.edu/class/cs124/lec/sentiment.pptx
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class BayesianEnsembleMethod extends AbstractNaiveBayes<BayesianEnsembleMethod.ModelParameters, BayesianEnsembleMethod.TrainingParameters> {
    
    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractNaiveBayes.AbstractModelParameters {
        private static final long serialVersionUID = 1L;
        
        /** 
         * @param dbc
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(DatabaseConnector)
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }

    } 

    /** {@inheritDoc} */
    public static class TrainingParameters extends AbstractNaiveBayes.AbstractTrainingParameters { 
        private static final long serialVersionUID = 1L;

    }

    /**
     * @param dbName
     * @param conf
     * @param trainingParameters
     * @see AbstractTrainer#AbstractTrainer(String, Configuration, AbstractTrainer.AbstractTrainingParameters)
     */
    protected BayesianEnsembleMethod(String dbName, Configuration conf, TrainingParameters trainingParameters) {
        super(dbName, conf, trainingParameters);
    }

    /**
     * @param dbName
     * @param conf
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected BayesianEnsembleMethod(String dbName, Configuration conf) {
        super(dbName, conf);
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