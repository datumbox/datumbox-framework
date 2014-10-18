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
package com.datumbox.framework.statistics.anova;

import com.datumbox.common.dataobjects.AssociativeArray2D;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.dataobjects.TransposeDataCollection;
import com.datumbox.common.dataobjects.TransposeDataCollection2D;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class AnovaTest {
    
    public AnovaTest() {
    }

    /**
     * Test of oneWayTestEqualVars method, of class Anova.
     */
    @Test
    public void testOneWayTestEqualVars() {
        System.out.println("oneWayTestEqualVars");
        TransposeDataCollection transposeDataCollection = new TransposeDataCollection();
        transposeDataCollection.put(0, new FlatDataCollection(Arrays.asList(new Object[]{86,79,81,70,84})));
        transposeDataCollection.put(1, new FlatDataCollection(Arrays.asList(new Object[]{90,76,88,82,89})));
        transposeDataCollection.put(2, new FlatDataCollection(Arrays.asList(new Object[]{82,68,73,71,81})));
        
        double aLevel = 0.05;
        AssociativeArray2D outputTable = new AssociativeArray2D();
        boolean expResult = false;
        boolean result = Anova.oneWayTestEqualVars(transposeDataCollection, aLevel, outputTable);
        assertEquals(expResult, result);
    }

    /**
     * Test of oneWayTestNotEqualVars method, of class Anova.
     */
    @Test
    public void testOneWayTestNotEqualVars() {
        System.out.println("oneWayTestNotEqualVars");
        TransposeDataCollection transposeDataCollection = new TransposeDataCollection();
        transposeDataCollection.put(0, new FlatDataCollection(Arrays.asList(new Object[]{86,79,81,70,84})));
        transposeDataCollection.put(1, new FlatDataCollection(Arrays.asList(new Object[]{90,76,88,82,89})));
        transposeDataCollection.put(2, new FlatDataCollection(Arrays.asList(new Object[]{82,68,73,71,81})));
        
        double aLevel = 0.05;
        AssociativeArray2D outputTable = new AssociativeArray2D();
        boolean expResult = false;
        boolean result = Anova.oneWayTestNotEqualVars(transposeDataCollection, aLevel, outputTable);
        assertEquals(expResult, result);
    }

    /**
     * Test of twoWayTestEqualCellsEqualVars method, of class Anova.
     */
    @Test
    public void testTwoWayTestEqualCellsEqualVars() {
        System.out.println("twoWayTestEqualCellsEqualVars");
        
        TransposeDataCollection transposeDataCollection1 = new TransposeDataCollection();
        transposeDataCollection1.put("StrainBright",  new FlatDataCollection(Arrays.asList(new Object[]{26,14,41,16,28,29,92,31})));
        transposeDataCollection1.put("StrainMixed",  new FlatDataCollection(Arrays.asList(new Object[]{41,82,26,86,19,45,59,37})));
        transposeDataCollection1.put("StrainDull",  new FlatDataCollection(Arrays.asList(new Object[]{36,87,39,99,59,126,27,104})));
        
        TransposeDataCollection transposeDataCollection2 = new TransposeDataCollection();
        transposeDataCollection2.put("StrainBright",  new FlatDataCollection(Arrays.asList(new Object[]{51,35,96,36,97,28,22,76})));
        transposeDataCollection2.put("StrainMixed",  new FlatDataCollection(Arrays.asList(new Object[]{39,114,104,92,130,87,122,64})));
        transposeDataCollection2.put("StrainDull",  new FlatDataCollection(Arrays.asList(new Object[]{42,133,92,144,156,68,144,142})));
        
        TransposeDataCollection2D twoFactorDataCollection = new TransposeDataCollection2D();
        twoFactorDataCollection.put("EnviromentFree", transposeDataCollection1);
        twoFactorDataCollection.put("EnviromentRestricted", transposeDataCollection2);
        
        double aLevel = 0.05;
        AssociativeArray2D outputTable = new AssociativeArray2D();
        boolean expResult = true;
        boolean result = Anova.twoWayTestEqualCellsEqualVars(twoFactorDataCollection, aLevel, outputTable);
        assertEquals(expResult, result);
    }

}
