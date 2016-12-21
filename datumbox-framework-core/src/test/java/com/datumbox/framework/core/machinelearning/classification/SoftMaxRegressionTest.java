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
import com.datumbox.framework.core.machinelearning.datatransformation.XMinMaxNormalizer;
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
 * Test cases for SoftMaxRegression.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class SoftMaxRegressionTest extends AbstractTest {

    /**
     * Test of predict method, of class SoftMaxRegression.
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
        df.save();


        SoftMaxRegression.TrainingParameters param = new SoftMaxRegression.TrainingParameters();
        param.setTotalIterations(2000);
        param.setL2(0.001);
        
        SoftMaxRegression instance = MLBuilder.create(param, dbName, conf);
        
        instance.fit(trainingData);
        instance.save();

        df.denormalize(trainingData);
        trainingData.delete();


        instance.close();
        df.close();
        //instance = null;
        //df = null;
        
        df = MLBuilder.load(DummyXYMinMaxNormalizer.class, dbName, conf);
        instance = MLBuilder.load(SoftMaxRegression.class, dbName, conf);

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

        validationData.delete();
    }


    /**
     * Test of validate method, of class SoftMaxRegression.
     */
    @Test
    public void testKFoldCrossValidation() {
        logger.info("testKFoldCrossValidation");
        
        Configuration conf = Configuration.getConfiguration();
        
        int k = 5;
        
        Dataframe[] data = Datasets.carsNumeric(conf);
        Dataframe trainingData = data[0];
        data[1].delete();
        
        
        String dbName = this.getClass().getSimpleName();
        XMinMaxNormalizer df = MLBuilder.create(new XMinMaxNormalizer.TrainingParameters(), dbName, conf);
        df.fit_transform(trainingData);

        SoftMaxRegression.TrainingParameters param = new SoftMaxRegression.TrainingParameters();
        param.setTotalIterations(30);
        param.setL1(0.0001);
        param.setL2(0.0001);

        ClassificationMetrics vm = new KFoldValidator<>(ClassificationMetrics.class, conf, k).validate(trainingData, param);

        df.denormalize(trainingData);
        
        double expResult = 0.7557492507492508;
        double result = vm.getMacroF1();
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
        df.close();
        
        trainingData.delete();
    }

    
}
