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
package com.datumbox.framework.statistics.descriptivestatistics;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.tests.abstracts.AbstractTest;
import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListMap;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for Ranks.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class RanksTest extends AbstractTest {

    /**
     * Test of getRanksFromValues method, of class Ranks.
     */
    @Test
    public void testGetRanksFromValues() {
        logger.info("getRanksFromValues");
        FlatDataList flatDataCollection =  new FlatDataList(Arrays.asList(new Object[]{50,10,10,30,40}));
        FlatDataList expResult = new FlatDataList(Arrays.asList(new Object[]{5.0,1.5,1.5,3.0,4.0}));
        AssociativeArray expResult2 = new AssociativeArray(new ConcurrentSkipListMap<>());
        expResult2.put(10, 2);
        AssociativeArray tiesCounter = Ranks.getRanksFromValues(flatDataCollection);
        assertEquals(expResult, flatDataCollection);
        assertEquals(expResult2, tiesCounter);
    }
    
}
