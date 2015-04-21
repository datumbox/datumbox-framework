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
package com.datumbox.framework.statistics.sampling;

import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.tests.bases.BaseTest;

import com.datumbox.tests.utilities.TestUtils;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class SystematicSamplingTest extends BaseTest {

    protected FlatDataCollection generateFlatDataCollection() {
        //Example from Papageorgious' notes
        FlatDataCollection flatDataCollection = new FlatDataCollection(Arrays.asList(new Object[]{54,16,250,8,145,62,64,55,87,23,60,120,18,29,320,160,102,12,28,280,130,45,74,340}));
        return flatDataCollection;
    }

    /**
     * Test of randomSampling method, of class SystematicSampling.
     */
    @Test
    public void testRandomSampling() {
        logger.info("randomSampling");
        FlatDataList idList = new FlatDataList(Arrays.asList(new Object[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16}));
        int n = 6;
        boolean randomizeRecords = false;
        double expResult = n;
        FlatDataCollection sampledIds = SystematicSampling.randomSampling(idList, n, randomizeRecords);
        double result = sampledIds.size();
        assertTrue(result>=expResult);
    }

    /**
     * Test of mean method, of class SystematicSampling.
     */
    @Test
    public void testMean() {
        logger.info("mean");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        double expResult = 103.41666666667;
        double result = SystematicSampling.mean(flatDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of xbarVariance method, of class SystematicSampling.
     */
    @Test
    public void testXbarVariance() {
        logger.info("xbarVariance");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        double expResult = 405.75;
        double result = SystematicSampling.xbarVariance(flatDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of xbarStd method, of class SystematicSampling.
     */
    @Test
    public void testXbarStd() {
        logger.info("xbarStd");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        double expResult = 20.143237078484;
        double result = SystematicSampling.xbarStd(flatDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }
    
}
