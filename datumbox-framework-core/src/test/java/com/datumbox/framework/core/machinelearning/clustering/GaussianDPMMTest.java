/**
 * Copyright (C) 2013-2020 Vasilis Vryniotis <bbriniotis@datumbox.com>
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

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.machinelearning.MLBuilder;
import com.datumbox.framework.core.machinelearning.modelselection.metrics.ClusteringMetrics;
import com.datumbox.framework.core.machinelearning.modelselection.Validator;
import com.datumbox.framework.core.machinelearning.modelselection.splitters.KFoldSplitter;
import com.datumbox.framework.tests.Constants;
import com.datumbox.framework.core.Datasets;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for GaussianDPMM.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class GaussianDPMMTest extends AbstractTest {

    /**
     * Test of predict method, of class GaussianDPMM.
     */
    @Test
    public void testPredict() {
        logger.info("testPredict");
        
        Configuration configuration = getConfiguration();
        
        Dataframe[] data = Datasets.gaussianClusters(configuration);
        
        Dataframe trainingData = data[0];
        Dataframe validationData = data[1];

        
        String storageName = this.getClass().getSimpleName();
        
        GaussianDPMM.TrainingParameters param = new GaussianDPMM.TrainingParameters();
        param.setAlpha(0.01);
        param.setMaxIterations(100);
        param.setInitializationMethod(GaussianDPMM.TrainingParameters.Initialization.ONE_CLUSTER_PER_RECORD);
        param.setKappa0(0);
        param.setNu0(1);
        param.setMu0(new OpenMapRealVector(2));
        param.setPsi0(MatrixUtils.createRealIdentityMatrix(2));

        GaussianDPMM instance = MLBuilder.create(param, configuration);
        instance.fit(trainingData);
        instance.save(storageName);

        trainingData.close();
        instance.close();

        instance = MLBuilder.load(GaussianDPMM.class, storageName, configuration);

        instance.predict(validationData);
        ClusteringMetrics vm = new ClusteringMetrics(validationData);

        double expResult = 1.0;
        double result = vm.getPurity();
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
        
        instance.delete();

        validationData.close();
    }

    
    /**
     * Test of validate method, of class GaussianDPMM.
     */
    @Test
    public void testKFoldCrossValidation() {
        logger.info("testKFoldCrossValidation");
         
        Configuration configuration = getConfiguration();
        
        int k = 5;
        
        Dataframe[] data = Datasets.gaussianClusters(configuration);
        Dataframe trainingData = data[0];
        data[1].close();
        

        
        GaussianDPMM.TrainingParameters param = new GaussianDPMM.TrainingParameters();
        param.setAlpha(0.01);
        param.setMaxIterations(100);
        param.setInitializationMethod(GaussianDPMM.TrainingParameters.Initialization.ONE_CLUSTER_PER_RECORD);
        param.setKappa0(0);
        param.setNu0(1);
        param.setMu0(new OpenMapRealVector(2));
        param.setPsi0(MatrixUtils.createRealIdentityMatrix(2));

        ClusteringMetrics vm = new Validator<>(ClusteringMetrics.class, configuration)
                .validate(new KFoldSplitter(k).split(trainingData), param);
        System.out.println(vm);

        
        double expResult = 1.0;
        double result = vm.getPurity();
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
        
        trainingData.close();
    }

    
}
