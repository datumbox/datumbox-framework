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
package com.datumbox.framework.statistics.nonparametrics.relatedsamples;

import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.TransposeDataList;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class SpearmanCorrelationTest {
    
    public SpearmanCorrelationTest() {
    }
    
    /**
     * Test of test method, of class SpearmanCorrelation.
     */
    @Test
    public void testTest() {
        System.out.println("test");
        TransposeDataList transposeDataList = new TransposeDataList();
        //Example from https://statistics.laerd.com/statistical-guides/spearmans-rank-order-correlation-statistical-guide.php. 
        //It should reject the null hypothesis and return true.
        transposeDataList.put(0, new FlatDataList(Arrays.asList(new Object[]{56,75,45,71,61,64,58,80,76,61})));
        transposeDataList.put(1, new FlatDataList(Arrays.asList(new Object[]{66,70,40,60,65,56,59,77,67,63})));
        boolean is_twoTailed = true;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = SpearmanCorrelation.test(transposeDataList, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
