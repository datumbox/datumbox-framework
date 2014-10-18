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
public class WilcoxonOneSampleTest {
    
    public WilcoxonOneSampleTest() {
    }

    /**
     * Test of test method, of class WilcoxonOneSample.
     */
    @Test
    public void testTest() {
        System.out.println("test");
        //Example from Dimaki's Non-parametrics notes. It should reject the null hypothesis and return true.
        FlatDataCollection flatDataCollection = new FlatDataCollection(Arrays.asList(new Object[]{19.5,19.8,18.9,20.4,20.2,21.5,19.9,20.9,18.1,20.5,18.3,19.5,18.3,19.0,18.2,23.9,17.0,19.7,21.7,19.5}));
        double median = 20.8;
        boolean is_twoTailed = true;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = WilcoxonOneSample.test(flatDataCollection, median, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
