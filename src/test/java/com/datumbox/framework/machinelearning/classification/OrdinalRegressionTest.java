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
package com.datumbox.framework.machinelearning.classification;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.RandomGenerator;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.framework.machinelearning.datatransformation.DummyXMinMaxNormalizer;
import com.datumbox.tests.utilities.Datasets;
import com.datumbox.tests.utilities.TestUtils;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class OrdinalRegressionTest {
    
    public OrdinalRegressionTest() {
    }

    /**
     * Test of predict method, of class OrdinalRegression.
     */
    @Test
    public void testValidate() {
        TestUtils.log(this.getClass(), "validate");
        RandomGenerator.setSeed(TestConfiguration.RANDOM_SEED);
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        
        Dataset[] data = Datasets.winesOrdinal(dbConf);
        
        Dataset trainingData = data[0];
        Dataset validationData = data[1];
        
        
        String dbName = this.getClass().getSimpleName();
        DummyXMinMaxNormalizer df = new DummyXMinMaxNormalizer(dbName, dbConf);
        
        df.fit_transform(trainingData, new DummyXMinMaxNormalizer.TrainingParameters());
        df.transform(validationData);
        
        OrdinalRegression instance = new OrdinalRegression(dbName, dbConf);
        
        OrdinalRegression.TrainingParameters param = new OrdinalRegression.TrainingParameters();
        param.setTotalIterations(100);
        
        instance.fit(trainingData, param);
        
        
        instance = null;
        df = null;
        
        df = new DummyXMinMaxNormalizer(dbName, dbConf);
        instance = new OrdinalRegression(dbName, dbConf);
        
        instance.validate(validationData);

        
        df.denormalize(trainingData);
        df.denormalize(validationData);

        Map<Integer, Object> expResult = new HashMap<>();
        Map<Integer, Object> result = new HashMap<>();
        for(Integer rId : validationData) {
            Record r = validationData.get(rId);
            expResult.put(rId, r.getY());
            result.put(rId, r.getYPredicted());
        }
        assertEquals(expResult, result);
        
        df.erase();
        instance.erase();
        
        trainingData.erase();
        validationData.erase();
    }


    /**
     * Test of kFoldCrossValidation method, of class OrdinalRegression.
     */
    @Test
    public void testKFoldCrossValidation() {
        TestUtils.log(this.getClass(), "kFoldCrossValidation");
        RandomGenerator.setSeed(TestConfiguration.RANDOM_SEED);
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        int k = 5;
        
        Dataset[] data = Datasets.winesOrdinal(dbConf);
        Dataset trainingData = data[0];
        data[1].erase();
        
        
        String dbName = this.getClass().getSimpleName();
        DummyXMinMaxNormalizer df = new DummyXMinMaxNormalizer(dbName, dbConf);
        
        df.fit_transform(trainingData, new DummyXMinMaxNormalizer.TrainingParameters());
        
        OrdinalRegression instance = new OrdinalRegression(dbName, dbConf);
        
        OrdinalRegression.TrainingParameters param = new OrdinalRegression.TrainingParameters();
        param.setTotalIterations(100);
        
        OrdinalRegression.ValidationMetrics vm = instance.kFoldCrossValidation(trainingData, param, k);

        	        
        df.denormalize(trainingData);


        
        double expResult = 0.9823403146614675;
        double result = vm.getMacroF1();
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
        
        df.erase();
        instance.erase();
        
        trainingData.erase();
    }

}
