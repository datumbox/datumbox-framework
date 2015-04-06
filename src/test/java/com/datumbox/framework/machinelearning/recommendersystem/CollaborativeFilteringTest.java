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
package com.datumbox.framework.machinelearning.recommendersystem;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.RandomValue;
import com.datumbox.common.utilities.TypeConversions;
import com.datumbox.configuration.TestConfiguration;
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
        DatabaseConfiguration dbConfig = TestUtils.getDBConfig();
        
        Dataset trainingData = new Dataset(dbConfig);
        
        AssociativeArray xData1 = new AssociativeArray();
        xData1.put("ml1", 5.0);
        xData1.put("ml2", 4.0);
        xData1.put("ml3", 4.5);
        xData1.put("vg1", 1.0);
        xData1.put("vg2", 1.5);
        xData1.put("vg3", 0.5);
        trainingData.add(new Record(xData1, "pizza"));
        
        AssociativeArray xData2 = new AssociativeArray();
        xData2.put("ml1", 3.5);
        xData2.put("ml2", 4.5);
        xData2.put("ml3", 4.0);
        xData2.put("vg1", 1.5);
        xData2.put("vg2", 1.0);
        xData2.put("vg3", 2);
        trainingData.add(new Record(xData2, "burger"));
        
        AssociativeArray xData3 = new AssociativeArray();
        xData3.put("ml1", 4.0);
        xData3.put("ml2", 3.0);
        xData3.put("ml3", 5.0);
        xData3.put("vg1", 4.0);
        xData3.put("vg2", 3.0);
        xData3.put("vg3", 2.0);
        trainingData.add(new Record(xData3, "beer"));
        
        AssociativeArray xData4 = new AssociativeArray();
        xData4.put("ml1", 4.5);
        xData4.put("ml2", 4.0);
        xData4.put("ml3", 4.5);
        xData4.put("vg1", 4.5);
        xData4.put("vg2", 2.5);
        xData4.put("vg3", 1.0);
        trainingData.add(new Record(xData4, "potato"));
        
        AssociativeArray xData5 = new AssociativeArray();
        xData5.put("ml1", 1.5);
        xData5.put("ml2", 1.0);
        xData5.put("ml3", 0.5);
        xData5.put("vg1", 4.5);
        xData5.put("vg2", 5.0);
        xData5.put("vg3", 4.0);
        trainingData.add(new Record(xData5, "salad"));
        
        AssociativeArray xData6 = new AssociativeArray();
        xData6.put("ml1", 0.5);
        xData6.put("ml2", 0.5);
        xData6.put("ml3", 1.0);
        xData6.put("vg1", 4.0);
        xData6.put("vg2", 4.5);
        xData6.put("vg3", 5.0);
        trainingData.add(new Record(xData6, "risecookie"));
        
        AssociativeArray xData7 = new AssociativeArray();
        xData7.put("ml1", 4.0);
        xData7.put("ml2", 1.5);
        xData7.put("ml3", 3.0);
        xData7.put("vg1", 1.0);
        xData7.put("vg2", 5.0);
        xData7.put("vg3", 4.5);
        trainingData.add(new Record(xData7, "sparklewatter"));
        
        AssociativeArray xData8 = new AssociativeArray();
        xData8.put("ml1", 1.0);
        xData8.put("ml2", 1.0);
        xData8.put("ml3", 0.5);
        xData8.put("vg1", 4.0);
        xData8.put("vg2", 3.5);
        xData8.put("vg3", 5.0);
        trainingData.add(new Record(xData8, "rise"));
        
        AssociativeArray xData9 = new AssociativeArray();
        xData9.put("ml1", 3.0);
        xData9.put("ml2", 2.0);
        xData9.put("ml3", 1.0);
        xData9.put("vg1", 4.5);
        xData9.put("vg2", 4.5);
        xData9.put("vg3", 5.0);
        trainingData.add(new Record(xData9, "tea"));
        
        AssociativeArray xData10 = new AssociativeArray();
        xData10.put("ml1", 3.5);
        xData10.put("ml2", 5.0);
        xData10.put("ml3", 4.0);
        xData10.put("vg1", 1.5);
        xData10.put("vg2", 2.0);
        xData10.put("vg3", 2.5);
        trainingData.add(new Record(xData10, "chocolate"));
        
        AssociativeArray xData11 = new AssociativeArray();
        xData11.put("ml1", 5.0);
        xData11.put("ml2", 5.0);
        xData11.put("ml3", 5.0);
        xData11.put("vg1", 0.5);
        xData11.put("vg2", 0.5);
        xData11.put("vg3", 0.5);
        trainingData.add(new Record(xData11, "pitta"));
        
        Dataset newData = new Dataset(dbConfig);
        
        AssociativeArray profileData = new AssociativeArray();
        profileData.put("pizza", 4.5);
        profileData.put("beer", 5);
        profileData.put("salad", 0.5);
        newData.add(new Record(profileData, null));
        
        
        
        
        String dbName = "JUnitRecommender";
        CollaborativeFiltering instance = new CollaborativeFiltering(dbName, dbConfig);
        
        CollaborativeFiltering.TrainingParameters param = new CollaborativeFiltering.TrainingParameters();
        param.setSimilarityMethod(CollaborativeFiltering.TrainingParameters.SimilarityMeasure.PEARSONS_CORRELATION);
        
        instance.fit(trainingData, param);
        
        
        instance = null;
        instance = new CollaborativeFiltering(dbName, dbConfig);
        
        instance.predict(newData);
        
        Map<Object, Double> expResult = new HashMap<>();
        expResult.put("potato", 4.7779023488181);
        expResult.put("pitta", 4.6745332285272);
        expResult.put("burger", 4.649291902095);
        expResult.put("chocolate", 4.5922134578209);
        expResult.put("sparklewatter", 0.5);
        expResult.put("rise", 0.5);
        expResult.put("risecookie", 0.5);
        expResult.put("tea", 0.5);
        
        
        AssociativeArray result = newData.get(newData.iterator().next()).getYPredictedProbabilities();
        for(Map.Entry<Object, Object> entry : result.entrySet()) {
            assertEquals(expResult.get(entry.getKey()), TypeConversions.toDouble(entry.getValue()), TestConfiguration.DOUBLE_ACCURACY_HIGH);
        }
        
        
        instance.erase();
    }

    
}
