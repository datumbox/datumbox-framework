/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datumbox.framework.mathematics.discrete;

import com.datumbox.configuration.TestConfiguration;
import com.datumbox.tests.bases.BaseTest;
import com.datumbox.tests.utilities.TestUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ArithmeticMathTest extends BaseTest {
    
    /**
     * Test of factorial method, of class ArithmeticMath.
     */
    @Test
    public void testFactorial() {
        TestUtils.log(this.getClass(), "factorial");
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
        TestUtils.log(this.getClass(), "combination");
        int n = 10;
        int k = 3;
        double expResult = 120.0;
        double result = ArithmeticMath.combination(n, k);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }
    
}
