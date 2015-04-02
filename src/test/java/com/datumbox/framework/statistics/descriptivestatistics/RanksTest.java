/*
 * Copyright (C) 2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
package com.datumbox.framework.statistics.descriptivestatistics;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.tests.utilities.TestUtils;
import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListMap;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class RanksTest {
    
    public RanksTest() {
    }


    /**
     * Test of getRanksFromValues method, of class Dataset.
     */
    @Test
    public void testGetRanksFromValues() {
        TestUtils.log(this.getClass(), "getRanksFromValues");
        FlatDataList flatDataCollection =  new FlatDataList(Arrays.asList(new Object[]{50,10,10,30,40}));
        FlatDataList expResult = new FlatDataList(Arrays.asList(new Object[]{5.0,1.5,1.5,3.0,4.0}));
        AssociativeArray expResult2 = new AssociativeArray(new ConcurrentSkipListMap<>());
        expResult2.put(10, 2);
        AssociativeArray tiesCounter = Ranks.getRanksFromValues(flatDataCollection);
        assertEquals(expResult, flatDataCollection);
        assertEquals(expResult2, tiesCounter);
    }
    
}
