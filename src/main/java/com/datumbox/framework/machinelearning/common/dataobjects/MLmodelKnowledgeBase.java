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
package com.datumbox.framework.machinelearning.common.dataobjects;

import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel;
import java.lang.reflect.InvocationTargetException;

/**
 * The knowledgeBase represents the "database" that the algorithm learned. 
 * It is a wrapper of the 3 classes of model & training parameters and validation metrics.
 * This object is imported and exported every time we use or train an algorithm.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public final class MLmodelKnowledgeBase<MP extends BaseMLmodel.ModelParameters, TP extends BaseMLmodel.TrainingParameters, VM extends BaseMLmodel.ValidationMetrics> extends TrainableKnowledgeBase<MP, TP> {

    /*
        VARIABLES
        =========
    */
    
    protected Class<VM> vmClass;
    
    private VM validationMetrics;
    
    
    
    /*
        EXTENDING ABSTRACT
        ==================
    */
    
    
    protected MLmodelKnowledgeBase() {
        super();
        //constructor only used in serialization/deserialization
    }
    
    public MLmodelKnowledgeBase(String dbName, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass) {
        super(dbName, mpClass, tpClass);
        this.vmClass = vmClass;
    }
    
    /**
     * Loads a BaseMLmodelKnowledgeBase
     */
    @Override
    public void load() {
        if(trainingParameters==null) {
            MLmodelKnowledgeBase kbObject = bdsf.load(MLmodelKnowledgeBase.class);
            if(kbObject==null) {
                throw new IllegalArgumentException("The KnowledgeBase could not be loaded.");
            }
            
            id = kbObject.id;
            
            mpClass = kbObject.mpClass;
            tpClass = kbObject.tpClass;
            vmClass = kbObject.vmClass;
            
            modelParameters = (MP) kbObject.modelParameters; 
            trainingParameters = (TP) kbObject.trainingParameters;
            validationMetrics = (VM) kbObject.validationMetrics;
            
            bdsf.postLoad(modelParameters, memoryConfiguration);
            
            setTrained(true);
        }
    }
    
    
    @Override
    public boolean isConfigured() {
        if(super.isConfigured()==false) {
            return false;
        }
        
        if(validationMetrics == null) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public void erase(boolean complete) {
        super.erase(complete);
        
        validationMetrics = null;
    }
    
    /**
     * Erases the data from BaseMLmodelKnowledgeBase and from permanent storage
     */
    @Override
    public void reinitialize() {
        super.reinitialize();
        validationMetrics = getEmptyValidationMetricsObject();
    }
    
    
    
    
    /*
        IMPORTANT PUBLIC METHODS
        ========================
    */

    /**
     * Returns an empty ValidationMetrics Object
     * @return 
     */
    public VM getEmptyValidationMetricsObject() {
        //There is already an object set, call its getEmptyObjec() method to generate one
        if(validationMetrics!=null) {
            return (VM) validationMetrics.getEmptyObject();
        }
        
        //else try using reflections
        try {
            return vmClass.getConstructor().newInstance();
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
    
    /*
        GETTER SETTERS
        ==============
    */

    public VM getValidationMetrics() {
        return validationMetrics;
    }

    public void setValidationMetrics(VM validationMetrics) {
        this.validationMetrics = validationMetrics;
    }
    
}
