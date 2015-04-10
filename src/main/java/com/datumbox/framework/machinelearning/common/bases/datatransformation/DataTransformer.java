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
package com.datumbox.framework.machinelearning.common.bases.datatransformation;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.framework.machinelearning.common.bases.BaseTrainable;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;

import com.datumbox.framework.machinelearning.common.bases.dataobjects.BaseModelParameters;
import com.datumbox.framework.machinelearning.common.bases.dataobjects.BaseTrainingParameters;
import com.datumbox.framework.machinelearning.common.dataobjects.KnowledgeBase;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class DataTransformer<MP extends DataTransformer.ModelParameters, TP extends DataTransformer.TrainingParameters> extends BaseTrainable<MP, TP, KnowledgeBase<MP, TP>> {

    
    public static abstract class ModelParameters extends BaseModelParameters {

        public ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
            
        //here goes the parameters of the transformer
    }
    
    public static abstract class TrainingParameters extends BaseTrainingParameters {
                
        //here goes public fields that are used as initial training parameters        
    }
    
    
    
    /*
        IMPORTANT METHODS FOR THE FUNCTIONALITY
    */
    protected DataTransformer(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass) {
        super(dbName, dbConf, mpClass, tpClass);
    }
    
    /*
     The DataTransformers perform transform() at the same time as fit(), concequently
     the fit() method alone is not supported. Use instead the fit_transform() method.
    */
    @Override
    public void fit(Dataset trainingData, TP trainingParameters) {  
        throw new UnsupportedOperationException("fit() is not supported. Call fit_transform() instead."); 
    }
    
    @Override
    protected void _fit(Dataset trainingData) {
        throw new UnsupportedOperationException("fit() is not supported. Call fit_transform() instead."); 
    }
    
    
    
    public void fit_transform(Dataset trainingData, TP trainingParameters) {  
        
        initializeTrainingConfiguration(trainingParameters);
        
        logger.info("fit_transform()");
        
        _transform(trainingData);     
        _normalize(trainingData);
            
        logger.info("Saving model");
        knowledgeBase.save();
    }
    
    
    public void transform(Dataset newData) {
        knowledgeBase.load();
        
        logger.info("transform()");
        _transform(newData); 
        _normalize(newData);
        
    }
    
    public void denormalize(Dataset data) {
        knowledgeBase.load();
        
        logger.info("denormalize()");
        
        _denormalize(data);
    }
    
    /**
     * Converts the data (adding/modifying/removing columns). The transformations 
     * are not possible to be rolledback.
     * 
     * @param data 
     * @param trainingMode 
     */
    protected abstract void _transform(Dataset data);
    
    /**
     * Normalizes the data by modifying the columns. The changes should be 
     * possible to be rolledback (denormalized). 
     * 
     * @param data 
     */
    protected abstract void _normalize(Dataset data);
    
    /**
     * Denormalizes the data by undoing the modifications performed by normilize().
     * 
     * @param data 
     */
    protected abstract void _denormalize(Dataset data);
    
    
}
