/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
package com.datumbox.framework.statistics.parametrics.onesample;

import com.datumbox.common.dataobjects.FlatDataCollection;

import com.datumbox.tests.utilities.TestUtils;
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
        TestUtils.log(this.getClass(), "testAutocorrelation");
        FlatDataCollection pkList = new FlatDataCollection(Arrays.asList(new Object[]{0.810,0.631,0.469,0.349}));
        int n = 100;
        double aLevel = 0.05;
        boolean expResult = true;
        boolean result = LjungBox.testAutocorrelation(pkList, n, aLevel);
        assertEquals(expResult, result);
    }
    
}
