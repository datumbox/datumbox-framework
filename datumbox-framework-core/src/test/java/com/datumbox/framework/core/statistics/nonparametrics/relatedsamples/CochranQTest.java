/**
 * Copyright (C) 2013-2016 Vasilis Vryniotis <bbriniotis@datumbox.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datumbox.framework.core.statistics.nonparametrics.relatedsamples;

import com.datumbox.framework.common.dataobjects.DataTable2D;
import com.datumbox.framework.tests.abstracts.AbstractTest;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for CochranQ.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class CochranQTest extends AbstractTest {
    
    /**
     * Test of test method, of class CochranQ.
     */
    @Test
    public void testTest() {
        logger.info("test");
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
