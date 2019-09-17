/**
 * Copyright (C) 2013-2019 Vasilis Vryniotis <bbriniotis@datumbox.com>
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

import com.datumbox.framework.applications.datamodeling.Modeler;
import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.core.common.text.StringCleaner;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.modelselection.metrics.ClassificationMetrics;
import com.datumbox.framework.core.common.text.extractors.AbstractTextExtractor;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * TextClassifier is a convenience class which can be used to train Text ClassificationMetrics
 * models. It is a wrapper class which automatically takes care of the text parsing, 
 tokenization, feature selection and modeler training processes. It takes as input
 either a Dataframe object or multiple text files (one for each category) with 
 one observation per row.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class TextClassifier extends Modeler {
    /**
     * Overwrite the pipeline steps and their order. No need for Categorical Encoding as the words are already encoded
     * by the Text Extractor.
     */
    {
        pipeline = Arrays.asList(FS_KEY, NS_KEY, /* CE_KEY, */ ML_KEY);
    }

    /**
     * It contains all the Model AbstractParameters which are learned during the training.
     */
    public static class ModelParameters extends Modeler.ModelParameters {
        private static final long serialVersionUID = 1L;
        
        /** 
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected ModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
        }
        
    }
    
    /**
     * It contains the Training AbstractParameters of the Text Classifier.
     */
    public static class TrainingParameters extends Modeler.TrainingParameters {
        private static final long serialVersionUID = 1L;
        
        //Classes

        //Parameter Objects

        private AbstractTextExtractor.AbstractParameters textExtractorParameters;

        //Field Getters/Setters
        
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
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainingParameters, Configuration)
     */
    protected TextClassifier(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(java.lang.String, Configuration)
     */
    protected TextClassifier(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }
    
    /**
     * Trains a Machine Learning modeler using the provided dataset files. The data
 map should have as index the names of each class and as values the URIs
 of the training files. The training files should contain one training example
 per row.
     * 
     * @param datasets
     */
    public void fit(Map<Object, URI> datasets) {
        TrainingParameters tp = (TrainingParameters) knowledgeBase.getTrainingParameters();
        Dataframe trainingData = Dataframe.Builder.parseTextFiles(datasets,
                AbstractTextExtractor.newInstance(tp.getTextExtractorParameters()),
                knowledgeBase.getConfiguration()
        );
        
        fit(trainingData);
        
        trainingData.close();
    }

    /**
     * Generates a Dataframe with the predictions for the provided data file. 
     * The data file should contain the text of one observation per row.
     * 
     * @param datasetURI
     * @return 
     */
    public Dataframe predict(URI datasetURI) {
        //create a dummy dataset map
        Map<Object, URI> dataset = new HashMap<>();
        dataset.put(null, datasetURI);
        
        TrainingParameters trainingParameters = (TrainingParameters) knowledgeBase.getTrainingParameters();
        
        Dataframe testDataset = Dataframe.Builder.parseTextFiles(dataset, 
                AbstractTextExtractor.newInstance(trainingParameters.getTextExtractorParameters()),
                knowledgeBase.getConfiguration()
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
        TrainingParameters trainingParameters = (TrainingParameters) knowledgeBase.getTrainingParameters();
        
        Dataframe testDataset = new Dataframe(knowledgeBase.getConfiguration());
        testDataset.add(
            new Record(
                new AssociativeArray(
                    AbstractTextExtractor.newInstance(trainingParameters.getTextExtractorParameters()).extract(StringCleaner.clear(text))
                ),
                null
            )
        );
        
        predict(testDataset);
        
        Record r = testDataset.iterator().next();
        
        testDataset.close();
        
        return r;
    }

    /**
     * It validates the modeler using the provided dataset and it returns the ClassificationMetrics. The testDataset should contain the real target variables.
     *
     * @param testDataset
     * @return
     */
    public ClassificationMetrics validate(Dataframe testDataset) {
        logger.info("validate()");

        predict(testDataset);

        ClassificationMetrics vm = new ClassificationMetrics(testDataset);

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
    public ClassificationMetrics validate(Map<Object, URI> datasets) {
        TrainingParameters trainingParameters = (TrainingParameters) knowledgeBase.getTrainingParameters();

        //build the testDataset
        Dataframe testDataset = Dataframe.Builder.parseTextFiles(
                datasets,
                AbstractTextExtractor.newInstance(trainingParameters.getTextExtractorParameters()),
                knowledgeBase.getConfiguration()
        );

        ClassificationMetrics vm = validate(testDataset);

        testDataset.close();

        return vm;
    }

}
