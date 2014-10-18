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
public class SignRelatedSamplesTest {
    
    public SignRelatedSamplesTest() {
    }

    /**
     * Test of test method, of class SignRelatedSamples.
     */
    @Test
    public void testTest() {
        System.out.println("test");
        TransposeDataList transposeDataList = new TransposeDataList();
        transposeDataList.put(0, new FlatDataList(Arrays.asList(new Object[]{136,115,142,140,123,147,133,150,138,147,151,145,147.0})));
        transposeDataList.put(1, new FlatDataList(Arrays.asList(new Object[]{141.0,117,141,145,127,146,135,152,135,152,149,148,147})));
        
        boolean is_twoTailed = true;
        double aLevel = 0.05;
        boolean expResult = false;
        boolean result = SignRelatedSamples.test(transposeDataList, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
