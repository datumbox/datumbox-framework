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
package com.datumbox.framework.machinelearning.featureselection.categorical;

import com.datumbox.common.dataobjects.Dataset;
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
public class MutualInformationTest {
    
    public MutualInformationTest() {
    }

    @Test
    public void testSelectFeatures() {
        System.out.println("selectFeatures");
        RandomValue.randomGenerator = new Random(42);
        
        String dbName = "JUnitMutualInformationFeatureSelection";
        
        MemoryConfiguration memoryConfiguration = new MemoryConfiguration();
        
        MutualInformation.TrainingParameters param = new MutualInformation.TrainingParameters();
        param.setRareFeatureThreshold(2);
        param.setMaxFeatures(5);
        param.setIgnoringNumericalFeatures(false);
        
        Dataset trainingData = ChisquareSelectTest.generateDataset(1000);
        MutualInformation instance = new MutualInformation(dbName);
        instance.initializeTrainingConfiguration(memoryConfiguration, param);
        
        instance.evaluateFeatures(trainingData);
        instance = null;
        
        
        instance = new MutualInformation(dbName);
        instance.setMemoryConfiguration(memoryConfiguration);
        instance.clearFeatures(trainingData);
        
        Set<Object> expResult = new HashSet<>(Arrays.asList("high_paid", "has_boat", "has_luxury_car", "has_butler", "has_pool"));
        Set<Object> result = trainingData.getColumns().keySet();
        assertEquals(expResult, result);
        instance.erase(true);
    }

}
