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
import com.datumbox.common.utilities.RandomGenerator;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.framework.machinelearning.datatransformation.DummyXYMinMaxNormalizer;
import com.datumbox.framework.machinelearning.featureselection.continuous.PCA;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.tests.utilities.Datasets;
import com.datumbox.tests.utilities.TestUtils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class NLMSTest {
    
    public NLMSTest() {
    }


    /**
     * Test of predict method, of class NLMS.
     */
    @Test
    public void testValidate() {
        TestUtils.log(this.getClass(), "validate");
        RandomGenerator.setSeed(TestConfiguration.RANDOM_SEED);
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        Dataset[] data = Datasets.regressionNumeric(dbConf);
        
        Dataset trainingData = data[0];
        Dataset validationData = data[1];
        
        String dbName = this.getClass().getSimpleName();
        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName, dbConf);
        df.fit_transform(trainingData, new DummyXYMinMaxNormalizer.TrainingParameters());
        
        df.transform(validationData);
        

        NLMS instance = new NLMS(dbName, dbConf);
        
        NLMS.TrainingParameters param = new NLMS.TrainingParameters();
        param.setTotalIterations(1600);
        
        
        instance.fit(trainingData, param);
        
        
        instance = null;
        df = null;
        
        df = new DummyXYMinMaxNormalizer(dbName, dbConf);
        instance = new NLMS(dbName, dbConf);
        
        instance.validate(validationData);
        
        df.denormalize(trainingData);
        df.denormalize(validationData);
        
        for(Integer rId : validationData) {
            Record r = validationData.get(rId);
            assertEquals(TypeInference.toDouble(r.getY()), TypeInference.toDouble(r.getYPredicted()), TestConfiguration.DOUBLE_ACCURACY_LOW);
        }
        
        df.erase();
        instance.erase();
        
        trainingData.erase();
        validationData.erase();
    }


    /**
     * Test of kFoldCrossValidation method, of class NLMS.
     */
    @Test
    public void testKFoldCrossValidation() {
        TestUtils.log(this.getClass(), "kFoldCrossValidation");
        RandomGenerator.setSeed(TestConfiguration.RANDOM_SEED);
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        int k = 5;
        
        Dataset[] data = Datasets.regressionMixed(dbConf);
        Dataset trainingData = data[0];
        data[1].erase();
        
        String dbName = this.getClass().getSimpleName();
        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName, dbConf);
        df.fit_transform(trainingData, new DummyXYMinMaxNormalizer.TrainingParameters());
        

        
        
        PCA featureSelection = new PCA(dbName, dbConf);
        PCA.TrainingParameters featureSelectionParameters = new PCA.TrainingParameters();
        featureSelectionParameters.setMaxDimensions(trainingData.getVariableNumber()-1);
        featureSelectionParameters.setWhitened(false);
        featureSelectionParameters.setVarianceThreshold(0.99999995);
        featureSelection.fit(trainingData, featureSelectionParameters);
        featureSelection.transform(trainingData);
        featureSelection.erase();
        
        
        NLMS instance = new NLMS(dbName, dbConf);
        
        NLMS.TrainingParameters param = new NLMS.TrainingParameters();
        param.setTotalIterations(500);
        
        NLMS.ValidationMetrics vm = instance.kFoldCrossValidation(trainingData, param, k);

        df.denormalize(trainingData);

        
        double expResult = 0.9995921505698557;
        double result = vm.getRSquare();
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
        
        df.erase();
        instance.erase();
        
        trainingData.erase();
    }

    
}
