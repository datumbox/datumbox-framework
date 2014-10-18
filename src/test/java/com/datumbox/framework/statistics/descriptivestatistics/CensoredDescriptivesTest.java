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
package com.datumbox.framework.statistics.descriptivestatistics;

import com.datumbox.common.dataobjects.AssociativeArray2D;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.configuration.TestConfiguration;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class CensoredDescriptivesTest {
    
    public CensoredDescriptivesTest() {
    }

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
        System.out.println("median");
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
        System.out.println("mean");
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
        System.out.println("meanVariance");
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
        System.out.println("meanStd");
        AssociativeArray2D survivalFunction = CensoredDescriptives.survivalFunction(generateFlatDataCollection());
        double expResult = 1.6695542375259;
        double result = CensoredDescriptives.meanStd(survivalFunction);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }
    
}
