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
package com.datumbox.framework.statistics.parametrics.relatedsamples;


import com.datumbox.tests.bases.BaseTest;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class StudentsRelatedSamplesTest extends BaseTest {
    
    /**
     * Test of testMean method, of class StudentsRelatedSamples.
     */
    @Test
    public void testTestMean() {
        logger.info("testMean");
        double dbar = 2.2;
        int n = 10;
        double dbarStd = 1.924;
        boolean is_twoTailed = false;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = StudentsRelatedSamples.testMean(dbar, n, dbarStd, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
