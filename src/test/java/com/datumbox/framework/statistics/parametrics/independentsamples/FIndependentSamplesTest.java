/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
package com.datumbox.framework.statistics.parametrics.independentsamples;


import com.datumbox.tests.utilities.TestUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class FIndependentSamplesTest {
    
    public FIndependentSamplesTest() {
    }
    
    /**
     * Test of testVariances method, of class FIndependentSamples.
     */
    @Test
    public void testTestVariances() {
        TestUtils.log(this.getClass(), "testVariances");
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
