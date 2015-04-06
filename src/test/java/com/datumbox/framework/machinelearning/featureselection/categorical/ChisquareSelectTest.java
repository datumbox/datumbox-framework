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
package com.datumbox.framework.machinelearning.featureselection.categorical;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.PHPfunctions;
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
public class ChisquareSelectTest {
    
    public ChisquareSelectTest() {
    }

    public static Dataset generateDataset(DatabaseConfiguration dbConfig, int n) {
        Dataset data = new Dataset(dbConfig);
        for(int i=0;i<n;++i) {
            AssociativeArray xData = new AssociativeArray();
            //important fields
            xData.put("high_paid", (PHPfunctions.mt_rand(0, 4)>3)?1:0);
            xData.put("has_boat", PHPfunctions.mt_rand(0, 1));
            xData.put("has_luxury_car", PHPfunctions.mt_rand(0, 1));
            xData.put("has_butler", PHPfunctions.mt_rand(0, 1));
            //xData.put("has_butler", (PHPfunctions.mt_rand(0, 1)==1)?"yes":"no");
            xData.put("has_pool", PHPfunctions.mt_rand(0, 1));
            
            //not important fields
            xData.put("has_tv", PHPfunctions.mt_rand(0, 1));
            xData.put("has_dog", PHPfunctions.mt_rand(0, 1));
            xData.put("has_cat", PHPfunctions.mt_rand(0, 1));
            xData.put("has_fish", PHPfunctions.mt_rand(0, 1));
            xData.put("random_field", (double)PHPfunctions.mt_rand(0, 1000));
            
            double richScore = xData.getDouble("has_boat")
                             + xData.getDouble("has_luxury_car")
                             //+ ((xData.get("has_butler").equals("yes"))?1.0:0.0)
                             + xData.getDouble("has_butler")
                             + xData.getDouble("has_pool");
            
            Boolean isRich=false;
            if(richScore>=2 || xData.getDouble("high_paid")==1.0) {
                isRich = true;
            }
            
            data.add(new Record(xData, isRich));
        }
        
        return data;
    }
    
    @Test
    public void testSelectFeatures() {
        TestUtils.log(this.getClass(), "selectFeatures");
        RandomValue.setRandomGenerator(new Random(TestConfiguration.RANDOM_SEED));
        DatabaseConfiguration dbConfig = TestUtils.getDBConfig();
        
        String dbName = "JUnitChisquareFeatureSelection";
        
        
        
        ChisquareSelect.TrainingParameters param = new ChisquareSelect.TrainingParameters();
        param.setRareFeatureThreshold(2);
        param.setMaxFeatures(5);
        param.setIgnoringNumericalFeatures(false);
        param.setALevel(0.05);
        
        Dataset trainingData = generateDataset(dbConfig, 1000);
        ChisquareSelect instance = new ChisquareSelect(dbName, dbConfig);
        
        
        instance.fit(trainingData, param);
        instance = null;
        
        
        instance = new ChisquareSelect(dbName, dbConfig);
        
        instance.transform(trainingData);
        
        Set<Object> expResult = new HashSet<>(Arrays.asList("high_paid", "has_boat", "has_luxury_car", "has_butler", "has_pool"));
        Set<Object> result = trainingData.getColumns().keySet();
        assertEquals(expResult, result);
        instance.erase();
    }
    
}
