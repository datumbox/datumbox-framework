/* 
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
import com.datumbox.common.utilities.DeepCopy;
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

    
    
    public void train(Dataset trainingData) { 
        //Check if training can be performed
        if(!knowledgeBase.isConfigured()) {
            throw new RuntimeException("The training configuration is not initialized.");
        }
        else if(knowledgeBase.isTrained()) {
            throw new RuntimeException("The algorithm is already trainned. Reinitialize it or erase it.");
        }
        
        //get the training parameters
        Modeler.TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        DatabaseConfiguration dbConf = knowledgeBase.getDbConf();
        
        //transform the training dataset
        Class dtClass = trainingParameters.getDataTransformerClass();
        
        boolean transformData = (dtClass!=null);
        if(transformData) {
            dataTransformer = DataTransformer.newInstance(dtClass, dbName, dbConf);
            dataTransformer.initializeTrainingConfiguration(knowledgeBase.getTrainingParameters().getDataTransformerTrainingParameters());
            dataTransformer.transform(trainingData, true);
            dataTransformer.normalize(trainingData);
        }
        
        
        //find the most popular features
        Class fsClass = trainingParameters.getFeatureSelectionClass();
        
        boolean selectFeatures = (fsClass!=null);
        if(selectFeatures) {
            featureSelection = FeatureSelection.newInstance(fsClass, dbName, dbConf);
            featureSelection.initializeTrainingConfiguration(trainingParameters.getFeatureSelectionTrainingParameters());
            featureSelection.evaluateFeatures(trainingData); 
        
            //remove unnecessary features
            featureSelection.clearFeatures(trainingData);  
        }
        
        
        
        
        //initialize mlmodel
        mlmodel = BaseMLmodel.newInstance(trainingParameters.getMLmodelClass(), dbName, dbConf); 
        mlmodel.initializeTrainingConfiguration(trainingParameters.getMLmodelTrainingParameters());
        
        int k = trainingParameters.getkFolds();
        if(k>1) {
            //call k-fold cross validation and get the average accuracy
            BaseMLmodel.ValidationMetrics averageValidationMetrics = (BaseMLmodel.ValidationMetrics) mlmodel.kFoldCrossValidation(trainingData, k);

            //train the mlmodel on the whole dataset and pass as ValidationDataset the empty set
            mlmodel.train(trainingData, new Dataset());

            //set its ValidationMetrics to the average VP from k-fold cross validation
            mlmodel.setValidationMetrics(averageValidationMetrics);
        }
        else { //k==1
            Dataset validationDataset = trainingData;
            
            boolean algorithmModifiesDataset = mlmodel.modifiesData();
            if(algorithmModifiesDataset) {
                validationDataset = DeepCopy.<Dataset>cloneObject(validationDataset);
            }
            mlmodel.train(trainingData, validationDataset);
        }
        if(transformData) {
            dataTransformer.denormalize(trainingData); //optional denormalization
        }
        
        //store database
        knowledgeBase.save();
        knowledgeBase.setTrained(true);
    }
    
    public void predict(Dataset newData) {
        evaluateData(newData, false);
    }

    public BaseMLmodel.ValidationMetrics test(Dataset testData) {
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
                dataTransformer = DataTransformer.newInstance(dtClass, dbName, dbConf);
            }        
            dataTransformer.transform(data, false);
            dataTransformer.normalize(data);
        }
        
        Class fsClass = trainingParameters.getFeatureSelectionClass();
        
        boolean selectFeatures = (fsClass!=null);
        if(selectFeatures) {
            if(featureSelection==null) {
                featureSelection = FeatureSelection.newInstance(fsClass, dbName, dbConf);
            }

            //remove unnecessary features
            featureSelection.clearFeatures(data);
        }
        
        
        //initialize mlmodel
        if(mlmodel==null) {
            mlmodel = BaseMLmodel.newInstance(trainingParameters.getMLmodelClass(), dbName, dbConf); 
        }
        
        //call predict of the mlmodel for the new dataset
        
        BaseMLmodel.ValidationMetrics vm = null;
        if(estimateValidationMetrics) {
            //run test which calculates validation metrics. It is used by test() method
            vm = mlmodel.test(data);
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
