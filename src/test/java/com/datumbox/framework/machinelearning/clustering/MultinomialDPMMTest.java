/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
package com.datumbox.framework.machinelearning.clustering;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.RandomValue;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.framework.machinelearning.common.bases.basemodels.BaseDPMM;
import com.datumbox.tests.utilities.TestUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class MultinomialDPMMTest {
    
    public MultinomialDPMMTest() {
    }

    private Dataset generateDataset(DatabaseConfiguration dbConfig) {
        Dataset trainingData = new Dataset(dbConfig);
        //cluster 1
        trainingData.add(Record.newDataVector(new Object[] {10.0,13.0, 5.0,6.0,5.0,4.0, 0.0,0.0,0.0,0.0}, "c1"));
        trainingData.add(Record.newDataVector(new Object[] {11.0,11.0, 6.0,7.0,7.0,3.0, 0.0,0.0,1.0,0.0}, "c1"));
        trainingData.add(Record.newDataVector(new Object[] {12.0,12.0, 10.0,16.0,4.0,6.0, 0.0,0.0,0.0,2.0}, "c1"));
        //cluster 2
        trainingData.add(Record.newDataVector(new Object[] {10.0,13.0, 0.0,0.0,0.0,0.0, 5.0,6.0,5.0,4.0}, "c2"));
        trainingData.add(Record.newDataVector(new Object[] {11.0,11.0, 0.0,0.0,1.0,0.0, 6.0,7.0,7.0,3.0}, "c2"));
        trainingData.add(Record.newDataVector(new Object[] {12.0,12.0, 0.0,0.0,0.0,2.0, 10.0,16.0,4.0,6.0}, "c2"));
        //cluster 3
        trainingData.add(Record.newDataVector(new Object[] {10.0,13.0, 5.0,6.0,5.0,4.0, 5.0,6.0,5.0,4.0}, "c3"));
        trainingData.add(Record.newDataVector(new Object[] {11.0,11.0, 6.0,7.0,7.0,3.0, 6.0,7.0,7.0,3.0}, "c3"));
        trainingData.add(Record.newDataVector(new Object[] {12.0,12.0, 10.0,16.0,4.0,6.0, 10.0,16.0,4.0,6.0}, "c3"));
        
        return trainingData;
    }
    
    /**
     * Test of predict method, of class MultinomialDPMM.
     */
    @Test
    public void testValidate() {
        TestUtils.log(this.getClass(), "validate"); 
        RandomValue.setRandomGenerator(new Random(TestConfiguration.RANDOM_SEED));
        DatabaseConfiguration dbConfig = TestUtils.getDBConfig();
        
        Dataset trainingData = generateDataset(dbConfig);
        Dataset validationData = trainingData;

        
        
        
        String dbName = "JUnitClusterer";
        
        MultinomialDPMM instance = new MultinomialDPMM(dbName, dbConfig);
        
        MultinomialDPMM.TrainingParameters param = new MultinomialDPMM.TrainingParameters();
        param.setAlpha(0.01);
        param.setMaxIterations(100);
        param.setInitializationMethod(BaseDPMM.TrainingParameters.Initialization.ONE_CLUSTER_PER_RECORD);
        param.setAlphaWords(1);
        
        instance.fit(trainingData, param);
        
        
        instance = null;
        instance = new MultinomialDPMM(dbName, dbConfig);
        
        instance.validate(validationData);
        
        
        Map<Integer, Object> expResult = new HashMap<>();
        Map<Integer, Object> result = new HashMap<>();
        
        Map<Integer, MultinomialDPMM.Cluster> clusters = instance.getClusters();
        for(Integer rId : validationData) {
            Record r = validationData.get(rId);
            expResult.put(rId, r.getY());
            Integer clusterId = (Integer) r.getYPredicted();
            Object label = clusters.get(clusterId).getLabelY();
            if(label==null) {
                label = clusterId;
            }
            result.put(rId, label);
        }
        assertEquals(expResult, result);
        
        instance.erase();
    }

    
    /**
     * Test of kFoldCrossValidation method, of class MultinomialDPMM.
     */
    @Test
    public void testKFoldCrossValidation() {
        TestUtils.log(this.getClass(), "kFoldCrossValidation");
        RandomValue.setRandomGenerator(new Random(TestConfiguration.RANDOM_SEED));
        DatabaseConfiguration dbConfig = TestUtils.getDBConfig();
        
        int k = 5;
        
        Dataset trainingData = generateDataset(dbConfig);
        
        
        
        
        String dbName = "JUnitRegressor";


        
        
        
        MultinomialDPMM instance = new MultinomialDPMM(dbName, dbConfig);
        
        MultinomialDPMM.TrainingParameters param = new MultinomialDPMM.TrainingParameters();
        param.setAlpha(0.01);
        param.setMaxIterations(100);
        param.setInitializationMethod(BaseDPMM.TrainingParameters.Initialization.ONE_CLUSTER_PER_RECORD);
        param.setAlphaWords(1);
        
        MultinomialDPMM.ValidationMetrics vm = instance.kFoldCrossValidation(trainingData, param, k);

        
        double expResult = 1.0;
        double result = vm.getPurity();
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
        instance.erase();
    }

    
}
