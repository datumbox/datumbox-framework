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
package com.datumbox.applications.nlp;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.bases.featureselection.CategoricalFeatureSelection;
import com.datumbox.framework.machinelearning.common.bases.featureselection.FeatureSelection;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel;
import com.datumbox.framework.machinelearning.common.bases.wrappers.BaseWrapper;
import com.datumbox.framework.machinelearning.common.bases.datatransformation.DataTransformer;
import com.datumbox.framework.utilities.dataset.DatasetBuilder;
import com.datumbox.framework.utilities.text.extractors.TextExtractor;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class TextClassifier extends BaseWrapper<TextClassifier.ModelParameters, TextClassifier.TrainingParameters>  {
    
    public static class ModelParameters extends BaseWrapper.ModelParameters {

        public ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
    }
    
    public static class TrainingParameters extends BaseWrapper.TrainingParameters<DataTransformer, FeatureSelection, BaseMLmodel> {
        
        //Classes

        private Class<? extends TextExtractor> textExtractorClass;

        //Parameter Objects

        private TextExtractor.Parameters textExtractorTrainingParameters;

        //Field Getters/Setters

        public Class<? extends TextExtractor> getTextExtractorClass() {
            return textExtractorClass;
        }

        public void setTextExtractorClass(Class<? extends TextExtractor> textExtractorClass) {
            this.textExtractorClass = textExtractorClass;
        }

        public TextExtractor.Parameters getTextExtractorTrainingParameters() {
            return textExtractorTrainingParameters;
        }

        public void setTextExtractorTrainingParameters(TextExtractor.Parameters textExtractorTrainingParameters) {
            this.textExtractorTrainingParameters = textExtractorTrainingParameters;
        }

    }

    
    
    
    public TextClassifier(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, TextClassifier.ModelParameters.class, TextClassifier.TrainingParameters.class);
    }
    
    @Deprecated
    @Override
    public void fit(Dataset trainingData, TrainingParameters trainingParameters) {
        throw new UnsupportedOperationException("This version of fit() is not supported."); 
    }
    
    public void fit(Map<Object, URI> dataset, TrainingParameters trainingParameters) { 
        logger.info("fit()");
        
        initializeTrainingConfiguration(trainingParameters);
        
        TextExtractor textExtractor = TextExtractor.newInstance(trainingParameters.getTextExtractorClass());
        textExtractor.setParameters(trainingParameters.getTextExtractorTrainingParameters());
        
        //build trainingDataset
        Dataset trainingDataset = DatasetBuilder.parseFromTextFiles(dataset, textExtractor, knowledgeBase.getDbConf());
        
        _fit(trainingDataset);
        
        trainingDataset.erase();
        
        //store database
        knowledgeBase.save();
    }
    
    @Override
    protected void _fit(Dataset trainingDataset) {
        TextClassifier.TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        DatabaseConfiguration dbConf = knowledgeBase.getDbConf();
        Class dtClass = trainingParameters.getDataTransformerClass();
        
        boolean transformData = (dtClass!=null);
        if(transformData) {
            dataTransformer = DataTransformer.<DataTransformer>newInstance(dtClass, dbName, dbConf);
            dataTransformer.fit_transform(trainingDataset, trainingParameters.getDataTransformerTrainingParameters());
        }
        
        Class fsClass = trainingParameters.getFeatureSelectionClass();
        
        boolean selectFeatures = (fsClass!=null);
        if(selectFeatures) {
            featureSelection = FeatureSelection.<FeatureSelection>newInstance(fsClass, dbName, dbConf);
            FeatureSelection.TrainingParameters featureSelectionParameters = trainingParameters.getFeatureSelectionTrainingParameters();
            if(CategoricalFeatureSelection.TrainingParameters.class.isAssignableFrom(featureSelectionParameters.getClass())) {
                ((CategoricalFeatureSelection.TrainingParameters)featureSelectionParameters).setIgnoringNumericalFeatures(false); //this should be turned off in feature selection
            }
            //find the most popular features
            featureSelection.fit(trainingDataset, trainingParameters.getFeatureSelectionTrainingParameters());   

            //remove unnecessary features
            featureSelection.transform(trainingDataset);
        }
        
        //initialize mlmodel
        mlmodel = BaseMLmodel.newInstance(trainingParameters.getMLmodelClass(), dbName, dbConf); 
        
        //train the mlmodel on the whole dataset
        mlmodel.fit(trainingDataset, trainingParameters.getMLmodelTrainingParameters());
        
        if(transformData) {
            dataTransformer.denormalize(trainingDataset); //optional denormalization
        }
    }
    
    public Dataset predict(URI datasetURI) {
        logger.info("predict()");
        
        //create a dummy dataset map
        Map<Object, URI> dataset = new HashMap<>();
        dataset.put(null, datasetURI);
        
        //ensure db loaded
        knowledgeBase.load();
        TextClassifier.TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        DatabaseConfiguration dbConf = knowledgeBase.getDbConf();
        
        TextExtractor textExtractor = TextExtractor.newInstance(trainingParameters.getTextExtractorClass());
        textExtractor.setParameters(trainingParameters.getTextExtractorTrainingParameters());
        
        //build the testDataset
        Dataset testDataset = DatasetBuilder.parseFromTextFiles(dataset, textExtractor, dbConf);
        
        getPredictions(testDataset);
        
        return testDataset;
    }
    
    public BaseMLmodel.ValidationMetrics validate(Map<Object, URI> dataset) {  
        logger.info("validate()");
        
        //ensure db loaded
        knowledgeBase.load();
        TextClassifier.TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        DatabaseConfiguration dbConf = knowledgeBase.getDbConf();
        
        TextExtractor textExtractor = TextExtractor.newInstance(trainingParameters.getTextExtractorClass());
        textExtractor.setParameters(trainingParameters.getTextExtractorTrainingParameters());
        

        //build the testDataset
        Dataset testDataset = DatasetBuilder.parseFromTextFiles(dataset, textExtractor, dbConf);
        
        BaseMLmodel.ValidationMetrics vm = getPredictions(testDataset);
        
        testDataset.erase();
        
        return vm;
    }
    
    protected BaseMLmodel.ValidationMetrics getPredictions(Dataset testDataset) {
        TextClassifier.TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        DatabaseConfiguration dbConf = knowledgeBase.getDbConf();
        
        Class dtClass = trainingParameters.getDataTransformerClass();
        
        boolean transformData = (dtClass!=null);
        if(transformData) {
            if(dataTransformer==null) {
                dataTransformer = DataTransformer.<DataTransformer>newInstance(dtClass, dbName, dbConf);
            }        
            
            dataTransformer.transform(testDataset);
        }

        Class fsClass = trainingParameters.getFeatureSelectionClass();
        
        boolean selectFeatures = (fsClass!=null);
        if(selectFeatures) {
            if(featureSelection==null) {
                featureSelection = FeatureSelection.<FeatureSelection>newInstance(fsClass, dbName, dbConf);
            }

            //remove unnecessary features
            featureSelection.transform(testDataset);
        }
        
        
        //initialize mlmodel
        if(mlmodel==null) {
            mlmodel = BaseMLmodel.newInstance(trainingParameters.getMLmodelClass(), dbName, dbConf); 
        }
        
        //call predict of the mlmodel for the new dataset
        BaseMLmodel.ValidationMetrics vm = mlmodel.validate(testDataset);
        
        if(transformData) {
            dataTransformer.denormalize(testDataset); //optional denormization
        }
        
        return vm;
    }
    
    
}
