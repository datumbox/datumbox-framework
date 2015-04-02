/**
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
package com.datumbox.applications.nlp;

import com.datumbox.common.utilities.RandomValue;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.framework.machinelearning.classification.MultinomialNaiveBayes;

import com.datumbox.framework.machinelearning.featureselection.categorical.ChisquareSelect;
import com.datumbox.framework.utilities.text.extractors.NgramsExtractor;
import com.datumbox.tests.utilities.TestUtils;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class TextClassifierTest {
    
    public TextClassifierTest() {
    }

    /**
     * Test of train method, of class TextClassifier.
     * @throws java.net.URISyntaxException
     */
    @Test
    public void testTrainAndPredict() throws URISyntaxException, MalformedURLException {
        TestConfiguration.getLogger().debug("TrainAndPredict");
        RandomValue.randomGenerator = new Random(42);
        
        
        String dbName = "JUnit";
        
        Map<Object, URI> dataset = new HashMap<>();
        dataset.put("negative", TestUtils.getRemoteFile(new URL("http://www.datumbox.com/files/datasets/example.neg")));
        dataset.put("positive", TestUtils.getRemoteFile(new URL("http://www.datumbox.com/files/datasets/example.pos")));
        
        
        TextClassifier instance = new TextClassifier(dbName, TestConfiguration.getDBConfig());
        TextClassifier.TrainingParameters trainingParameters = new TextClassifier.TrainingParameters();
        
        trainingParameters.setkFolds(5);
        
        //Classifier configuration
        trainingParameters.setMLmodelClass(MultinomialNaiveBayes.class);
        MultinomialNaiveBayes.TrainingParameters classifierTrainingParameters = new MultinomialNaiveBayes.TrainingParameters();
        trainingParameters.setMLmodelTrainingParameters(classifierTrainingParameters);
        
        //trainingParameters.setClassifierClass(SoftMaxRegression.class);
        //SoftMaxRegression.Parameters classifierTrainingParameters = new SoftMaxRegression.Parameters();
        //classifierTrainingParameters.setTotalIterations(100);
        //trainingParameters.setClassifierTrainingParameters(classifierTrainingParameters);
        
        //trainingParameters.setClassifierClass(SupportVectorMachine.class);
        //SupportVectorMachine.Parameters classifierTrainingParameters = new SupportVectorMachine.Parameters();
        //classifierTrainingParameters.getSvmParameter().kernel_type = svm_parameter.LINEAR;
        //trainingParameters.setClassifierTrainingParameters(classifierTrainingParameters);

        //trainingParameters.setMLmodelClass(Kmeans.class);
        //Kmeans.TrainingParameters modelTrainingParameters = new Kmeans.TrainingParameters();
        //modelTrainingParameters.setK(2);
        //modelTrainingParameters.setMaxIterations(200);
        //modelTrainingParameters.setInitMethod(Kmeans.TrainingParameters.Initialization.FORGY);
        //modelTrainingParameters.setDistanceMethod(Kmeans.TrainingParameters.Distance.EUCLIDIAN);
        //modelTrainingParameters.setWeighted(false);
        //modelTrainingParameters.setCategoricalGamaMultiplier(1.0);
        //modelTrainingParameters.setSubsetFurthestFirstcValue(2.0);
        //trainingParameters.setMLmodelTrainingParameters(modelTrainingParameters);
        
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
        
        //trainingParameters.setFeatureSelectionClass(TFIDF.class);
        //TFIDF.TrainingParameters fsParams = new TFIDF.TrainingParameters();
        //fsParams.setBinarized(false);
        //fsParams.setMaxFeatures(10000);
        //trainingParameters.setFeatureSelectionTrainingParameters(fsParams);
        
        //text extraction configuration
        trainingParameters.setTextExtractorClass(NgramsExtractor.class);
        trainingParameters.setTextExtractorTrainingParameters(new NgramsExtractor.Parameters());
        
        instance.fit(dataset, trainingParameters);
        
        
        
        MultinomialNaiveBayes.ValidationMetrics vm = (MultinomialNaiveBayes.ValidationMetrics) instance.validate(dataset);
        
        double expResult2 = 0.853460320496835;
        assertEquals(expResult2, vm.getMacroF1(), TestConfiguration.DOUBLE_ACCURACY_HIGH);
        
        instance = null;
        
        
        
        instance = new TextClassifier(dbName, TestConfiguration.getDBConfig());
        
        List<Object> result = instance.predict(TestUtils.getRemoteFile(new URL("http://www.datumbox.com/files/datasets/example.test")));
        
        List<Object> expResult = Arrays.asList("negative","positive");
        assertEquals(expResult, result);
        
        instance.erase();
    }

}
