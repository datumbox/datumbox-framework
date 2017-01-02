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
package com.datumbox.framework.core.statistics.parametrics.onesample;


import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for StudentsOneSample.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class StudentsOneSampleTest extends AbstractTest {
    
    /**
     * Test of testMean method, of class StudentsOneSample.
     */
    @Test
    public void testTestMean() {
        logger.info("testMean");
        double xbar = 7.4;
        int n = 28;
        double H0mean = 7.0;
        double std = 0.95;
        boolean is_twoTailed = false;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = StudentsOneSample.testMean(xbar, n, H0mean, std, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }

    /**
     * Test of testAutocorrelation method, of class StudentsOneSample.
     */
    @Test
    public void testTestAutocorrelation() {
        logger.info("testAutocorrelation");
        double pk = 0.2;
        int n = 50;
        boolean is_twoTailed = false;
        double aLevel = 0.05;
        boolean expResult = false;
        boolean result = StudentsOneSample.testAutocorrelation(pk, n, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
