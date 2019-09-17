/**
 * Copyright (C) 2013-2019 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.core.statistics.parametrics.independentsamples;


import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for FIndependentSamples.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class FIndependentSamplesTest extends AbstractTest {
    
    /**
     * Test of testVariances method, of class FIndependentSamples.
     */
    @Test
    public void testTestVariances() {
        logger.info("testVariances");
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
