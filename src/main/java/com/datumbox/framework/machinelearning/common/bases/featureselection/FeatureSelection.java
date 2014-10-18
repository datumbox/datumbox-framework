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
package com.datumbox.framework.machinelearning.common.bases.featureselection;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.objecttypes.Parameterizable;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureContainer;
import com.datumbox.configuration.GeneralConfiguration;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.configuration.StorageConfiguration;
import com.datumbox.framework.machinelearning.common.bases.BaseTrainable;
import com.datumbox.framework.machinelearning.common.dataobjects.TrainableKnowledgeBase;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class FeatureSelection<MP extends FeatureSelection.ModelParameters, TP extends FeatureSelection.TrainingParameters> extends BaseTrainable<MP, TP, TrainableKnowledgeBase<MP, TP>> {

    
    public static abstract class ModelParameters implements BigDataStructureContainer {
        
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            
        }
        
        @Override
        public void bigDataStructureCleaner(BigDataStructureFactory bdsf) {
            
        }
        
        //here goes the parameters of the feature slection
    }
    
    public static abstract class TrainingParameters implements Parameterizable, TrainableKnowledgeBase.SelfConstructible<FeatureSelection.TrainingParameters> {
        
        /**
         * This method allows us to build a new empty object of the current object
         * directly from it. Casting to the appropriate type is required.
         * 
         * @return 
         */
        @Override
        public FeatureSelection.TrainingParameters getEmptyObject() {
            try {
                return this.getClass().getConstructor().newInstance();
            } 
            catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                throw new RuntimeException(ex);
            }
        }
        
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
    public static <F extends FeatureSelection> F newInstance(Class<F> aClass, String dbName) {
        F algorithm = null;
        try {
            algorithm = (F) aClass.getConstructor(String.class).newInstance(dbName);
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
        
        return algorithm;
    }
    
    
    /*
        IMPORTANT METHODS FOR THE FUNCTIONALITY
    */
    protected FeatureSelection(String dbName, Class<MP> mpClass, Class<TP> tpClass) {
        String methodName = shortMethodName(); //this.getClass().getSimpleName();
        dbName += StorageConfiguration.getDBnameSeparator() + methodName;
        
        this.dbName = dbName;
        knowledgeBase = new TrainableKnowledgeBase<>(dbName, mpClass, tpClass);
        knowledgeBase.setOwnerClass(this.getClass());
    }

    
    public void evaluateFeatures(Dataset trainingData) {   
        //Check if training can be performed
        if(!knowledgeBase.isConfigured()) {
            throw new RuntimeException("The training configuration is not set.");
        }
        else if(knowledgeBase.isTrained()) {
            throw new RuntimeException("The feature selection algorithm is already trainned. Reinitialize it or erase it.");
        }
        
        if(GeneralConfiguration.DEBUG) {
            System.out.println("evaluateFeatures()");
        }
        
        estimateModelParameters(trainingData);
        
        //store database if not temporary model
        if(isTemporary()==false) {
            if(GeneralConfiguration.DEBUG) {
                System.out.println("Saving feature model");
            }
            knowledgeBase.save(true);
        }
        knowledgeBase.setTrained(true);
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
