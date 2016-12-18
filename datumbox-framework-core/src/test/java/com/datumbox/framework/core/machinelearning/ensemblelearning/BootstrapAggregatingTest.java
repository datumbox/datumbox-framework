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
package com.datumbox.framework.core.machinelearning.ensemblelearning;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.Record;
import com.datumbox.framework.core.machinelearning.classification.MultinomialNaiveBayes;
import com.datumbox.framework.core.machinelearning.datatransformation.DummyXYMinMaxNormalizer;
import com.datumbox.framework.core.machinelearning.modelselection.metrics.ClassificationMetrics;
import com.datumbox.framework.core.machinelearning.modelselection.validators.KFoldValidator;
import com.datumbox.framework.tests.Constants;
import com.datumbox.framework.tests.Datasets;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for BootstrapAggregating.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class BootstrapAggregatingTest extends AbstractTest {

    /**
     * Test of validate method, of class BootstrapAggregating.
     */
    @Test
    public void testValidate() {
        logger.info("validate");
        
        Configuration conf = Configuration.getConfiguration();
        
        
        Dataframe[] data = Datasets.carsCategorical(conf);
        
        Dataframe trainingData = data[0];
        Dataframe validationData = data[1];
        
        
        String dbName = this.getClass().getSimpleName();
        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName, conf);
        df.fit_transform(trainingData, new DummyXYMinMaxNormalizer.TrainingParameters());
        df.transform(validationData);
        
        
        BootstrapAggregating instance = new BootstrapAggregating(dbName, conf);
        
        BootstrapAggregating.TrainingParameters param = new BootstrapAggregating.TrainingParameters();
        param.setMaxWeakClassifiers(5);
        param.setWeakClassifierClass(MultinomialNaiveBayes.class);
        
        
        MultinomialNaiveBayes.TrainingParameters trainingParameters = new MultinomialNaiveBayes.TrainingParameters();
        trainingParameters.setMultiProbabilityWeighted(true);
        
        
        param.setWeakClassifierTrainingParameters(trainingParameters);
        
        
        instance.fit(trainingData, param);
        
        
        instance.close();
        df.close();
        //instance = null;
        //df = null;
        
        df = new DummyXYMinMaxNormalizer(dbName, conf);
        instance = new BootstrapAggregating(dbName, conf);
        
        instance.predict(validationData);
        
        df.denormalize(trainingData);
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
        
        trainingData.delete();
        validationData.delete();
    }
    

    /**
     * Test of validate method, of class BootstrapAggregating.
     */
    @Test
    public void testKFoldCrossValidation() {
        logger.info("validate");
        
        Configuration conf = Configuration.getConfiguration();
        
        int k = 5;
        
        Dataframe[] data = Datasets.carsNumeric(conf);
        Dataframe trainingData = data[0];
        data[1].delete();
        

        
        BootstrapAggregating.TrainingParameters param = new BootstrapAggregating.TrainingParameters();
        param.setMaxWeakClassifiers(5);
        param.setWeakClassifierClass(MultinomialNaiveBayes.class);
        
        
        MultinomialNaiveBayes.TrainingParameters trainingParameters = new MultinomialNaiveBayes.TrainingParameters();
        trainingParameters.setMultiProbabilityWeighted(true);
        
        param.setWeakClassifierTrainingParameters(trainingParameters);

        
        ClassificationMetrics vm = new KFoldValidator<>(ClassificationMetrics.class, conf, k).validate(trainingData, param);
        
        double expResult = 0.6609432234432234;
        double result = vm.getMacroF1();
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
        
        trainingData.delete();
    }
    
}
