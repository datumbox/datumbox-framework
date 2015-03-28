/* 
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
package com.datumbox.framework.machinelearning.common.bases.datatransformation;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.framework.machinelearning.common.bases.BaseTrainable;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.configuration.GeneralConfiguration;
import com.datumbox.framework.machinelearning.common.bases.dataobjects.BaseModelParameters;
import com.datumbox.framework.machinelearning.common.bases.dataobjects.BaseTrainingParameters;
import com.datumbox.framework.machinelearning.common.dataobjects.KnowledgeBase;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
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
        
        if(GeneralConfiguration.DEBUG) {
            System.out.println("fit_transform()");
        }
        
        _transform(trainingData, true);     
        _normalize(trainingData);
            
        if(GeneralConfiguration.DEBUG) {
            System.out.println("Saving model");
        }
        knowledgeBase.save();
    }
    
    
    public void transform(Dataset newData) {
        knowledgeBase.load();
        
        if(GeneralConfiguration.DEBUG) {
            System.out.println("transform()");
        }
        _transform(newData, false); 
        _normalize(newData);
        
    }
    
    public void denormalize(Dataset data) {
        knowledgeBase.load();
        
        if(GeneralConfiguration.DEBUG) {
            System.out.println("denormalize()");
        }
        
        _denormalize(data);
    }
    
    /**
     * Converts the data (adding/modifying/removing columns). The transformations 
     * are not possible to be rolledback.
     * 
     * @param data 
     * @param trainingMode 
     */
    protected abstract void _transform(Dataset data, boolean trainingMode);
    
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
