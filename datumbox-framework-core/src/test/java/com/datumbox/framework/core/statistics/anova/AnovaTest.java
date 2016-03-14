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
package com.datumbox.framework.core.statistics.anova;

import com.datumbox.framework.common.dataobjects.AssociativeArray2D;
import com.datumbox.framework.common.dataobjects.FlatDataCollection;
import com.datumbox.framework.common.dataobjects.TransposeDataCollection;
import com.datumbox.framework.common.dataobjects.TransposeDataCollection2D;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for Anova.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class AnovaTest extends AbstractTest {

    /**
     * Test of oneWayTestEqualVars method, of class Anova.
     */
    @Test
    public void testOneWayTestEqualVars() {
        logger.info("oneWayTestEqualVars");
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
        logger.info("oneWayTestNotEqualVars");
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
        logger.info("twoWayTestEqualCellsEqualVars");
        
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
