/* 
 * Copyright (C) 2014 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
package com.datumbox.applications.nlp;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.utilities.DeepCopy;
import com.datumbox.framework.machinelearning.common.bases.featureselection.CategoricalFeatureSelection;
import com.datumbox.framework.machinelearning.common.bases.featureselection.FeatureSelection;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel;
import com.datumbox.framework.machinelearning.common.bases.wrappers.BaseWrapper;
import com.datumbox.framework.machinelearning.common.bases.datatransformation.DataTransformer;
import com.datumbox.framework.utilities.dataset.DatasetBuilder;
import com.datumbox.framework.utilities.text.extractors.TextExtractor;
import com.datumbox.framework.utilities.text.cleaners.StringCleaner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class TextClassifier extends BaseWrapper<TextClassifier.ModelParameters, TextClassifier.TrainingParameters>  {
    
    public static final String SHORT_METHOD_NAME = "TxtCl";
    
    public static class ModelParameters extends BaseWrapper.ModelParameters {
        
    }
    
    public static class TrainingParameters extends BaseWrapper.TrainingParameters<DataTransformer, FeatureSelection, BaseMLmodel> {

        //primitives/wrappers
        private Integer kFolds = 5;
        
        //Classes

        private Class<? extends TextExtractor> textExtractorClass;

        //Parameter Objects

        private TextExtractor.Parameters textExtractorTrainingParameters;

        //Field Getters/Setters

        public Integer getkFolds() {
            return kFolds;
        }

        public void setkFolds(Integer kFolds) {
            this.kFolds = kFolds;
        }

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

    
    
    
    public TextClassifier(String dbName) {
        super(dbName, TextClassifier.ModelParameters.class, TextClassifier.TrainingParameters.class);
    }
    
    @Override
    public final String shortMethodName() {
        return SHORT_METHOD_NAME;
    }

    
    
    public void train(Map<Object, URI> dataset) {  
        //Check if training can be performed
        if(!knowledgeBase.isConfigured()) {
            throw new RuntimeException("The training configuration is not initialized.");
        }
        else if(knowledgeBase.isTrained()) {
            throw new RuntimeException("The algorithm is already trainned. Reinitialize it or erase it.");
        }
        
        //get the training parameters
        TextClassifier.TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        
        TextExtractor textExtractor = TextExtractor.newInstance(trainingParameters.getTextExtractorClass());
        textExtractor.setParameters(trainingParameters.getTextExtractorTrainingParameters());
        
        //build trainingDataset
        Dataset trainingDataset = DatasetBuilder.parseFromTextFiles(dataset, textExtractor);
        //Dataset trainingDataset = DatasetBuilder.parseFromTextLists(DatasetBuilder.stringListsFromTextFiles(dataset), textExtractor);
        
        Class dtClass = trainingParameters.getDataTransformerClass();
        
        boolean transformData = (dtClass!=null);
        if(transformData) {
            dataTransformer = DataTransformer.newInstance(dtClass, dbName);
            dataTransformer.initializeTrainingConfiguration(knowledgeBase.getMemoryConfiguration(), trainingParameters.getDataTransformerTrainingParameters());
            dataTransformer.transform(trainingDataset, true);
            dataTransformer.normalize(trainingDataset);
        }
        
        Class fsClass = trainingParameters.getFeatureSelectionClass();
        
        boolean selectFeatures = (fsClass!=null);
        if(selectFeatures) {
            featureSelection = FeatureSelection.newInstance(fsClass, dbName);
            FeatureSelection.TrainingParameters featureSelectionParameters = trainingParameters.getFeatureSelectionTrainingParameters();
            if(CategoricalFeatureSelection.TrainingParameters.class.isAssignableFrom(featureSelectionParameters.getClass())) {
                ((CategoricalFeatureSelection.TrainingParameters)featureSelectionParameters).setIgnoringNumericalFeatures(false); //this should be turned off in feature selection
            }
            featureSelection.initializeTrainingConfiguration(knowledgeBase.getMemoryConfiguration(), trainingParameters.getFeatureSelectionTrainingParameters());

            //find the most popular features
            featureSelection.evaluateFeatures(trainingDataset);   

            //remove unnecessary features
            featureSelection.clearFeatures(trainingDataset);
        }
        
        //initialize mlmodel
        mlmodel = BaseMLmodel.newInstance(trainingParameters.getMLmodelClass(), dbName); 
        mlmodel.initializeTrainingConfiguration(knowledgeBase.getMemoryConfiguration(), trainingParameters.getMLmodelTrainingParameters());
        
        int k = trainingParameters.getkFolds();
        if(k>1) {
            //call k-fold cross validation and get the average accuracy
            BaseMLmodel.ValidationMetrics averageValidationMetrics = (BaseMLmodel.ValidationMetrics) mlmodel.kFoldCrossValidation(trainingDataset, k);

            //train the mlmodel on the whole dataset and pass as ValidationDataset the empty set
            mlmodel.train(trainingDataset, new Dataset());

            //set its ValidationMetrics to the average VP from k-fold cross validation
            mlmodel.setValidationMetrics(averageValidationMetrics);
        }
        else { //k==1
            Dataset validationDataset = trainingDataset;
            
            boolean algorithmModifiesDataset = mlmodel.modifiesData();
            if(algorithmModifiesDataset) {
                validationDataset = DeepCopy.<Dataset>cloneObject(validationDataset);
            }
            mlmodel.train(trainingDataset, validationDataset);
        }
        
        if(transformData) {
            dataTransformer.denormalize(trainingDataset); //optional denormalization
        }
        
        //store database
        knowledgeBase.save(true);
        knowledgeBase.setTrained(true);
    }
    
    public List<Object> predict(URI datasetURI) {
        List<String> text = new LinkedList<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(datasetURI)), "UTF8"))) {
            //read strings one by one
            for(String line; (line = br.readLine()) != null; ) {
                text.add(line);
            }
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        } 
        
        return predict(text);
    }
    
    public List<Object> predict(List<String> text) {
        
        Dataset predictedDataset = getPredictions(text);
        
        //extract responses
        List<Object> predictedClasses = new LinkedList<>();
        for(Record r : predictedDataset) {
            predictedClasses.add(r.getYPredicted());
        }
        predictedDataset = null;
        
        return predictedClasses;
    }
    
    public List<AssociativeArray> predictProbabilities(URI datasetURI) {
        List<String> text = new LinkedList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(new File(datasetURI)))) {
            //read strings one by one
            for(String line; (line = br.readLine()) != null; ) {
                text.add(line);
            }
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        } 
        
        return predictProbabilities(text);
    }
    
    public List<AssociativeArray> predictProbabilities(List<String> text) {
        
        Dataset predictedDataset = getPredictions(text);
        
        //extract responses
        List<AssociativeArray> predictedClassProbabilities = new LinkedList<>();
        for(Record r : predictedDataset) {
            predictedClassProbabilities.add(r.getYPredictedProbabilities());
        }
        predictedDataset = null;
        
        return predictedClassProbabilities;
    }
    
    public BaseMLmodel.ValidationMetrics test(Map<Object, URI> dataset) {  
        
        //ensure db loaded
        knowledgeBase.load();
        TextClassifier.TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        TextExtractor textExtractor = TextExtractor.newInstance(trainingParameters.getTextExtractorClass());
        textExtractor.setParameters(trainingParameters.getTextExtractorTrainingParameters());
        

        //build the testDataset
        Dataset testDataset = DatasetBuilder.parseFromTextFiles(dataset, textExtractor);
        //Dataset testDataset = DatasetBuilder.parseFromTextLists(DatasetBuilder.stringListsFromTextFiles(dataset), textExtractor);
        
        Class dtClass = trainingParameters.getDataTransformerClass();
        
        boolean transformData = (dtClass!=null);
        if(transformData) {
            if(dataTransformer==null) {
                dataTransformer = DataTransformer.newInstance(dtClass, dbName);
            }        
            dataTransformer.setMemoryConfiguration(knowledgeBase.getMemoryConfiguration());
            dataTransformer.transform(testDataset, false);
            dataTransformer.normalize(testDataset);
        }

        Class fsClass = trainingParameters.getFeatureSelectionClass();
        
        boolean selectFeatures = (fsClass!=null);
        if(selectFeatures) {
            if(featureSelection==null) {
                featureSelection = FeatureSelection.newInstance(fsClass, dbName);
            }
            featureSelection.setMemoryConfiguration(knowledgeBase.getMemoryConfiguration());

            //remove unnecessary features
            featureSelection.clearFeatures(testDataset);
        }
        
        
        //initialize mlmodel
        if(mlmodel==null) {
            mlmodel = BaseMLmodel.newInstance(trainingParameters.getMLmodelClass(), dbName); 
        }
        
        //call predict of the mlmodel for the new dataset
        mlmodel.setMemoryConfiguration(knowledgeBase.getMemoryConfiguration());
        BaseMLmodel.ValidationMetrics vm = mlmodel.test(testDataset);
        
        if(transformData) {
            dataTransformer.denormalize(testDataset); //optional denormization
        }
        
        return vm;
    }
    
    public BaseMLmodel.ValidationMetrics getValidationMetrics() {
        if(mlmodel==null) {
            test(new HashMap<>()); //this forces the loading of the algorithm
        }
        BaseMLmodel.ValidationMetrics vm =  mlmodel.getValidationMetrics();
        
        return vm;
    }
    
    
    private Dataset getPredictions(List<String> text) {
        
        //ensure db loaded
        knowledgeBase.load();
        TextClassifier.TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        //build the newDataset
        Dataset newData = new Dataset();
        
        TextExtractor textExtractor = TextExtractor.newInstance(trainingParameters.getTextExtractorClass());
        textExtractor.setParameters(trainingParameters.getTextExtractorTrainingParameters());
        
        //loop through every line of the text array
        for(String line : text) {
            Record r = new Record();
                        
            //extract features of the string and add every keyword combination in X map
            r.getX().putAll(textExtractor.extract(StringCleaner.clear(line)));

            //add each example in the newData
            newData.add(r); 
        }
        
        Class dtClass = trainingParameters.getDataTransformerClass();
        
        boolean transformData = (dtClass!=null);
        if(transformData) {
            if(dataTransformer==null) {
                dataTransformer = DataTransformer.newInstance(dtClass, dbName);
            }        
            dataTransformer.setMemoryConfiguration(knowledgeBase.getMemoryConfiguration());
            dataTransformer.transform(newData, false);
            dataTransformer.normalize(newData);
        }
        
        Class fsClass = trainingParameters.getFeatureSelectionClass();
        
        boolean selectFeatures = (fsClass!=null);
        if(selectFeatures) {
            if(featureSelection==null) {
                featureSelection = FeatureSelection.newInstance(fsClass, dbName);
            }
            featureSelection.setMemoryConfiguration(knowledgeBase.getMemoryConfiguration());

            //remove unnecessary features
            featureSelection.clearFeatures(newData);
        }
        
        
        //initialize mlmodel
        if(mlmodel==null) {
            mlmodel = BaseMLmodel.newInstance(trainingParameters.getMLmodelClass(), dbName); 
        }
        
        //call predict of the mlmodel for the new dataset
        mlmodel.setMemoryConfiguration(knowledgeBase.getMemoryConfiguration());
        mlmodel.predict(newData);
        
        if(transformData) {
            dataTransformer.denormalize(newData); //optional denormization
        }
        
        return newData;
    }
    

    
    
}
