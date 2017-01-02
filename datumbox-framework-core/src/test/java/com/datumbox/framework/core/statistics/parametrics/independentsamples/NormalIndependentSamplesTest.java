/**
 * Copyright (C) 2013-2017 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
 * Test cases for NormalIndependentSamples.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class NormalIndependentSamplesTest extends AbstractTest {

    /**
     * Test of testMeans method, of class NormalIndependentSamples.
     */
    @Test
    public void testTestMeans() {
        logger.info("testMeans");
        double xbar = 215.0;
        double ybar = 200.0;
        int n = 60;
        int m = 50;
        double stdx = 55.0;
        double stdy = 50.0;
        boolean is_twoTailed = false;
        double aLevel = 0.05;
        boolean expResult = false;
        boolean result = NormalIndependentSamples.testMeans(xbar, ybar, n, m, stdx, stdy, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }

    /**
     * Test of testPercentages method, of class NormalIndependentSamples.
     */
    @Test
    public void testTestPercentages() {
        logger.info("testPercentages");
        double p1bar = 0.54;
        double p2bar = 0.61;
        int n1 = 1000;
        int n2 = 1000;
        boolean is_twoTailed = false;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = NormalIndependentSamples.testPercentages(p1bar, p2bar, n1, n2, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }

    /**
     * Test of testOddsRatio method, of class NormalIndependentSamples.
     */
    @Test
    public void testTestOddsRatio() {
        logger.info("testOddsRatio");
        int n11 = 131;
        int n12 = 33;
        int n21 = 251;
        int n22 = 4;
        boolean is_twoTailed = false;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = NormalIndependentSamples.testOddsRatio(n11, n12, n21, n22, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
