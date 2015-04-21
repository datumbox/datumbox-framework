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
package com.datumbox.framework.statistics.nonparametrics.onesample;

import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.tests.bases.BaseTest;

import com.datumbox.tests.utilities.TestUtils;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class LillieforsTest extends BaseTest {

    /**
     * Test of test method, of class Lilliefors.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testTest() throws Exception {
        logger.info("test");
        //Example from Dimaki's Non-parametrics notes. It should NOT reject the null hypothesis and return false.
        FlatDataCollection flatDataCollection =  new FlatDataCollection(Arrays.asList(new Object[]{33.4, 33.3, 31.0, 31.4, 33.5, 34.4, 33.7, 36.2, 34.9, 37.0}));
        String cdfMethod = "normalDistribution";
        double aLevel = 0.3;
        boolean expResult = false;
        boolean result = Lilliefors.test(flatDataCollection, cdfMethod, aLevel);
        assertEquals(expResult, result);
    }

}
