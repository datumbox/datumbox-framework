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
package com.datumbox.framework.machinelearning.common.bases.featureselection;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;

import com.datumbox.framework.machinelearning.common.bases.BaseTrainable;
import com.datumbox.framework.machinelearning.common.bases.dataobjects.BaseModelParameters;
import com.datumbox.framework.machinelearning.common.bases.dataobjects.BaseTrainingParameters;
import com.datumbox.framework.machinelearning.common.dataobjects.KnowledgeBase;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
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
    
    /*
        IMPORTANT METHODS FOR THE FUNCTIONALITY
    */
    protected FeatureSelection(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass) {
        super(dbName, dbConf, mpClass, tpClass);
    }
    
    public void fit_transform(Dataset trainingData, TP trainingParameters) {
        fit(trainingData, trainingParameters);
        transform(trainingData);
    }
    
    public void transform(Dataset newData) {
        logger.info("transform()");
        
        knowledgeBase.load();
        
        filterFeatures(newData);
    }
    
    protected abstract void filterFeatures(Dataset newdata);
}
