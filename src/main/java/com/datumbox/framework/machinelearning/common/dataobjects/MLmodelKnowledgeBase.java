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
package com.datumbox.framework.machinelearning.common.dataobjects;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel;
import java.lang.reflect.InvocationTargetException;

/**
 * The MLmodelKnowledgeBase is the Knowledge Base object which is used by most Machine 
 * Learning models. It extends the KnowledgeBase class and includes an extra field
 * to store the validation metrics of the algorithm.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public class MLmodelKnowledgeBase<MP extends BaseMLmodel.ModelParameters, TP extends BaseMLmodel.TrainingParameters, VM extends BaseMLmodel.ValidationMetrics> extends KnowledgeBase<MP, TP> {
    private static final long serialVersionUID = 1L;

    /**
     * The class of the ValidationMetrics class of the algorithm.
     */
    protected Class<VM> vmClass;
    
    /**
     * The ValidationMetrics object of the algorithm.
     */
    protected VM validationMetrics;
    
    /**
     * Public constructor of the object.
     * 
     * @param dbName
     * @param dbConf 
     * @param mpClass 
     * @param tpClass 
     * @param vmClass 
     */
    public MLmodelKnowledgeBase(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass) {
        super(dbName, dbConf, mpClass, tpClass);
        this.vmClass = vmClass;
    }
    
    /**
     * Loads a KnowledgeBase from the permanent storage.
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
    
    /**
     * Deletes the database of the algorithm. 
     */
    @Override
    public void delete() {
        super.delete();
        validationMetrics = null;
    }
    
    /**
     * Deletes and re-initializes KnowledgeBase object. It deletes all data from 
     * storage, it releases all resources, reinitializes the internal objects and
     * opens new connection to the permanent storage.
     */
    @Override
    public void reinitialize() {
        super.reinitialize();
        validationMetrics = getEmptyValidationMetricsObject();
    }
    
    /**
     * Returns an empty ValidationMetrics Object.
     * 
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
    
    /**
     * Getter for the Validation Metrics of the algorithm.
     * 
     * @return 
     */
    public VM getValidationMetrics() {
        return validationMetrics;
    }
    
    /**
     * Setter for the Validation Metrics of the algorithm.
     * 
     * @param validationMetrics 
     */
    public void setValidationMetrics(VM validationMetrics) {
        this.validationMetrics = validationMetrics;
    }
    
}
