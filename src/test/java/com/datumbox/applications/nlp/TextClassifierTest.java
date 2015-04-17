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
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.framework.machinelearning.classification.MultinomialNaiveBayes;

import com.datumbox.framework.machinelearning.featureselection.categorical.ChisquareSelect;
import com.datumbox.framework.utilities.text.extractors.NgramsExtractor;
import com.datumbox.tests.bases.BaseTest;
import com.datumbox.tests.utilities.TestUtils;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class TextClassifierTest extends BaseTest {

    /**
     * Test of train method, of class TextClassifier.
     * @throws java.net.URISyntaxException
     * @throws java.net.MalformedURLException
     */
    @Test
    public void testTrainAndPredict() throws URISyntaxException, MalformedURLException {
        TestUtils.log(this.getClass(), "TrainAndPredict");
        
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        
        String dbName = this.getClass().getSimpleName();
        
        Map<Object, URI> dataset = new HashMap<>();
        try {
            dataset.put("negative", TestUtils.getRemoteFile(new URL("http://www.datumbox.com/files/datasets/example.neg")));
            dataset.put("positive", TestUtils.getRemoteFile(new URL("http://www.datumbox.com/files/datasets/example.pos")));
        }
        catch(Exception ex) {
            TestUtils.log(this.getClass(), "Unable to download datasets, skipping test.");
            return;
        }
        
        TextClassifier instance = new TextClassifier(dbName, dbConf);
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
        trainingParameters.setTextExtractorTrainingParameters(new NgramsExtractor.Parameters());
        
        instance.fit(dataset, trainingParameters);
        
        
        
        MultinomialNaiveBayes.ValidationMetrics vm = (MultinomialNaiveBayes.ValidationMetrics) instance.validate(dataset);
        
        instance.setValidationMetrics(vm);
        
        double expResult2 = 0.853460320496835;
        assertEquals(expResult2, vm.getMacroF1(), TestConfiguration.DOUBLE_ACCURACY_HIGH);
        
        instance = null;
        
        
        
        instance = new TextClassifier(dbName, dbConf);
        Dataset validationDataset = null;
        try {
            validationDataset = instance.predict(TestUtils.getRemoteFile(new URL("http://www.datumbox.com/files/datasets/example.test")));
        }
        catch(Exception ex) {
            TestUtils.log(this.getClass(), "Unable to download datasets, skipping test.");
            return;
        }
        
        List<Object> expResult = Arrays.asList("negative","positive");
        for(Integer rId : validationDataset) {
            Record r = validationDataset.get(rId);
            assertEquals(expResult.get(rId), r.getYPredicted());
        }
        
        instance.erase();
        validationDataset.erase();
    }

}
