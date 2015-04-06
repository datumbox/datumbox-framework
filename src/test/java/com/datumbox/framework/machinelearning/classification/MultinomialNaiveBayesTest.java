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
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.RandomValue;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.framework.machinelearning.datatransformation.DummyXYMinMaxNormalizer;
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
public class MultinomialNaiveBayesTest {
    
    public MultinomialNaiveBayesTest() {
    }

    /**
     * Test of predict method, of class MultinomialNaiveBayes.
     */
    @Test
    public void testValidate() {
        TestUtils.log(this.getClass(), "validate");
        RandomValue.setRandomGenerator(new Random(TestConfiguration.RANDOM_SEED));
        DatabaseConfiguration dbConfig = TestUtils.getDBConfig();
        
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
        /*
        Dataset trainingData = new Dataset(dbConfig);
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
        Dataset validationData = new Dataset(dbConfig);
        validationData.add(Record.newDataVector(new Double[] {1.0, 0.0, 0.0, 1.0, 1.0, 0.0}, 0));
        */
        Dataset trainingData = new Dataset(dbConfig);
        trainingData.add(Record.newDataVector(new String[] {"red", "sports", "domestic"}, "yes"));
        trainingData.add(Record.newDataVector(new String[] {"red", "sports", "domestic"}, "no"));
        trainingData.add(Record.newDataVector(new String[] {"red", "sports", "domestic"}, "yes"));
        trainingData.add(Record.newDataVector(new String[] {"yellow", "sports", "domestic"}, "no"));
        trainingData.add(Record.newDataVector(new String[] {"yellow", "sports", "imported"}, "yes"));
        trainingData.add(Record.newDataVector(new String[] {"yellow", "suv", "imported"}, "no"));
        trainingData.add(Record.newDataVector(new String[] {"yellow", "suv", "imported"}, "yes"));
        trainingData.add(Record.newDataVector(new String[] {"yellow", "suv", "domestic"}, "no"));
        trainingData.add(Record.newDataVector(new String[] {"red", "suv", "imported"}, "no"));
        trainingData.add(Record.newDataVector(new String[] {"red", "sports", "imported"}, "yes"));
        
        Dataset validationData = new Dataset(dbConfig);
        validationData.add(Record.newDataVector(new String[] {"red", "suv", "domestic"}, "no"));
        
        
        
        
        String dbName = "JUnitClassifier";
        
        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName, dbConfig);
        
        df.fit_transform(trainingData, new DummyXYMinMaxNormalizer.TrainingParameters());
        df.transform(validationData);
        
        MultinomialNaiveBayes instance = new MultinomialNaiveBayes(dbName, dbConfig);
        
        MultinomialNaiveBayes.TrainingParameters param = new MultinomialNaiveBayes.TrainingParameters();
        param.setMultiProbabilityWeighted(true);
        
        instance.fit(trainingData, param);
        
        
        instance = null;
        instance = new MultinomialNaiveBayes(dbName, dbConfig);
        
        instance.validate(validationData);
        
        df.denormalize(trainingData);
        df.denormalize(validationData);
        df.erase();


        
        Map<Integer, Object> expResult = new HashMap<>();
        Map<Integer, Object> result = new HashMap<>();
        for(Integer rId : validationData) {
            Record r = validationData.get(rId);
            expResult.put(rId, r.getY());
            result.put(rId, r.getYPredicted());
        }
        assertEquals(expResult, result);
        
        instance.erase();
    }


    /**
     * Test of kFoldCrossValidation method, of class MultinomialNaiveBayes.
     */
    @Test
    public void testKFoldCrossValidation() {
        TestUtils.log(this.getClass(), "kFoldCrossValidation");
        RandomValue.setRandomGenerator(new Random(TestConfiguration.RANDOM_SEED));
        DatabaseConfiguration dbConfig = TestUtils.getDBConfig();
        
        int k = 5;
        
        Dataset trainingData = new Dataset(dbConfig);
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
        MultinomialNaiveBayes instance = new MultinomialNaiveBayes(dbName, dbConfig);
        
        MultinomialNaiveBayes.TrainingParameters param = new MultinomialNaiveBayes.TrainingParameters();
        param.setMultiProbabilityWeighted(true);
        
        MultinomialNaiveBayes.ValidationMetrics vm = instance.kFoldCrossValidation(trainingData, param, k);
        
        double expResult = 0.6631318681318682;
        double result = vm.getMacroF1();
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
        instance.erase();
    }
    
}
