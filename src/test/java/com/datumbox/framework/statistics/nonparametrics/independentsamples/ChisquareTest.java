/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.statistics.nonparametrics.independentsamples;

import com.datumbox.common.dataobjects.DataTable2D;
import com.datumbox.tests.bases.BaseTest;

import com.datumbox.tests.utilities.TestUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ChisquareTest extends BaseTest {

    /**
     * Test of test method, of class Chisquare.
     */
    @Test
    public void testTest() {
        TestUtils.log(this.getClass(), "test");
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
