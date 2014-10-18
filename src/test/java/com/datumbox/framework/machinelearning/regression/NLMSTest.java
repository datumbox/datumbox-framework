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
package com.datumbox.framework.machinelearning.regression;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.utilities.RandomValue;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.framework.machinelearning.datatransformation.DummyXYMinMaxNormalizer;
import com.datumbox.framework.machinelearning.featureselection.continuous.PCA;
import com.datumbox.configuration.TestConfiguration;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class NLMSTest {
    
    public NLMSTest() {
    }


    /**
     * Test of predict method, of class NLMS.
     */
    @Test
    public void testPredict() {
        System.out.println("predict");
        
        /*
        Synthetic Data generated with:
        
        $x1=rand(1,3);
        $x2=rand(40,50);
        $x3=rand(1,60)/10;
        $x4=rand(0,4);

        $y=2+10*$x1+0.002*$x2+30*$x3+10*$x4;
        $dataTable[]=array(array((string)$x1,$x2,$x3,(string)$x4),null);
        */
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
        
        Dataset validationData = new Dataset();
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
        
        
        
        MemoryConfiguration memoryConfiguration = new MemoryConfiguration();
        //the analysis is VERY slow if not performed in memory training, so we force it anyway.
        //memoryConfiguration.setMapType(InMemoryStructureFactory.MapType.HASH_MAP);
        
        String dbName = "JUnitRegressor";

        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName);
        df.initializeTrainingConfiguration(memoryConfiguration, df.getEmptyTrainingParametersObject());
        df.transform(trainingData, true);
        df.normalize(trainingData);
        df.transform(validationData, false);
        df.normalize(validationData);

        NLMS instance = new NLMS(dbName);
        
        NLMS.TrainingParameters param = instance.getEmptyTrainingParametersObject();
        param.setTotalIterations(1600);
        
        instance.initializeTrainingConfiguration(memoryConfiguration, param);
        instance.train(trainingData, validationData);
        
        
        instance = null;
        instance = new NLMS(dbName);
        instance.setMemoryConfiguration(memoryConfiguration);
        instance.predict(validationData);
        
        df.denormalize(trainingData);
        df.denormalize(validationData);
        df.erase(true);


        for(Record r : validationData) {
            assertEquals(Dataset.toDouble(r.getY()), Dataset.toDouble(r.getYPredicted()), TestConfiguration.DOUBLE_ACCURACY_LOW);
        }
        
        instance.erase(true);
    }


    /**
     * Test of kFoldCrossValidation method, of class NLMS.
     */
    @Test
    public void testKFoldCrossValidation() {
        System.out.println("kFoldCrossValidation");
        RandomValue.randomGenerator = new Random(42);
        int k = 5;
        
        Dataset trainingData = new Dataset();
        trainingData.add(Record.newDataVector(new Object[] {(String)"3",(Integer)49,(Integer)49,(Double)4.5,(String)"0"}, (Double)167.098));
        trainingData.add(Record.newDataVector(new Object[] {(String)"1",(Integer)46,(Integer)46,(Double)2.9,(String)"0"}, (Double)99.092));
        trainingData.add(Record.newDataVector(new Object[] {(String)"1",(Integer)46,(Integer)46,(Double)1.9,(String)"2"}, (Double)89.092));
        trainingData.add(Record.newDataVector(new Object[] {(String)"2",(Integer)40,(Integer)40,(Double)1.7,(String)"3"}, (Double)103.08));
        trainingData.add(Record.newDataVector(new Object[] {(String)"3",(Integer)45,(Integer)45,(Double)2.1,(String)"0"}, (Double)95.09));
        trainingData.add(Record.newDataVector(new Object[] {(String)"1",(Integer)41,(Integer)41,(Double)3.8,(String)"1"}, (Double)136.082));
        trainingData.add(Record.newDataVector(new Object[] {(String)"2",(Integer)47,(Integer)47,(Double)5.0,(String)"3"}, (Double)202.094));
        trainingData.add(Record.newDataVector(new Object[] {(String)"1",(Integer)41,(Integer)41,(Double)2.0,(String)"4"}, (Double)112.082));
        trainingData.add(Record.newDataVector(new Object[] {(String)"3",(Integer)40,(Integer)40,(Double)0.9,(String)"0"}, (Double)59.08));
        trainingData.add(Record.newDataVector(new Object[] {(String)"2",(Integer)46,(Integer)46,(Double)1.2,(String)"4"}, (Double)98.092));
        trainingData.add(Record.newDataVector(new Object[] {(String)"3",(Integer)49,(Integer)49,(Double)4.5,(String)"0"}, (Double)167.098));
        trainingData.add(Record.newDataVector(new Object[] {(String)"1",(Integer)46,(Integer)46,(Double)2.9,(String)"0"}, (Double)99.092));
        trainingData.add(Record.newDataVector(new Object[] {(String)"1",(Integer)46,(Integer)46,(Double)1.9,(String)"2"}, (Double)89.092));
        trainingData.add(Record.newDataVector(new Object[] {(String)"2",(Integer)40,(Integer)40,(Double)1.7,(String)"3"}, (Double)103.08));
        trainingData.add(Record.newDataVector(new Object[] {(String)"3",(Integer)45,(Integer)45,(Double)2.1,(String)"0"}, (Double)95.09));
        trainingData.add(Record.newDataVector(new Object[] {(String)"1",(Integer)41,(Integer)41,(Double)3.8,(String)"1"}, (Double)136.082));
        trainingData.add(Record.newDataVector(new Object[] {(String)"2",(Integer)47,(Integer)47,(Double)5.0,(String)"3"}, (Double)202.094));
        trainingData.add(Record.newDataVector(new Object[] {(String)"1",(Integer)41,(Integer)41,(Double)2.0,(String)"4"}, (Double)112.082));
        trainingData.add(Record.newDataVector(new Object[] {(String)"3",(Integer)40,(Integer)40,(Double)0.9,(String)"0"}, (Double)59.08));
        trainingData.add(Record.newDataVector(new Object[] {(String)"2",(Integer)46,(Integer)46,(Double)1.2,(String)"4"}, (Double)98.092));
        trainingData.add(Record.newDataVector(new Object[] {(String)"3",(Integer)49,(Integer)49,(Double)4.5,(String)"0"}, (Double)167.098));
        trainingData.add(Record.newDataVector(new Object[] {(String)"1",(Integer)46,(Integer)46,(Double)2.9,(String)"0"}, (Double)99.092));
        trainingData.add(Record.newDataVector(new Object[] {(String)"1",(Integer)46,(Integer)46,(Double)1.9,(String)"2"}, (Double)89.092));
        trainingData.add(Record.newDataVector(new Object[] {(String)"2",(Integer)40,(Integer)40,(Double)1.7,(String)"3"}, (Double)103.08));
        trainingData.add(Record.newDataVector(new Object[] {(String)"3",(Integer)45,(Integer)45,(Double)2.1,(String)"0"}, (Double)95.09));
        trainingData.add(Record.newDataVector(new Object[] {(String)"1",(Integer)41,(Integer)41,(Double)3.8,(String)"1"}, (Double)136.082));
        trainingData.add(Record.newDataVector(new Object[] {(String)"2",(Integer)47,(Integer)47,(Double)5.0,(String)"3"}, (Double)202.094));
        trainingData.add(Record.newDataVector(new Object[] {(String)"1",(Integer)41,(Integer)41,(Double)2.0,(String)"4"}, (Double)112.082));
        trainingData.add(Record.newDataVector(new Object[] {(String)"3",(Integer)40,(Integer)40,(Double)0.9,(String)"0"}, (Double)59.08));
        trainingData.add(Record.newDataVector(new Object[] {(String)"2",(Integer)46,(Integer)46,(Double)1.2,(String)"4"}, (Double)98.092));
        
        MemoryConfiguration memoryConfiguration = new MemoryConfiguration();
        //the analysis is VERY slow if not performed in memory training, so we force it anyway.
        //memoryConfiguration.setMapType(InMemoryStructureFactory.MapType.HASH_MAP);
        
        
        String dbName = "JUnitRegressor";

        DummyXYMinMaxNormalizer df = new DummyXYMinMaxNormalizer(dbName);
        df.initializeTrainingConfiguration(memoryConfiguration, df.getEmptyTrainingParametersObject());
        df.transform(trainingData, true);
        df.normalize(trainingData);

        
        
        PCA featureSelection = new PCA(dbName);
        PCA.TrainingParameters featureSelectionParameters = featureSelection.getEmptyTrainingParametersObject();
        featureSelectionParameters.setMaxDimensions(trainingData.getColumnSize()-1);
        featureSelectionParameters.setWhitened(false);
        featureSelectionParameters.setVarianceThreshold(0.99999995);
        featureSelection.initializeTrainingConfiguration(memoryConfiguration, featureSelectionParameters);
        featureSelection.evaluateFeatures(trainingData);
        /*
        featureSelection=null;
        
        
        featureSelection = new PCA(dbName);
        featureSelection.setMemoryConfiguration(memoryConfiguration);
        */
        featureSelection.clearFeatures(trainingData);
        featureSelection.erase(true);
        
        
        NLMS instance = new NLMS(dbName);
        
        NLMS.TrainingParameters param = instance.getEmptyTrainingParametersObject();
        param.setTotalIterations(500);
        instance.initializeTrainingConfiguration(memoryConfiguration, param);
        NLMS.ValidationMetrics vm = instance.kFoldCrossValidation(trainingData, k);

        df.denormalize(trainingData);
        df.erase(true);

        
        double expResult = 1.0;
        double result = vm.getRSquare();
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_MEDIUM);
        instance.erase(true);
    }

    
}
