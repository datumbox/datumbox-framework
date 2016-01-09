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
package com.datumbox.applications.datamodeling;

import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.interfaces.Trainable;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.abstracts.featureselectors.AbstractFeatureSelector;
import com.datumbox.framework.machinelearning.common.abstracts.modelers.AbstractModeler;
import com.datumbox.framework.machinelearning.common.abstracts.wrappers.AbstractWrapper;
import com.datumbox.framework.machinelearning.common.abstracts.datatransformers.AbstractTransformer;
import com.datumbox.framework.machinelearning.common.interfaces.ValidationMetrics;

/**
 * Modeler is a convenience class which can be used to train Machine Learning
 * models. It is a wrapper class which automatically takes care of the data 
 * transformation, feature selection and model training processes.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Modeler extends AbstractWrapper<Modeler.ModelParameters, Modeler.TrainingParameters>  {
    
    /**
     * It contains all the Model Parameters which are learned during the training.
     */
    public static class ModelParameters extends AbstractWrapper.AbstractModelParameters {
        private static final long serialVersionUID = 1L;
        
        /**
         * @param dbc
         * @see com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer.AbstractModelParameters#AbstractModelParameters(com.datumbox.common.persistentstorage.interfaces.DatabaseConnector)
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
    }
    
    /**
     * It contains the Training Parameters of the Modeler.
     */
    public static class TrainingParameters extends AbstractWrapper.AbstractTrainingParameters<AbstractTransformer, AbstractFeatureSelector, AbstractModeler> {
        private static final long serialVersionUID = 1L;

    }

    /**
     * Constructor for the Modeler class. It accepts as arguments the name of the
     * database were the results are stored and the Database Configuration.
     * 
     * @param dbName
     * @param dbConf 
     */
    public Modeler(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, Modeler.ModelParameters.class, Modeler.TrainingParameters.class);
    }

    /**
     * Generates predictions for the given dataset.
     * 
     * @param newData 
     */
    public void predict(Dataframe newData) {
        logger.info("predict()");
        
        evaluateData(newData, false);
    }
    
    /**
     * Validates the algorithm with the provided dataset and returns the Validation
     * Metrics. The provided dataset must contain the real response variables.
     * 
     * @param testData
     * @return 
     */
    public ValidationMetrics validate(Dataframe testData) {
        logger.info("validate()");
        
        return evaluateData(testData, true);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) { 
        
        //get the training parameters
        Modeler.TrainingParameters trainingParameters = kb().getTrainingParameters();
        
        DatabaseConfiguration dbConf = kb().getDbConf();
        
        //transform the training dataset
        Class dtClass = trainingParameters.getDataTransformerClass();
        
        boolean transformData = (dtClass!=null);
        if(transformData) {
            dataTransformer = Trainable.<AbstractTransformer>newInstance(dtClass, dbName, dbConf);
            dataTransformer.fit_transform(trainingData, trainingParameters.getDataTransformerTrainingParameters());
        }
        
        
        //find the most popular features
        Class fsClass = trainingParameters.getFeatureSelectionClass();
        
        boolean selectFeatures = (fsClass!=null);
        if(selectFeatures) {
            featureSelection = Trainable.<AbstractFeatureSelector>newInstance(fsClass, dbName, dbConf);
            featureSelection.fit_transform(trainingData, trainingParameters.getFeatureSelectionTrainingParameters()); 
        }
        
        
        
        
        //initialize mlmodel
        Class mlClass = trainingParameters.getMLmodelClass();
        mlmodel = Trainable.<AbstractModeler>newInstance(mlClass, dbName, dbConf); 
        
        //train the mlmodel on the whole dataset
        mlmodel.fit(trainingData, trainingParameters.getMLmodelTrainingParameters());
        
        if(transformData) {
            dataTransformer.denormalize(trainingData); //optional denormalization
        }
    }
    
    private ValidationMetrics evaluateData(Dataframe data, boolean estimateValidationMetrics) {
        //ensure db loaded
        kb().load();
        Modeler.TrainingParameters trainingParameters = kb().getTrainingParameters();
        
        DatabaseConfiguration dbConf = kb().getDbConf();
        
        Class dtClass = trainingParameters.getDataTransformerClass();
        
        boolean transformData = (dtClass!=null);
        if(transformData) {
            if(dataTransformer==null) {
                dataTransformer = Trainable.<AbstractTransformer>newInstance(dtClass, dbName, dbConf);
            }        
            dataTransformer.transform(data);
        }
        
        Class fsClass = trainingParameters.getFeatureSelectionClass();
        
        boolean selectFeatures = (fsClass!=null);
        if(selectFeatures) {
            if(featureSelection==null) {
                featureSelection = Trainable.<AbstractFeatureSelector>newInstance(fsClass, dbName, dbConf);
            }

            //remove unnecessary features
            featureSelection.transform(data);
        }
        
        
        //initialize mlmodel
        if(mlmodel==null) {
            Class mlClass = trainingParameters.getMLmodelClass();
            mlmodel = Trainable.<AbstractModeler>newInstance(mlClass, dbName, dbConf); 
        }
        
        //call predict of the mlmodel for the new dataset
        
        ValidationMetrics vm = null;
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
