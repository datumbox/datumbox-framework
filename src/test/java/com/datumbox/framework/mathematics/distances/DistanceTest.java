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
package com.datumbox.framework.mathematics.distances;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.configuration.TestConfiguration;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class DistanceTest {
    
    public DistanceTest() {
    }
    
    private Map<Object, Object> getMap1() {
        Map<Object, Object> map = new HashMap<>();
        map.put(0, 1.0);
        map.put(1, "cat1");
        map.put(2, true);
        map.put(3, (short)7);
        map.put(4, "same");
        
        return map;
    }
    
    private Map<Object, Object> getMap2() {
        Map<Object, Object> map = new HashMap<>();
        map.put(0, 3.0);
        map.put(1, "cat2");
        map.put(2, false);
        map.put(3, (short)3);
        map.put(4, "same");
        
        return map;
    }
    
    private Map<Object, Double> getWeights() {
        Map<Object, Double> weights = new HashMap<>();
        weights.put(0, 0.5);
        weights.put(1, 1.0);
        weights.put(2, 2.0);
        weights.put(3, 1.0);
        weights.put(4, 0.0);
        
        return weights;
    }
    
    /**
     * Test of euclidean method, of class Distance.
     */
    @Test
    public void testEuclidean() {
        System.out.println("euclidean");
        AssociativeArray a1 = new AssociativeArray(getMap1());
        AssociativeArray a2 = new AssociativeArray(getMap2());
        double expResult = 2.6457513110645905905016157536393;
        double result = Distance.euclidean(a1, a2);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of euclideanWeighhted method, of class Distance.
     */
    @Test
    public void testEuclideanWeighhted() {
        System.out.println("euclideanWeighhted");
        AssociativeArray a1 = new AssociativeArray(getMap1());
        AssociativeArray a2 = new AssociativeArray(getMap2());
        Map<Object, Double> columnWeights = getWeights();
        double expResult = 2.449489742783178;
        double result = Distance.euclideanWeighhted(a1, a2, columnWeights);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of manhattan method, of class Distance.
     */
    @Test
    public void testManhattan() {
        System.out.println("manhattan");
        AssociativeArray a1 = new AssociativeArray(getMap1());
        AssociativeArray a2 = new AssociativeArray(getMap2());
        double expResult = 5.0;
        double result = Distance.manhattan(a1, a2);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of manhattanWeighhted method, of class Distance.
     */
    @Test
    public void testManhattanWeighhted() {
        System.out.println("manhattanWeighhted");
        AssociativeArray a1 = new AssociativeArray(getMap1());
        AssociativeArray a2 = new AssociativeArray(getMap2());
        Map<Object, Double> columnWeights = getWeights();
        double expResult = 5.0;
        double result = Distance.manhattanWeighhted(a1, a2, columnWeights);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of maximum method, of class Distance.
     */
    @Test
    public void testMaximum() {
        System.out.println("maximum");
        AssociativeArray a1 = new AssociativeArray(getMap1());
        AssociativeArray a2 = new AssociativeArray(getMap2());
        double expResult = 2.0;
        double result = Distance.maximum(a1, a2);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }
    
}
