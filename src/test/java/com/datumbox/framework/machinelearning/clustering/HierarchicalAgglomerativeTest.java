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
import com.datumbox.framework.machinelearning.datatransformation.DummyXYMinMaxNormalizer;
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
public class HierarchicalAgglomerativeTest {
    
    public HierarchicalAgglomerativeTest() {
    }


    /**
     * Test of predict method, of class MaximumEntropy.
     */
    @Test
    public void testValidate() {
        TestUtils.log(this.getClass(), "validate"); 
        RandomValue.setRandomGenerator(new Random(TestConfiguration.RANDOM_SEED));
        DatabaseConfiguration dbConfig = TestUtils.getDBConfig();
        
        Dataset trainingData = KmeansTest.generateDataset(dbConfig);
        Dataset validationData = new Dataset(dbConfig);
        validationData.add(Record.newDataVector(new Object[] {51,"M","3",100,222,"no","0",143,"yes", 1.2,2,0,"3"}, "healthy"));
        validationData.add(Record.newDataVector(new Object[] {67,"M","4",120,229,"no","2",129,"yes", 2.6,2,2,"7"}, "problem"));
        
        
        
        String dbName = "JUnitClusterer";
        

        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName, dbConfig);
        
        df.fit_transform(trainingData, new DummyXYMinMaxNormalizer.TrainingParameters());
        df.transform(validationData);
        
        HierarchicalAgglomerative instance = new HierarchicalAgglomerative(dbName, dbConfig);
        
        HierarchicalAgglomerative.TrainingParameters param = new HierarchicalAgglomerative.TrainingParameters();
        param.setDistanceMethod(HierarchicalAgglomerative.TrainingParameters.Distance.EUCLIDIAN);
        param.setLinkageMethod(HierarchicalAgglomerative.TrainingParameters.Linkage.COMPLETE);
        param.setMinClustersThreshold(2);
        param.setMaxDistanceThreshold(Double.MAX_VALUE);
        
        instance.fit(trainingData, param);
        
        
        instance = null;
        instance = new HierarchicalAgglomerative(dbName, dbConfig);
        
        instance.validate(validationData);
        
        df.denormalize(trainingData);
        df.denormalize(validationData);
        df.erase();
        
        Map<Integer, Object> expResult = new HashMap<>();
        Map<Integer, Object> result = new HashMap<>();
        
        Map<Integer, HierarchicalAgglomerative.Cluster> clusters = instance.getClusters();
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
     * Test of kFoldCrossValidation method, of class HierarchicalAgglomerative.
     */
    @Test
    public void testKFoldCrossValidation() {
        TestUtils.log(this.getClass(), "kFoldCrossValidation");
        RandomValue.setRandomGenerator(new Random(TestConfiguration.RANDOM_SEED));
        DatabaseConfiguration dbConfig = TestUtils.getDBConfig();
        
        int k = 5;
        
        Dataset trainingData = KmeansTest.generateDataset(dbConfig);
        
        
        
        
        String dbName = "JUnitRegressor";

        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName, dbConfig);
        df.fit_transform(trainingData, new DummyXYMinMaxNormalizer.TrainingParameters());

        
        
        
        HierarchicalAgglomerative instance = new HierarchicalAgglomerative(dbName, dbConfig);
        
        HierarchicalAgglomerative.TrainingParameters param = new HierarchicalAgglomerative.TrainingParameters();
        param.setDistanceMethod(HierarchicalAgglomerative.TrainingParameters.Distance.EUCLIDIAN);
        param.setLinkageMethod(HierarchicalAgglomerative.TrainingParameters.Linkage.COMPLETE);
        param.setMinClustersThreshold(2);
        param.setMaxDistanceThreshold(Double.MAX_VALUE);
        
        HierarchicalAgglomerative.ValidationMetrics vm = instance.kFoldCrossValidation(trainingData, param, k);

        df.denormalize(trainingData);
        df.erase();

        
        double expResult = 0.7666666666666667;
        double result = vm.getPurity();
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
        instance.erase();
    }

}
