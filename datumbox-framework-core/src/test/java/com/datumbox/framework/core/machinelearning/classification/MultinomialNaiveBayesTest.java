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
package com.datumbox.framework.core.machinelearning.classification;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.Record;
import com.datumbox.framework.core.machinelearning.MLBuilder;
import com.datumbox.framework.core.machinelearning.datatransformation.DummyXYMinMaxNormalizer;
import com.datumbox.framework.core.machinelearning.modelselection.metrics.ClassificationMetrics;
import com.datumbox.framework.core.machinelearning.modelselection.Validator;
import com.datumbox.framework.core.machinelearning.modelselection.splitters.ShuffleSplitter;
import com.datumbox.framework.tests.Constants;
import com.datumbox.framework.tests.Datasets;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for MultinomialNaiveBayes.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MultinomialNaiveBayesTest extends AbstractTest {

    /**
     * Test of predict method, of class MultinomialNaiveBayes.
     */
    @Test
    public void testPredict() {
        logger.info("testPredict");
        
        Configuration configuration = Configuration.getConfiguration();
        
        
        Dataframe[] data = Datasets.carsCategorical(configuration);
        
        Dataframe trainingData = data[0];
        Dataframe validationData = data[1];
        
        
        String storageName = this.getClass().getSimpleName();
        DummyXYMinMaxNormalizer df = MLBuilder.create(new DummyXYMinMaxNormalizer.TrainingParameters(), configuration);
        
        df.fit_transform(trainingData);
        df.save(storageName);

        MultinomialNaiveBayes.TrainingParameters param = new MultinomialNaiveBayes.TrainingParameters();
        param.setMultiProbabilityWeighted(true);
        
        MultinomialNaiveBayes instance = MLBuilder.create(param, configuration);
        
        instance.fit(trainingData);
        instance.save(storageName);

        df.denormalize(trainingData);
        trainingData.close();
        
        instance.close();
        df.close();
        //instance = null;
        //df = null;
        
        df = MLBuilder.load(DummyXYMinMaxNormalizer.class, storageName, configuration);
        instance = MLBuilder.load(MultinomialNaiveBayes.class, storageName, configuration);

        df.transform(validationData);
        instance.predict(validationData);

        df.denormalize(validationData);


        
        Map<Integer, Object> expResult = new HashMap<>();
        Map<Integer, Object> result = new HashMap<>();
        for(Map.Entry<Integer, Record> e : validationData.entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            expResult.put(rId, r.getY());
            result.put(rId, r.getYPredicted());
        }
        assertEquals(expResult, result);
        
        df.delete();
        instance.delete();

        validationData.close();
    }


    /**
     * Test of validate method, of class MultinomialNaiveBayes.
     */
    @Test
    public void testShuffleValidation() {
        logger.info("testShuffleValidation");
        
        Configuration configuration = Configuration.getConfiguration();

        double proportion = 0.8;
        int splits = 5;
        
        Dataframe[] data = Datasets.carsNumeric(configuration);
        Dataframe trainingData = data[0];
        data[1].close();
        

        
        MultinomialNaiveBayes.TrainingParameters param = new MultinomialNaiveBayes.TrainingParameters();
        param.setMultiProbabilityWeighted(true);

        ClassificationMetrics vm = new Validator<>(ClassificationMetrics.class, configuration)
                .validate(new ShuffleSplitter(proportion, splits).split(trainingData), param);
        
        double expResult = 0.5983838383838384;
        double result = vm.getMacroF1();
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
        
        trainingData.close();
    }
    
}
