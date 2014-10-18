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
package com.datumbox.framework.statistics.timeseries;

import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.configuration.TestConfiguration;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class SmoothingTest {
    
    public SmoothingTest() {
    }
    
    private FlatDataList generateFlatDataList() {
        //Example from Tzortzopoulos' notes
        Object[] values = { 20,13,19,19,31,18,16,13,22,28,24,20,30,15,24.0 };
        
        FlatDataList flatDataList = new FlatDataList(new ArrayList<>(Arrays.asList(values)));
        
        return flatDataList;
    }

    /**
     * Test of simpleMovingAverage method, of class Smoothing.
     */
    @Test
    public void testSimpleMovingAverage() {
        System.out.println("simpleMovingAverage");
        FlatDataList flatDataList = generateFlatDataList();
        int N = 3;
        double expResult = 23.0;
        double result = Smoothing.simpleMovingAverage(flatDataList, N);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of simpleMovingAverageQuick method, of class Smoothing.
     */
    @Test
    public void testSimpleMovingAverageQuick() {
        System.out.println("simpleMovingAverageQuick");
        double Yt = 23.5;
        double YtminusN = 20.0;
        double Ft = 23.0;
        int N = 3;
        double expResult = 24.166666666667;
        double result = Smoothing.simpleMovingAverageQuick(Yt, YtminusN, Ft, N);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of weightedMovingAverage method, of class Smoothing.
     */
    @Test
    public void testWeightedMovingAverage() {
        System.out.println("weightedMovingAverage");
        FlatDataList flatDataList = generateFlatDataList();
        int N = 3;
        double expResult =22.0;
        double result = Smoothing.weightedMovingAverage(flatDataList, N);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of simpleExponentialSmoothing method, of class Smoothing.
     */
    @Test
    public void testSimpleExponentialSmoothing() {
        System.out.println("simpleExponentialSmoothing");
        FlatDataList flatDataList = generateFlatDataList();
        double a = 0.9;
        double expResult = 23.240433133179;
        double result = Smoothing.simpleExponentialSmoothing(flatDataList, a);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of simpleExponentialSmoothingQuick method, of class Smoothing.
     */
    @Test
    public void testSimpleExponentialSmoothingQuick() {
        System.out.println("simpleExponentialSmoothingQuick");
        double Ytminus1 = 23.5;
        double Stminus1 = 23.240433133179;
        double a = 0.9;
        double expResult = 23.474043313318;
        double result = Smoothing.simpleExponentialSmoothingQuick(Ytminus1, Stminus1, a);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of holtWintersSmoothing method, of class Smoothing.
     */
    @Test
    public void testHoltWintersSmoothing() {
        System.out.println("holtWintersSmoothing");
        FlatDataList flatDataList = generateFlatDataList();
        int season_length = 3;
        double alpha = 0.2;
        double beta = 0.01;
        double gamma = 0.01;
        double dev_gamma = 0.1;
        double expResult = 30.636044533784;
        double result = Smoothing.holtWintersSmoothing(flatDataList, season_length, alpha, beta, gamma, dev_gamma);
        assertEquals(expResult, result, 0.01);
    }
    
}
