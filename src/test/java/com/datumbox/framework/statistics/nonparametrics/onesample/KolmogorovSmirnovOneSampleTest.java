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

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.tests.bases.BaseTest;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class KolmogorovSmirnovOneSampleTest extends BaseTest {
    
    /**
     * Test of test method, of class KolmogorovSmirnovOneSample.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testTest() throws Exception {
        logger.info("test");
        //Example from Dimaki's Non-parametrics notes. It should reject the null hypothesis and return True.
        FlatDataCollection flatDataCollection =  new FlatDataCollection(Arrays.asList(new Object[]{33.4, 33.3, 31.0, 31.4, 33.5, 34.4, 33.7, 36.2, 34.9, 37.0}));
        String cdfMethod = "normalDistribution";
        AssociativeArray params = new AssociativeArray();
        params.put("mean", 32.0);
        params.put("variance", 3.24);
        
        boolean is_twoTailed = true;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = KolmogorovSmirnovOneSample.test(flatDataCollection, cdfMethod, params, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
