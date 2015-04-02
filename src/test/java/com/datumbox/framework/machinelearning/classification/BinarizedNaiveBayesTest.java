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
package com.datumbox.framework.machinelearning.classification;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.utilities.RandomValue;
import com.datumbox.configuration.TestConfiguration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class BinarizedNaiveBayesTest {
    
    public BinarizedNaiveBayesTest() {
    }

    /**
     * Test of predict method, of class BinarizedNaiveBayes.
     */
    @Test
    public void testValidate() {
        TestConfiguration.getLogger().debug("validate");
        
        /*
        Example from http://www.inf.u-szeged.hu/~ormandi/ai2/06-naiveBayes-example.pdf
        FeatureList: 
            - 0: red
            - 1: yellow
            - 2: sports
            - 3: suv
            - 4: domestic
            - 5: imported
            - c1: yes
            - c2: no
        */
        Dataset trainingData = new Dataset();
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 1.0, 0.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 1.0, 0.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 1.0, 0.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 1.0, 0.0, 1.0, 0.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 1.0, 0.0, 0.0, 1.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 0.0, 1.0, 0.0, 1.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 0.0, 1.0, 0.0, 1.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 0.0, 1.0, 1.0, 0.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 0.0, 1.0, 0.0, 1.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 0.0, 1.0}, 1));
        
        Dataset validationData = new Dataset();
        validationData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 1.0, 0.0}, 1));
        
        
        
        String dbName = "JUnitClassifier";
        BinarizedNaiveBayes instance = new BinarizedNaiveBayes(dbName, TestConfiguration.getDBConfig());
        
        BinarizedNaiveBayes.TrainingParameters param = new BinarizedNaiveBayes.TrainingParameters();
        
        
        instance.fit(trainingData, param);
        
        
        instance = null;
        instance = new BinarizedNaiveBayes(dbName, TestConfiguration.getDBConfig());
        
        instance.validate(validationData);
        
        Map<Integer, Object> expResult = new HashMap<>();
        Map<Integer, Object> result = new HashMap<>();
        for(Record r : validationData) {
            expResult.put(r.getId(), r.getY());
            result.put(r.getId(), r.getYPredicted());
        }
        assertEquals(expResult, result);
        
        instance.erase();
    }


    /**
     * Test of kFoldCrossValidation method, of class BinarizedNaiveBayes.
     */
    @Test
    public void testKFoldCrossValidation() {
        TestConfiguration.getLogger().debug("kFoldCrossValidation");
        RandomValue.randomGenerator = new Random(42);
        int k = 5;
        
        Dataset trainingData = new Dataset();
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 1.0, 0.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 1.0, 0.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 1.0, 0.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 1.0, 0.0, 1.0, 0.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 1.0, 0.0, 0.0, 1.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 0.0, 1.0, 0.0, 1.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 0.0, 1.0, 0.0, 1.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 0.0, 1.0, 1.0, 0.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 0.0, 1.0, 0.0, 1.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 0.0, 1.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 1.0, 0.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 1.0, 0.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 1.0, 0.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 1.0, 0.0, 1.0, 0.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 1.0, 0.0, 0.0, 1.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 0.0, 1.0, 0.0, 1.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 0.0, 1.0, 0.0, 1.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 0.0, 1.0, 1.0, 0.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 0.0, 1.0, 0.0, 1.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 0.0, 1.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 1.0, 0.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 1.0, 0.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 1.0, 0.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 1.0, 0.0, 1.0, 0.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 1.0, 0.0, 0.0, 1.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 0.0, 1.0, 0.0, 1.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 0.0, 1.0, 0.0, 1.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 0.0, 1.0, 1.0, 0.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 0.0, 1.0, 0.0, 1.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 0.0, 1.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 1.0, 0.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 1.0, 0.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 1.0, 0.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 1.0, 0.0, 1.0, 0.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 1.0, 0.0, 0.0, 1.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 0.0, 1.0, 0.0, 1.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 0.0, 1.0, 0.0, 1.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 0.0, 1.0, 1.0, 0.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 0.0, 1.0, 0.0, 1.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 0.0, 1.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 1.0, 0.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 1.0, 0.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 1.0, 0.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 1.0, 0.0, 1.0, 0.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 1.0, 0.0, 0.0, 1.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 0.0, 1.0, 0.0, 1.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 0.0, 1.0, 0.0, 1.0}, 1));
        trainingData.add(Record.newDataVector(new Double[] {0.0, 1.0, 0.0, 1.0, 1.0, 0.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 0.0, 1.0, 0.0, 1.0}, 0));
        trainingData.add(Record.newDataVector(new Double[] {1.0, 0.0, 1.0, 0.0, 0.0, 1.0}, 1));
        
        
        
        
        
        
        String dbName = "JUnitClassifier";
        BinarizedNaiveBayes instance = new BinarizedNaiveBayes(dbName, TestConfiguration.getDBConfig());
        
        BinarizedNaiveBayes.TrainingParameters param = new BinarizedNaiveBayes.TrainingParameters();
        
        BinarizedNaiveBayes.ValidationMetrics vm = instance.kFoldCrossValidation(trainingData, param, k);
        
        double expResult = 0.6631318681318682;
        double result = vm.getMacroF1();
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
        instance.erase();
    }
    
}
