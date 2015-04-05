/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
import com.datumbox.common.utilities.TypeConversions;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.tests.utilities.TestUtils;
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
        TestUtils.log(this.getClass(), "sum");
        DataTable2D classifierClassProbabilityMatrix = getClassifierClassProbabilityMatrix();
        
        AssociativeArray expResult = new AssociativeArray();
        expResult.put("class1", 0.933);
        expResult.put("class2", 1.833);
        expResult.put("class3", 1.233);
        
        AssociativeArray result = FixedCombinationRules.sum(classifierClassProbabilityMatrix);
        for(Object k: expResult.keySet()) {
            assertEquals(TypeConversions.toDouble(expResult.get(k)), TypeConversions.toDouble(result.get(k)), TestConfiguration.DOUBLE_ACCURACY_HIGH);
        }
    }

    /**
     * Test of average method, of class FixedCombinationRules.
     */
    @Test
    public void testAverage() {
        TestUtils.log(this.getClass(), "average");
        DataTable2D classifierClassProbabilityMatrix = getClassifierClassProbabilityMatrix();
        
        AssociativeArray expResult = new AssociativeArray();
        expResult.put("class1", 0.23325);
        expResult.put("class2", 0.45825);
        expResult.put("class3", 0.30825);
        
        AssociativeArray result = FixedCombinationRules.average(classifierClassProbabilityMatrix);
        for(Object k: expResult.keySet()) {
            assertEquals(TypeConversions.toDouble(expResult.get(k)), TypeConversions.toDouble(result.get(k)), TestConfiguration.DOUBLE_ACCURACY_HIGH);
        }
        
    }

    /**
     * Test of weightedAverage method, of class FixedCombinationRules.
     */
    @Test
    public void testWeightedAverage() {
        TestUtils.log(this.getClass(), "weightedAverage");
        DataTable2D classifierClassProbabilityMatrix = getClassifierClassProbabilityMatrix();
        AssociativeArray classifierWeights = new AssociativeArray();
        classifierWeights.put("Classifier1", 0.5);
        classifierWeights.put("Classifier2", 0.3);
        classifierWeights.put("Classifier3", 0.1);
        classifierWeights.put("Classifier4", 0.1);

        AssociativeArray expResult = new AssociativeArray();
        expResult.put("class1", 0.1733);
        expResult.put("class2", 0.5033);
        expResult.put("class3", 0.3233);
        
        AssociativeArray result = FixedCombinationRules.weightedAverage(classifierClassProbabilityMatrix, classifierWeights);
        for(Object k: expResult.keySet()) {
            assertEquals(TypeConversions.toDouble(expResult.get(k)), TypeConversions.toDouble(result.get(k)), TestConfiguration.DOUBLE_ACCURACY_HIGH);
        }
    }

    /**
     * Test of median method, of class FixedCombinationRules.
     */
    @Test
    public void testMedian() {
        TestUtils.log(this.getClass(), "median");
        DataTable2D classifierClassProbabilityMatrix = getClassifierClassProbabilityMatrix();
        
        AssociativeArray expResult = new AssociativeArray();
        expResult.put("class1", 0.2665);
        expResult.put("class2", 0.45);
        expResult.put("class3", 0.3165);
        
        AssociativeArray result = FixedCombinationRules.median(classifierClassProbabilityMatrix);
        for(Object k: expResult.keySet()) {
            assertEquals(TypeConversions.toDouble(expResult.get(k)), TypeConversions.toDouble(result.get(k)), TestConfiguration.DOUBLE_ACCURACY_HIGH);
        }
    }

    /**
     * Test of maximum method, of class FixedCombinationRules.
     */
    @Test
    public void testMaximum() {
        TestUtils.log(this.getClass(), "maximum");
        DataTable2D classifierClassProbabilityMatrix = getClassifierClassProbabilityMatrix();
        
        AssociativeArray expResult = new AssociativeArray();
        expResult.put("class1", 0.4);
        expResult.put("class2", 0.6);
        expResult.put("class3", 0.4);
        
        AssociativeArray result = FixedCombinationRules.maximum(classifierClassProbabilityMatrix);
        for(Object k: expResult.keySet()) {
            assertEquals(TypeConversions.toDouble(expResult.get(k)), TypeConversions.toDouble(result.get(k)), TestConfiguration.DOUBLE_ACCURACY_HIGH);
        }
    }

    /**
     * Test of minimum method, of class FixedCombinationRules.
     */
    @Test
    public void testMinimum() {
        TestUtils.log(this.getClass(), "minimum");
        DataTable2D classifierClassProbabilityMatrix = getClassifierClassProbabilityMatrix();
        
        AssociativeArray expResult = new AssociativeArray();
        expResult.put("class1", 0.0);
        expResult.put("class2", 0.333);
        expResult.put("class3", 0.2);
        
        AssociativeArray result = FixedCombinationRules.minimum(classifierClassProbabilityMatrix);
        for(Object k: expResult.keySet()) {
            assertEquals(TypeConversions.toDouble(expResult.get(k)), TypeConversions.toDouble(result.get(k)), TestConfiguration.DOUBLE_ACCURACY_HIGH);
        }
    }

    /**
     * Test of product method, of class FixedCombinationRules.
     */
    @Test
    public void testProduct() {
        TestUtils.log(this.getClass(), "product");
        DataTable2D classifierClassProbabilityMatrix = getClassifierClassProbabilityMatrix();
        
        AssociativeArray expResult = new AssociativeArray();
        expResult.put("class1", 0.0);
        expResult.put("class2", 0.03996);
        expResult.put("class3", 0.007992);
        
        AssociativeArray result = FixedCombinationRules.product(classifierClassProbabilityMatrix);
        for(Object k: expResult.keySet()) {
            assertEquals(TypeConversions.toDouble(expResult.get(k)), TypeConversions.toDouble(result.get(k)), TestConfiguration.DOUBLE_ACCURACY_HIGH);
        }
    }

    /**
     * Test of majorityVote method, of class FixedCombinationRules.
     */
    @Test
    public void testMajorityVote() {
        TestUtils.log(this.getClass(), "majorityVote");
        DataTable2D classifierClassProbabilityMatrix = getClassifierClassProbabilityMatrix();
        
        AssociativeArray expResult = new AssociativeArray();
        expResult.put("class1", 2.0);
        expResult.put("class2", 2.0);
        expResult.put("class3", 0.0);
        
        AssociativeArray result = FixedCombinationRules.majorityVote(classifierClassProbabilityMatrix);
        for(Object k: expResult.keySet()) {
            assertEquals(TypeConversions.toDouble(expResult.get(k)), TypeConversions.toDouble(result.get(k)), TestConfiguration.DOUBLE_ACCURACY_HIGH);
        }
    }
    
}
