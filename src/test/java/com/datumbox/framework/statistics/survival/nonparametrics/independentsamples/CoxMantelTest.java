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
package com.datumbox.framework.statistics.survival.nonparametrics.independentsamples;

import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.dataobjects.TransposeDataCollection;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class CoxMantelTest {
    
    public CoxMantelTest() {
    }

    /**
     * Test of test method, of class CoxMantel.
     */
    @Test
    public void testTest() {
        System.out.println("test");
        //Example from Dimaki's Survival Non-parametrics notes. It should reject the null hypothesis and return true.
        TransposeDataCollection transposeDataCollection = new TransposeDataCollection();
        transposeDataCollection.put(0, new FlatDataCollection(Arrays.asList(new Object[]{23,"16+","18+","20+","24+"})));
        transposeDataCollection.put(1, new FlatDataCollection(Arrays.asList(new Object[]{15,18,19,19,20.0})));
        boolean is_twoTailed = true;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = CoxMantel.test(transposeDataCollection, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
