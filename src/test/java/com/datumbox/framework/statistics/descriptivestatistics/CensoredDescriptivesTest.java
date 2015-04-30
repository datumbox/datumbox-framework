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
package com.datumbox.framework.statistics.descriptivestatistics;

import com.datumbox.common.dataobjects.AssociativeArray2D;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.tests.bases.BaseTest;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class CensoredDescriptivesTest extends BaseTest {

    private FlatDataCollection generateFlatDataCollection() {
        Object[] values = { 3,"4+","5.7+",6.5,6.5,"8.4+",10,"10+",12,15 };
        
        FlatDataCollection flatDataCollection = new FlatDataCollection(Arrays.asList(values));
        
        return flatDataCollection;
    }

    /**
     * Test of median method, of class CensoredDescriptives.
     */
    @Test
    public void testMedian() {
        logger.info("median");
        AssociativeArray2D survivalFunction = CensoredDescriptives.survivalFunction(generateFlatDataCollection());
        double expResult = 9.6111111111111;
        double result = CensoredDescriptives.median(survivalFunction);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of mean method, of class CensoredDescriptives.
     */
    @Test
    public void testMean() {
        logger.info("mean");
        AssociativeArray2D survivalFunction = CensoredDescriptives.survivalFunction(generateFlatDataCollection());
        double expResult = 10.0875;
        double result = CensoredDescriptives.mean(survivalFunction);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of meanVariance method, of class CensoredDescriptives.
     */
    @Test
    public void testMeanVariance() {
        logger.info("meanVariance");
        AssociativeArray2D survivalFunction = CensoredDescriptives.survivalFunction(generateFlatDataCollection());
        double expResult = 2.7874113520408;
        double result = CensoredDescriptives.meanVariance(survivalFunction);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of meanStd method, of class CensoredDescriptives.
     */
    @Test
    public void testMeanStd() {
        logger.info("meanStd");
        AssociativeArray2D survivalFunction = CensoredDescriptives.survivalFunction(generateFlatDataCollection());
        double expResult = 1.6695542375259;
        double result = CensoredDescriptives.meanStd(survivalFunction);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }
    
}
