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
package com.datumbox.applications.nlp;

import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.Configuration;
import com.datumbox.tests.TestConfiguration;
import com.datumbox.framework.machinelearning.classification.MultinomialNaiveBayes;

import com.datumbox.framework.machinelearning.featureselection.categorical.ChisquareSelect;
import com.datumbox.framework.utilities.text.extractors.NgramsExtractor;
import com.datumbox.tests.abstracts.AbstractTest;
import com.datumbox.tests.utilities.TestUtils;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for TextClassifier.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class TextClassifierTest extends AbstractTest {

    /**
     * Test of train method, of class TextClassifier.
     */
    @Test
    public void testTrainAndPredict() {
        logger.info("TrainAndPredict");
        
        Configuration conf = TestUtils.getConfig();
        
        
        String dbName = this.getClass().getSimpleName();
        
        Map<Object, URI> dataset = new HashMap<>();
        try {
            dataset.put("negative", this.getClass().getClassLoader().getResource("datasets/sentimentAnalysis.neg.txt").toURI());
            dataset.put("positive", this.getClass().getClassLoader().getResource("datasets/sentimentAnalysis.pos.txt").toURI());
        }
        catch(UncheckedIOException | URISyntaxException ex) {
            logger.warn("Unable to download datasets, skipping test.");
            throw new RuntimeException(ex);
        }
        
        TextClassifier instance = new TextClassifier(dbName, conf);
        TextClassifier.TrainingParameters trainingParameters = new TextClassifier.TrainingParameters();
        
        //Classifier configuration
        trainingParameters.setMLmodelClass(MultinomialNaiveBayes.class);
        MultinomialNaiveBayes.TrainingParameters classifierTrainingParameters = new MultinomialNaiveBayes.TrainingParameters();
        trainingParameters.setMLmodelTrainingParameters(classifierTrainingParameters);
        
        //data transfomation configuration
        trainingParameters.setDataTransformerClass(null);
        trainingParameters.setDataTransformerTrainingParameters(null);
        
        //feature selection configuration
        trainingParameters.setFeatureSelectionClass(ChisquareSelect.class);
        ChisquareSelect.TrainingParameters fsParams = new ChisquareSelect.TrainingParameters();
        fsParams.setALevel(0.05);
        fsParams.setIgnoringNumericalFeatures(false);
        fsParams.setMaxFeatures(10000);
        fsParams.setRareFeatureThreshold(3);
        trainingParameters.setFeatureSelectionTrainingParameters(fsParams);
        
        //text extraction configuration
        trainingParameters.setTextExtractorClass(NgramsExtractor.class);
        NgramsExtractor.Parameters exParams = new NgramsExtractor.Parameters();
        exParams.setMaxDistanceBetweenKwds(2);
        exParams.setExaminationWindowLength(6);
        trainingParameters.setTextExtractorParameters(exParams);
        
        instance.fit(dataset, trainingParameters);
        
        
        
        MultinomialNaiveBayes.ValidationMetrics vm = (MultinomialNaiveBayes.ValidationMetrics) instance.validate(dataset);
        
        instance.setValidationMetrics(vm);
        
        double expResult2 = 0.8515582285401859;
        assertEquals(expResult2, vm.getMacroF1(), TestConfiguration.DOUBLE_ACCURACY_HIGH);
        instance.close();
        //instance = null;
        
        
        
        instance = new TextClassifier(dbName, conf);
        Dataframe validationData = null;
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
        validationData.delete();
    }

}
