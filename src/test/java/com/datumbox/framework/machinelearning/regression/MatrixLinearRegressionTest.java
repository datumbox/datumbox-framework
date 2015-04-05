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
package com.datumbox.framework.machinelearning.regression;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.utilities.RandomValue;
import com.datumbox.common.utilities.TypeConversions;
import com.datumbox.framework.machinelearning.datatransformation.DummyXYMinMaxNormalizer;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.framework.machinelearning.datatransformation.XYMinMaxNormalizer;
import com.datumbox.tests.utilities.TestUtils;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class MatrixLinearRegressionTest {
    
    public MatrixLinearRegressionTest() {
    }


    /**
     * Test of predict method, of class MatrixLinearRegression.
     */
    @Test
    public void testValidate() {
        TestUtils.log(this.getClass(), "validate");
        
        /*
        Synthetic Data generated with:
        
        $x1=rand(40,50);
        $x2=rand(1,60)/10;

        $y=2+0.002*$x1+30*$x2;
        $dataTable[]=array(array($x1,$x2),null);
        */
        Dataset trainingData = new Dataset();
        trainingData.add(Record.newDataVector(new Object[] {(Integer)49,(Double)4.5}, (Double)137.098));
        trainingData.add(Record.newDataVector(new Object[] {(Integer)46,(Double)2.9}, (Double)89.092));
        trainingData.add(Record.newDataVector(new Object[] {(Integer)46,(Double)1.9}, (Double)59.092));
        trainingData.add(Record.newDataVector(new Object[] {(Integer)40,(Double)1.7}, (Double)53.08));
        trainingData.add(Record.newDataVector(new Object[] {(Integer)45,(Double)2.1}, (Double)65.09));
        trainingData.add(Record.newDataVector(new Object[] {(Integer)41,(Double)3.8}, (Double)116.082));
        trainingData.add(Record.newDataVector(new Object[] {(Integer)47,(Double)5.0}, (Double)152.094));
        trainingData.add(Record.newDataVector(new Object[] {(Integer)41,(Double)2.0}, (Double)62.082));
        trainingData.add(Record.newDataVector(new Object[] {(Integer)40,(Double)0.9}, (Double)29.08));
        trainingData.add(Record.newDataVector(new Object[] {(Integer)46,(Double)1.2}, (Double)38.092));
        
        Dataset validationData = new Dataset();
        validationData.add(Record.newDataVector(new Object[] {(Integer)49,(Double)4.5}, (Double)137.098));
        validationData.add(Record.newDataVector(new Object[] {(Integer)46,(Double)2.9}, (Double)89.092));
        validationData.add(Record.newDataVector(new Object[] {(Integer)46,(Double)1.9}, (Double)59.092));
        validationData.add(Record.newDataVector(new Object[] {(Integer)40,(Double)1.7}, (Double)53.08));
        validationData.add(Record.newDataVector(new Object[] {(Integer)45,(Double)2.1}, (Double)65.09));
        validationData.add(Record.newDataVector(new Object[] {(Integer)41,(Double)3.8}, (Double)116.082));
        validationData.add(Record.newDataVector(new Object[] {(Integer)47,(Double)5.0}, (Double)152.094));
        validationData.add(Record.newDataVector(new Object[] {(Integer)41,(Double)2.0}, (Double)62.082));
        validationData.add(Record.newDataVector(new Object[] {(Integer)40,(Double)0.9}, (Double)29.08));
        validationData.add(Record.newDataVector(new Object[] {(Integer)46,(Double)1.2}, (Double)38.092));
        
        
        
        
        //the analysis is VERY slow if not performed in memory training, so we force it anyway.
        //memoryConfiguration.setMapType(InMemoryStructureFactory.MapType.HASH_MAP);
        
        String dbName = "JUnitRegressor";

        XYMinMaxNormalizer df = new XYMinMaxNormalizer(dbName, TestUtils.getDBConfig());
        df.fit_transform(trainingData, new XYMinMaxNormalizer.TrainingParameters());
        
        df.transform(validationData);
        
        df = null;

        MatrixLinearRegression instance = new MatrixLinearRegression(dbName, TestUtils.getDBConfig());
        
        MatrixLinearRegression.TrainingParameters param = new MatrixLinearRegression.TrainingParameters();
        
        instance.fit(trainingData, param);
        
        
        instance = null;
        instance = new MatrixLinearRegression(dbName, TestUtils.getDBConfig());
        
        instance.validate(validationData);
        
        df = new XYMinMaxNormalizer(dbName, TestUtils.getDBConfig());
        
	        
        df.denormalize(trainingData);
        df.denormalize(validationData);
        df.erase();


        for(Integer rId : validationData) {
            Record r = validationData.get(rId);
            assertEquals(TypeConversions.toDouble(r.getY()), TypeConversions.toDouble(r.getYPredicted()), TestConfiguration.DOUBLE_ACCURACY_HIGH);
        }
        
        instance.erase();
    }


    /**
     * Test of kFoldCrossValidation method, of class MatrixLinearRegression.
     */
    @Test
    public void testKFoldCrossValidation() {
        TestUtils.log(this.getClass(), "kFoldCrossValidation");
        RandomValue.randomGenerator = new Random(42);
        int k = 5;
        
        Dataset trainingData = new Dataset();
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
        
        
        //the analysis is VERY slow if not performed in memory training, so we force it anyway.
        //memoryConfiguration.setMapType(InMemoryStructureFactory.MapType.HASH_MAP);
        
        
        String dbName = "JUnitRegressor";

        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName, TestUtils.getDBConfig());
        df.fit_transform(trainingData, new DummyXYMinMaxNormalizer.TrainingParameters());
        
        
        MatrixLinearRegression instance = new MatrixLinearRegression(dbName, TestUtils.getDBConfig());
        
        MatrixLinearRegression.TrainingParameters param = new MatrixLinearRegression.TrainingParameters();
        
        MatrixLinearRegression.ValidationMetrics vm = instance.kFoldCrossValidation(trainingData, param, k);
        
        df.denormalize(trainingData);
        df.erase();

        double expResult = 1;
        double result = vm.getRSquare();
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
        instance.erase();
    }


}
