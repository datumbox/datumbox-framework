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
package com.datumbox.framework.core.machinelearning.featureselection.categorical;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.core.machinelearning.MLBuilder;
import com.datumbox.framework.tests.Datasets;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for MutualInformation.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MutualInformationTest extends AbstractTest {

    /**
     * Test of fit_transform method, of class MutualInformation.
     */
    @Test
    public void testSelectFeatures() {
        logger.info("selectFeatures");
        
        Configuration conf = Configuration.getConfiguration();
        
        Dataframe[] data = Datasets.featureSelectorCategorical(conf, 1000);
        Dataframe trainingData = data[0];
        Dataframe validationData = data[1];
        
        String dbName = this.getClass().getSimpleName();
        MutualInformation.TrainingParameters param = new MutualInformation.TrainingParameters();
        param.setRareFeatureThreshold(2);
        param.setMaxFeatures(5);
        
        MutualInformation instance = MLBuilder.create(param, dbName, conf);
        
        
        instance.fit_transform(trainingData);
        instance.save();

        instance.close();
        //instance = null;
        
        
        instance = MLBuilder.load(MutualInformation.class, dbName, conf);
        
        instance.transform(validationData);
        
        Set<Object> expResult = new HashSet<>(Arrays.asList("high_paid", "has_boat", "has_luxury_car", "has_butler", "has_pool"));
        Set<Object> result = trainingData.getXDataTypes().keySet();
        assertEquals(expResult, result);
        instance.delete();
        
        trainingData.delete();
        validationData.delete();
    }

}
