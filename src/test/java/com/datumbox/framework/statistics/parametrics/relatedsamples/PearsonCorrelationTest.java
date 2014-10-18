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
package com.datumbox.framework.statistics.parametrics.relatedsamples;

import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.TransposeDataList;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class PearsonCorrelationTest {
    
    public PearsonCorrelationTest() {
    }
    
    /**
     * Test of test method, of class PearsonCorrelation.
     */
    @Test
    public void testTest() {
        System.out.println("test");
        TransposeDataList transposeDataList = new TransposeDataList();
        transposeDataList.put(0, new FlatDataList(Arrays.asList(new Object[]{64,61,84,70,88,92,72,77})));
        transposeDataList.put(1, new FlatDataList(Arrays.asList(new Object[]{20,16,34,23,27,32,18,22})));
        
        boolean is_twoTailed = true;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = PearsonCorrelation.test(transposeDataList, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
