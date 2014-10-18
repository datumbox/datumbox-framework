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
package com.datumbox.framework.statistics.nonparametrics.relatedsamples;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class McNemarTest {
    
    public McNemarTest() {
    }

    /**
     * Test of test method, of class McNemar.
     */
    @Test
    public void testTest() {
        System.out.println("test");
        //Example from Wikipedia: http://en.wikipedia.org/wiki/McNemar's_test 
        //It should reject the null hypothesis and return true.
        int n11 = 101;
        int n12 = 121;
        int n21 = 59;
        int n22 = 33;
        boolean is_twoTailed = true;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = McNemar.test(n11, n12, n21, n22, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
