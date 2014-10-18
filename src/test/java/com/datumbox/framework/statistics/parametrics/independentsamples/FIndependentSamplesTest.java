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
public class FIndependentSamplesTest {
    
    public FIndependentSamplesTest() {
    }
    
    /**
     * Test of testVariances method, of class FIndependentSamples.
     */
    @Test
    public void testTestVariances() {
        System.out.println("testVariances");
        double stdbarx = 65.54909;
        double stdbary = 61.85425;
        int n = 100;
        int m = 240;
        boolean is_twoTailed = true;
        double aLevel = 0.05;
        boolean expResult = false;
        boolean result = FIndependentSamples.testVariances(stdbarx, stdbary, n, m, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
