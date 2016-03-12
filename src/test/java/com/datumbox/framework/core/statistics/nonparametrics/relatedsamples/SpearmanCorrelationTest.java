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
package com.datumbox.framework.core.statistics.nonparametrics.relatedsamples;

import com.datumbox.framework.common.dataobjects.FlatDataList;
import com.datumbox.framework.common.dataobjects.TransposeDataList;
import com.datumbox.framework.tests.abstracts.AbstractTest;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for SpearmanCorrelation.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class SpearmanCorrelationTest extends AbstractTest {
    
    /**
     * Test of test method, of class SpearmanCorrelation.
     */
    @Test
    public void testTest() {
        logger.info("test");
        TransposeDataList transposeDataList = new TransposeDataList();
        //Example from https://statistics.laerd.com/statistical-guides/spearmans-rank-order-correlation-statistical-guide.php. 
        //It should reject the null hypothesis and return true.
        transposeDataList.put(0, new FlatDataList(Arrays.asList(new Object[]{56,75,45,71,61,64,58,80,76,61})));
        transposeDataList.put(1, new FlatDataList(Arrays.asList(new Object[]{66,70,40,60,65,56,59,77,67,63})));
        boolean is_twoTailed = true;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = SpearmanCorrelation.test(transposeDataList, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
