/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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

import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.bases.basemodels.BaseNaiveBayes;


/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class MultinomialNaiveBayes extends BaseNaiveBayes<MultinomialNaiveBayes.ModelParameters, MultinomialNaiveBayes.TrainingParameters, MultinomialNaiveBayes.ValidationMetrics> {
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

    public MultinomialNaiveBayes(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, MultinomialNaiveBayes.ModelParameters.class, MultinomialNaiveBayes.TrainingParameters.class, MultinomialNaiveBayes.ValidationMetrics.class);
    }
    
}