/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
public class GaussianDPMMTest {
    
    public GaussianDPMMTest() {
    }

    private Dataset generateDataset() {
        Random rnd = RandomValue.randomGenerator;
        Dataset trainingData = new Dataset();
        /*
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
        */
        int observationsPerCluster = 5;
        for(int i=0;i<observationsPerCluster;++i) {
            trainingData.add(Record.newDataVector(new Object[] {rnd.nextGaussian(),rnd.nextGaussian()}, "c1"));
        }
        
        for(int i=0;i<observationsPerCluster;++i) {
            trainingData.add(Record.newDataVector(new Object[] {100+rnd.nextGaussian(),50+rnd.nextGaussian()}, "c2"));
        }
        
        for(int i=0;i<observationsPerCluster;++i) {
            trainingData.add(Record.newDataVector(new Object[] {50+rnd.nextGaussian(),100+rnd.nextGaussian()}, "c3"));
        }
        
        return trainingData;
    }
    
    /**
     * Test of predict method, of class GaussianDPMM.
     */
    @Test
    public void testValidate() {
        TestUtils.log(this.getClass(), "validate"); 
        RandomValue.randomGenerator = new Random(42); 
        
        Dataset trainingData = generateDataset();
        Dataset validationData = trainingData;

        
        
        
        String dbName = "JUnitClusterer";
        
        GaussianDPMM instance = new GaussianDPMM(dbName, TestUtils.getDBConfig());
        
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
        instance = new GaussianDPMM(dbName, TestUtils.getDBConfig());
        
        instance.validate(validationData);
        
        
        Map<Integer, Object> expResult = new HashMap<>();
        Map<Integer, Object> result = new HashMap<>();
        
        Map<Integer, GaussianDPMM.Cluster> clusters = instance.getClusters();
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
        
        instance.erase();
    }

    
    /**
     * Test of kFoldCrossValidation method, of class GaussianDPMM.
     */
    @Test
    public void testKFoldCrossValidation() {
        TestUtils.log(this.getClass(), "kFoldCrossValidation");
        RandomValue.randomGenerator = new Random(42); 
        int k = 5;
        
        Dataset trainingData = generateDataset();
        
        
        
        
        String dbName = "JUnitRegressor";


        
        
        
        GaussianDPMM instance = new GaussianDPMM(dbName, TestUtils.getDBConfig());
        
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
    }

    
}
