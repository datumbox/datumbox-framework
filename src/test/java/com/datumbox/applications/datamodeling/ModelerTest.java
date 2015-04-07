/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.RandomValue;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.framework.machinelearning.classification.MultinomialNaiveBayes;
import com.datumbox.framework.machinelearning.datatransformation.DummyXMinMaxNormalizer;
import com.datumbox.tests.utilities.Datasets;
import com.datumbox.tests.utilities.TestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class ModelerTest {
    
    public ModelerTest() {
    }

    /**
     * Test of train and predict method, of class Modeler.
     */
    @Test
    public void testTrainAndValidate() {
        TestUtils.log(this.getClass(), "testTrainAndValidate");
        RandomValue.setRandomGenerator(new Random(TestConfiguration.RANDOM_SEED));
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        Dataset[] data = Datasets.carsNumeric(dbConf);
        Dataset trainingData = data[0];
        
        Dataset newData = data[1];
        
        
        
        
        
        
        
        String dbName = "JUnit";
        
        Modeler instance = new Modeler(dbName, dbConf);
        Modeler.TrainingParameters trainingParameters = new Modeler.TrainingParameters();
        trainingParameters.setkFolds(5);
        
        
        //Model Configuration
        
        trainingParameters.setMLmodelClass(MultinomialNaiveBayes.class);
        MultinomialNaiveBayes.TrainingParameters modelTrainingParameters = new MultinomialNaiveBayes.TrainingParameters();
        modelTrainingParameters.setMultiProbabilityWeighted(true);
        trainingParameters.setMLmodelTrainingParameters(modelTrainingParameters);

        //data transfomation configuration
        trainingParameters.setDataTransformerClass(DummyXMinMaxNormalizer.class);
        DummyXMinMaxNormalizer.TrainingParameters dtParams = new DummyXMinMaxNormalizer.TrainingParameters();
        trainingParameters.setDataTransformerTrainingParameters(dtParams);
        
        //feature selection configuration
        trainingParameters.setFeatureSelectionClass(null);
        trainingParameters.setFeatureSelectionTrainingParameters(null);
        
        instance.fit(trainingData, trainingParameters);
        
        
        MultinomialNaiveBayes.ValidationMetrics vm = (MultinomialNaiveBayes.ValidationMetrics) instance.validate(trainingData);
        
        double expResult2 = 0.8;
        assertEquals(expResult2, vm.getMacroF1(), TestConfiguration.DOUBLE_ACCURACY_HIGH);
        
        instance = null;
        
        
        TestUtils.log(this.getClass(), "validate");
        
        
        instance = new Modeler(dbName, dbConf);
        
        instance.validate(newData);
        
        
        
        Map<Integer, Object> expResult = new HashMap<>();
        Map<Integer, Object> result = new HashMap<>();
        for(Integer rId : newData) {
            Record r = newData.get(rId);
            expResult.put(rId, r.getY());
            result.put(rId, r.getYPredicted());
        }
        assertEquals(expResult, result);
        
        instance.erase();
    }
    
}
