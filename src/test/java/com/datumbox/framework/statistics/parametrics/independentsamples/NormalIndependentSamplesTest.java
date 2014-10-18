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
package com.datumbox.framework.statistics.parametrics.independentsamples;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class NormalIndependentSamplesTest {
    
    public NormalIndependentSamplesTest() {
    }

    /**
     * Test of testMeans method, of class NormalIndependentSamples.
     */
    @Test
    public void testTestMeans() {
        System.out.println("testMeans");
        double xbar = 215.0;
        double ybar = 200.0;
        int n = 60;
        int m = 50;
        double stdx = 55.0;
        double stdy = 50.0;
        boolean is_twoTailed = false;
        double aLevel = 0.05;
        boolean expResult = false;
        boolean result = NormalIndependentSamples.testMeans(xbar, ybar, n, m, stdx, stdy, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }

    /**
     * Test of testPercentages method, of class NormalIndependentSamples.
     */
    @Test
    public void testTestPercentages() {
        System.out.println("testPercentages");
        double p1bar = 0.54;
        double p2bar = 0.61;
        int n1 = 1000;
        int n2 = 1000;
        boolean is_twoTailed = false;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = NormalIndependentSamples.testPercentages(p1bar, p2bar, n1, n2, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }

    /**
     * Test of testOddsRatio method, of class NormalIndependentSamples.
     */
    @Test
    public void testTestOddsRatio() {
        System.out.println("testOddsRatio");
        int n11 = 131;
        int n12 = 33;
        int n21 = 251;
        int n22 = 4;
        boolean is_twoTailed = false;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = NormalIndependentSamples.testOddsRatio(n11, n12, n21, n22, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
