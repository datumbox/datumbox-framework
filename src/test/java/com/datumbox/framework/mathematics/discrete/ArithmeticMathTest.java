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
package com.datumbox.framework.mathematics.discrete;

import com.datumbox.configuration.TestConfiguration;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class ArithmeticMathTest {
    
    public ArithmeticMathTest() {
    }

    /**
     * Test of factorial method, of class ArithmeticMath.
     */
    @Test
    public void testFactorial() {
        System.out.println("factorial");
        int k = 10;
        double expResult = 3628800.0;
        double result = ArithmeticMath.factorial(k);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of combination method, of class ArithmeticMath.
     */
    @Test
    public void testCombination() {
        System.out.println("combination");
        int n = 10;
        int k = 3;
        double expResult = 120.0;
        double result = ArithmeticMath.combination(n, k);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }
    
}
