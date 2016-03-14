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
package com.datumbox.framework.core.statistics.parametrics.independentsamples;


import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for StudentsIndependentSamples.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class StudentsIndependentSamplesTest extends AbstractTest {

    /**
     * Test of testMeansUnknownNotEqualVars method, of class StudentsIndependentSamples.
     */
    @Test
    public void testTestMeansUnknownNotEqualVars() {
        logger.info("testMeansUnknownNotEqualVars");
        double xbar = 215.0;
        double ybar = 200.0;
        int n = 60;
        int m = 50;
        double stdx = 55.0;
        double stdy = 50.0;
        boolean is_twoTailed = false;
        double aLevel = 0.05;
        boolean expResult = false;
        boolean result = StudentsIndependentSamples.testMeansUnknownNotEqualVars(xbar, ybar, n, m, stdx, stdy, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }

    /**
     * Test of testMeansUnknownEqualVars method, of class StudentsIndependentSamples.
     */
    @Test
    public void testTestMeansUnknownEqualVars() {
        logger.info("testMeansUnknownEqualVars");
        double xbar = 14.14;
        double ybar = 12.08;
        int n = 15;
        int m = 15;
        double stdx = 1.020784;
        double stdy = 0.951314;
        boolean is_twoTailed = false;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = StudentsIndependentSamples.testMeansUnknownEqualVars(xbar, ybar, n, m, stdx, stdy, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
