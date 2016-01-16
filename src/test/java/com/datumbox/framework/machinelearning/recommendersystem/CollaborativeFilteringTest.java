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
package com.datumbox.framework.machinelearning.recommendersystem;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.Configuration;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.tests.Constants;
import com.datumbox.tests.abstracts.AbstractTest;
import com.datumbox.tests.Datasets;
import com.datumbox.tests.utilities.TestUtils;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for CollaborativeFiltering.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class CollaborativeFilteringTest extends AbstractTest {

    /**
     * Test of validate method, of class CollaborativeFiltering.
     */
    @Test
    public void testValidate() {
        logger.info("validate");
        
        Configuration conf = TestUtils.getConfig();
        
        Dataframe[] data = Datasets.recommenderSystemFood(conf);
        
        Dataframe trainingData = data[0];
        Dataframe validationData = data[1];
        
        
        String dbName = this.getClass().getSimpleName();
        CollaborativeFiltering instance = new CollaborativeFiltering(dbName, conf);
        
        CollaborativeFiltering.TrainingParameters param = new CollaborativeFiltering.TrainingParameters();
        param.setSimilarityMethod(CollaborativeFiltering.TrainingParameters.SimilarityMeasure.PEARSONS_CORRELATION);
        
        instance.fit(trainingData, param);
        
        instance.close();
        //instance = null;
        instance = new CollaborativeFiltering(dbName, conf);
        
        CollaborativeFiltering.ValidationMetrics vm = instance.validate(validationData);
        
        Map<Object, Double> expResult = new HashMap<>();
        expResult.put("pitta", 4.686394033077408);
        expResult.put("burger", 4.68408210680137);
        expResult.put("pizza", 4.6194430718558745);
        expResult.put("chocolate", 4.580630241051733);
        expResult.put("potato", 4.291658734729706);
        expResult.put("beer", 4.264285969929414);
        expResult.put("sparklewatter", 2.8034325749458997);
        expResult.put("salad", 1.496493323119103);
        expResult.put("risecookie", 1.372309723394662);
        expResult.put("tea", 1.3577402217087802);
        expResult.put("rise", 1.2243050068650592);
        
        AssociativeArray result = validationData.iterator().next().getYPredictedProbabilities();
        for(Map.Entry<Object, Object> entry : result.entrySet()) {
            assertEquals(expResult.get(entry.getKey()), TypeInference.toDouble(entry.getValue()), Constants.DOUBLE_ACCURACY_HIGH);
        }
        
        assertEquals(vm.getRMSE(), 0.7184568473420477, Constants.DOUBLE_ACCURACY_HIGH);
        
        instance.delete();
        
        trainingData.delete();
        validationData.delete();
    }

    
}
