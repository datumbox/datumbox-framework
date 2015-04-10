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
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.configuration.TestConfiguration;

import com.datumbox.tests.utilities.TestUtils;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class SRSTest {
    
    public SRSTest() {
    }

    protected FlatDataCollection generateFlatDataCollection() {
        //Example from Papageorgious' notes
        FlatDataCollection flatDataCollection = new FlatDataCollection(Arrays.asList(new Object[]{9.44,24.25,20.49,14.40,14.20,19.51,6.53,5.03,25.46,7.05,11.40,19.33,7.08,9.58,25.18}));
        return flatDataCollection;
    }
    
    /**
     * Test of weightedSampling method, of class SRS.
     */
    @Test
    public void testWeightedProbabilitySampling() {
        TestUtils.log(this.getClass(), "weightedProbabilitySampling");
        AssociativeArray frequencyTable = new AssociativeArray();
        frequencyTable.put(1, 0.20);
        frequencyTable.put(2, 0.30);
        frequencyTable.put(3, 0.25);
        frequencyTable.put(4, 0.25);
        
        int n = 100;
        boolean withReplacement = true;
        double expResult = n;
        FlatDataCollection sampledIds = SRS.weightedSampling(frequencyTable, n, withReplacement);
        double result = sampledIds.size();
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of weightedSampling method, of class SRS.
     */
    @Test
    public void testWeightedSampling() {
        TestUtils.log(this.getClass(), "weightedSampling");
        AssociativeArray frequencyTable = new AssociativeArray();
        frequencyTable.put(1, 10);
        frequencyTable.put(2, 20);
        frequencyTable.put(3, 30);
        frequencyTable.put(4, 40);

        int n = 100;
        boolean withReplacement = true;
        double expResult = n;
        FlatDataCollection sampledIds = SRS.weightedSampling(frequencyTable, n, withReplacement);
        double result = sampledIds.size();
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of randomSampling method, of class SRS.
     */
    @Test
    public void testRandomSampling() {
        TestUtils.log(this.getClass(), "randomSampling");
        FlatDataList idList = new FlatDataList();
        idList.add("a");
        idList.add("0");
        idList.add("c");
        idList.add("1");
        idList.add("5");
        
        int n = 100;
        boolean withReplacement = true;
        double expResult = n;
        FlatDataCollection sampledIds = SRS.randomSampling(idList, n, withReplacement);
        double result = sampledIds.size();
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }
    
    /**
     * Test of mean method, of class SRS.
     */
    @Test
    public void testMean() {
        TestUtils.log(this.getClass(), "mean");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        double expResult = 14.595333333333;
        double result = SRS.mean(flatDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of variance method, of class SRS.
     */
    @Test
    public void testVariance() {
        TestUtils.log(this.getClass(), "variance");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        double expResult = 52.621426666667;
        double result = SRS.variance(flatDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of std method, of class SRS.
     */
    @Test
    public void testStd() {
        TestUtils.log(this.getClass(), "std");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        double expResult = 7.2540627696944;
        double result = SRS.std(flatDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of xbarVariance method, of class SRS.
     */
    @Test
    public void testXbarVariance_3args() {
        TestUtils.log(this.getClass(), "xbarVariance");
        double variance = 52.621426666667;
        int sampleN = 15;
        int populationN = 2147483647;
        double expResult = 3.50809508661;
        double result = SRS.xbarVariance(variance, sampleN, populationN);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of xbarStd method, of class SRS.
     */
    @Test
    public void testXbarStd_3args() {
        TestUtils.log(this.getClass(), "xbarStd");
        double std = 7.2540627696944;
        int sampleN = 15;
        int populationN = 2147483647;
        double expResult = 1.87299094675;
        double result = SRS.xbarStd(std, sampleN, populationN);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of pbarVariance method, of class SRS.
     */
    @Test
    public void testPbarVariance_3args() {
        TestUtils.log(this.getClass(), "pbarVariance");
        double pbar = 0.19;
        int sampleN = 200;
        int populationN = 3042;
        double expResult = 0.00072252088;
        double result = SRS.pbarVariance(pbar, sampleN, populationN);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of pbarStd method, of class SRS.
     */
    @Test
    public void testPbarStd_3args() {
        TestUtils.log(this.getClass(), "pbarStd");
        double pbar = 0.19;
        int sampleN = 200;
        int populationN = 3042;
        double expResult = 0.026879748668207;
        double result = SRS.pbarStd(pbar, sampleN, populationN);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of minimumSampleSizeForMaximumXbarStd method, of class SRS.
     */
    @Test
    public void testMinimumSampleSizeForMaximumXbarStd_3args() {
        TestUtils.log(this.getClass(), "minimumSampleSizeForMaximumXbarStd");
        double maximumXbarStd = 1.2;
        double populationStd = 7.25;
        int populationN = 2147483647;
        int expResult = 37;
        int result = SRS.minimumSampleSizeForMaximumXbarStd(maximumXbarStd, populationStd, populationN);
        assertEquals(expResult, result);
    }

    /**
     * Test of minimumSampleSizeForGivenDandMaximumRisk method, of class SRS.
     */
    @Test
    public void testMinimumSampleSizeForGivenDandMaximumRisk_4args() {
        TestUtils.log(this.getClass(), "minimumSampleSizeForGivenDandMaximumRisk");
        double d = 0.323;
        double aLevel = 0.1;
        double populationStd = 1.7289303051309;
        int populationN = 7000;
        int expResult = 77;
        int result = SRS.minimumSampleSizeForGivenDandMaximumRisk(d, aLevel, populationStd, populationN);
        assertEquals(expResult, result);
    }
    
}
