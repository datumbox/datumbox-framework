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
public class CochranQTest {
    
    public CochranQTest() {
    }
    
    /**
     * Test of test method, of class CochranQ.
     */
    @Test
    public void testTest() {
        System.out.println("test");
        //Example from Dimaki's Non-parametrics notes. It should reject the null hypothesis and return True.
        DataTable2D dataTable = new DataTable2D();
        dataTable.put2d(0, 0, 0); dataTable.put2d(0, 1, 0); dataTable.put2d(0, 2, 0); 
        dataTable.put2d(1, 0, 1); dataTable.put2d(1, 1, 1); dataTable.put2d(1, 2, 0); 
        dataTable.put2d(2, 0, 0); dataTable.put2d(2, 1, 1); dataTable.put2d(2, 2, 0); 
        dataTable.put2d(3, 0, 0); dataTable.put2d(3, 1, 0); dataTable.put2d(3, 2, 0);
        dataTable.put2d(4, 0, 1); dataTable.put2d(4, 1, 0); dataTable.put2d(4, 2, 0); 
        dataTable.put2d(5, 0, 1); dataTable.put2d(5, 1, 1); dataTable.put2d(5, 2, 0); 
        dataTable.put2d(6, 0, 0); dataTable.put2d(6, 1, 1); dataTable.put2d(6, 2, 0); 
        dataTable.put2d(7, 0, 1); dataTable.put2d(7, 1, 1); dataTable.put2d(7, 2, 0); 
        dataTable.put2d(8, 0, 0); dataTable.put2d(8, 1, 0); dataTable.put2d(8, 2, 0); 
        dataTable.put2d(9, 0, 1); dataTable.put2d(9, 1, 0); dataTable.put2d(9, 2, 0); 
        dataTable.put2d(10, 0, 1); dataTable.put2d(10, 1, 1); dataTable.put2d(10, 2, 1); 
        dataTable.put2d(11, 0, 1); dataTable.put2d(11, 1, 1); dataTable.put2d(11, 2, 1); 
        dataTable.put2d(12, 0, 1); dataTable.put2d(12, 1, 1); dataTable.put2d(12, 2, 0); 
        dataTable.put2d(13, 0, 1); dataTable.put2d(13, 1, 1); dataTable.put2d(13, 2, 0); 
        dataTable.put2d(14, 0, 1); dataTable.put2d(14, 1, 1); dataTable.put2d(14, 2, 0); 
        dataTable.put2d(15, 0, 1); dataTable.put2d(15, 1, 1); dataTable.put2d(15, 2, 1); 
        dataTable.put2d(16, 0, 1); dataTable.put2d(16, 1, 1); dataTable.put2d(16, 2, 0); 
        dataTable.put2d(17, 0, 1); dataTable.put2d(17, 1, 1); dataTable.put2d(17, 2, 0);
        
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = CochranQ.test(dataTable, aLevel);
        assertEquals(expResult, result);
    }
    
}
