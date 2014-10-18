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
package com.datumbox.framework.statistics.parametrics.onesample;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class NormalOneSampleTest {
    
    public NormalOneSampleTest() {
    }

    /**
     * Test of testMean method, of class NormalOneSample.
     */
    @Test
    public void testTestMean() {
        System.out.println("testMean");
        double xbar = 215.0;
        int n = 60;
        double H0mean = 200.0;
        double std = 55.0;
        boolean is_twoTailed = false;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = NormalOneSample.testMean(xbar, n, H0mean, std, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }

    /**
     * Test of testSum method, of class NormalOneSample.
     */
    @Test
    public void testTestSum() {
        System.out.println("testSum");
        double xsum = 65.3;
        int n = 100;
        double H0sum = 0.65;
        double std = 0.02;
        boolean is_twoTailed = false;
        double aLevel = 0.05;
        boolean expResult = false;
        boolean result = NormalOneSample.testSum(xsum, n, H0sum, std, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }

    /**
     * Test of testPercentage method, of class NormalOneSample.
     */
    @Test
    public void testTestPercentage() {
        System.out.println("testPercentage");
        double pbar = 0.60;
        int n = 100;
        double H0p = 0.65;
        boolean is_twoTailed = false;
        double aLevel = 0.05;
        boolean expResult = false;
        boolean result = NormalOneSample.testPercentage(pbar, n, H0p, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
