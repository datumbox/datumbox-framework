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

import com.datumbox.common.dataobjects.DataTable2D;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class FriedmanTest {
    
    public FriedmanTest() {
    }
    
    /**
     * Test of test method, of class Friedman.
     */
    @Test
    public void testTest() {
        System.out.println("test");
        DataTable2D dataTable = new DataTable2D();
        //Example from Dimaki's Non-parametrics notes. It should reject the null hypothesis and return true.
        dataTable.put2d(0,0,4); dataTable.put2d(0,1,7); dataTable.put2d(0,2,8); dataTable.put2d(0,3,6); dataTable.put2d(0,4,5); dataTable.put2d(0,5,5); 
        dataTable.put2d(1,0,6); dataTable.put2d(1,1,9); dataTable.put2d(1,2,7); dataTable.put2d(1,3,6); dataTable.put2d(1,4,4); dataTable.put2d(1,5,5); 
        dataTable.put2d(2,0,8); dataTable.put2d(2,1,10); dataTable.put2d(2,2,10); dataTable.put2d(2,3,9); dataTable.put2d(2,4,6); dataTable.put2d(2,5,7); 
        dataTable.put2d(3,0,6); dataTable.put2d(3,1,4); dataTable.put2d(3,2,8); dataTable.put2d(3,3,5); dataTable.put2d(3,4,3); dataTable.put2d(3,5,7); 

        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = Friedman.test(dataTable, aLevel);
        assertEquals(expResult, result);
    }
    
}
