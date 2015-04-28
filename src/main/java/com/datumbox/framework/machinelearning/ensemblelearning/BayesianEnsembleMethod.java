/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.machinelearning.ensemblelearning;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.bases.basemodels.BaseNaiveBayes;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class BayesianEnsembleMethod extends BaseNaiveBayes<BayesianEnsembleMethod.ModelParameters, BayesianEnsembleMethod.TrainingParameters, BayesianEnsembleMethod.ValidationMetrics> {
    //References: http://www.stanford.edu/class/cs124/lec/sentiment.pptx
    
    public static class ModelParameters extends BaseNaiveBayes.ModelParameters {

        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }

    } 

    
    public static class TrainingParameters extends BaseNaiveBayes.TrainingParameters {    

    } 

    
    public static class ValidationMetrics extends BaseNaiveBayes.ValidationMetrics {

    }
    
    public BayesianEnsembleMethod(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, BayesianEnsembleMethod.ModelParameters.class, BayesianEnsembleMethod.TrainingParameters.class, BayesianEnsembleMethod.ValidationMetrics.class);
        isBinarized = true;
    }
    
    @Override
    public void _fit(Dataset trainingData) {
        knowledgeBase.getTrainingParameters().setMultiProbabilityWeighted(false);
        super._fit(trainingData);
    }
    
}