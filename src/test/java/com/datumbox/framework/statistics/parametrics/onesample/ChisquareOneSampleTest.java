/**
 * Copyright (C) 2013-2016 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.statistics.parametrics.onesample;


import com.datumbox.tests.bases.BaseTest;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ChisquareOneSampleTest extends BaseTest {

    /**
     * Test of testVariance method, of class ChisquareOneSample.
     */
    @Test
    public void testTestVariance() {
        logger.info("testVariance");
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
