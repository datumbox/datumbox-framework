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

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.bases.featureselection.CategoricalFeatureSelection;
import com.datumbox.framework.machinelearning.common.bases.featureselection.FeatureSelection;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel;
import com.datumbox.framework.machinelearning.common.bases.wrappers.BaseWrapper;
import com.datumbox.framework.machinelearning.common.bases.datatransformation.DataTransformer;
import com.datumbox.framework.utilities.text.cleaners.StringCleaner;
import com.datumbox.framework.utilities.text.extractors.TextExtractor;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Convenience class which can be used to train Text Classifiers from text files.
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
    
    @Override
    public void fit(Dataset trainingData, TrainingParameters trainingParameters) { 
        logger.info("fit()");
        
        initializeTrainingConfiguration(trainingParameters);
        
        _fit(trainingData);
        
        //store database
        knowledgeBase.save();
    }
    
    public void fit(Map<Object, URI> dataset, TrainingParameters trainingParameters) { 
        //build trainingDataset
        TextExtractor textExtractor = TextExtractor.newInstance(trainingParameters.getTextExtractorClass(), trainingParameters.getTextExtractorTrainingParameters());
        Dataset trainingData = Dataset.Builder.parseTextFiles(dataset, textExtractor, knowledgeBase.getDbConf());
        
        fit(trainingData, trainingParameters);
        
        trainingData.erase();
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
            //find the most popular features & remove unnecessary features
            featureSelection.fit_transform(trainingDataset, trainingParameters.getFeatureSelectionTrainingParameters());   
        }
        
        //initialize mlmodel
        mlmodel = BaseMLmodel.newInstance(trainingParameters.getMLmodelClass(), dbName, dbConf); 
        
        //train the mlmodel on the whole dataset
        mlmodel.fit(trainingDataset, trainingParameters.getMLmodelTrainingParameters());
        
        if(transformData) {
            dataTransformer.denormalize(trainingDataset); //optional denormalization
        }
    }
    
    public void predict(Dataset testDataset) {
        logger.info("predict()");
        
        //ensure db loaded
        knowledgeBase.load();
        
        getPredictions(testDataset);
    }
    
    public Dataset predict(URI datasetURI) {        
        //ensure db loaded
        knowledgeBase.load();
        
        //create a dummy dataset map
        Map<Object, URI> dataset = new HashMap<>();
        dataset.put(null, datasetURI);
        
        TextClassifier.TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        TextExtractor textExtractor = TextExtractor.newInstance(trainingParameters.getTextExtractorClass(), trainingParameters.getTextExtractorTrainingParameters());
        
        //build the testDataset
        Dataset testDataset = Dataset.Builder.parseTextFiles(dataset, textExtractor, knowledgeBase.getDbConf());
        
        predict(testDataset);
        
        return testDataset;
    }
    
    public Record predict(String text) {         
        //ensure db loaded
        knowledgeBase.load();
        
        TextClassifier.TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        TextExtractor textExtractor = TextExtractor.newInstance(trainingParameters.getTextExtractorClass(), trainingParameters.getTextExtractorTrainingParameters());
        
        Dataset testDataset = new Dataset(knowledgeBase.getDbConf());
        
        testDataset.add(new Record(new AssociativeArray(textExtractor.extract(StringCleaner.clear(text))), null));
        
        predict(testDataset);
        
        Record r = testDataset.get(testDataset.iterator().next());
        
        testDataset.erase();
        
        return r;
    }
        
    public BaseMLmodel.ValidationMetrics validate(Dataset testDataset) {  
        logger.info("validate()");
        
        //ensure db loaded
        knowledgeBase.load();
        
        BaseMLmodel.ValidationMetrics vm = getPredictions(testDataset);
        
        return vm;
    }
    
    public BaseMLmodel.ValidationMetrics validate(Map<Object, URI> dataset) {
        //ensure db loaded
        knowledgeBase.load();
        
        TextClassifier.TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        TextExtractor textExtractor = TextExtractor.newInstance(trainingParameters.getTextExtractorClass(), trainingParameters.getTextExtractorTrainingParameters());
        
        //build the testDataset
        Dataset testDataset = Dataset.Builder.parseTextFiles(dataset, textExtractor, knowledgeBase.getDbConf());
        
        BaseMLmodel.ValidationMetrics vm = validate(testDataset);
        
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
