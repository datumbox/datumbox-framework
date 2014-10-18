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
package com.datumbox.framework.statistics.decisiontheory;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.DataTable2D;
import java.util.AbstractMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class DecisionCriteriaTest {
    
    public DecisionCriteriaTest() {
    }
    
    protected DataTable2D generatePayoffMatrix() {
        //Example from Dimakis' Notes on Decision Theory
        DataTable2D payoffMatrix = new DataTable2D();
        payoffMatrix.put2d("E1", "A1", 400.0);
        payoffMatrix.put2d("E1", "A2", 100.0);
        payoffMatrix.put2d("E1", "A3", -200.0);
        payoffMatrix.put2d("E1", "A4", -500.0);
        payoffMatrix.put2d("E2", "A1", 400.0);
        payoffMatrix.put2d("E2", "A2", 600.0);
        payoffMatrix.put2d("E2", "A3", 300.0);
        payoffMatrix.put2d("E2", "A4", 0.0);
        payoffMatrix.put2d("E3", "A1", 400.0);
        payoffMatrix.put2d("E3", "A2", 600.0);
        payoffMatrix.put2d("E3", "A3", 800.0);
        payoffMatrix.put2d("E3", "A4", 500.0);
        payoffMatrix.put2d("E4", "A1", 400.0);
        payoffMatrix.put2d("E4", "A2", 600.0);
        payoffMatrix.put2d("E4", "A3", 800.0);
        payoffMatrix.put2d("E4", "A4", 1000.0);
        
        return payoffMatrix;
    }
    
    protected AssociativeArray generateEventProbabilities() {
        AssociativeArray eventProbabilities = new AssociativeArray();
        eventProbabilities.put("E1", 0.3);
        eventProbabilities.put("E2", 0.4);
        eventProbabilities.put("E3", 0.2);
        eventProbabilities.put("E4", 0.1);
        
        return eventProbabilities;
    }
    
    /**
     * Test of maxMin method, of class DecisionCriteria.
     */
    @Test
    public void testMaxMin() {
        System.out.println("maxMin");
        DataTable2D payoffMatrix = generatePayoffMatrix();
        Map.Entry<Object, Object> expResult = new AbstractMap.SimpleEntry<>("A1", 400.0);
        Map.Entry<Object, Object> result = DecisionCriteria.maxMin(payoffMatrix);
        assertEquals(expResult, result);
    }

    /**
     * Test of maxMax method, of class DecisionCriteria.
     */
    @Test
    public void testMaxMax() {
        System.out.println("maxMax");
        DataTable2D payoffMatrix = generatePayoffMatrix();
        Map.Entry<Object, Object> expResult = new AbstractMap.SimpleEntry<>("A4", 1000.0);
        Map.Entry<Object, Object> result = DecisionCriteria.maxMax(payoffMatrix);
        assertEquals(expResult, result);
    }

    /**
     * Test of savage method, of class DecisionCriteria.
     */
    @Test
    public void testSavage() {
        System.out.println("savage");
        DataTable2D payoffMatrix = generatePayoffMatrix();
        Map.Entry<Object, Object> expResult = new AbstractMap.SimpleEntry<>("A2", -400.0);
        Map.Entry<Object, Object> result = DecisionCriteria.savage(payoffMatrix);
        assertEquals(expResult, result);
    }

    /**
     * Test of laplace method, of class DecisionCriteria.
     */
    @Test
    public void testLaplace() {
        System.out.println("laplace");
        DataTable2D payoffMatrix = generatePayoffMatrix();
        Map.Entry<Object, Object> expResult = new AbstractMap.SimpleEntry<>("A2", 475.0);
        Map.Entry<Object, Object> result = DecisionCriteria.laplace(payoffMatrix);
        assertEquals(expResult, result);
    }

    /**
     * Test of hurwiczAlpha method, of class DecisionCriteria.
     */
    @Test
    public void testHurwiczAlpha() {
        System.out.println("hurwiczAlpha");
        DataTable2D payoffMatrix = generatePayoffMatrix();
        double alpha = 0.5;
        Map.Entry<Object, Object> expResult = new AbstractMap.SimpleEntry<>("A1", 400.0);
        Map.Entry<Object, Object> result = DecisionCriteria.hurwiczAlpha(payoffMatrix, alpha);
        assertEquals(expResult, result);
    }

    /**
     * Test of maximumLikelihood method, of class DecisionCriteria.
     */
    @Test
    public void testMaximumLikelihood() {
        System.out.println("maximumLikelihood");
        DataTable2D payoffMatrix = generatePayoffMatrix();
        AssociativeArray eventProbabilities = generateEventProbabilities();
        Map.Entry<Object, Object> expResult = new AbstractMap.SimpleEntry<>("A2", 600.0);
        Map.Entry<Object, Object> result = DecisionCriteria.maximumLikelihood(payoffMatrix, eventProbabilities);
        assertEquals(expResult, result);
    }

    /**
     * Test of bayes method, of class DecisionCriteria.
     */
    @Test
    public void testBayes() {
        System.out.println("bayes");
        DataTable2D payoffMatrix = generatePayoffMatrix();
        AssociativeArray eventProbabilities = generateEventProbabilities();
        Map.Entry<Object, Object> expResult = new AbstractMap.SimpleEntry<>("A2", 450.0);
        Map.Entry<Object, Object> result = DecisionCriteria.bayes(payoffMatrix, eventProbabilities);
        assertEquals(expResult, result);
    }
    
}
