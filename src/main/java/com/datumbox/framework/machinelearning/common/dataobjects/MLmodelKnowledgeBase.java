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
package com.datumbox.framework.machinelearning.common.dataobjects;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * The knowledgeBase represents the "database" that the algorithm learned. 
 * It is a wrapper of the 3 classes of model & training parameters and validation metrics.
 * This object is imported and exported every time we use or train an algorithm.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public final class MLmodelKnowledgeBase<MP extends BaseMLmodel.ModelParameters, TP extends BaseMLmodel.TrainingParameters, VM extends BaseMLmodel.ValidationMetrics> extends KnowledgeBase<MP, TP> {

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
    
    public MLmodelKnowledgeBase(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass) {
        super(dbName, dbConf, mpClass, tpClass);
        this.vmClass = vmClass;
    }
    
    /**
     * Loads a BaseMLmodelKnowledgeBase
     */
    @Override
    public void load() {
        if(trainingParameters==null) {
            MLmodelKnowledgeBase kbObject = dbc.load("KnowledgeBase", MLmodelKnowledgeBase.class);
            if(kbObject==null) {
                throw new IllegalArgumentException("The KnowledgeBase could not be loaded.");
            }
            
            mpClass = kbObject.mpClass;
            tpClass = kbObject.tpClass;
            vmClass = kbObject.vmClass;
            
            modelParameters = (MP) kbObject.modelParameters; 
            trainingParameters = (TP) kbObject.trainingParameters;
            validationMetrics = (VM) kbObject.validationMetrics;
            
        }
    }
    
    @Override
    public void erase() {
        super.erase();
        
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
