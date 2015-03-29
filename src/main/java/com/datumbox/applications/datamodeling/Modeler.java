/**
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
package com.datumbox.applications.datamodeling;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.bases.BaseTrainable;
import com.datumbox.framework.machinelearning.common.bases.featureselection.FeatureSelection;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel;
import com.datumbox.framework.machinelearning.common.bases.wrappers.BaseWrapper;
import com.datumbox.framework.machinelearning.common.bases.datatransformation.DataTransformer;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class Modeler extends BaseWrapper<Modeler.ModelParameters, Modeler.TrainingParameters>  {
    
    public static class ModelParameters extends BaseWrapper.ModelParameters {

        public ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
    }
    
    public static class TrainingParameters extends BaseWrapper.TrainingParameters<DataTransformer, FeatureSelection, BaseMLmodel> {
        //primitives/wrappers
        private Integer kFolds = 5;
        
        //Field Getters/Setters

        public Integer getkFolds() {
            return kFolds;
        }

        public void setkFolds(Integer kFolds) {
            this.kFolds = kFolds;
        }

    }

    
    
    public Modeler(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, Modeler.ModelParameters.class, Modeler.TrainingParameters.class);
    }

    
    
    @Override
    public void _fit(Dataset trainingData) { 
        
        //get the training parameters
        Modeler.TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        DatabaseConfiguration dbConf = knowledgeBase.getDbConf();
        
        //transform the training dataset
        Class dtClass = trainingParameters.getDataTransformerClass();
        
        boolean transformData = (dtClass!=null);
        if(transformData) {
            dataTransformer = DataTransformer.<DataTransformer>newInstance(dtClass, dbName, dbConf);
            dataTransformer.fit_transform(trainingData, trainingParameters.getDataTransformerTrainingParameters());
        }
        
        
        //find the most popular features
        Class fsClass = trainingParameters.getFeatureSelectionClass();
        
        boolean selectFeatures = (fsClass!=null);
        if(selectFeatures) {
            featureSelection = FeatureSelection.<FeatureSelection>newInstance(fsClass, dbName, dbConf);
            featureSelection.fit(trainingData, trainingParameters.getFeatureSelectionTrainingParameters()); 
            
            featureSelection.transform(trainingData);
        }
        
        
        
        
        //initialize mlmodel
        Class mlClass = trainingParameters.getMLmodelClass();
        mlmodel = BaseMLmodel.<BaseMLmodel>newInstance(mlClass, dbName, dbConf); 
        int k = trainingParameters.getkFolds();
        
        //call k-fold cross validation and get the average accuracy
        BaseMLmodel.ValidationMetrics averageValidationMetrics = (BaseMLmodel.ValidationMetrics) mlmodel.kFoldCrossValidation(trainingData, trainingParameters.getMLmodelTrainingParameters(), k);

        //train the mlmodel on the whole dataset
        mlmodel.fit(trainingData, trainingParameters.getMLmodelTrainingParameters());

        //set its ValidationMetrics to the average VP from k-fold cross validation
        mlmodel.setValidationMetrics(averageValidationMetrics);
        
        
        if(transformData) {
            dataTransformer.denormalize(trainingData); //optional denormalization
        }
    }
    
    public void predict(Dataset newData) {
        evaluateData(newData, false);
    }

    public BaseMLmodel.ValidationMetrics validate(Dataset testData) {
        return evaluateData(testData, true);
    }
    
    public BaseMLmodel.ValidationMetrics getValidationMetrics() {
        BaseMLmodel.ValidationMetrics vm = null;
        if(mlmodel!=null) {
            vm =  mlmodel.getValidationMetrics();
        }
        
        return vm;
    }
            
    private BaseMLmodel.ValidationMetrics evaluateData(Dataset data, boolean estimateValidationMetrics) {
        //ensure db loaded
        knowledgeBase.load();
        Modeler.TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        DatabaseConfiguration dbConf = knowledgeBase.getDbConf();
        
        Class dtClass = trainingParameters.getDataTransformerClass();
        
        boolean transformData = (dtClass!=null);
        if(transformData) {
            if(dataTransformer==null) {
                dataTransformer = DataTransformer.<DataTransformer>newInstance(dtClass, dbName, dbConf);
            }        
            dataTransformer.transform(data);
        }
        
        Class fsClass = trainingParameters.getFeatureSelectionClass();
        
        boolean selectFeatures = (fsClass!=null);
        if(selectFeatures) {
            if(featureSelection==null) {
                featureSelection = FeatureSelection.<FeatureSelection>newInstance(fsClass, dbName, dbConf);
            }

            //remove unnecessary features
            featureSelection.transform(data);
        }
        
        
        //initialize mlmodel
        if(mlmodel==null) {
            Class mlClass = trainingParameters.getMLmodelClass();
            mlmodel = BaseMLmodel.<BaseMLmodel>newInstance(mlClass, dbName, dbConf); 
        }
        
        //call predict of the mlmodel for the new dataset
        
        BaseMLmodel.ValidationMetrics vm = null;
        if(estimateValidationMetrics) {
            //run validate which calculates validation metrics. It is used by validate() method
            vm = mlmodel.validate(data);
        }
        else {
            //run predict which does not calculate validation metrics. It is used in from predict() method
            mlmodel.predict(data);
        }
        
        if(transformData) {
            dataTransformer.denormalize(data); //optional denormization
        }
        
        return vm;
    }
}
