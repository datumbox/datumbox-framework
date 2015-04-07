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
package com.datumbox.framework.machinelearning.recommendersystem;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.RandomValue;
import com.datumbox.common.utilities.TypeConversions;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.tests.utilities.Datasets;
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
public class CollaborativeFilteringTest {
    
    public CollaborativeFilteringTest() {
    }

    /**
     * Test of predict method, of class MaximumEntropy.
     */
    @Test
    public void testPredict() {
        TestUtils.log(this.getClass(), "predict");
        RandomValue.setRandomGenerator(new Random(TestConfiguration.RANDOM_SEED));
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        Dataset[] data = Datasets.recommenderSystemFood(dbConf);
        
        Dataset trainingData = data[0];
        Dataset validationData = data[1];
        
        
        String dbName = "JUnitRecommender";
        CollaborativeFiltering instance = new CollaborativeFiltering(dbName, dbConf);
        
        CollaborativeFiltering.TrainingParameters param = new CollaborativeFiltering.TrainingParameters();
        param.setSimilarityMethod(CollaborativeFiltering.TrainingParameters.SimilarityMeasure.PEARSONS_CORRELATION);
        
        instance.fit(trainingData, param);
        
        
        instance = null;
        instance = new CollaborativeFiltering(dbName, dbConf);
        
        instance.predict(validationData);
        
        Map<Object, Double> expResult = new HashMap<>();
        expResult.put("potato", 4.7779023488181);
        expResult.put("pitta", 4.6745332285272);
        expResult.put("burger", 4.649291902095);
        expResult.put("chocolate", 4.5922134578209);
        expResult.put("sparklewatter", 0.5);
        expResult.put("rise", 0.5);
        expResult.put("risecookie", 0.5);
        expResult.put("tea", 0.5);
        
        
        AssociativeArray result = validationData.get(validationData.iterator().next()).getYPredictedProbabilities();
        for(Map.Entry<Object, Object> entry : result.entrySet()) {
            assertEquals(expResult.get(entry.getKey()), TypeConversions.toDouble(entry.getValue()), TestConfiguration.DOUBLE_ACCURACY_HIGH);
        }
        
        
        instance.erase();
    }

    
}
