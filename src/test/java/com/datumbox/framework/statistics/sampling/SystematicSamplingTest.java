/* 
 * Copyright (C) 2014 Vasilis Vryniotis <bbriniotis at datumbox.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.datumbox.framework.statistics.sampling;

import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.configuration.TestConfiguration;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class SystematicSamplingTest {
    
    public SystematicSamplingTest() {
    }

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
        System.out.println("randomSampling");
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
        System.out.println("mean");
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
        System.out.println("xbarVariance");
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
        System.out.println("xbarStd");
        FlatDataCollection flatDataCollection = generateFlatDataCollection();
        double expResult = 20.143237078484;
        double result = SystematicSampling.xbarStd(flatDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }
    
}
