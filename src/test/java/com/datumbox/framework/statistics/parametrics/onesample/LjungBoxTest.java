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
package com.datumbox.framework.statistics.parametrics.onesample;

import com.datumbox.common.dataobjects.FlatDataCollection;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class LjungBoxTest {
    
    public LjungBoxTest() {
    }
    
    /**
     * Test of testAutocorrelation method, of class LjungBox.
     */
    @Test
    public void testTestAutocorrelation() {
        System.out.println("testAutocorrelation");
        FlatDataCollection pkList = new FlatDataCollection(Arrays.asList(new Object[]{0.810,0.631,0.469,0.349}));
        int n = 100;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = LjungBox.testAutocorrelation(pkList, n, aLevel);
        assertEquals(expResult, result);
    }
    
}
