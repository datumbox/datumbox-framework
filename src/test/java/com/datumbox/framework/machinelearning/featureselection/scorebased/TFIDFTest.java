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
package com.datumbox.framework.machinelearning.featureselection.scorebased;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.RandomValue;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.tests.utilities.TestUtils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class TFIDFTest {
    
    public TFIDFTest() {
    }

    /**
     * Test of shortMethodName method, of class TFIDF.
     */
    @Test
    public void testSelectFeatures() {
        TestUtils.log(this.getClass(), "selectFeatures");
        RandomValue.setRandomGenerator(new Random(TestConfiguration.RANDOM_SEED));
        DatabaseConfiguration dbConfig = TestUtils.getDBConfig();
        
        String dbName = "JUnitChisquareFeatureSelection";
        
        
        
        TFIDF.TrainingParameters param = new TFIDF.TrainingParameters();
        param.setBinarized(false);
        param.setMaxFeatures(3);
        
        Dataset trainingData = new Dataset(dbConfig);
        
        AssociativeArray xData1 = new AssociativeArray();
        xData1.put("important1", 2.0);
        xData1.put("important2", 3.0);
        xData1.put("stopword1", 10.0);
        xData1.put("stopword2", 4.0);
        xData1.put("stopword3", 8.0);
        trainingData.add(new Record(xData1, null));
        
        AssociativeArray xData2 = new AssociativeArray();
        xData2.put("important1", 2.0);
        xData2.put("important3", 5.0);
        xData2.put("stopword1", 10.0);
        xData2.put("stopword2", 2.0);
        xData2.put("stopword3", 4.0);
        trainingData.add(new Record(xData2, null));
        
        AssociativeArray xData3 = new AssociativeArray();
        xData3.put("important2", 2.0);
        xData3.put("important3", 5.0);
        xData3.put("stopword1", 10.0);
        xData3.put("stopword2", 2.0);
        xData3.put("stopword3", 4.0);
        trainingData.add(new Record(xData3, null));
        
        TFIDF instance = new TFIDF(dbName, dbConfig);
        
        instance.fit(trainingData, param);
        instance = null;
        
        
        instance = new TFIDF(dbName, dbConfig);
        
        instance.transform(trainingData);
        
        Set<Object> expResult = new HashSet<>(Arrays.asList("important1", "important2", "important3"));
        Set<Object> result = trainingData.getColumns().keySet();
        assertEquals(expResult, result);
        instance.erase();
    }
    
}
