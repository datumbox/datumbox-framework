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
package com.datumbox.framework.applications.nlp;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.Record;
import com.datumbox.framework.common.interfaces.Trainable;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.core.machinelearning.common.abstracts.featureselectors.AbstractCategoricalFeatureSelector;
import com.datumbox.framework.core.machinelearning.common.abstracts.featureselectors.AbstractFeatureSelector;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelers.AbstractModeler;
import com.datumbox.framework.core.machinelearning.common.abstracts.wrappers.AbstractWrapper;
import com.datumbox.framework.core.machinelearning.common.abstracts.datatransformers.AbstractTransformer;
import com.datumbox.framework.core.machinelearning.common.interfaces.ValidationMetrics;
import com.datumbox.framework.common.utilities.StringCleaner;
import com.datumbox.framework.core.utilities.text.extractors.AbstractTextExtractor;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * TextClassifier is a convenience class which can be used to train Text Classification
 * models. It is a wrapper class which automatically takes care of the text parsing, 
 tokenization, feature selection and modeler training processes. It takes as input
 either a Dataframe object or multiple text files (one for each category) with 
 one observation per row.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class TextClassifier extends AbstractWrapper<TextClassifier.ModelParameters, TextClassifier.TrainingParameters> {
    
    /**
     * It contains all the Model AbstractParameters which are learned during the training.
     */
    public static class ModelParameters extends AbstractWrapper.AbstractModelParameters {
        private static final long serialVersionUID = 1L;
        
        /** 
         * @param dbc
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(DatabaseConnector)
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
    }
    
    /**
     * It contains the Training AbstractParameters of the Text Classifier.
     */
    public static class TrainingParameters extends AbstractWrapper.AbstractTrainingParameters<AbstractTransformer, AbstractFeatureSelector, AbstractModeler> {
        private static final long serialVersionUID = 1L;
        
        //Classes

        private Class<? extends AbstractTextExtractor> textExtractorClass;

        //Parameter Objects

        private AbstractTextExtractor.AbstractParameters textExtractorParameters;

        //Field Getters/Setters
        
        /**
         * Getter for the Text Extractor class which we use during the analysis.
         * 
         * @return 
         */
        public Class<? extends AbstractTextExtractor> getTextExtractorClass() {
            return textExtractorClass;
        }
        
        /**
         * Setter for the Text Extractor class which we use during the analysis.
         * The Text Extractor is responsible for extracting the keywords from
         * the text examples.
         * 
         * @param textExtractorClass 
         */
        public void setTextExtractorClass(Class<? extends AbstractTextExtractor> textExtractorClass) {
            this.textExtractorClass = textExtractorClass;
        }
        
        /**
         * Getter for the Text Extractor AbstractParameters.
         * 
         * @return 
         */
        public AbstractTextExtractor.AbstractParameters getTextExtractorParameters() {
            return textExtractorParameters;
        }
        
        /**
         * Setter for the Text Extractor AbstractParameters.
         * 
         * @param textExtractorParameters 
         */
        public void setTextExtractorParameters(AbstractTextExtractor.AbstractParameters textExtractorParameters) {
            this.textExtractorParameters = textExtractorParameters;
        }

    }

    /**
     * Constructor for the TextClassifier class. It accepts as arguments the name of the
     * database were the results are stored and the Database Configuration.
     * 
     * @param dbName
     * @param conf 
     */
    public TextClassifier(String dbName, Configuration conf) {
        super(dbName, conf, TextClassifier.ModelParameters.class, TextClassifier.TrainingParameters.class);
    }
    
    /**
     * Trains a Machine Learning modeler using the provided dataset files. The data
 map should have as index the names of each class and as values the URIs
 of the training files. The training files should contain one training example
 per row.
     * 
     * @param datasets
     * @param trainingParameters 
     */
    public void fit(Map<Object, URI> datasets, TrainingParameters trainingParameters) { 
        //build trainingDataset
        Dataframe trainingData = Dataframe.Builder.parseTextFiles(datasets,
                AbstractTextExtractor.newInstance(trainingParameters.getTextExtractorClass(), trainingParameters.getTextExtractorParameters()), 
                kb().getConf()
        );
        
        fit(trainingData, trainingParameters);
        
        trainingData.delete();
    }
    
    /**
     * Generates predictions for the provided dataset.
     * 
     * @param testDataset 
     */
    public void predict(Dataframe testDataset) {
        logger.info("predict()");
        
        //ensure db loaded
        kb().load();
        
        preprocessTestDataset(testDataset);
        modeler.predict(testDataset);
    }
    
    /**
     * Generates a Dataframe with the predictions for the provided data file. 
     * The data file should contain the text of one observation per row.
     * 
     * @param datasetURI
     * @return 
     */
    public Dataframe predict(URI datasetURI) {        
        //ensure db loaded
        kb().load();
        
        //create a dummy dataset map
        Map<Object, URI> dataset = new HashMap<>();
        dataset.put(null, datasetURI);
        
        TextClassifier.TrainingParameters trainingParameters = kb().getTrainingParameters();
        
        Dataframe testDataset = Dataframe.Builder.parseTextFiles(dataset, 
                AbstractTextExtractor.newInstance(trainingParameters.getTextExtractorClass(), trainingParameters.getTextExtractorParameters()), 
                kb().getConf()
        );
        
        predict(testDataset);
        
        return testDataset;
    }
    
    /**
     * It generates a prediction for a particular string. It returns a Record
     * object which contains the observation data, the predicted class and 
     * probabilities.
     * 
     * @param text
     * @return 
     */
    public Record predict(String text) {         
        //ensure db loaded
        kb().load();
        
        TextClassifier.TrainingParameters trainingParameters = kb().getTrainingParameters();
        
        Dataframe testDataset = new Dataframe(kb().getConf());
        
        testDataset.add(new Record(new AssociativeArray(
                AbstractTextExtractor.newInstance(trainingParameters.getTextExtractorClass(), trainingParameters.getTextExtractorParameters())
                        .extract(StringCleaner.clear(text))), null)
        );
        
        predict(testDataset);
        
        Record r = testDataset.iterator().next();
        
        testDataset.delete();
        
        return r;
    }
    
    /**
     * It validates the modeler using the provided dataset and it returns the 
 AbstractValidationMetrics. The testDataset should contain the real target variables.
     * 
     * @param testDataset
     * @return 
     */
    public ValidationMetrics validate(Dataframe testDataset) {
        logger.info("validate()");
        
        //ensure db loaded
        kb().load();
        
        preprocessTestDataset(testDataset);
        ValidationMetrics vm = modeler.validate(testDataset);
        
        return vm;
    }
    
    /**
     * It validates the modeler using the provided dataset files. The data
 map should have as index the names of each class and as values the URIs
 of the training files. The data files should contain one example
 per row.
     * 
     * @param datasets
     * @return 
     */
    public ValidationMetrics validate(Map<Object, URI> datasets) {
        //ensure db loaded
        kb().load();
        
        TextClassifier.TrainingParameters trainingParameters = kb().getTrainingParameters();
        
        //build the testDataset
        Dataframe testDataset = Dataframe.Builder.parseTextFiles(
                datasets, 
                AbstractTextExtractor.newInstance(trainingParameters.getTextExtractorClass(), trainingParameters.getTextExtractorParameters()), 
                kb().getConf()
        );
        
        ValidationMetrics vm = validate(testDataset);
        
        testDataset.delete();
        
        return vm;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingDataset) {
        TextClassifier.TrainingParameters trainingParameters = kb().getTrainingParameters();
        Configuration conf = kb().getConf();
        Class dtClass = trainingParameters.getDataTransformerClass();
        
        boolean transformData = (dtClass!=null);
        if(transformData) {
            dataTransformer = Trainable.<AbstractTransformer>newInstance(dtClass, dbName, conf);
            
            setParallelized(dataTransformer);
            
            dataTransformer.fit_transform(trainingDataset, trainingParameters.getDataTransformerTrainingParameters());
        }
        
        Class fsClass = trainingParameters.getFeatureSelectorClass();
        
        boolean selectFeatures = (fsClass!=null);
        if(selectFeatures) {
            featureSelector = Trainable.<AbstractFeatureSelector>newInstance(fsClass, dbName, conf);
            AbstractFeatureSelector.AbstractTrainingParameters featureSelectorParameters = trainingParameters.getFeatureSelectorTrainingParameters();
            if(AbstractCategoricalFeatureSelector.AbstractTrainingParameters.class.isAssignableFrom(featureSelectorParameters.getClass())) {
                ((AbstractCategoricalFeatureSelector.AbstractTrainingParameters)featureSelectorParameters).setIgnoringNumericalFeatures(false); //this should be turned off in feature selection
            }
            
            setParallelized(featureSelector);
            
            //find the most popular features & remove unnecessary features
            featureSelector.fit_transform(trainingDataset, trainingParameters.getFeatureSelectorTrainingParameters());   
        }
        
        //initialize modeler
        modeler = Trainable.<AbstractModeler>newInstance((Class<AbstractModeler>) trainingParameters.getModelerClass(), dbName, conf); 
            
        setParallelized(modeler);
        
        //train the modeler on the whole dataset
        modeler.fit(trainingDataset, trainingParameters.getModelerTrainingParameters());
        
        if(transformData) {
            dataTransformer.denormalize(trainingDataset); //optional denormalization
        }
    }
    
    private void preprocessTestDataset(Dataframe testDataset) {
        TextClassifier.TrainingParameters trainingParameters = kb().getTrainingParameters();
        Configuration conf = kb().getConf();
        
        Class dtClass = trainingParameters.getDataTransformerClass();
        
        boolean transformData = (dtClass!=null);
        if(transformData) {
            if(dataTransformer==null) {
                dataTransformer = Trainable.<AbstractTransformer>newInstance(dtClass, dbName, conf);
            }        
            
            setParallelized(dataTransformer);
            
            dataTransformer.transform(testDataset);
        }

        Class fsClass = trainingParameters.getFeatureSelectorClass();
        
        boolean selectFeatures = (fsClass!=null);
        if(selectFeatures) {
            if(featureSelector==null) {
                featureSelector = Trainable.<AbstractFeatureSelector>newInstance(fsClass, dbName, conf);
            }
            
            setParallelized(featureSelector);

            //remove unnecessary features
            featureSelector.transform(testDataset);
        }
        
        //initialize modeler
        if(modeler==null) {
            modeler = Trainable.<AbstractModeler>newInstance((Class<AbstractModeler>) trainingParameters.getModelerClass(), dbName, conf); 
        }
        
        setParallelized(modeler);
    }
    
}
