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
public class StudentsOneSampleTest {
    
    public StudentsOneSampleTest() {
    }
    
    /**
     * Test of testMean method, of class StudentsOneSample.
     */
    @Test
    public void testTestMean() {
        System.out.println("testMean");
        double xbar = 7.4;
        int n = 28;
        double H0mean = 7.0;
        double std = 0.95;
        boolean is_twoTailed = false;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = StudentsOneSample.testMean(xbar, n, H0mean, std, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }

    /**
     * Test of testAutocorrelation method, of class StudentsOneSample.
     */
    @Test
    public void testTestAutocorrelation() {
        System.out.println("testAutocorrelation");
        double pk = 0.2;
        int n = 50;
        boolean is_twoTailed = false;
        double aLevel = 0.05;
        boolean expResult = false;
        boolean result = StudentsOneSample.testAutocorrelation(pk, n, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
