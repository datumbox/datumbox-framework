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

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.core.machinelearning.MLBuilder;
import com.datumbox.framework.core.machinelearning.modelselection.metrics.ClusteringMetrics;
import com.datumbox.framework.core.machinelearning.modelselection.Validator;
import com.datumbox.framework.core.machinelearning.modelselection.splitters.KFoldSplitter;
import com.datumbox.framework.tests.Constants;
import com.datumbox.framework.tests.Datasets;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for MultinomialDPMM.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MultinomialDPMMTest extends AbstractTest {
    
    /**
     * Test of predict method, of class MultinomialDPMM.
     */
    @Test
    public void testPredict() {
        logger.info("testPredict");
        
        Configuration conf = Configuration.getConfiguration();
        
        Dataframe[] data = Datasets.multinomialClusters(conf);
        
        Dataframe trainingData = data[0];
        Dataframe validationData = data[1];

        
        String storageName = this.getClass().getSimpleName();
        
        MultinomialDPMM.TrainingParameters param = new MultinomialDPMM.TrainingParameters();
        param.setAlpha(0.01);
        param.setMaxIterations(100);
        param.setInitializationMethod(MultinomialDPMM.TrainingParameters.Initialization.ONE_CLUSTER_PER_RECORD);
        param.setAlphaWords(1);

        MultinomialDPMM instance = MLBuilder.create(param, conf);
        instance.fit(trainingData);
        instance.save(storageName);
        
        instance.close();
        //instance = null;
        instance = MLBuilder.load(MultinomialDPMM.class, storageName, conf);

        instance.predict(validationData);
        ClusteringMetrics vm = new ClusteringMetrics(validationData);

        double expResult = 1.0;
        double result = vm.getPurity();
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
        
        instance.delete();
        
        trainingData.close();
        validationData.close();
    }

    
    /**
     * Test of validate method, of class MultinomialDPMM.
     */
    @Test
    public void testKFoldCrossValidation() {
        logger.info("testKFoldCrossValidation");
        
        Configuration conf = Configuration.getConfiguration();
        
        int k = 5;
        
        Dataframe[] data = Datasets.multinomialClusters(conf);
        Dataframe trainingData = data[0];
        data[1].close();
        

        
        MultinomialDPMM.TrainingParameters param = new MultinomialDPMM.TrainingParameters();
        param.setAlpha(0.01);
        param.setMaxIterations(100);
        param.setInitializationMethod(MultinomialDPMM.TrainingParameters.Initialization.ONE_CLUSTER_PER_RECORD);
        param.setAlphaWords(1);

        ClusteringMetrics vm = new Validator<>(ClusteringMetrics.class, conf)
                .validate(new KFoldSplitter(k).split(trainingData), param);

        
        double expResult = 1.0;
        double result = vm.getPurity();
        Assert.assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
        
        trainingData.close();
    }

    
}
