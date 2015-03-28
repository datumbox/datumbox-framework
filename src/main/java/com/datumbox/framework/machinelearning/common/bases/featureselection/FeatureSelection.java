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
package com.datumbox.framework.machinelearning.common.bases.featureselection;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.configuration.GeneralConfiguration;
import com.datumbox.framework.machinelearning.common.bases.BaseTrainable;
import com.datumbox.framework.machinelearning.common.bases.dataobjects.BaseModelParameters;
import com.datumbox.framework.machinelearning.common.bases.dataobjects.BaseTrainingParameters;
import com.datumbox.framework.machinelearning.common.dataobjects.KnowledgeBase;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class FeatureSelection<MP extends FeatureSelection.ModelParameters, TP extends FeatureSelection.TrainingParameters> extends BaseTrainable<MP, TP, KnowledgeBase<MP, TP>> {

    
    public static abstract class ModelParameters extends BaseModelParameters {

        public ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
        //here goes the parameters of the feature slection
    }
    
    public static abstract class TrainingParameters extends BaseTrainingParameters {
        //here goes public fields that are used as initial training parameters        
    }
    
    
    /**
     * Generates a new instance of a MLmodel by providing the dbName and 
 the Class of the algorithm.
     * 
     * @param <F>
     * @param dbName
     * @param aClass
     * @return 
     */
    public static <F extends FeatureSelection> F newInstance(Class<F> aClass, String dbName, DatabaseConfiguration dbConfig) {
        F algorithm = null;
        try {
            algorithm = (F) aClass.getConstructor(String.class, DatabaseConfiguration.class).newInstance(dbName, dbConfig);
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
        
        return algorithm;
    }
    
    
    /*
        IMPORTANT METHODS FOR THE FUNCTIONALITY
    */
    protected FeatureSelection(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass) {
        String methodName = this.getClass().getSimpleName();
        dbName += dbConf.getDBnameSeparator() + methodName;
        
        this.dbName = dbName;
        knowledgeBase = new KnowledgeBase<>(dbName, dbConf, mpClass, tpClass);
        knowledgeBase.setOwnerClass(this.getClass());
    }

    
    public void evaluateFeatures(Dataset trainingData) { 
        
        if(GeneralConfiguration.DEBUG) {
            System.out.println("evaluateFeatures()");
        }
        
        estimateModelParameters(trainingData);
        
        if(GeneralConfiguration.DEBUG) {
            System.out.println("Saving model");
        }
        knowledgeBase.save();
        
    }
    
    public void clearFeatures(Dataset newData) {
        if(GeneralConfiguration.DEBUG) {
            System.out.println("clearFeatures()");
        }
        
        knowledgeBase.load();
        
        filterFeatures(newData);
    }
    
    protected abstract void estimateModelParameters(Dataset trainingData);
    
    protected abstract void filterFeatures(Dataset newdata);
}
