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
public class LillieforsTest {
    
    public LillieforsTest() {
    }

    /**
     * Test of test method, of class Lilliefors.
     */
    @Test
    public void testTest() throws Exception {
        System.out.println("test");
        //Example from Dimaki's Non-parametrics notes. It should NOT reject the null hypothesis and return false.
        FlatDataCollection flatDataCollection =  new FlatDataCollection(Arrays.asList(new Object[]{33.4, 33.3, 31.0, 31.4, 33.5, 34.4, 33.7, 36.2, 34.9, 37.0}));
        String cdfMethod = "normalDistribution";
        double aLevel = 0.3;
        boolean expResult = false;
        boolean result = Lilliefors.test(flatDataCollection, cdfMethod, aLevel);
        assertEquals(expResult, result);
    }

}
