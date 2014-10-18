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
package com.datumbox.framework.statistics.nonparametrics.independentsamples;

import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.dataobjects.TransposeDataCollection;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class MannWhitneyTest {
    
    public MannWhitneyTest() {
    }

    /**
     * Test of test method, of class MannWhitney.
     */
    @Test
    public void testTest() {
        System.out.println("test");
        //Example from Dimaki's Non-parametrics notes. It should reject the null hypothesis and return true.
        TransposeDataCollection transposeDataCollection = new TransposeDataCollection();
        transposeDataCollection.put("group1", new FlatDataCollection(Arrays.asList(new Object[]{32,26.5,28.5,30,26})));
        transposeDataCollection.put("group2", new FlatDataCollection(Arrays.asList(new Object[]{18.5,16,19.5,20})));
        
        boolean is_twoTailed = true;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = MannWhitney.test(transposeDataCollection, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
