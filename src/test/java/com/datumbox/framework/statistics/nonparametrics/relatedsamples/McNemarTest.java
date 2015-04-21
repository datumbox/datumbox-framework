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
package com.datumbox.framework.statistics.nonparametrics.relatedsamples;


import com.datumbox.tests.bases.BaseTest;
import com.datumbox.tests.utilities.TestUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class McNemarTest extends BaseTest {

    /**
     * Test of test method, of class McNemar.
     */
    @Test
    public void testTest() {
        logger.info("test");
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
