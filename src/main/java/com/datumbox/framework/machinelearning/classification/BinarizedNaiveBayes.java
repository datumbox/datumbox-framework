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
package com.datumbox.framework.machinelearning.classification;

import com.datumbox.framework.machinelearning.common.bases.basemodels.BaseNaiveBayes;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class BinarizedNaiveBayes extends BaseNaiveBayes<BinarizedNaiveBayes.ModelParameters, BinarizedNaiveBayes.TrainingParameters, BinarizedNaiveBayes.ValidationMetrics> {
    //References: http://www.stanford.edu/class/cs124/lec/sentiment.pptx
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    public static class ModelParameters extends BaseNaiveBayes.ModelParameters {

        public ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }

    } 

    
    public static class TrainingParameters extends BaseNaiveBayes.TrainingParameters {    

    } 

    
    public static class ValidationMetrics extends BaseNaiveBayes.ValidationMetrics {

    }

    protected static final boolean IS_BINARIZED = true;
    
    public BinarizedNaiveBayes(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, BinarizedNaiveBayes.ModelParameters.class, BinarizedNaiveBayes.TrainingParameters.class, BinarizedNaiveBayes.ValidationMetrics.class);
    }
    
    @Override
    protected void _fit(Dataset trainingData) {
        knowledgeBase.getTrainingParameters().setMultiProbabilityWeighted(false);
        super._fit(trainingData);
    }
    
}