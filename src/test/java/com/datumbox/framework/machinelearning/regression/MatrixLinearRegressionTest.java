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
package com.datumbox.framework.machinelearning.regression;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.RandomSingleton;
import com.datumbox.common.utilities.TypeInference;
import com.datumbox.framework.machinelearning.datatransformation.DummyXYMinMaxNormalizer;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.framework.machinelearning.datatransformation.XYMinMaxNormalizer;
import com.datumbox.tests.utilities.Datasets;
import com.datumbox.tests.utilities.TestUtils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MatrixLinearRegressionTest {
    
    public MatrixLinearRegressionTest() {
    }


    /**
     * Test of predict method, of class MatrixLinearRegression.
     */
    @Test
    public void testValidate() {
        TestUtils.log(this.getClass(), "validate");
        RandomSingleton.getInstance().setSeed(TestConfiguration.RANDOM_SEED);
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        Dataset[] data = Datasets.regressionNumeric(dbConf);
        
        Dataset trainingData = data[0];
        Dataset validationData = data[1];
        
        String dbName = "JUnitRegressor";
        XYMinMaxNormalizer df = new XYMinMaxNormalizer(dbName, dbConf);
        df.fit_transform(trainingData, new XYMinMaxNormalizer.TrainingParameters());
        
        df.transform(validationData);
        
        df = null;

        MatrixLinearRegression instance = new MatrixLinearRegression(dbName, dbConf);
        
        MatrixLinearRegression.TrainingParameters param = new MatrixLinearRegression.TrainingParameters();
        
        instance.fit(trainingData, param);
        
        
        instance = null;
        instance = new MatrixLinearRegression(dbName, dbConf);
        
        instance.validate(validationData);
        
        df = new XYMinMaxNormalizer(dbName, dbConf);
        
	        
        df.denormalize(trainingData);
        df.denormalize(validationData);
        df.erase();


        for(Integer rId : validationData) {
            Record r = validationData.get(rId);
            assertEquals(TypeInference.toDouble(r.getY()), TypeInference.toDouble(r.getYPredicted()), TestConfiguration.DOUBLE_ACCURACY_HIGH);
        }
        
        instance.erase();
    }


    /**
     * Test of kFoldCrossValidation method, of class MatrixLinearRegression.
     */
    @Test
    public void testKFoldCrossValidation() {
        TestUtils.log(this.getClass(), "kFoldCrossValidation");
        RandomSingleton.getInstance().setSeed(TestConfiguration.RANDOM_SEED);
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        int k = 5;
        
        Dataset trainingData = Datasets.regressionMixed(dbConf)[0];
                
        String dbName = "JUnitRegressor";

        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName, dbConf);
        df.fit_transform(trainingData, new DummyXYMinMaxNormalizer.TrainingParameters());
        
        
        MatrixLinearRegression instance = new MatrixLinearRegression(dbName, dbConf);
        
        MatrixLinearRegression.TrainingParameters param = new MatrixLinearRegression.TrainingParameters();
        
        MatrixLinearRegression.ValidationMetrics vm = instance.kFoldCrossValidation(trainingData, param, k);
        
        df.denormalize(trainingData);
        df.erase();

        double expResult = 1;
        double result = vm.getRSquare();
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
        instance.erase();
    }


}
