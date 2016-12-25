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
import com.datumbox.framework.core.machinelearning.MLBuilder;
import com.datumbox.framework.core.machinelearning.classification.MultinomialNaiveBayes;
import com.datumbox.framework.core.machinelearning.modelselection.metrics.ClassificationMetrics;
import com.datumbox.framework.core.machinelearning.preprocessing.CornerConstraintsEncoder;
import com.datumbox.framework.core.machinelearning.preprocessing.MinMaxScaler;
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
        
        Configuration configuration = Configuration.getConfiguration();
        
        Dataframe[] data = Datasets.carsNumeric(configuration);
        Dataframe trainingData = data[0];
        
        Dataframe validationData = data[1];
        
        
        String storageName = this.getClass().getSimpleName();

        Modeler.TrainingParameters trainingParameters = new Modeler.TrainingParameters();
        

        //numerical scaling configuration
        MinMaxScaler.TrainingParameters nsParams = new MinMaxScaler.TrainingParameters();
        trainingParameters.setNumericalScalerTrainingParameters(nsParams);

        //categorical encoding configuration
        CornerConstraintsEncoder.TrainingParameters ceParams = new CornerConstraintsEncoder.TrainingParameters();
        trainingParameters.setCategoricalEncoderTrainingParameters(ceParams);
        
        //feature selection configuration
        trainingParameters.setFeatureSelectorTrainingParameters(null);

        //model Configuration
        MultinomialNaiveBayes.TrainingParameters modelTrainingParameters = new MultinomialNaiveBayes.TrainingParameters();
        modelTrainingParameters.setMultiProbabilityWeighted(true);
        trainingParameters.setModelerTrainingParameters(modelTrainingParameters);

        Modeler instance = MLBuilder.create(trainingParameters, configuration);
        instance.fit(trainingData);
        instance.save(storageName);

        instance.close();

        instance = MLBuilder.load(Modeler.class, storageName, configuration);

        instance.predict(trainingData);

        ClassificationMetrics vm = new ClassificationMetrics(trainingData);

        double expResult2 = 0.8;
        assertEquals(expResult2, vm.getMacroF1(), Constants.DOUBLE_ACCURACY_HIGH);

        trainingData.close();
        instance.close();


        instance = MLBuilder.load(Modeler.class, storageName, configuration);
        
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

        validationData.close();
    }
    
}
