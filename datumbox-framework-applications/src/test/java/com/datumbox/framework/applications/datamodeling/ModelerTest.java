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
package com.datumbox.framework.applications.datamodeling;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.Record;
import com.datumbox.framework.core.machinelearning.classification.MultinomialNaiveBayes;
import com.datumbox.framework.core.machinelearning.datatransformation.DummyXMinMaxNormalizer;
import com.datumbox.framework.core.machinelearning.modelselection.metrics.ClassificationMetrics;
import com.datumbox.framework.tests.Constants;
import com.datumbox.framework.tests.Datasets;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for Modeler.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ModelerTest extends AbstractTest {

    /**
     * Test of train and predict method, of class Modeler.
     */
    @Test
    public void testTrainAndValidate() {
        logger.info("testTrainAndValidate");
        
        Configuration conf = Configuration.getConfiguration();
        
        Dataframe[] data = Datasets.carsNumeric(conf);
        Dataframe trainingData = data[0];
        
        Dataframe validationData = data[1];
        
        
        String dbName = this.getClass().getSimpleName();
        
        Modeler instance = new Modeler(dbName, conf);
        Modeler.TrainingParameters trainingParameters = new Modeler.TrainingParameters();
        
        
        //Model Configuration
        
        trainingParameters.setModelerClass(MultinomialNaiveBayes.class);
        MultinomialNaiveBayes.TrainingParameters modelTrainingParameters = new MultinomialNaiveBayes.TrainingParameters();
        modelTrainingParameters.setMultiProbabilityWeighted(true);
        trainingParameters.setModelerTrainingParameters(modelTrainingParameters);

        //data transfomation configuration
        trainingParameters.setDataTransformerClass(DummyXMinMaxNormalizer.class);
        DummyXMinMaxNormalizer.TrainingParameters dtParams = new DummyXMinMaxNormalizer.TrainingParameters();
        trainingParameters.setDataTransformerTrainingParameters(dtParams);
        
        //feature selection configuration
        trainingParameters.setFeatureSelectorClass(null);
        trainingParameters.setFeatureSelectorTrainingParameters(null);
        
        instance.fit(trainingData, trainingParameters);

        instance.predict(trainingData);

        ClassificationMetrics vm = new ClassificationMetrics(trainingData);

        double expResult2 = 0.8;
        assertEquals(expResult2, vm.getMacroF1(), Constants.DOUBLE_ACCURACY_HIGH);

        instance.close();
        //instance = null;
        
        
        logger.info("validate");
        
        
        instance = new Modeler(dbName, conf);
        
        instance.predict(validationData);
        
        
        
        Map<Integer, Object> expResult = new HashMap<>();
        Map<Integer, Object> result = new HashMap<>();
        for(Map.Entry<Integer, Record> e : validationData.entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            expResult.put(rId, r.getY());
            result.put(rId, r.getYPredicted());
        }
        assertEquals(expResult, result);
        
        instance.delete();
        
        trainingData.delete();
        validationData.delete();
    }
    
}
