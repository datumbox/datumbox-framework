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
package com.datumbox.framework.statistics.sampling;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.configuration.TestConfiguration;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
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
     * Test of weightedProbabilitySampling method, of class SRS.
     */
    @Test
    public void testWeightedProbabilitySampling() {
        System.out.println("weightedProbabilitySampling");
        AssociativeArray frequencyTable = new AssociativeArray();
        frequencyTable.put(1, 0.20);
        frequencyTable.put(2, 0.30);
        frequencyTable.put(3, 0.25);
        frequencyTable.put(4, 0.25);
        
        int n = 100;
        boolean withReplacement = true;
        double expResult = n;
        FlatDataCollection sampledIds = SRS.weightedProbabilitySampling(frequencyTable, n, withReplacement);
        double result = sampledIds.size();
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of weightedSampling method, of class SRS.
     */
    @Test
    public void testWeightedSampling() {
        System.out.println("weightedSampling");
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
        System.out.println("randomSampling");
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
        System.out.println("mean");
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
        System.out.println("variance");
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
        System.out.println("std");
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
        System.out.println("xbarVariance");
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
        System.out.println("xbarStd");
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
        System.out.println("pbarVariance");
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
        System.out.println("pbarStd");
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
        System.out.println("minimumSampleSizeForMaximumXbarStd");
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
        System.out.println("minimumSampleSizeForGivenDandMaximumRisk");
        double d = 0.323;
        double aLevel = 0.1;
        double populationStd = 1.7289303051309;
        int populationN = 7000;
        int expResult = 77;
        int result = SRS.minimumSampleSizeForGivenDandMaximumRisk(d, aLevel, populationStd, populationN);
        assertEquals(expResult, result);
    }
    
}
