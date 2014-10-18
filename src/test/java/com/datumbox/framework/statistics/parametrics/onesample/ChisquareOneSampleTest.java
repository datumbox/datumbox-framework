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
public class ChisquareOneSampleTest {
    
    public ChisquareOneSampleTest() {
    }

    /**
     * Test of testVariance method, of class ChisquareOneSample.
     */
    @Test
    public void testTestVariance() {
        System.out.println("testVariance");
        double stdbar = 0.0063;
        int n = 100;
        double H0std = 0.01;
        boolean is_twoTailed = true;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = ChisquareOneSample.testVariance(stdbar, n, H0std, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
