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
package com.datumbox.framework.machinelearning.regression;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.RandomValue;
import com.datumbox.common.utilities.TypeConversions;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.framework.machinelearning.datatransformation.DummyXYMinMaxNormalizer;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import com.datumbox.tests.utilities.TestUtils;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class StepwiseRegressionTest {
    
    public StepwiseRegressionTest() {
    }

    
    

    /**
     * Test of predict method, of class NLMS.
     */
    @Test
    public void testValidate() {
        TestUtils.log(this.getClass(), "validate");
        RandomValue.setRandomGenerator(new Random(TestConfiguration.RANDOM_SEED));
        DatabaseConfiguration dbConfig = TestUtils.getDBConfig();
        
        /*
        Synthetic Data generated with:
        
        $x1=rand(1,3);
        $x2=rand(40,50);
        $x3=rand(1,60)/10;
        $x4=rand(0,4);

        $y=2+10*$x1+0.002*$x2+30*$x3+10*$x4;
        $dataTable[]=array(array((string)$x1,$x2,$x3,(string)$x4),null);
        */
        Dataset trainingData = new Dataset(dbConfig);
        trainingData.add(Record.newDataVector(new Object[] {(String)"3",(Integer)49,(Double)4.5,(String)"0"}, (Double)167.098));
        trainingData.add(Record.newDataVector(new Object[] {(String)"1",(Integer)46,(Double)2.9,(String)"0"}, (Double)99.092));
        trainingData.add(Record.newDataVector(new Object[] {(String)"1",(Integer)46,(Double)1.9,(String)"2"}, (Double)89.092));
        trainingData.add(Record.newDataVector(new Object[] {(String)"2",(Integer)40,(Double)1.7,(String)"3"}, (Double)103.08));
        trainingData.add(Record.newDataVector(new Object[] {(String)"3",(Integer)45,(Double)2.1,(String)"0"}, (Double)95.09));
        trainingData.add(Record.newDataVector(new Object[] {(String)"1",(Integer)41,(Double)3.8,(String)"1"}, (Double)136.082));
        trainingData.add(Record.newDataVector(new Object[] {(String)"2",(Integer)47,(Double)5.0,(String)"3"}, (Double)202.094));
        trainingData.add(Record.newDataVector(new Object[] {(String)"1",(Integer)41,(Double)2.0,(String)"4"}, (Double)112.082));
        trainingData.add(Record.newDataVector(new Object[] {(String)"3",(Integer)40,(Double)0.9,(String)"0"}, (Double)59.08));
        trainingData.add(Record.newDataVector(new Object[] {(String)"2",(Integer)46,(Double)1.2,(String)"4"}, (Double)98.092));
        
        Dataset validationData = new Dataset(dbConfig);
        validationData.add(Record.newDataVector(new Object[] {(String)"3",(Integer)49,(Double)4.5,(String)"0"}, (Double)167.098));
        validationData.add(Record.newDataVector(new Object[] {(String)"1",(Integer)46,(Double)2.9,(String)"0"}, (Double)99.092));
        validationData.add(Record.newDataVector(new Object[] {(String)"1",(Integer)46,(Double)1.9,(String)"2"}, (Double)89.092));
        validationData.add(Record.newDataVector(new Object[] {(String)"2",(Integer)40,(Double)1.7,(String)"3"}, (Double)103.08));
        validationData.add(Record.newDataVector(new Object[] {(String)"3",(Integer)45,(Double)2.1,(String)"0"}, (Double)95.09));
        validationData.add(Record.newDataVector(new Object[] {(String)"1",(Integer)41,(Double)3.8,(String)"1"}, (Double)136.082));
        validationData.add(Record.newDataVector(new Object[] {(String)"2",(Integer)47,(Double)5.0,(String)"3"}, (Double)202.094));
        validationData.add(Record.newDataVector(new Object[] {(String)"1",(Integer)41,(Double)2.0,(String)"4"}, (Double)112.082));
        validationData.add(Record.newDataVector(new Object[] {(String)"3",(Integer)40,(Double)0.9,(String)"0"}, (Double)59.08));
        validationData.add(Record.newDataVector(new Object[] {(String)"2",(Integer)46,(Double)1.2,(String)"4"}, (Double)98.092));
        
        
        
        
        //the analysis is VERY slow if not performed in memory training, so we force it anyway.
        //memoryConfiguration.setMapType(InMemoryStructureFactory.MapType.HASH_MAP);
        
        String dbName = "JUnitRegressor";

        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName, dbConfig);
        df.fit_transform(trainingData, new DummyXYMinMaxNormalizer.TrainingParameters());
        df.transform(validationData);
        
        StepwiseRegression instance = new StepwiseRegression(dbName, dbConfig);
        
        StepwiseRegression.TrainingParameters param = new StepwiseRegression.TrainingParameters();
        param.setAout(0.05);
        param.setRegressionClass(MatrixLinearRegression.class);
        
        MatrixLinearRegression.TrainingParameters trainingParams = new MatrixLinearRegression.TrainingParameters();
        param.setRegressionTrainingParameters(trainingParams);
                
        
        instance.fit(trainingData, param);
        
        
        instance = null;
        instance = new StepwiseRegression(dbName, dbConfig);
        
        instance.validate(validationData);
        
	        
        df.denormalize(trainingData);
        df.denormalize(validationData);
        df.erase();
        
        double std = Descriptives.std(trainingData.extractYValues().toFlatDataCollection(), true);
        for(Integer rId : validationData) {
            Record r = validationData.get(rId);
            assertEquals(TypeConversions.toDouble(r.getY()), TypeConversions.toDouble(r.getYPredicted()), std);
        }
        
        instance.erase();
    }


}
