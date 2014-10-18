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
package com.datumbox.framework.machinelearning.common.bases.datatransformation;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.framework.machinelearning.common.bases.BaseTrainable;
import com.datumbox.common.objecttypes.Parameterizable;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureContainer;
import com.datumbox.configuration.GeneralConfiguration;
import com.datumbox.configuration.StorageConfiguration;
import com.datumbox.framework.machinelearning.common.dataobjects.TrainableKnowledgeBase;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class DataTransformer<MP extends DataTransformer.ModelParameters, TP extends DataTransformer.TrainingParameters> extends BaseTrainable<MP, TP, TrainableKnowledgeBase<MP, TP>> {

    
    public static abstract class ModelParameters implements BigDataStructureContainer {
        
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            
        }
        
        @Override
        public void bigDataStructureCleaner(BigDataStructureFactory bdsf) {
            
        }
            
        //here goes the parameters of the transformer
    }
    
    public static abstract class TrainingParameters implements Parameterizable, TrainableKnowledgeBase.SelfConstructible<DataTransformer.TrainingParameters> {
        
        /**
         * This method allows us to build a new empty object of the current object
         * directly from it. Casting to the appropriate type is required.
         * 
         * @return 
         */
        @Override
        public DataTransformer.TrainingParameters getEmptyObject() {
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
     * @param <D>
     * @param dbName
     * @param aClass
     * @return 
     */
    public static <D extends DataTransformer> D newInstance(Class<D> aClass, String dbName) {
        D algorithm = null;
        try {
            algorithm = (D) aClass.getConstructor(String.class).newInstance(dbName);
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
        
        return algorithm;
    }
    
    
    /*
        IMPORTANT METHODS FOR THE FUNCTIONALITY
    */
    protected DataTransformer(String dbName, Class<MP> mpClass, Class<TP> tpClass) {
        String methodName = shortMethodName(); //this.getClass().getSimpleName();
        dbName += StorageConfiguration.getDBnameSeparator() + methodName;
        
        this.dbName = dbName;
        knowledgeBase = new TrainableKnowledgeBase<>(dbName, mpClass, tpClass);
        knowledgeBase.setOwnerClass(this.getClass());
    }
    
    
    
    
    public void transform(Dataset data, boolean trainingMode) {
        if(trainingMode) {
            //Check if training can be performed
            if(!knowledgeBase.isConfigured()) {
                throw new RuntimeException("The training configuration is not set.");
            }
            else if(knowledgeBase.isTrained()) {
                throw new RuntimeException("The dimension reduction algorithm is already trainned. Reinitialize it or erase it.");
            }

            if(GeneralConfiguration.DEBUG) {
                System.out.println("transform()");
            }
        }
        else {
            if(GeneralConfiguration.DEBUG) {
                System.out.println("transform()");
            }

            knowledgeBase.load();
        }
        
        
        _transform(data, trainingMode);
        
        
        if(trainingMode) {
    
            //store database if not temporary model
            if(isTemporary()==false) {
                if(GeneralConfiguration.DEBUG) {
                    System.out.println("Saving feature model");
                }
                knowledgeBase.save(true);
            }
            knowledgeBase.setTrained(true);
            
        }
    }
    
    public void normalize(Dataset data) {
        if(GeneralConfiguration.DEBUG) {
            System.out.println("normalize()");
        }
        
        knowledgeBase.load();
        
        _normalize(data);
    }
    
    public void denormalize(Dataset data) {
        if(GeneralConfiguration.DEBUG) {
            System.out.println("denormalize()");
        }
        
        knowledgeBase.load();
        
        _denormalize(data);
    }
    
    /**
     * Transforms the data (adding/modifying/removing columns). The transformations 
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
