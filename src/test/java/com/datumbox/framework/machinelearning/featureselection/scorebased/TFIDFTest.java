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
