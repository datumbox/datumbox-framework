/* 
 * Copyright (C) 2014 Vasilis Vryniotis <bbriniotis at datumbox.com>
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

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.utilities.RandomValue;
import com.datumbox.configuration.MemoryConfiguration;
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
        System.out.println("selectFeatures");
        RandomValue.randomGenerator = new Random(42);
        
        String dbName = "JUnitChisquareFeatureSelection";
        
        MemoryConfiguration memoryConfiguration = new MemoryConfiguration();
        
        TFIDF.TrainingParameters param = new TFIDF.TrainingParameters();
        param.setBinarized(false);
        param.setMaxFeatures(3);
        
        Dataset trainingData = new Dataset();
        Record r1 = new Record();
        r1.getX().put("important1", 2.0);
        r1.getX().put("important2", 3.0);
        r1.getX().put("stopword1", 10.0);
        r1.getX().put("stopword2", 4.0);
        r1.getX().put("stopword3", 8.0);
        trainingData.add(r1);
        
        Record r2 = new Record();
        r2.getX().put("important1", 2.0);
        r2.getX().put("important3", 5.0);
        r2.getX().put("stopword1", 10.0);
        r2.getX().put("stopword2", 2.0);
        r2.getX().put("stopword3", 4.0);
        trainingData.add(r2);
        
        Record r3 = new Record();
        r3.getX().put("important2", 2.0);
        r3.getX().put("important3", 5.0);
        r3.getX().put("stopword1", 10.0);
        r3.getX().put("stopword2", 2.0);
        r3.getX().put("stopword3", 4.0);
        trainingData.add(r3);
        
        TFIDF instance = new TFIDF(dbName);
        instance.initializeTrainingConfiguration(memoryConfiguration, param);
        
        instance.evaluateFeatures(trainingData);
        instance = null;
        
        
        instance = new TFIDF(dbName);
        instance.setMemoryConfiguration(memoryConfiguration);
        instance.clearFeatures(trainingData);
        
        Set<Object> expResult = new HashSet<>(Arrays.asList("important1", "important2", "important3"));
        Set<Object> result = trainingData.getColumns().keySet();
        assertEquals(expResult, result);
        instance.erase(true);
    }
    
}
