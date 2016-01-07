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
package com.datumbox.framework.machinelearning.common.bases.wrappers;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseTrainable;
import com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseModelParameters;
import com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseTrainingParameters;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel;
import com.datumbox.framework.machinelearning.common.dataobjects.KnowledgeBase;
import com.datumbox.framework.machinelearning.common.bases.datatransformation.DataTransformer;
import com.datumbox.framework.machinelearning.common.bases.featureselection.FeatureSelection;

/**
 * The BaseWrapper is a trainable object that uses composition instead of inheritance
 * to extend the functionality of a BaseMLmodel. It includes various internal objects
 * such as Data Transformers, Feature Selectors and Machine Learning models which 
 * are combined in the training and prediction process. 
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class BaseWrapper<MP extends BaseWrapper.ModelParameters, TP extends BaseWrapper.TrainingParameters> extends BaseTrainable<MP, TP, KnowledgeBase<MP, TP>> {
    
    /**
     * The DataTransformer instance of the wrapper.
     */
    protected DataTransformer dataTransformer = null;
    
    /**
     * The FeatureSelection instance of the wrapper.
     */
    protected FeatureSelection featureSelection = null;
    
    /**
     * The Machine Learning model instance of the wrapper.
     */
    protected BaseMLmodel mlmodel = null;
    
    /**
     * The ModelParameters class stores the coefficients that were learned during
     * the training of the algorithm.
     */
    public static abstract class ModelParameters extends BaseModelParameters {
        
        /** 
         * @param dbc
         * @see com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseModelParameters#BaseModelParameters(com.datumbox.common.persistentstorage.interfaces.DatabaseConnector) 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
    }
    
    /**
     * The TrainingParameters class stores the parameters that can be changed
     * before training the algorithm.
     * 
     * @param <DT>
     * @param <FS>
     * @param <ML>
     */
    public static abstract class TrainingParameters<DT extends DataTransformer, FS extends FeatureSelection, ML extends BaseMLmodel> extends BaseTrainingParameters {
        
        //Classes
        private Class<? extends DT> dataTransformerClass;

        private Class<? extends FS> featureSelectionClass;
        
        private Class<? extends ML> mlmodelClass;
       
        //Parameter Objects
        private DT.TrainingParameters dataTransformerTrainingParameters;
        
        private FS.TrainingParameters featureSelectionTrainingParameters;
        
        private ML.TrainingParameters mlmodelTrainingParameters;

        /**
         * Getter for the Java class of the Data Transformer.
         * 
         * @return 
         */
        public Class<? extends DT> getDataTransformerClass() {
            return dataTransformerClass;
        }
        
        /**
         * Setter for the Java class of the Data Transformer. Pass null for none.
         * 
         * @param dataTransformerClass 
         */
        public void setDataTransformerClass(Class<? extends DT> dataTransformerClass) {
            this.dataTransformerClass = dataTransformerClass;
        }
        
        /**
         * Getter for the Java class of the Feature Selector.
         * 
         * @return 
         */
        public Class<? extends FS> getFeatureSelectionClass() {
            return featureSelectionClass;
        }
        
        /**
         * Setter for the Java class of the Feature Selector. Pass null for none.
         * 
         * @param featureSelectionClass 
         */
        public void setFeatureSelectionClass(Class<? extends FS> featureSelectionClass) {
            this.featureSelectionClass = featureSelectionClass;
        }
        
        /**
         * Getter for the Java class of the Machine Learning model which will
         * be used internally.
         * 
         * @return 
         */
        public Class<? extends ML> getMLmodelClass() {
            return mlmodelClass;
        }
        
        /**
         * Setter for the Java class of the Machine Learning model which will
         * be used internally.
         * 
         * @param mlmodelClass 
         */
        public void setMLmodelClass(Class<? extends ML> mlmodelClass) {
            this.mlmodelClass = mlmodelClass;
        }
        
        /**
         * Getter for the Training Parameters of the Data Transformer.
         * 
         * @return 
         */
        public DT.TrainingParameters getDataTransformerTrainingParameters() {
            return dataTransformerTrainingParameters;
        }
        
        /**
         * Setter for the Training Parameters of the Data Transformer. Pass null
         * for none.
         * 
         * @param dataTransformerTrainingParameters 
         */
        public void setDataTransformerTrainingParameters(DT.TrainingParameters dataTransformerTrainingParameters) {
            this.dataTransformerTrainingParameters = dataTransformerTrainingParameters;
        }

        /**
         * Getter for the Training Parameters of the Feature Selector.
         * 
         * @return 
         */
        public FS.TrainingParameters getFeatureSelectionTrainingParameters() {
            return featureSelectionTrainingParameters;
        }
        
        /**
         * Setter for the Training Parameters of the Feature Selector. Pass null
         * for none.
         * 
         * @param featureSelectionTrainingParameters 
         */
        public void setFeatureSelectionTrainingParameters(FS.TrainingParameters featureSelectionTrainingParameters) {
            this.featureSelectionTrainingParameters = featureSelectionTrainingParameters;
        }

        /**
         * Getter for the Training Parameters of the Machine Learning model.
         * 
         * @return 
         */
        public ML.TrainingParameters getMLmodelTrainingParameters() {
            return mlmodelTrainingParameters;
        }
        
        /**
         * Setter for the Training Parameters of the Machine Learning model.
         * 
         * @param mlmodelTrainingParameters 
         */
        public void setMLmodelTrainingParameters(ML.TrainingParameters mlmodelTrainingParameters) {
            this.mlmodelTrainingParameters = mlmodelTrainingParameters;
        }
        
    }
    
    /** 
     * @param dbName
     * @param dbConf
     * @param mpClass
     * @param tpClass
     * @see com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseTrainable#BaseTrainable(java.lang.String, com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration, java.lang.Class, java.lang.Class)  
     */
    protected BaseWrapper(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass) {
        super(dbName, dbConf, mpClass, tpClass);
    }
      
    /** {@inheritDoc} */
    @Override
    public void delete() {
        if(dataTransformer!=null) {
            dataTransformer.delete();
        }
        if(featureSelection!=null) {
            featureSelection.delete();
        }
        if(mlmodel!=null) {
            mlmodel.delete();
        }
        knowledgeBase.delete();
    }
      
    /** {@inheritDoc} */
    @Override
    public void close() {
        if(dataTransformer!=null) {
            dataTransformer.close();
        }
        if(featureSelection!=null) {
            featureSelection.close();
        }
        if(mlmodel!=null) {
            mlmodel.close();
        }
        knowledgeBase.close();
    }

    /**
     * Getter for the Validation Metrics of the algorithm.
     * 
     * @param <VM>
     * @return 
     */
    public <VM extends BaseMLmodel.ValidationMetrics> VM getValidationMetrics() {
        if(mlmodel!=null) {
            return (VM) mlmodel.getValidationMetrics();
        }
        else {
            return null;
        }
    }
    
    /**
     * Setter for the Validation Metrics of the algorithm.
     * 
     * @param <VM>
     * @param validationMetrics 
     */
    public <VM extends BaseMLmodel.ValidationMetrics> void setValidationMetrics(VM validationMetrics) {
        if(mlmodel!=null) {
            mlmodel.setValidationMetrics(validationMetrics);
        }
    }
}
