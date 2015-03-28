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
package com.datumbox.framework.machinelearning.common.bases.mlmodels;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.framework.machinelearning.common.bases.BaseTrainable;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.configuration.GeneralConfiguration;
import com.datumbox.framework.machinelearning.common.bases.dataobjects.BaseModelParameters;
import com.datumbox.framework.machinelearning.common.bases.dataobjects.BaseTrainingParameters;
import com.datumbox.framework.machinelearning.common.dataobjects.KnowledgeBase;
import java.lang.reflect.InvocationTargetException;

/**
 * Abstract Class for a Machine Learning algorithm.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class BaseMLrecommender<MP extends BaseMLrecommender.ModelParameters, TP extends BaseMLrecommender.TrainingParameters> extends BaseTrainable<MP, TP, KnowledgeBase<MP, TP>> {
    
    /**
     * Parameters/Weights of a trained model: For example in regression you have the weights of the parameters learned.
     */
    public static abstract class ModelParameters extends BaseModelParameters {

        public ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
            
        //here goes the parameters of the Machine Learning model
    }
    
    /**
     * Training Parameters of an algorithm: For example in regression you have the number of total regressors
     */
    public static abstract class TrainingParameters extends BaseTrainingParameters {
        
        //here goes public fields that are used as initial training parameters
    } 
    
    
    
    /*
        IMPORTANT METHODS FOR THE FUNCTIONALITY
    */
    protected BaseMLrecommender(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass) {
        super(dbName, dbConf, mpClass, tpClass);
    } 
    
    
    /**
     * Calculates the predictions for the newData and stores the predictions
     * inside the object.
     * 
     * @param newData 
     */
    public void predict(Dataset newData) { 
        
        if(GeneralConfiguration.DEBUG) {
            System.out.println("predict()");
        }
        
        knowledgeBase.load();
        
        predictDataset(newData);

    }
    
    
    /**
     * Returns whether the algorithm or the configuration modifies the data.
     * @return 
     */
    public boolean modifiesData() {
        //check if the algorithm itself modifies the data
        try { 
            Boolean dataSafe = (Boolean) this.getClass().getDeclaredField("DATA_SAFE_CALL_BY_REFERENCE").get(this);
            //see if the data are safe mearning that algorithm does not modify the data internally.
            //if the data are not safe, mark it for deep copy
            if(dataSafe!=true) {
                return true;
            }
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
            return true; //if no information available play it safe and mark it as true
        }
        
        return false;
    }
    
    protected abstract void predictDataset(Dataset newData);


}
