/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
import com.datumbox.framework.machinelearning.common.bases.BaseTrainable;
import com.datumbox.framework.machinelearning.common.bases.dataobjects.BaseModelParameters;
import com.datumbox.framework.machinelearning.common.bases.dataobjects.BaseTrainingParameters;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel;
import com.datumbox.framework.machinelearning.common.dataobjects.KnowledgeBase;
import com.datumbox.framework.machinelearning.common.bases.datatransformation.DataTransformer;
import com.datumbox.framework.machinelearning.common.bases.featureselection.FeatureSelection;

/**
 * The BaseWrapper is a trainable object that uses composition instead of inheritance
 to extend the functionality of a BaseMLmodel. It includes various internal objects
 * and defines differently the training and prediction process. Even though it 
 * is trainable the methods of train(), predict(), etc are not specified in the
 * Trainable interface to give the freedom of defining truly custom expansions
 * on the original models.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class BaseWrapper<MP extends BaseWrapper.ModelParameters, TP extends BaseWrapper.TrainingParameters> extends BaseTrainable<MP, TP, KnowledgeBase<MP, TP>> {

    
    //internal objects
    protected DataTransformer dataTransformer = null;
    protected FeatureSelection featureSelection = null;
    protected BaseMLmodel mlmodel = null;
    

    public static abstract class ModelParameters extends BaseModelParameters {

        public ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
    }
    
    public static abstract class TrainingParameters<DT extends DataTransformer, FS extends FeatureSelection, ML extends BaseMLmodel> extends BaseTrainingParameters {
        
        
        //Classes
        private Class<? extends DT> dataTransformerClass;

        private Class<? extends FS> featureSelectionClass;
        
        private Class<? extends ML> mlmodelClass;
        
        

        //Parameter Objects
        private DT.TrainingParameters dataTransformerTrainingParameters;
        
        private FS.TrainingParameters featureSelectionTrainingParameters;
        
        private ML.TrainingParameters mlmodelTrainingParameters;

        //Getters/Setters
        public Class<? extends DT> getDataTransformerClass() {
            return dataTransformerClass;
        }

        public void setDataTransformerClass(Class<? extends DT> dataTransformerClass) {
            this.dataTransformerClass = dataTransformerClass;
        }

        public Class<? extends FS> getFeatureSelectionClass() {
            return featureSelectionClass;
        }

        public void setFeatureSelectionClass(Class<? extends FS> featureSelectionClass) {
            this.featureSelectionClass = featureSelectionClass;
        }

        public Class<? extends ML> getMLmodelClass() {
            return mlmodelClass;
        }

        public void setMLmodelClass(Class<? extends ML> mlmodelClass) {
            this.mlmodelClass = mlmodelClass;
        }

        public DT.TrainingParameters getDataTransformerTrainingParameters() {
            return dataTransformerTrainingParameters;
        }

        public void setDataTransformerTrainingParameters(DT.TrainingParameters dataTransformerTrainingParameters) {
            this.dataTransformerTrainingParameters = dataTransformerTrainingParameters;
        }

        public FS.TrainingParameters getFeatureSelectionTrainingParameters() {
            return featureSelectionTrainingParameters;
        }

        public void setFeatureSelectionTrainingParameters(FS.TrainingParameters featureSelectionTrainingParameters) {
            this.featureSelectionTrainingParameters = featureSelectionTrainingParameters;
        }

        public ML.TrainingParameters getMLmodelTrainingParameters() {
            return mlmodelTrainingParameters;
        }

        public void setMLmodelTrainingParameters(ML.TrainingParameters mlmodelTrainingParameters) {
            this.mlmodelTrainingParameters = mlmodelTrainingParameters;
        }
        
        
    }

    

    /*
        IMPORTANT METHODS FOR THE FUNCTIONALITY
    */
    protected BaseWrapper(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass) {
        super(dbName, dbConf, mpClass, tpClass);
    }
      
    
    @Override
    public void erase() {
        if(dataTransformer!=null) {
            dataTransformer.erase();
        }
        if(featureSelection!=null) {
            featureSelection.erase();
        }
        if(mlmodel!=null) {
            mlmodel.erase();
        }
        knowledgeBase.erase();
    }

}
