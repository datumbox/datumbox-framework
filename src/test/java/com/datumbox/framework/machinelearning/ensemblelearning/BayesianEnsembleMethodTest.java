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
package com.datumbox.framework.machinelearning.ensemblelearning;

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
public class BayesianEnsembleMethodTest {
    
    public BayesianEnsembleMethodTest() {
    }


    /**
     * Test of predict method, of class BayesianEnsembleMethod.
     */
    @Test
    public void testValidate() {
        TestUtils.log(this.getClass(), "validate");
        RandomValue.setRandomGenerator(new Random(TestConfiguration.RANDOM_SEED));
        DatabaseConfiguration dbConfig = TestUtils.getDBConfig();
        
        Dataset trainingData = new Dataset(dbConfig);
        trainingData.add(Record.newDataVector(new String[] {"pos","pos"}, "pos"));
        trainingData.add(Record.newDataVector(new String[] {"pos","pos"}, "pos"));
        trainingData.add(Record.newDataVector(new String[] {"pos","pos"}, "pos"));
        trainingData.add(Record.newDataVector(new String[] {"pos","pos"}, "pos"));
        trainingData.add(Record.newDataVector(new String[] {"pos","pos"}, "pos"));

        trainingData.add(Record.newDataVector(new String[] {"pos","neg"}, "pos"));
        trainingData.add(Record.newDataVector(new String[] {"pos","neg"}, "pos"));
        trainingData.add(Record.newDataVector(new String[] {"pos","neg"}, "pos"));
        trainingData.add(Record.newDataVector(new String[] {"neg","pos"}, "pos"));
        trainingData.add(Record.newDataVector(new String[] {"neg","pos"}, "neg"));
        trainingData.add(Record.newDataVector(new String[] {"neg","pos"}, "neg"));
        trainingData.add(Record.newDataVector(new String[] {"pos","neg"}, "neg"));
        trainingData.add(Record.newDataVector(new String[] {"pos","neg"}, "neg"));
        trainingData.add(Record.newDataVector(new String[] {"neg","neg"}, "neg"));
        trainingData.add(Record.newDataVector(new String[] {"neg","neg"}, "neg"));
        trainingData.add(Record.newDataVector(new String[] {"neg","neg"}, "neg"));
        trainingData.add(Record.newDataVector(new String[] {"neg","neg"}, "neg"));
        
        
        Dataset validationData = new Dataset(dbConfig);
        validationData.add(Record.newDataVector(new String[] {"pos","pos"}, "pos"));
        validationData.add(Record.newDataVector(new String[] {"pos","neg"}, "pos"));
        validationData.add(Record.newDataVector(new String[] {"neg","pos"}, "neg"));
        validationData.add(Record.newDataVector(new String[] {"neg","neg"}, "neg"));
        
        
        
        String dbName = "JUnitBayesianEnsembleMethod";
        
        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName, dbConfig);
        df.fit_transform(trainingData, new DummyXYMinMaxNormalizer.TrainingParameters());
        
        df.transform(validationData);
        
        
        BayesianEnsembleMethod instance = new BayesianEnsembleMethod(dbName, dbConfig);
        
        BayesianEnsembleMethod.TrainingParameters param = new BayesianEnsembleMethod.TrainingParameters();
        
        instance.fit(trainingData, param);
        
        
        instance = null;
        instance = new BayesianEnsembleMethod(dbName, dbConfig);
        
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

}
