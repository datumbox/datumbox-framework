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
public class StudentsIndependentSamplesTest {
    
    public StudentsIndependentSamplesTest() {
    }

    /**
     * Test of testMeansUnknownNotEqualVars method, of class StudentsIndependentSamples.
     */
    @Test
    public void testTestMeansUnknownNotEqualVars() {
        System.out.println("testMeansUnknownNotEqualVars");
        double xbar = 215.0;
        double ybar = 200.0;
        int n = 60;
        int m = 50;
        double stdx = 55.0;
        double stdy = 50.0;
        boolean is_twoTailed = false;
        double aLevel = 0.05;
        boolean expResult = false;
        boolean result = StudentsIndependentSamples.testMeansUnknownNotEqualVars(xbar, ybar, n, m, stdx, stdy, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }

    /**
     * Test of testMeansUnknownEqualVars method, of class StudentsIndependentSamples.
     */
    @Test
    public void testTestMeansUnknownEqualVars() {
        System.out.println("testMeansUnknownEqualVars");
        double xbar = 14.14;
        double ybar = 12.08;
        int n = 15;
        int m = 15;
        double stdx = 1.020784;
        double stdy = 0.951314;
        boolean is_twoTailed = false;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = StudentsIndependentSamples.testMeansUnknownEqualVars(xbar, ybar, n, m, stdx, stdy, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
