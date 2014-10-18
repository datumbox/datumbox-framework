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
package com.datumbox.framework.statistics.descriptivestatistics;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.DataTable2D;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.tests.utilities.TestUtils;
import org.junit.Test;

/**
 *
 * @author bbriniotis
 */
public class BivariateTest {
    
    public BivariateTest() {
    }

    private Dataset generateDataset() {
        Dataset dataset = new Dataset();
        
        Record r1 = new Record();
        r1.getX().put(0, 6);
        r1.getX().put(1, 5);
        r1.getX().put(2, 3);
        r1.getX().put(3, 4);
        dataset.add(r1);
        
        Record r2 = new Record();
        r2.getX().put(0, 7);
        r2.getX().put(1, 3);
        r2.getX().put(2, 2);
        r2.getX().put(3, 2);
        dataset.add(r2);
        
        Record r3 = new Record();
        r3.getX().put(0, 6);
        r3.getX().put(1, 4);
        r3.getX().put(2, 4);
        r3.getX().put(3, 5);
        dataset.add(r3);
        
        Record r4 = new Record();
        r4.getX().put(0, 5);
        r4.getX().put(1, 7);
        r4.getX().put(2, 1);
        r4.getX().put(3, 3);
        dataset.add(r4);
        
        Record r5 = new Record();
        r5.getX().put(0, 7);
        r5.getX().put(1, 7);
        r5.getX().put(2, 5);
        r5.getX().put(3, 5);
        dataset.add(r5);
        
        Record r6 = new Record();
        r6.getX().put(0, 6);
        r6.getX().put(1, 4);
        r6.getX().put(2, 2);
        r6.getX().put(3, 3);
        dataset.add(r6);
        
        Record r7 = new Record();
        r7.getX().put(0, 5);
        r7.getX().put(1, 7);
        r7.getX().put(2, 2);
        r7.getX().put(3, 1);
        dataset.add(r7);
        
        Record r8 = new Record();
        r8.getX().put(0, 6);
        r8.getX().put(1, 5);
        r8.getX().put(2, 4);
        r8.getX().put(3, 4);
        dataset.add(r8);
        
        Record r9 = new Record();
        r9.getX().put(0, 3);
        r9.getX().put(1, 5);
        r9.getX().put(2, 6);
        r9.getX().put(3, 7);
        dataset.add(r9);
        
        Record r10 = new Record();
        r10.getX().put(0, 1);
        r10.getX().put(1, 3);
        r10.getX().put(2, 7);
        r10.getX().put(3, 5);
        dataset.add(r10);
        
        Record r11 = new Record();
        r11.getX().put(0, 2);
        r11.getX().put(1, 6);
        r11.getX().put(2, 6);
        r11.getX().put(3, 7);
        dataset.add(r11);
        
        Record r12 = new Record();
        r12.getX().put(0, 5);
        r12.getX().put(1, 7);
        r12.getX().put(2, 7);
        r12.getX().put(3, 6);
        dataset.add(r12);
        
        Record r13 = new Record();
        r13.getX().put(0, 2);
        r13.getX().put(1, 4);
        r13.getX().put(2, 5);
        r13.getX().put(3, 6);
        dataset.add(r13);
        
        Record r14 = new Record();
        r14.getX().put(0, 3);
        r14.getX().put(1, 5);
        r14.getX().put(2, 6);
        r14.getX().put(3, 5);
        dataset.add(r14);
        
        Record r15 = new Record();
        r15.getX().put(0, 1);
        r15.getX().put(1, 6);
        r15.getX().put(2, 5);
        r15.getX().put(3, 5);
        dataset.add(r15);
        
        Record r16 = new Record();
        r16.getX().put(0, 2);
        r16.getX().put(1, 3);
        r16.getX().put(2, 7);
        r16.getX().put(3, 7);
        dataset.add(r16);
        
        return dataset;
    }
    
    /**
     * Test of covarianceMatrix method, of class Bivariate.
     */
    @Test
    public void testCovarianceMatrix() {
        System.out.println("covarianceMatrix");
        Dataset dataSet = generateDataset();
        DataTable2D expResult = new DataTable2D();
        expResult.put2d(0, 0, 4.5625);
        expResult.put2d(0, 1, 0.5875);
        expResult.put2d(0, 2, -2.7);
        expResult.put2d(0, 3, -2.2041666666667);
        expResult.put2d(1, 0, 0.5875);
        expResult.put2d(1, 1, 2.1958333333333);
        expResult.put2d(1, 2, -0.43333333333333);
        expResult.put2d(1, 3, -0.3125);
        expResult.put2d(2, 0, -2.7);
        expResult.put2d(2, 1, -0.43333333333333);
        expResult.put2d(2, 2, 4.0);
        expResult.put2d(2, 3, 3.0333333333333);
        expResult.put2d(3, 0, -2.2041666666667);
        expResult.put2d(3, 1, -0.3125);
        expResult.put2d(3, 2, 3.0333333333333);
        expResult.put2d(3, 3, 3.1625);
        
        DataTable2D result = Bivariate.covarianceMatrix(dataSet);
        TestUtils.assertDoubleDataTable2D(expResult, result);
    }

    /**
     * Test of pearsonMatrix method, of class Bivariate.
     */
    @Test
    public void testPearsonMatrix() {
        System.out.println("pearsonMatrix");
        Dataset dataSet = generateDataset();
        DataTable2D expResult = new DataTable2D();
        expResult.put2d(0, 0, 1.0);
        expResult.put2d(0, 1, 0.18561229707779);
        expResult.put2d(0, 2, -0.63202219485911);
        expResult.put2d(0, 3, -0.58026680188263);
        expResult.put2d(1, 0, 0.18561229707779);
        expResult.put2d(1, 1, 1.0);
        expResult.put2d(1, 2, -0.14621516381791);
        expResult.put2d(1, 3, -0.11858644989229);
        expResult.put2d(2, 0, -0.63202219485911);
        expResult.put2d(2, 1, -0.14621516381791);
        expResult.put2d(2, 2, 1.0);
        expResult.put2d(2, 3, 0.85285436162523);
        expResult.put2d(3, 0, -0.58026680188263);
        expResult.put2d(3, 1, -0.11858644989229);
        expResult.put2d(3, 2, 0.85285436162523);
        expResult.put2d(3, 3, 1.0);
        
        DataTable2D result = Bivariate.pearsonMatrix(dataSet);
        TestUtils.assertDoubleDataTable2D(expResult, result);
    }

    /**
     * Test of spearmanMatrix method, of class Bivariate.
     */
    @Test
    public void testSpearmanMatrix() {
        System.out.println("spearmanMatrix");
        Dataset dataSet = generateDataset();
        DataTable2D expResult = new DataTable2D();
        expResult.put(0, new AssociativeArray());
        expResult.put2d(0, 0, 1.0);
        expResult.put2d(0, 1, 0.10229198378533);
        expResult.put2d(0, 2, -0.60665935791938);
        expResult.put2d(0, 3, -0.56631689758552);
        expResult.put2d(1, 0, 0.10229198378533);
        expResult.put2d(1, 1, 1.0);
        expResult.put2d(1, 2, -0.14688588181833);
        expResult.put2d(1, 3, -0.087423415709411);
        expResult.put2d(2, 0, -0.60665935791938);
        expResult.put2d(2, 1, -0.14688588181833);
        expResult.put2d(2, 2, 1.0);
        expResult.put2d(2, 3, 0.8472888999181);
        expResult.put2d(3, 0, -0.56631689758552);
        expResult.put2d(3, 1, -0.087423415709411);
        expResult.put2d(3, 2, 0.8472888999181);
        expResult.put2d(3, 3, 1.0);
        
        DataTable2D result = Bivariate.spearmanMatrix(dataSet);
        TestUtils.assertDoubleDataTable2D(expResult, result);
    }

    /**
     * Test of kendalltauMatrix method, of class Bivariate.
     */
    @Test
    public void testKendalltauMatrix() {
        System.out.println("kendalltauMatrix");
        Dataset dataSet = generateDataset();
        DataTable2D expResult = new DataTable2D();
        expResult.put2d(0, 0, 1.0);
        expResult.put2d(0, 1, 0.066666666666667);
        expResult.put2d(0, 2, -0.36666666666667);
        expResult.put2d(0, 3, -0.35);
        expResult.put2d(1, 0, 0.066666666666667);
        expResult.put2d(1, 1, 1.0);
        expResult.put2d(1, 2, -0.083333333333333);
        expResult.put2d(1, 3, -0.05);
        expResult.put2d(2, 0, -0.36666666666667);
        expResult.put2d(2, 1, -0.083333333333333);
        expResult.put2d(2, 2, 1.0);
        expResult.put2d(2, 3, 0.64166666666667);
        expResult.put2d(3, 0, -0.35);
        expResult.put2d(3, 1, -0.05);
        expResult.put2d(3, 2, 0.64166666666667);
        expResult.put2d(3, 3, 1.0);
        
        DataTable2D result = Bivariate.kendalltauMatrix(dataSet);
        TestUtils.assertDoubleDataTable2D(expResult, result);
    }
    
}
