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
package com.datumbox.framework.core.machinelearning.clustering;

import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.Record;
import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.tests.Constants;
import com.datumbox.framework.core.machinelearning.datatransformation.DummyXYMinMaxNormalizer;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import com.datumbox.framework.tests.Datasets;
import com.datumbox.framework.tests.utilities.TestUtils;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for HierarchicalAgglomerative.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class HierarchicalAgglomerativeTest extends AbstractTest {

    /**
     * Test of validate method, of class HierarchicalAgglomerative.
     */
    @Test
    public void testValidate() {
        logger.info("validate"); 
        
        Configuration conf = TestUtils.getConfig();
        
        
        Dataframe[] data = Datasets.heartDiseaseClusters(conf);
        
        Dataframe trainingData = data[0];
        Dataframe validationData = data[1];
        
        
        String dbName = this.getClass().getSimpleName();
        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName, conf);
        
        df.fit_transform(trainingData, new DummyXYMinMaxNormalizer.TrainingParameters());
        df.transform(validationData);
        
        HierarchicalAgglomerative instance = new HierarchicalAgglomerative(dbName, conf);
        
        HierarchicalAgglomerative.TrainingParameters param = new HierarchicalAgglomerative.TrainingParameters();
        param.setDistanceMethod(HierarchicalAgglomerative.TrainingParameters.Distance.EUCLIDIAN);
        param.setLinkageMethod(HierarchicalAgglomerative.TrainingParameters.Linkage.COMPLETE);
        param.setMinClustersThreshold(2);
        param.setMaxDistanceThreshold(Double.MAX_VALUE);
        
        instance.fit(trainingData, param);
        
        
        instance.close();
        df.close();
        //instance = null;
        //df = null;
        
        df = new DummyXYMinMaxNormalizer(dbName, conf);
        instance = new HierarchicalAgglomerative(dbName, conf);
        
        instance.validate(validationData);
        
        df.denormalize(trainingData);
        df.denormalize(validationData);
        
        Map<Integer, Object> expResult = new HashMap<>();
        Map<Integer, Object> result = new HashMap<>();
        
        Map<Integer, HierarchicalAgglomerative.Cluster> clusters = instance.getClusters();
        for(Map.Entry<Integer, Record> e : validationData.entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            expResult.put(rId, r.getY());
            Integer clusterId = (Integer) r.getYPredicted();
            Object label = clusters.get(clusterId).getLabelY();
            if(label==null) {
                label = clusterId;
            }
            result.put(rId, label);
        }
        assertEquals(expResult, result);
        
        df.delete();
        instance.delete();
        
        trainingData.delete();
        validationData.delete();
    }

    
    /**
     * Test of kFoldCrossValidation method, of class HierarchicalAgglomerative.
     */
    @Test
    public void testKFoldCrossValidation() {
        logger.info("kFoldCrossValidation");
        
        Configuration conf = TestUtils.getConfig();
        
        int k = 5;
        
        Dataframe[] data = Datasets.heartDiseaseClusters(conf);
        Dataframe trainingData = data[0];
        data[1].delete();
        
        
        String dbName = this.getClass().getSimpleName();
        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName, conf);
        df.fit_transform(trainingData, new DummyXYMinMaxNormalizer.TrainingParameters());

        
        
        
        HierarchicalAgglomerative instance = new HierarchicalAgglomerative(dbName, conf);
        
        HierarchicalAgglomerative.TrainingParameters param = new HierarchicalAgglomerative.TrainingParameters();
        param.setDistanceMethod(HierarchicalAgglomerative.TrainingParameters.Distance.EUCLIDIAN);
        param.setLinkageMethod(HierarchicalAgglomerative.TrainingParameters.Linkage.COMPLETE);
        param.setMinClustersThreshold(2);
        param.setMaxDistanceThreshold(Double.MAX_VALUE);
        
        HierarchicalAgglomerative.ValidationMetrics vm = instance.kFoldCrossValidation(trainingData, param, k);

        df.denormalize(trainingData);

        
        double expResult = 0.7666666666666667;
        double result = vm.getPurity();
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
        
        df.delete();
        instance.delete();
        
        trainingData.delete();
    }

}
