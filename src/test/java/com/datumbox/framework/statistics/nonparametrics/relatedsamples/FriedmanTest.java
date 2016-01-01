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
package com.datumbox.framework.statistics.nonparametrics.relatedsamples;

import com.datumbox.common.dataobjects.DataTable2D;
import com.datumbox.tests.bases.BaseTest;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class FriedmanTest extends BaseTest {
    
    /**
     * Test of test method, of class Friedman.
     */
    @Test
    public void testTest() {
        logger.info("test");
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
