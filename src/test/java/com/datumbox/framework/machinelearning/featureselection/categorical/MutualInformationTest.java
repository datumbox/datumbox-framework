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
package com.datumbox.framework.machinelearning.featureselection.categorical;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.tests.bases.BaseTest;
import com.datumbox.tests.utilities.Datasets;
import com.datumbox.tests.utilities.TestUtils;
import java.util.Arrays;
import java.util.HashSet;

import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MutualInformationTest extends BaseTest {

    @Test
    public void testSelectFeatures() {
        logger.info("selectFeatures");
        
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        Dataset[] data = Datasets.featureSelectionCategorical(dbConf, 1000);
        Dataset trainingData = data[0];
        Dataset validationData = data[1];
        
        String dbName = this.getClass().getSimpleName();
        MutualInformation.TrainingParameters param = new MutualInformation.TrainingParameters();
        param.setRareFeatureThreshold(2);
        param.setMaxFeatures(5);
        param.setIgnoringNumericalFeatures(false);
        
        MutualInformation instance = new MutualInformation(dbName, dbConf);
        
        
        instance.fit_transform(trainingData, param);
        instance = null;
        
        
        instance = new MutualInformation(dbName, dbConf);
        
        instance.transform(validationData);
        
        Set<Object> expResult = new HashSet<>(Arrays.asList("high_paid", "has_boat", "has_luxury_car", "has_butler", "has_pool"));
        Set<Object> result = trainingData.getXDataTypes().keySet();
        assertEquals(expResult, result);
        instance.erase();
        
        trainingData.erase();
        validationData.erase();
    }

}
