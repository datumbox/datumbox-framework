/* 
 * Copyright (C) 2014 Vasilis Vryniotis <bbriniotis at datumbox.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.datumbox.framework.machinelearning.clustering;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.utilities.RandomValue;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.framework.machinelearning.datatransformation.DummyXYMinMaxNormalizer;
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
    public void testPredict() {
        System.out.println("predict"); 
        
        Dataset trainingData = KmeansTest.generateDataset();
        Dataset validationData = new Dataset();
        validationData.add(Record.newDataVector(new Object[] {51,"M","3",100,222,"no","0",143,"yes", 1.2,2,0,"3"}, "healthy"));
        validationData.add(Record.newDataVector(new Object[] {67,"M","4",120,229,"no","2",129,"yes", 2.6,2,2,"7"}, "problem"));
        
        MemoryConfiguration memoryConfiguration = new MemoryConfiguration();
        
        String dbName = "JUnitClusterer";
        

        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName);
        df.initializeTrainingConfiguration(memoryConfiguration, df.getEmptyTrainingParametersObject());
        df.transform(trainingData, true);
        df.normalize(trainingData);
        df.transform(validationData, false);
        df.normalize(validationData);
        
        HierarchicalAgglomerative instance = new HierarchicalAgglomerative(dbName);
        
        HierarchicalAgglomerative.TrainingParameters param = instance.getEmptyTrainingParametersObject();
        param.setDistanceMethod(HierarchicalAgglomerative.TrainingParameters.Distance.EUCLIDIAN);
        param.setLinkageMethod(HierarchicalAgglomerative.TrainingParameters.Linkage.COMPLETE);
        param.setMinClustersThreshold(2);
        param.setMaxDistanceThreshold(Double.MAX_VALUE);
        instance.initializeTrainingConfiguration(memoryConfiguration, param);
        instance.train(trainingData, validationData);
        
        
        instance = null;
        instance = new HierarchicalAgglomerative(dbName);
        instance.setMemoryConfiguration(memoryConfiguration);
        instance.predict(validationData);
        
        df.denormalize(trainingData);
        df.denormalize(validationData);
        df.erase(true);
        
        Map<Integer, Object> expResult = new HashMap<>();
        Map<Integer, Object> result = new HashMap<>();
        
        Map<Integer, HierarchicalAgglomerative.Cluster> clusters = instance.getClusters();
        for(Record r : validationData) {
            expResult.put(r.getId(), r.getY());
            Integer clusterId = (Integer) r.getYPredicted();
            Object label = clusters.get(clusterId).getLabelY();
            if(label==null) {
                label = clusterId;
            }
            result.put(r.getId(), label);
        }
        assertEquals(expResult, result);
        
        instance.erase(true);
    }

    
    /**
     * Test of kFoldCrossValidation method, of class HierarchicalAgglomerative.
     */
    @Test
    public void testKFoldCrossValidation() {
        System.out.println("kFoldCrossValidation");
        RandomValue.randomGenerator = new Random(42); 
        int k = 5;
        
        Dataset trainingData = KmeansTest.generateDataset();
        
        MemoryConfiguration memoryConfiguration = new MemoryConfiguration();
        
        
        String dbName = "JUnitRegressor";

        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName);
        df.initializeTrainingConfiguration(memoryConfiguration, df.getEmptyTrainingParametersObject());
        df.transform(trainingData, true);
        df.normalize(trainingData);

        
        
        
        HierarchicalAgglomerative instance = new HierarchicalAgglomerative(dbName);
        
        HierarchicalAgglomerative.TrainingParameters param = instance.getEmptyTrainingParametersObject();
        param.setDistanceMethod(HierarchicalAgglomerative.TrainingParameters.Distance.EUCLIDIAN);
        param.setLinkageMethod(HierarchicalAgglomerative.TrainingParameters.Linkage.COMPLETE);
        param.setMinClustersThreshold(2);
        param.setMaxDistanceThreshold(Double.MAX_VALUE);
        instance.initializeTrainingConfiguration(memoryConfiguration, param);
        HierarchicalAgglomerative.ValidationMetrics vm = instance.kFoldCrossValidation(trainingData, k);

        df.denormalize(trainingData);
        df.erase(true);

        
        double expResult = 0.76111111111111;
        double result = vm.getPurity();
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_MEDIUM);
        instance.erase(true);
    }

}
