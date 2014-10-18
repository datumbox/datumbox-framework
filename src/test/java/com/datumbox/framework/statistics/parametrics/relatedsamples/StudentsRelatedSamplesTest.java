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
package com.datumbox.framework.statistics.parametrics.relatedsamples;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class StudentsRelatedSamplesTest {
    
    public StudentsRelatedSamplesTest() {
    }
    
    /**
     * Test of testMean method, of class StudentsRelatedSamples.
     */
    @Test
    public void testTestMean() {
        System.out.println("testMean");
        double dbar = 2.2;
        int n = 10;
        double dbarStd = 1.924;
        boolean is_twoTailed = false;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = StudentsRelatedSamples.testMean(dbar, n, dbarStd, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
