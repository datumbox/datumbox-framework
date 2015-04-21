/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.statistics.sampling;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.AssociativeArray2D;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.dataobjects.TransposeDataList;
import com.datumbox.common.dataobjects.TransposeDataCollection;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import com.datumbox.tests.bases.BaseTest;

import com.datumbox.tests.utilities.TestUtils;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class StratifiedSamplingTest extends BaseTest {
    
    protected AssociativeArray generateNh() {
        AssociativeArray nh = new AssociativeArray();
        nh.put("strata1", 3);
        nh.put("strata2", 4);
        
        return nh;
    }
    
    protected AssociativeArray generateNh2() {
        AssociativeArray nh = new AssociativeArray();
        nh.put(1, 4);
        nh.put(2, 3);
        nh.put(3, 3);
        
        return nh;
    }
    
    protected AssociativeArray generatePopulationNh() {
        AssociativeArray populationNh = new AssociativeArray();
        populationNh.put(1, 14);
        populationNh.put(2, 8);
        populationNh.put(3, 8);
        
        return populationNh;
    }

    
    protected TransposeDataCollection generateSampleDataCollection() {
        TransposeDataCollection sampleDataCollection = new TransposeDataCollection();
        sampleDataCollection.put(1, new FlatDataCollection(Arrays.asList(new Object[]{2,3,6,5}))); //,6,8,6,7,8,6,7,7,9,8
        sampleDataCollection.put(2, new FlatDataCollection(Arrays.asList(new Object[]{10,9,12}))); //,8,14,7,12,9
        sampleDataCollection.put(3, new FlatDataCollection(Arrays.asList(new Object[]{8,6,7}))); //,4,5,6,4,3
        
        return sampleDataCollection;
    }
    
    /**
     * Test of weightedProbabilitySampling method, of class StratifiedSampling.
     */
    @Test
    public void testWeightedProbabilitySampling() {
        logger.info("weightedProbabilitySampling");
        AssociativeArray2D strataFrequencyTable = new AssociativeArray2D();
        strataFrequencyTable.put2d("strata1", "1", 10);
        strataFrequencyTable.put2d("strata1", "2", 20);
        strataFrequencyTable.put2d("strata1", "3", 30);
        strataFrequencyTable.put2d("strata1", "4", 40);
        strataFrequencyTable.put2d("strata2", "1", 100);
        strataFrequencyTable.put2d("strata2", "2", 200);
        strataFrequencyTable.put2d("strata2", "3", 300);
        strataFrequencyTable.put2d("strata2", "4", 400);
        strataFrequencyTable.put2d("strata2", "5", 500);
        strataFrequencyTable.put2d("strata2", "6", 600);
        strataFrequencyTable.put2d("strata2", "7", 800);
        
        AssociativeArray nh = generateNh();
        
        boolean withReplacement = true;
        double expResult = Descriptives.sum(nh.toFlatDataCollection());
        TransposeDataCollection sampledIds = StratifiedSampling.weightedProbabilitySampling(strataFrequencyTable, nh, withReplacement);
        double result = 0;
        for(Object stata : sampledIds.keySet()) {
            result+= sampledIds.get(stata).size();
        }
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
        
    }

    /**
     * Test of randomSampling method, of class StratifiedSampling.
     */
    @Test
    public void testRandomSampling() {
        logger.info("randomSampling");
        
        TransposeDataList strataIdList = new TransposeDataList();
        strataIdList.put("strata1", new FlatDataList(Arrays.asList(new Object[]{"1","2","3","4"})));
        strataIdList.put("strata2", new FlatDataList(Arrays.asList(new Object[]{"1","2","3","4","5","6","7"})));
        
        AssociativeArray nh = generateNh();
        
        boolean withReplacement = true;
        double expResult = Descriptives.sum(nh.toFlatDataCollection());
        TransposeDataCollection sampledIds = StratifiedSampling.randomSampling(strataIdList, nh, withReplacement);
        double result = 0;
        for(Object stata : sampledIds.keySet()) {
            result+= sampledIds.get(stata).size();
        }
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of mean method, of class StratifiedSampling.
     */
    @Test
    public void testMean() {
        logger.info("mean");
        TransposeDataCollection sampleDataCollection = generateSampleDataCollection();
        AssociativeArray populationNh = generatePopulationNh();
        double expResult = 6.4888888888889;
        double result = StratifiedSampling.mean(sampleDataCollection, populationNh);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of variance method, of class StratifiedSampling.
     */
    @Test
    public void testVariance() {
        logger.info("variance");
        TransposeDataCollection sampleDataCollection = generateSampleDataCollection();
        AssociativeArray populationNh = generatePopulationNh();
        double expResult = 9.43856960409;
        double result = StratifiedSampling.variance(sampleDataCollection, populationNh);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of std method, of class StratifiedSampling.
     */
    @Test
    public void testStd() {
        logger.info("std");
        TransposeDataCollection sampleDataCollection = generateSampleDataCollection();
        AssociativeArray populationNh = generatePopulationNh();
        double expResult = 3.0722255132211;
        double result = StratifiedSampling.std(sampleDataCollection, populationNh);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of xbarVariance method, of class StratifiedSampling.
     */
    @Test
    public void testXbarVariance() {
        logger.info("xbarVariance");
        TransposeDataCollection sampleDataCollection = generateSampleDataCollection();
        AssociativeArray nh = generateNh2();
        AssociativeArray populationNh = generatePopulationNh();
        double expResult = 0.17901234567;
        double result = StratifiedSampling.xbarVariance(sampleDataCollection, nh, populationNh);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of xbarStd method, of class StratifiedSampling.
     */
    @Test
    public void testXbarStd() {
        logger.info("xbarStd");
        TransposeDataCollection sampleDataCollection = generateSampleDataCollection();
        AssociativeArray nh = generateNh2();
        AssociativeArray populationNh = generatePopulationNh();
        double expResult = 0.42309850588133;
        double result = StratifiedSampling.xbarStd(sampleDataCollection, nh, populationNh);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of optimumSampleSize method, of class StratifiedSampling.
     */
    @Test
    public void testOptimumSampleSize() {
        logger.info("optimumSampleSize");
        int n = 0;
        AssociativeArray populationNh = new AssociativeArray();
        populationNh.put(1,394);
        populationNh.put(2,461);
        populationNh.put(3,391);
        populationNh.put(4,334);
        populationNh.put(5,169);
        populationNh.put(6,113);
        populationNh.put(7,148);

        AssociativeArray populationStdh = new AssociativeArray();
        populationStdh.put(1,8.3);
        populationStdh.put(2,13.3);
        populationStdh.put(3,15.1);
        populationStdh.put(4,19.8);
        populationStdh.put(5,24.5);
        populationStdh.put(6,26.0);
        populationStdh.put(7,35.0);
        
        double expResult = n;
        AssociativeArray sampleSizes = StratifiedSampling.optimumSampleSize(n, populationNh, populationStdh);
        double result = Descriptives.sum(sampleSizes.toFlatDataCollection());
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }
    
}
