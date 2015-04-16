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
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.framework.machinelearning.datatransformation.DummyXYMinMaxNormalizer;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import com.datumbox.tests.utilities.Datasets;
import com.datumbox.tests.utilities.TestUtils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class StepwiseRegressionTest {
    
    public StepwiseRegressionTest() {
    }

    
    

    /**
     * Test of predict method, of class NLMS.
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
        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName, dbConf);
        df.fit_transform(trainingData, new DummyXYMinMaxNormalizer.TrainingParameters());
        df.transform(validationData);
        
        StepwiseRegression instance = new StepwiseRegression(dbName, dbConf);
        
        StepwiseRegression.TrainingParameters param = new StepwiseRegression.TrainingParameters();
        param.setAout(0.05);
        param.setRegressionClass(MatrixLinearRegression.class);
        
        MatrixLinearRegression.TrainingParameters trainingParams = new MatrixLinearRegression.TrainingParameters();
        param.setRegressionTrainingParameters(trainingParams);
                
        
        instance.fit(trainingData, param);
        
        
        instance = null;
        df = null;
        
        df = new DummyXYMinMaxNormalizer(dbName, dbConf);
        instance = new StepwiseRegression(dbName, dbConf);
        
        instance.validate(validationData);
        
	        
        df.denormalize(trainingData);
        df.denormalize(validationData);
        
        double std = Descriptives.std(trainingData.extractYValues().toFlatDataCollection(), true);
        for(Integer rId : validationData) {
            Record r = validationData.get(rId);
            assertEquals(TypeInference.toDouble(r.getY()), TypeInference.toDouble(r.getYPredicted()), std);
        }
        
        df.erase();
        instance.erase();
        
        trainingData.erase();
        validationData.erase();
    }


}
