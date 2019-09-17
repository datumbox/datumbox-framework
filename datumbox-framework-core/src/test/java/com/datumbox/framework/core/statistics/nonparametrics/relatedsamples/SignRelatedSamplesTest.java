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
package com.datumbox.framework.core.statistics.nonparametrics.relatedsamples;

import com.datumbox.framework.common.dataobjects.FlatDataList;
import com.datumbox.framework.common.dataobjects.TransposeDataList;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for SignRelatedSamples.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class SignRelatedSamplesTest extends AbstractTest {

    /**
     * Test of test method, of class SignRelatedSamples.
     */
    @Test
    public void testTest() {
        logger.info("test");
        TransposeDataList transposeDataList = new TransposeDataList();
        transposeDataList.put(0, new FlatDataList(Arrays.asList(new Object[]{136,115,142,140,123,147,133,150,138,147,151,145,147.0})));
        transposeDataList.put(1, new FlatDataList(Arrays.asList(new Object[]{141.0,117,141,145,127,146,135,152,135,152,149,148,147})));
        
        boolean is_twoTailed = true;
        double aLevel = 0.05;
        boolean expResult = false;
        boolean result = SignRelatedSamples.test(transposeDataList, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
