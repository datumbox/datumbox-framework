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
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.Record;
import com.datumbox.framework.core.machinelearning.MLBuilder;
import com.datumbox.framework.core.machinelearning.classification.*;
import com.datumbox.framework.core.machinelearning.common.abstracts.featureselectors.AbstractFeatureSelector;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelers.AbstractClassifier;
import com.datumbox.framework.core.machinelearning.common.abstracts.transformers.AbstractNumericalScaler;
import com.datumbox.framework.core.machinelearning.featureselection.categorical.ChisquareSelect;
import com.datumbox.framework.core.machinelearning.featureselection.categorical.MutualInformation;
import com.datumbox.framework.core.machinelearning.featureselection.scorebased.TFIDF;
import com.datumbox.framework.core.machinelearning.modelselection.metrics.ClassificationMetrics;
import com.datumbox.framework.core.machinelearning.preprocessing.BinaryScaler;
import com.datumbox.framework.core.utilities.text.extractors.NgramsExtractor;
import com.datumbox.framework.tests.Constants;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.junit.Test;

import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for TextClassifier.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class TextClassifierTest extends AbstractTest {

    /**
     * Test of train and validate method, of class TextClassifier using BernoulliNaiveBayes.
     */ 
    @Test
    public void testTrainAndValidateBernoulliNaiveBayes() {
        logger.info("testTrainAndValidateBernoulliNaiveBayes");
        
        BernoulliNaiveBayes.TrainingParameters mlParams = new BernoulliNaiveBayes.TrainingParameters();
        
        ChisquareSelect.TrainingParameters fsParams = new ChisquareSelect.TrainingParameters();
        fsParams.setALevel(0.05);
        fsParams.setMaxFeatures(1000);
        fsParams.setRareFeatureThreshold(3);
        
        trainAndValidate(
                mlParams,
                fsParams,
                null,
                0.8393075950598075,
                1
        );
    }

    /**
     * Test of train and validate method, of class TextClassifier using BinarizedNaiveBayes.
     */ 
    @Test
    public void testTrainAndValidateBinarizedNaiveBayes() {
        logger.info("testTrainAndValidateBinarizedNaiveBayes");
        
        BinarizedNaiveBayes.TrainingParameters mlParams = new BinarizedNaiveBayes.TrainingParameters();
        
        ChisquareSelect.TrainingParameters fsParams = new ChisquareSelect.TrainingParameters();
        fsParams.setALevel(0.05);
        fsParams.setMaxFeatures(1000);
        fsParams.setRareFeatureThreshold(3);
        
        trainAndValidate(
                mlParams,
                fsParams,
                null,
                0.8413587159387832,
                2
        );
    }

    /**
     * Test of train and validate method, of class TextClassifier using MaximumEntropy.
     */ 
    @Test
    public void testTrainAndValidateMaximumEntropy() {
        logger.info("testTrainAndValidateMaximumEntropy");
        
        MaximumEntropy.TrainingParameters mlParams = new MaximumEntropy.TrainingParameters();
        
        ChisquareSelect.TrainingParameters fsParams = new ChisquareSelect.TrainingParameters();
        fsParams.setALevel(0.05);
        fsParams.setMaxFeatures(1000);
        fsParams.setRareFeatureThreshold(3);
        
        trainAndValidate(
                mlParams,
                fsParams,
                null,
                0.9411031042128604,
                3
        );
    }

    /**
     * Test of train and validate method, of class TextClassifier using MultinomialNaiveBayes.
     */ 
    @Test
    public void testTrainAndValidateMultinomialNaiveBayes() {
        logger.info("testTrainAndValidateMultinomialNaiveBayes");
        
        MultinomialNaiveBayes.TrainingParameters mlParams = new MultinomialNaiveBayes.TrainingParameters();
        
        ChisquareSelect.TrainingParameters fsParams = new ChisquareSelect.TrainingParameters();
        fsParams.setALevel(0.05);
        fsParams.setMaxFeatures(1000);
        fsParams.setRareFeatureThreshold(3);
        
        trainAndValidate(
                mlParams,
                fsParams,
                null,
                0.8685865263692268,
                4
        );
    }

    /**
     * Test of train and validate method, of class TextClassifier using OrdinalRegression.
     */ 
    @Test
    public void testTrainAndValidateOrdinalRegression() {
        logger.info("testTrainAndValidateOrdinalRegression");
        
        OrdinalRegression.TrainingParameters mlParams = new OrdinalRegression.TrainingParameters();
        
        ChisquareSelect.TrainingParameters fsParams = new ChisquareSelect.TrainingParameters();
        fsParams.setALevel(0.05);
        fsParams.setMaxFeatures(1000);
        fsParams.setRareFeatureThreshold(3);

        BinaryScaler.TrainingParameters nsParams = new BinaryScaler.TrainingParameters();
        nsParams.setScaleResponse(false);
        nsParams.setThreshold(0.0);

        trainAndValidate(
                mlParams,
                fsParams,
                nsParams,
                0.9272762308507563,
                5
        );
    }

    /**
     * Test of train and validate method, of class TextClassifier using SoftMaxRegression.
     */ 
    @Test
    public void testTrainAndValidateSoftMaxRegression() {
        logger.info("testTrainAndValidateSoftMaxRegression");
        
        SoftMaxRegression.TrainingParameters mlParams = new SoftMaxRegression.TrainingParameters();
        
        ChisquareSelect.TrainingParameters fsParams = new ChisquareSelect.TrainingParameters();
        fsParams.setALevel(0.05);
        fsParams.setMaxFeatures(1000);
        fsParams.setRareFeatureThreshold(3);

        BinaryScaler.TrainingParameters nsParams = new BinaryScaler.TrainingParameters();
        nsParams.setScaleResponse(false);
        nsParams.setThreshold(0.0);
        
        trainAndValidate(
                mlParams,
                fsParams,
                nsParams,
                0.8979999999999999,
                6
        );
    }

    /**
     * Test of train and validate method, of class TextClassifier using SupportVectorMachine.
     */ 
    @Test
    public void testTrainAndValidateSupportVectorMachine() {
        logger.info("testTrainAndValidateSupportVectorMachine");
        
        SupportVectorMachine.TrainingParameters mlParams = new SupportVectorMachine.TrainingParameters();
        
        ChisquareSelect.TrainingParameters fsParams = new ChisquareSelect.TrainingParameters();
        fsParams.setALevel(0.05);
        fsParams.setMaxFeatures(1000);
        fsParams.setRareFeatureThreshold(3);
        
        trainAndValidate(
                mlParams,
                fsParams,
                null,
                0.9803846153846154,
                7
        );
    }

    /**
     * Test of train and validate method, of class TextClassifier using MutualInformation.
     */ 
    @Test
    public void testTrainAndValidateMutualInformation() {
        logger.info("testTrainAndValidateMutualInformation");
        
        MultinomialNaiveBayes.TrainingParameters mlParams = new MultinomialNaiveBayes.TrainingParameters();
        
        MutualInformation.TrainingParameters fsParams = new MutualInformation.TrainingParameters();
        fsParams.setMaxFeatures(10000);
        fsParams.setRareFeatureThreshold(3);
        
        trainAndValidate(
                mlParams,
                fsParams,
                null,
                0.8954671493044679,
                8
        );
    }

    /**
     * Test of train and validate method, of class TextClassifier using TFIDF.
     */ 
    @Test
    public void testTrainAndValidateTFIDF() {
        logger.info("testTrainAndValidateTFIDF");
        
        MultinomialNaiveBayes.TrainingParameters mlParams = new MultinomialNaiveBayes.TrainingParameters();
        
        TFIDF.TrainingParameters fsParams = new TFIDF.TrainingParameters();
        fsParams.setMaxFeatures(1000);
        
        trainAndValidate(
                mlParams,
                fsParams,
                null,
                0.80461962936161,
                9
        );
    }
    
    /**
     * Trains and validates a model with the provided modeler and feature selector.
     * 
     * @param <ML>
     * @param <FS>
     * @param <NS>
     * @param modelerTrainingParameters
     * @param featureSelectorTrainingParameters
     * @param numericalScalerTrainingParameters
     * @param testId
     */
    private <ML extends AbstractClassifier, FS extends AbstractFeatureSelector, NS extends AbstractNumericalScaler> void trainAndValidate(
            ML.AbstractTrainingParameters modelerTrainingParameters,
            FS.AbstractTrainingParameters featureSelectorTrainingParameters,
            NS.AbstractTrainingParameters numericalScalerTrainingParameters,
            double expectedF1score,
            int testId) {
        Configuration configuration = Configuration.getConfiguration();
        
        
        String storageName = this.getClass().getSimpleName() + testId;
        
        Map<Object, URI> dataset = new HashMap<>();
        try {
            dataset.put("negative", this.getClass().getClassLoader().getResource("datasets/sentimentAnalysis.neg.txt").toURI());
            dataset.put("positive", this.getClass().getClassLoader().getResource("datasets/sentimentAnalysis.pos.txt").toURI());
        }
        catch(UncheckedIOException | URISyntaxException ex) {
            logger.warn("Unable to download datasets, skipping test.");
            throw new RuntimeException(ex);
        }

        TextClassifier.TrainingParameters trainingParameters = new TextClassifier.TrainingParameters();

        //numerical scaling configuration
        trainingParameters.setNumericalScalerTrainingParameters(numericalScalerTrainingParameters);

        //categorical encoding configuration
        trainingParameters.setCategoricalEncoderTrainingParameters(null);
        
        //feature selection configuration
        trainingParameters.setFeatureSelectorTrainingParameters(featureSelectorTrainingParameters);

        //classifier configuration
        trainingParameters.setModelerTrainingParameters(modelerTrainingParameters);
        
        //text extraction configuration
        NgramsExtractor.Parameters exParams = new NgramsExtractor.Parameters();
        exParams.setMaxDistanceBetweenKwds(2);
        exParams.setExaminationWindowLength(6);
        trainingParameters.setTextExtractorParameters(exParams);

        TextClassifier instance = MLBuilder.create(trainingParameters, configuration);
        instance.fit(dataset);
        instance.save(storageName);


        ClassificationMetrics vm = instance.validate(dataset);
        assertEquals(expectedF1score, vm.getMacroF1(), Constants.DOUBLE_ACCURACY_HIGH);

        instance.close();
        
        
        
        instance = MLBuilder.load(TextClassifier.class, storageName, configuration);
        Dataframe validationData;
        try {
            validationData = instance.predict(this.getClass().getClassLoader().getResource("datasets/sentimentAnalysis.unlabelled.txt").toURI());
        }
        catch(UncheckedIOException | URISyntaxException ex) {
            logger.warn("Unable to download datasets, skipping test.");
            throw new RuntimeException(ex);
        }
        
        List<Object> expResult = Arrays.asList("negative","positive");
        int i = 0;
        for(Record r : validationData.values()) {
            assertEquals(expResult.get(i), r.getYPredicted());
            ++i;
        }
        
        instance.delete();
        validationData.close();
    }

}
