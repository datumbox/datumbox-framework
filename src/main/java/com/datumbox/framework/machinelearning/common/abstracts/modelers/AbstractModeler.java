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
package com.datumbox.framework.machinelearning.common.abstracts.modelers;

import com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.machinelearning.common.abstracts.validators.AbstractValidator;
import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.framework.machinelearning.common.interfaces.ModelParameters;
import com.datumbox.framework.machinelearning.common.interfaces.ValidationMetrics;
import com.datumbox.framework.machinelearning.common.interfaces.KnowledgeBase;
import com.datumbox.framework.machinelearning.common.dataobjects.TripleKnowledgeBase;
import com.datumbox.framework.machinelearning.common.interfaces.TrainingParameters;

/**
 * Base Class for Machine Learning algorithms.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public abstract class AbstractModeler<MP extends ModelParameters, TP extends TrainingParameters, VM extends ValidationMetrics> extends AbstractTrainer<MP, TP, TripleKnowledgeBase<MP, TP, VM>> {
    
    private final AbstractValidator<MP, TP, VM> modelValidator;
    
    /**
     * The AbstractValidationMetrics class stores information about the performance of the
 algorithm.
     */
    public static abstract class AbstractValidationMetrics implements ValidationMetrics {   
        
    }
    
    /** 
     * @param baseDBname
     * @param dbConf
     * @param mpClass
     * @param tpClass
     * @param vmClass
     * @param modelValidator
     * @see com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer#AbstractTrainer(java.lang.String, com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration, java.lang.Class, java.lang.Class...) 
     */
    protected AbstractModeler(String baseDBname, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass, AbstractValidator<MP, TP, VM> modelValidator) {
        super(baseDBname, dbConf, TripleKnowledgeBase.class, mpClass, tpClass, vmClass);
        this.modelValidator = modelValidator;
    } 
    
    /**
     * Performs k-fold cross validation on the dataset and returns the AbstractValidationMetrics
 Object.
     * 
     * @param trainingData
     * @param trainingParameters
     * @param k
     * @return  
     */
    public VM kFoldCrossValidation(Dataframe trainingData, TP trainingParameters, int k) {
        logger.info("kFoldCrossValidation()");
        
        return modelValidator.kFoldCrossValidation(trainingData, k, dbName, kb().getDbConf(), this.getClass(), trainingParameters);
    }
    
    /**
     * Calculates the predictions for the newData and stores them in the provided 
     * Dataframe.
     * 
     * @param newData 
     */
    public void predict(Dataframe newData) { 
        logger.info("predict()");
        
        kb().load();
        
        _predictDataset(newData);

    }
    
    /**
     * Validate the model against the testingData and returns the validationMetrics;
     * It does not update the validationMetrics.
     * 
     * @param testingData
     * @return 
     */
     public VM validate(Dataframe testingData) {  
        logger.info("validate()");
        
        kb().load();

        //validate the model with the testing data and update the validationMetrics
        VM validationMetrics = validateModel(testingData);
        
        return validationMetrics;
    }
    
    
    /**
     * Setter for the Validation Metrics of the algorithm. Usually used to set 
     * the metrics after running a validate() or when doing K-fold cross validation.
     * 
     * @param validationMetrics 
     */
    public void setValidationMetrics(VM validationMetrics) {
        kb().setValidationMetrics(validationMetrics);
        
        logger.info("Updating model");
        kb().save();
    }
    
    /**
     * Getter for the Validation Metrics of the algorithm.
     * 
     * @return 
     */
    public VM getValidationMetrics() {
        return kb().getValidationMetrics();
    }
    
    /**
     * Getter for KnowledgeBase; this version returns the object explicitly casted to 
 TripleKnowledgeBase. This method exists to resolve any Unchecked/unconfirmed 
     * cast warnings.
     * 
     * @return 
     */
    @Override
    protected TripleKnowledgeBase<MP, TP, VM> kb() {
        KnowledgeBase kbObj = super.kb();
        if(kbObj instanceof TripleKnowledgeBase) {
            return (TripleKnowledgeBase<MP, TP, VM>) kbObj;
        }
        else {
            throw new ClassCastException("Invalid KnowledgeBase type."); //we will never get here
        }
    }
    
    /**
     * Validates the model with the provided dataset and returns the validation
     * metrics.
     * 
     * @param validationData
     * @return 
     */
    protected abstract VM validateModel(Dataframe validationData);
    
    /**
     * Estimates the predictions for a new Dataframe.
     * 
     * @param newData 
     */
    protected abstract void _predictDataset(Dataframe newData);
}
