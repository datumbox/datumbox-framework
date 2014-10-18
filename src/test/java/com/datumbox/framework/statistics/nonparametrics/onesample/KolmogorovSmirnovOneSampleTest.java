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

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.FlatDataCollection;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class KolmogorovSmirnovOneSampleTest {
    
    public KolmogorovSmirnovOneSampleTest() {
    }
    /**
     * Test of test method, of class KolmogorovSmirnovOneSample.
     */
    @Test
    public void testTest() throws Exception {
        System.out.println("test");
        //Example from Dimaki's Non-parametrics notes. It should reject the null hypothesis and return True.
        FlatDataCollection flatDataCollection =  new FlatDataCollection(Arrays.asList(new Object[]{33.4, 33.3, 31.0, 31.4, 33.5, 34.4, 33.7, 36.2, 34.9, 37.0}));
        String cdfMethod = "normalDistribution";
        AssociativeArray params = new AssociativeArray();
        params.put("mean", 32.0);
        params.put("variance", 3.24);
        
        boolean is_twoTailed = true;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = KolmogorovSmirnovOneSample.test(flatDataCollection, cdfMethod, params, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
