/* 
 * Copyright (C) 2014 Vasilis Vryniotis <bbriniotis at datumbox.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.datumbox.framework.machinelearning.classification;

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
    
    public static final String SHORT_METHOD_NAME = "MulNB";
    
    public static class ModelParameters extends BaseNaiveBayes.ModelParameters {

    } 

    
    public static class TrainingParameters extends BaseNaiveBayes.TrainingParameters {    
        
    } 

    
    public static class ValidationMetrics extends BaseNaiveBayes.ValidationMetrics {

    }

    public MultinomialNaiveBayes(String dbName) {
        super(dbName, MultinomialNaiveBayes.ModelParameters.class, MultinomialNaiveBayes.TrainingParameters.class, MultinomialNaiveBayes.ValidationMetrics.class);
    }
    
    @Override
    public final String shortMethodName() {
        return SHORT_METHOD_NAME;
    }
    
}