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
package com.datumbox.framework.machinelearning.ensemblelearning;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.DataTable2D;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class FixedCombinationRulesTest {
    
    public FixedCombinationRulesTest() {
    }
    
    private DataTable2D getClassifierClassProbabilityMatrix() {
        DataTable2D d = new DataTable2D();
        d.put("Classifier1", new AssociativeArray());
        d.get("Classifier1").put("class1", 0.2);
        d.get("Classifier1").put("class2", 0.5);
        d.get("Classifier1").put("class3", 0.3);
        
        d.put("Classifier2", new AssociativeArray());
        d.get("Classifier2").put("class1", 0.0);
        d.get("Classifier2").put("class2", 0.6);
        d.get("Classifier2").put("class3", 0.4);
        
        d.put("Classifier3", new AssociativeArray());
        d.get("Classifier3").put("class1", 0.4);
        d.get("Classifier3").put("class2", 0.4);
        d.get("Classifier3").put("class3", 0.2);
        
        d.put("Classifier4", new AssociativeArray());
        d.get("Classifier4").put("class1", 0.333);
        d.get("Classifier4").put("class2", 0.333);
        d.get("Classifier4").put("class3", 0.333);
        
        return d;
    }

    /**
     * Test of sum method, of class FixedCombinationRules.
     */
    @Test
    public void testSum() {
        System.out.println("sum");
        DataTable2D classifierClassProbabilityMatrix = getClassifierClassProbabilityMatrix();
        
        AssociativeArray expResult = new AssociativeArray();
        expResult.put("class2", 1.8330000000000002);
        expResult.put("class3", 1.233);
        expResult.put("class1", 0.933);
        
        AssociativeArray result = FixedCombinationRules.sum(classifierClassProbabilityMatrix);
        assertEquals(expResult, result);
    }

    /**
     * Test of average method, of class FixedCombinationRules.
     */
    @Test
    public void testAverage() {
        System.out.println("average");
        DataTable2D classifierClassProbabilityMatrix = getClassifierClassProbabilityMatrix();
        
        AssociativeArray expResult = new AssociativeArray();
        expResult.put("class2", 0.45825000000000005);
        expResult.put("class3", 0.30825);
        expResult.put("class1", 0.23325);
        
        AssociativeArray result = FixedCombinationRules.average(classifierClassProbabilityMatrix);
        assertEquals(expResult, result);
    }

    /**
     * Test of weightedAverage method, of class FixedCombinationRules.
     */
    @Test
    public void testWeightedAverage() {
        System.out.println("weightedAverage");
        DataTable2D classifierClassProbabilityMatrix = getClassifierClassProbabilityMatrix();
        AssociativeArray classifierWeights = new AssociativeArray();
        classifierWeights.put("Classifier1", 0.5);
        classifierWeights.put("Classifier2", 0.3);
        classifierWeights.put("Classifier3", 0.1);
        classifierWeights.put("Classifier4", 0.1);

        AssociativeArray expResult = new AssociativeArray();
        expResult.put("class2", 0.5033);
        expResult.put("class3", 0.32330000000000003);
        expResult.put("class1", 0.1733);
        
        AssociativeArray result = FixedCombinationRules.weightedAverage(classifierClassProbabilityMatrix, classifierWeights);
        assertEquals(expResult, result);
    }

    /**
     * Test of median method, of class FixedCombinationRules.
     */
    @Test
    public void testMedian() {
        System.out.println("median");
        DataTable2D classifierClassProbabilityMatrix = getClassifierClassProbabilityMatrix();
        
        AssociativeArray expResult = new AssociativeArray();
        expResult.put("class2", 0.45);
        expResult.put("class3", 0.3165);
        expResult.put("class1", 0.2665);
        
        AssociativeArray result = FixedCombinationRules.median(classifierClassProbabilityMatrix);
        assertEquals(expResult, result);
    }

    /**
     * Test of maximum method, of class FixedCombinationRules.
     */
    @Test
    public void testMaximum() {
        System.out.println("maximum");
        DataTable2D classifierClassProbabilityMatrix = getClassifierClassProbabilityMatrix();
        
        AssociativeArray expResult = new AssociativeArray();
        expResult.put("class2", 0.6);
        expResult.put("class3", 0.4);
        expResult.put("class1", 0.4);
        
        AssociativeArray result = FixedCombinationRules.maximum(classifierClassProbabilityMatrix);
        assertEquals(expResult, result);
    }

    /**
     * Test of minimum method, of class FixedCombinationRules.
     */
    @Test
    public void testMinimum() {
        System.out.println("minimum");
        DataTable2D classifierClassProbabilityMatrix = getClassifierClassProbabilityMatrix();
        
        AssociativeArray expResult = new AssociativeArray();
        expResult.put("class2", 0.333);
        expResult.put("class3", 0.2);
        expResult.put("class1", 0.0);
        
        AssociativeArray result = FixedCombinationRules.minimum(classifierClassProbabilityMatrix);
        assertEquals(expResult, result);
    }

    /**
     * Test of product method, of class FixedCombinationRules.
     */
    @Test
    public void testProduct() {
        System.out.println("product");
        DataTable2D classifierClassProbabilityMatrix = getClassifierClassProbabilityMatrix();
        
        AssociativeArray expResult = new AssociativeArray();
        expResult.put("class2", 0.03996);
        expResult.put("class3", 0.007992);
        expResult.put("class1", 0.0);
        
        AssociativeArray result = FixedCombinationRules.product(classifierClassProbabilityMatrix);
        assertEquals(expResult, result);
    }

    /**
     * Test of majorityVote method, of class FixedCombinationRules.
     */
    @Test
    public void testMajorityVote() {
        System.out.println("majorityVote");
        DataTable2D classifierClassProbabilityMatrix = getClassifierClassProbabilityMatrix();
        
        AssociativeArray expResult = new AssociativeArray();
        expResult.put("class2", 3.0);
        expResult.put("class3", 1.0);
        expResult.put("class1", 0.0);
        
        AssociativeArray result = FixedCombinationRules.majorityVote(classifierClassProbabilityMatrix);
        assertEquals(expResult, result);
    }
    
}
