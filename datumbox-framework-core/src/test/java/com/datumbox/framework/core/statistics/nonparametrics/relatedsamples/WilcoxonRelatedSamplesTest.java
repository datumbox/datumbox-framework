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
package com.datumbox.framework.core.statistics.nonparametrics.relatedsamples;

import com.datumbox.framework.common.dataobjects.FlatDataList;
import com.datumbox.framework.common.dataobjects.TransposeDataList;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for WilcoxonRelatedSamples.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class WilcoxonRelatedSamplesTest extends AbstractTest {

    /**
     * Test of test method, of class WilcoxonRelatedSamples.
     */
    @Test
    public void testTest() {
        logger.info("test");
        TransposeDataList transposeDataList = new TransposeDataList();
        //Example from Dimaki's Non-parametrics notes. It should reject the null hypothesis and return true.
        transposeDataList.put(0, new FlatDataList(Arrays.asList(new Object[]{39.8,38.8,38.4,39.9,39.4,38.4,38.6,41.2,39.0,39.1})));
        transposeDataList.put(1, new FlatDataList(Arrays.asList(new Object[]{38.8,38.6,37.5,38.0,38.7,38.4,38.7,38.6,38.3,38.6})));
        
        boolean is_twoTailed = false;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = WilcoxonRelatedSamples.test(transposeDataList, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
