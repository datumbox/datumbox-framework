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
package com.datumbox.framework.machinelearning.clustering;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.framework.machinelearning.datatransformation.DummyXYMinMaxNormalizer;
import com.datumbox.tests.bases.BaseTest;
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
public class HierarchicalAgglomerativeTest extends BaseTest {

    /**
     * Test of predict method, of class MaximumEntropy.
     */
    @Test
    public void testValidate() {
        logger.info("validate"); 
        
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        
        Dataset[] data = Datasets.heartDiseaseClusters(dbConf);
        
        Dataset trainingData = data[0];
        Dataset validationData = data[1];
        
        
        String dbName = this.getClass().getSimpleName();
        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName, dbConf);
        
        df.fit_transform(trainingData, new DummyXYMinMaxNormalizer.TrainingParameters());
        df.transform(validationData);
        
        HierarchicalAgglomerative instance = new HierarchicalAgglomerative(dbName, dbConf);
        
        HierarchicalAgglomerative.TrainingParameters param = new HierarchicalAgglomerative.TrainingParameters();
        param.setDistanceMethod(HierarchicalAgglomerative.TrainingParameters.Distance.EUCLIDIAN);
        param.setLinkageMethod(HierarchicalAgglomerative.TrainingParameters.Linkage.COMPLETE);
        param.setMinClustersThreshold(2);
        param.setMaxDistanceThreshold(Double.MAX_VALUE);
        
        instance.fit(trainingData, param);
        
        
        instance = null;
        df = null;
        
        df = new DummyXYMinMaxNormalizer(dbName, dbConf);
        instance = new HierarchicalAgglomerative(dbName, dbConf);
        
        instance.validate(validationData);
        
        df.denormalize(trainingData);
        df.denormalize(validationData);
        
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
        
        df.erase();
        instance.erase();
        
        trainingData.erase();
        validationData.erase();
    }

    
    /**
     * Test of kFoldCrossValidation method, of class HierarchicalAgglomerative.
     */
    @Test
    public void testKFoldCrossValidation() {
        logger.info("kFoldCrossValidation");
        
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        int k = 5;
        
        Dataset[] data = Datasets.heartDiseaseClusters(dbConf);
        Dataset trainingData = data[0];
        data[1].erase();
        
        
        String dbName = this.getClass().getSimpleName();
        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName, dbConf);
        df.fit_transform(trainingData, new DummyXYMinMaxNormalizer.TrainingParameters());

        
        
        
        HierarchicalAgglomerative instance = new HierarchicalAgglomerative(dbName, dbConf);
        
        HierarchicalAgglomerative.TrainingParameters param = new HierarchicalAgglomerative.TrainingParameters();
        param.setDistanceMethod(HierarchicalAgglomerative.TrainingParameters.Distance.EUCLIDIAN);
        param.setLinkageMethod(HierarchicalAgglomerative.TrainingParameters.Linkage.COMPLETE);
        param.setMinClustersThreshold(2);
        param.setMaxDistanceThreshold(Double.MAX_VALUE);
        
        HierarchicalAgglomerative.ValidationMetrics vm = instance.kFoldCrossValidation(trainingData, param, k);

        df.denormalize(trainingData);

        
        double expResult = 0.7666666666666667;
        double result = vm.getPurity();
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
        
        df.erase();
        instance.erase();
        
        trainingData.erase();
    }

}
