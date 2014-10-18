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
package com.datumbox.framework.machinelearning.common.bases.mlmodels;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.framework.machinelearning.common.bases.BaseTrainable;
import com.datumbox.common.objecttypes.Parameterizable;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureContainer;
import com.datumbox.configuration.GeneralConfiguration;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.configuration.StorageConfiguration;
import com.datumbox.framework.machinelearning.common.dataobjects.TrainableKnowledgeBase;
import java.lang.reflect.InvocationTargetException;

/**
 * Abstract Class for a Machine Learning algorithm.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class BaseMLrecommender<MP extends BaseMLrecommender.ModelParameters, TP extends BaseMLrecommender.TrainingParameters> extends BaseTrainable<MP, TP, TrainableKnowledgeBase<MP, TP>> {
    
    /**
     * Parameters/Weights of a trained model: For example in regression you have the weights of the parameters learned.
     */
    public static abstract class ModelParameters implements BigDataStructureContainer {
        
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            
        }
        
        @Override
        public void bigDataStructureCleaner(BigDataStructureFactory bdsf) {
            
        }
            
        //here goes the parameters of the Machine Learning model
    }
    
    /**
     * Training Parameters of an algorithm: For example in regression you have the number of total regressors
     */
    public static abstract class TrainingParameters implements Parameterizable, TrainableKnowledgeBase.SelfConstructible<BaseMLrecommender.TrainingParameters> {    

        public TrainingParameters() {
            //here goes initialization of parameters that are private and must be overriden by inherited classes
        }
        
        /**
         * This method allows us to build a new empty object of the current object
         * directly from it. Casting to the appropriate type is required.
         * 
         * @return 
         */
        @Override
        public BaseMLrecommender.TrainingParameters getEmptyObject() {
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
     * Generates a new instance of a BaseMLrecommender by providing the dbName and 
 the Class of the algorithm.
     * 
     * @param <M>
     * @param dbName
     * @param aClass
     * @return 
     */
    public static <M extends BaseMLrecommender> M newInstance(Class<M> aClass, String dbName) {
        M algorithm = null;
        try {
            algorithm = (M) aClass.getConstructor(String.class).newInstance(dbName);
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
        
        return algorithm;
    }

    
    
    
    
    
    
    /*
        IMPORTANT METHODS FOR THE FUNCTIONALITY
    */
    protected BaseMLrecommender(String dbName, Class<MP> mpClass, Class<TP> tpClass) {
        //the line below calls an overrided method in the constructor. This is not elegant but the only thing that this method does is to rename the short name of classifier
        String methodName = shortMethodName(); //this.getClass().getSimpleName();
        dbName += StorageConfiguration.getDBnameSeparator() + methodName;
        
        this.dbName = dbName;
        knowledgeBase = new TrainableKnowledgeBase<>(dbName, mpClass, tpClass);
        knowledgeBase.setOwnerClass(this.getClass());
    } 
    
    /**
     * Trains a model with the trainingData and validates it with the validationData.
     * 
     * @param trainingData
     */
    @SuppressWarnings("unchecked")
    public void train(Dataset trainingData) {    
        //Check if training can be performed
        if(!knowledgeBase.isConfigured()) {
            throw new RuntimeException("The training configuration is not set.");
        }
        else if(knowledgeBase.isTrained()) {
            throw new RuntimeException("The algorithm is already trainned. Reinitialize it or erase it.");
        }
        
        if(GeneralConfiguration.DEBUG) {
            System.out.println("train()");
        }
        
        
        if(GeneralConfiguration.DEBUG) {
            System.out.println("estimateModelParameters()");
        }
        
        //train the model to get the parameters
        estimateModelParameters(trainingData);        
        
        //store database if not temporary model
        if(isTemporary()==false) {
            if(GeneralConfiguration.DEBUG) {
                System.out.println("Saving model");
            }
            knowledgeBase.save(true);
        }
        knowledgeBase.setTrained(true);
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
    
    protected abstract void estimateModelParameters(Dataset trainingData);
    
    protected abstract void predictDataset(Dataset newData);


}
