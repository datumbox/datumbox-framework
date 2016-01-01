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
package com.datumbox.framework.statistics.decisiontheory;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.DataTable2D;
import com.datumbox.tests.bases.BaseTest;

import java.util.AbstractMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class DecisionCriteriaTest extends BaseTest {
    
    /**
     * Generates an example PayoffMatrix.
     * 
     * @return 
     */
    private DataTable2D generatePayoffMatrix() {
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
    
    /**
     * Generates the example Event Probabilities.
     * 
     * @return 
     */
    private AssociativeArray generateEventProbabilities() {
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
        logger.info("maxMin");
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
        logger.info("maxMax");
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
        logger.info("savage");
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
        logger.info("laplace");
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
        logger.info("hurwiczAlpha");
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
        logger.info("maximumLikelihood");
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
        logger.info("bayes");
        DataTable2D payoffMatrix = generatePayoffMatrix();
        AssociativeArray eventProbabilities = generateEventProbabilities();
        Map.Entry<Object, Object> expResult = new AbstractMap.SimpleEntry<>("A2", 450.0);
        Map.Entry<Object, Object> result = DecisionCriteria.bayes(payoffMatrix, eventProbabilities);
        assertEquals(expResult, result);
    }
    
}
