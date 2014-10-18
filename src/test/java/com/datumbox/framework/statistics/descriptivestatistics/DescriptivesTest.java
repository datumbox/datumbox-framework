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
package com.datumbox.framework.statistics.descriptivestatistics;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.dataobjects.TransposeDataList;
import com.datumbox.common.utilities.PHPfunctions;
import com.datumbox.configuration.TestConfiguration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class DescriptivesTest {
    
    public DescriptivesTest() {
    }

    private FlatDataCollection generateFlatDataCollection() {
        Object[] values = { -12.76, 9.07, 3.11, 0.99, -36.40, -34.18, 2.07, 50.85, 5.34, 2.08, 1.49, -19.01, 45.68, -11.80, -1.19, -34.63, -28.10, 35.33, 28.38, 24.60, 10.36, -12.01, 47.92, 3.34, 9.63, 44.09, 4.65, 2.04, 27.39, -14.52, 9.91, 36.45, -24.62, 2.99, -9.49, 2.14, -18.48, 38.69, 43.87, -20.56 };
        
        FlatDataCollection flatDataCollection = new FlatDataCollection(new ArrayList<>(Arrays.asList(values)));
        
        return flatDataCollection;
    }
    
    /**
     * Test of sum method, of class Descriptives.
     */
    @Test
    public void testSum() {
        System.out.println("sum");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        double expResult = 214.71;
        double result = Descriptives.sum(flatDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of mean method, of class Descriptives.
     */
    @Test
    public void testMean() {
        System.out.println("mean");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        double expResult = 5.36775;
        double result = Descriptives.mean(flatDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of meanSE method, of class Descriptives.
     */
    @Test
    public void testMeanSE() {
        System.out.println("meanSE");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        double expResult = 3.8698920757412;
        double result = Descriptives.meanSE(flatDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of median method, of class Descriptives.
     */
    @Test
    public void testMedian() {
        System.out.println("median");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        double expResult = 2.565;
        double result = Descriptives.median(flatDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of min method, of class Descriptives.
     */
    @Test
    public void testMin() {
        System.out.println("min");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        double expResult = -36.4;
        double result = Descriptives.min(flatDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of max method, of class Descriptives.
     */
    @Test
    public void testMax() {
        System.out.println("max");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        double expResult = 50.85;
        double result = Descriptives.max(flatDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of range method, of class Descriptives.
     */
    @Test
    public void testRange() {
        System.out.println("range");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        double expResult = 87.25;
        double result = Descriptives.range(flatDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of geometricMean method, of class Descriptives.
     */
    @Test
    public void testGeometricMean() {
        System.out.println("geometricMean");
        FlatDataCollection flatDataCollection = new FlatDataCollection(Arrays.asList(new Object[]{56.0,75.0,45.0,71.0,61.0,64.0,58.0,80.0,76.0,61.0}));
        double expResult = 63.85415130126;
        double result = Descriptives.geometricMean(flatDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of harmonicMean method, of class Descriptives.
     */
    @Test
    public void testHarmonicMean() {
        System.out.println("harmonicMean");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        double expResult = 9.7666088743776;
        double result = Descriptives.harmonicMean(flatDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of variance method, of class Descriptives.
     */
    @Test
    public void testVariance() {
        System.out.println("variance");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        boolean isSample = true;
        double expResult = 599.04258711538;
        double result = Descriptives.variance(flatDataCollection, isSample);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of std method, of class Descriptives.
     */
    @Test
    public void testStd() {
        System.out.println("std");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        boolean isSample = true;
        double expResult = 24.475346516758;
        double result = Descriptives.std(flatDataCollection, isSample);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of cv method, of class Descriptives.
     */
    @Test
    public void testCv() {
        System.out.println("cv");
        double std = 24.475346516758;
        double mean = 5.36775;
        double expResult = 4.5597031375824;
        double result = Descriptives.cv(std, mean);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of moment method, of class Descriptives.
     */
    @Test
    public void testMoment_Collection_int() {
        System.out.println("moment");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        int r = 3;
        double expResult = 3484.6106601128;
        double result = Descriptives.moment(flatDataCollection, r);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of moment method, of class Descriptives.
     */
    @Test
    public void testMoment_3args() {
        System.out.println("moment");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        int r = 3;
        double mean = 5.36775;
        double expResult = 3484.6106601128;
        double result = Descriptives.moment(flatDataCollection, r, mean);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of kurtosis method, of class Descriptives.
     */
    @Test
    public void testKurtosis() {
        System.out.println("kurtosis");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        double expResult = -0.74454696650836;
        double result = Descriptives.kurtosis(flatDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of kurtosisSE method, of class Descriptives.
     */
    @Test
    public void testKurtosisSE() {
        System.out.println("kurtosisSE");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        double expResult = 0.77459666924148;
        double result = Descriptives.kurtosisSE(flatDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of skewness method, of class Descriptives.
     */
    @Test
    public void testSkewness() {
        System.out.println("skewness");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        double expResult = 0.24686572127408;
        double result = Descriptives.skewness(flatDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of skewnessSE method, of class Descriptives.
     */
    @Test
    public void testSkewnessSE() {
        System.out.println("skewnessSE");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        double expResult = 0.37378336538587;
        double result = Descriptives.skewnessSE(flatDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of percentiles method, of class Descriptives.
     */
    @Test
    public void testPercentiles() {
        System.out.println("percentiles");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        int cutPoints = 4;
        AssociativeArray expResult = new AssociativeArray();
        expResult.put(25.0, -12.5725);
        expResult.put(50.0, 2.565);
        expResult.put(75.0, 26.6925);
        AssociativeArray result = Descriptives.percentiles(flatDataCollection, cutPoints);
        for (Object key : result.keySet()) {
            double rounded = PHPfunctions.round(result.getDouble(key), 5);
            result.put(key, rounded);
        }
        assertEquals(expResult, result);
    }

    /**
     * Test of quartiles method, of class Descriptives.
     */
    @Test
    public void testQuartiles() {
        System.out.println("quartiles");
        FlatDataCollection flatDataCollection = new FlatDataCollection(Arrays.asList(new Object[]{0,1,2,3}));
        AssociativeArray expResult = new AssociativeArray();
        expResult.put(25.0, 0.25);
        expResult.put(50.0, 1.5);
        expResult.put(75.0, 2.75);
        AssociativeArray result = Descriptives.quartiles(flatDataCollection);
        assertEquals(expResult, result);
    }

    /**
     * Test of covariance method, of class Descriptives.
     */
    @Test
    public void testCovariance() {
        System.out.println("covariance");
        
        TransposeDataList transposeDataList = new TransposeDataList(new LinkedHashMap<>());
        
        transposeDataList.put(0, new FlatDataList(Arrays.asList(new Object[]{56,75,45,71,61,64,58,80,76,61})));
        transposeDataList.put(1, new FlatDataList(Arrays.asList(new Object[]{66,70,40,60,65,56,59,77,67,63})));
        
        boolean isSample = false;
        double expResult = 76.39;
        double result = Descriptives.covariance(transposeDataList, isSample);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of autocorrelation method, of class Descriptives.
     */
    @Test
    public void testAutocorrelation() {
        System.out.println("autocorrelation");
        FlatDataList flatDataList = generateFlatDataCollection().toFlatDataList();
        int lags = 1;
        double expResult = -0.014242212135952;
        double result = Descriptives.autocorrelation(flatDataList, lags);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of frequencies method, of class Descriptives.
     */
    @Test
    public void testFrequencies() {
        System.out.println("frequencies");
        FlatDataCollection flatDataCollection = new FlatDataCollection(Arrays.asList(new Object[]{1.0,1.0,1.0,2.0,3.0,4.3,2.0}));
        AssociativeArray expResult = new AssociativeArray();
        expResult.put(1.0, 3);
        expResult.put(2.0, 2);
        expResult.put(3.0, 1);
        expResult.put(4.3, 1);
        AssociativeArray result = Descriptives.frequencies(flatDataCollection);
        assertEquals(expResult, result);
    }

    /**
     * Test of mode method, of class Descriptives.
     */
    @Test
    public void testMode() {
        System.out.println("mode");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        flatDataCollection.add(4.65);
        flatDataCollection.add(4.65);
        FlatDataCollection expResult = new FlatDataCollection(new ArrayList<>());
        expResult.add(4.65);
        FlatDataCollection result = Descriptives.mode(flatDataCollection);
        assertEquals(expResult, result);
    }
    
}
