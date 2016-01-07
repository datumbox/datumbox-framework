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
package com.datumbox.framework.statistics.nonparametrics.onesample;

import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.tests.bases.BaseTest;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class WilcoxonOneSampleTest extends BaseTest {

    /**
     * Test of test method, of class WilcoxonOneSample.
     */
    @Test
    public void testTest() {
        logger.info("test");
        //Example from Dimaki's Non-parametrics notes. It should reject the null hypothesis and return true.
        FlatDataCollection flatDataCollection = new FlatDataCollection(Arrays.asList(new Object[]{19.5,19.8,18.9,20.4,20.2,21.5,19.9,20.9,18.1,20.5,18.3,19.5,18.3,19.0,18.2,23.9,17.0,19.7,21.7,19.5}));
        double median = 20.8;
        boolean is_twoTailed = true;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = WilcoxonOneSample.test(flatDataCollection, median, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
