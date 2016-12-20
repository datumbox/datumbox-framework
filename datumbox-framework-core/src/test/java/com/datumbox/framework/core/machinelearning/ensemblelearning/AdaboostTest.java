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
import com.datumbox.framework.core.machinelearning.MLBuilder;
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
 * Test cases for Adaboost.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class AdaboostTest extends AbstractTest {

    /**
     * Test of predict method, of class Adaboost.
     */
    @Test
    public void testPredict() {
        logger.info("testPredict");
        
        Configuration conf = Configuration.getConfiguration();
        
        
        Dataframe[] data = Datasets.carsCategorical(conf);
        
        Dataframe trainingData = data[0];
        Dataframe validationData = data[1];
        
        
        String dbName = this.getClass().getSimpleName();
        DummyXYMinMaxNormalizer df = MLBuilder.create(new DummyXYMinMaxNormalizer.TrainingParameters(), dbName, conf);
        df.fit_transform(trainingData);
        
        df.transform(validationData);
        

        
        Adaboost.TrainingParameters param = new Adaboost.TrainingParameters();
        param.setMaxWeakClassifiers(5);
        
        
        MultinomialNaiveBayes.TrainingParameters trainingParameters = new MultinomialNaiveBayes.TrainingParameters();
        trainingParameters.setMultiProbabilityWeighted(true);
        
        
        param.setWeakClassifierTrainingParameters(trainingParameters);

        Adaboost instance = MLBuilder.create(param, dbName, conf);
        
        instance.fit(trainingData);
        
        
        instance.close();
        df.close();
        //instance = null;
        //df = null;
        
        df = MLBuilder.load(DummyXYMinMaxNormalizer.class, dbName, conf);
        instance = MLBuilder.load(Adaboost.class, dbName, conf);
        
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
     * Test of validate method, of class Adaboost.
     */
    @Test
    public void testKFoldCrossValidation() {
        logger.info("testKFoldCrossValidation");
        
        Configuration conf = Configuration.getConfiguration();
        
        int k = 5;
        
        Dataframe[] data = Datasets.carsNumeric(conf);
        Dataframe trainingData = data[0];
        data[1].delete();

        
        Adaboost.TrainingParameters param = new Adaboost.TrainingParameters();
        param.setMaxWeakClassifiers(5);
        
        
        MultinomialNaiveBayes.TrainingParameters trainingParameters = new MultinomialNaiveBayes.TrainingParameters();
        trainingParameters.setMultiProbabilityWeighted(true);
        
        param.setWeakClassifierTrainingParameters(trainingParameters);


        ClassificationMetrics vm = new KFoldValidator<>(ClassificationMetrics.class, conf, k).validate(trainingData, param);;
        
        double expResult = 0.6923992673992675;
        double result = vm.getMacroF1();
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
        
        trainingData.delete();
    }
    
}
