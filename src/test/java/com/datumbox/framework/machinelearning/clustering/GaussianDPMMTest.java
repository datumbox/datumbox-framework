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
import com.datumbox.framework.machinelearning.common.bases.basemodels.BaseDPMM;
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
public class GaussianDPMMTest extends BaseTest {

    /**
     * Test of predict method, of class GaussianDPMM.
     */
    @Test
    public void testValidate() {
        logger.info("validate"); 
        
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        Dataset[] data = Datasets.gaussianClusters(dbConf);
        
        Dataset trainingData = data[0];
        Dataset validationData = data[1];

        
        String dbName = this.getClass().getSimpleName();
        GaussianDPMM instance = new GaussianDPMM(dbName, dbConf);
        
        GaussianDPMM.TrainingParameters param = new GaussianDPMM.TrainingParameters();
        param.setAlpha(0.01);
        param.setMaxIterations(100);
        param.setInitializationMethod(BaseDPMM.TrainingParameters.Initialization.ONE_CLUSTER_PER_RECORD);
        param.setKappa0(0);
        param.setNu0(1);
        param.setMu0(new double[]{0.0, 0.0});
        param.setPsi0(new double[][]{{1.0,0.0},{0.0,1.0}});
        
        instance.fit(trainingData, param);
        
        
        instance = null;
        instance = new GaussianDPMM(dbName, dbConf);
        
        instance.validate(validationData);
        
        
        Map<Integer, Object> expResult = new HashMap<>();
        Map<Integer, Object> result = new HashMap<>();
        
        Map<Integer, GaussianDPMM.Cluster> clusters = instance.getClusters();
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
        
        trainingData.erase();
        validationData.erase();
    }

    
    /**
     * Test of kFoldCrossValidation method, of class GaussianDPMM.
     */
    @Test
    public void testKFoldCrossValidation() {
        logger.info("kFoldCrossValidation");
         
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        int k = 5;
        
        Dataset[] data = Datasets.gaussianClusters(dbConf);
        Dataset trainingData = data[0];
        data[1].erase();
        
        
        String dbName = this.getClass().getSimpleName();
        GaussianDPMM instance = new GaussianDPMM(dbName, dbConf);
        
        GaussianDPMM.TrainingParameters param = new GaussianDPMM.TrainingParameters();
        param.setAlpha(0.01);
        param.setMaxIterations(100);
        param.setInitializationMethod(BaseDPMM.TrainingParameters.Initialization.ONE_CLUSTER_PER_RECORD);
        param.setKappa0(0);
        param.setNu0(1);
        param.setMu0(new double[]{0.0, 0.0});
        param.setPsi0(new double[][]{{1.0,0.0},{0.0,1.0}});
        
        GaussianDPMM.ValidationMetrics vm = instance.kFoldCrossValidation(trainingData, param, k);

        
        double expResult = 1.0;
        double result = vm.getPurity();
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
        instance.erase();
        
        trainingData.erase();
    }

    
}
