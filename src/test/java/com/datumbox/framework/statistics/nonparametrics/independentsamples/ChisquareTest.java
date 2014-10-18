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

import com.datumbox.common.dataobjects.DataTable2D;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class ChisquareTest {
    
    public ChisquareTest() {
    }

    /**
     * Test of test method, of class Chisquare.
     */
    @Test
    public void testTest() {
        System.out.println("test");
        //Example from Dimaki's Non-parametrics notes. It should reject the null hypothesis and return True.
        DataTable2D dataTable = new DataTable2D();
        dataTable.put2d(0, 0, 13);
        dataTable.put2d(0, 1, 8);
        dataTable.put2d(0, 2, 10);
        dataTable.put2d(0, 3, 3);
        dataTable.put2d(1, 0, 20);
        dataTable.put2d(1, 1, 23);
        dataTable.put2d(1, 2, 27);
        dataTable.put2d(1, 3, 18);
        dataTable.put2d(2, 0, 11);
        dataTable.put2d(2, 1, 12);
        dataTable.put2d(2, 2, 12);
        dataTable.put2d(2, 3, 21);
        
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = Chisquare.test(dataTable, aLevel);
        assertEquals(expResult, result);
    }
    
}
