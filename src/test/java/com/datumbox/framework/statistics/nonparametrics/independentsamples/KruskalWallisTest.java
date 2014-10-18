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

import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.dataobjects.TransposeDataCollection;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class KruskalWallisTest {
    
    public KruskalWallisTest() {
    }

    /**
     * Test of test method, of class KruskalWallis.
     */
    @Test
    public void testTest() {
        System.out.println("test");
        TransposeDataCollection transposeDataCollection = new TransposeDataCollection();
        transposeDataCollection.put(0, new FlatDataCollection(Arrays.asList(new Object[]{82,93,86,87,99,95,93,89,96})));
        transposeDataCollection.put(1, new FlatDataCollection(Arrays.asList(new Object[]{81,85,93,91,84,88,84,92,81,92})));
        transposeDataCollection.put(2, new FlatDataCollection(Arrays.asList(new Object[]{97,85,83,93,88,86,90,94,87,93})));
        transposeDataCollection.put(3, new FlatDataCollection(Arrays.asList(new Object[]{93,89,94,96,81,84,80,84,92})));        
        double aLevel = 0.05;
        boolean expResult = false;
        boolean result = KruskalWallis.test(transposeDataCollection, aLevel);
        assertEquals(expResult, result);
    }
    
}
