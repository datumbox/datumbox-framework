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
package com.datumbox.framework.statistics.nonparametrics.onesample;

import com.datumbox.common.dataobjects.FlatDataCollection;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class SignOneSampleTest {
    
    public SignOneSampleTest() {
    }

    /**
     * Test of test method, of class SignOneSample.
     */
    @Test
    public void testTest() {
        System.out.println("test");
        //Example from Dimaki's Non-parametrics notes. It should reject the null hypothesis and return true.
        FlatDataCollection flatDataCollection = new FlatDataCollection(Arrays.asList(new Object[]{0.16,0.12,0.19,0.16,0.17,0.18,0.15,0.20,0.16,0.18,0.13,0.17,0.18,0.21,0.18,0.17,0.19,0.11,0.16,0.16}));
        double median = 0.15;
        boolean is_twoTailed = true;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = SignOneSample.test(flatDataCollection, median, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
