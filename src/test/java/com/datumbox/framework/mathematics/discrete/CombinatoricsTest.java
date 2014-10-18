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
package com.datumbox.framework.mathematics.discrete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class CombinatoricsTest {
    
    public CombinatoricsTest() {
    }

    /**
     * Test of permutations method, of class Combinatorics.
     */
    @Test
    public void testPermutations() {
        System.out.println("permutations");
        Collection<List<String>> expResult = new ArrayList<>();
        expResult.add(new ArrayList<>(Arrays.asList("a","b","c")));
        expResult.add(new ArrayList<>(Arrays.asList("a","c","b")));
        expResult.add(new ArrayList<>(Arrays.asList("c","a","b")));
        expResult.add(new ArrayList<>(Arrays.asList("c","b","a")));
        expResult.add(new ArrayList<>(Arrays.asList("b","c","a")));
        expResult.add(new ArrayList<>(Arrays.asList("b","a","c")));
        Collection<List<String>> result = Combinatorics.<String>permutations(new ArrayList<>(Arrays.asList("a","b","c")));
        assertEquals(expResult, result);
    }

    /**
     * Test of combinations method, of class Combinatorics.
     */
    @Test
    public void testCombinations() {
        System.out.println("combinations");
        Collection<List<String>> expResult = new ArrayList<>();
        expResult.add(new ArrayList<>(Arrays.asList("a","b")));
        expResult.add(new ArrayList<>(Arrays.asList("a","c")));
        expResult.add(new ArrayList<>(Arrays.asList("b","c")));
        Collection<List<String>> result = Combinatorics.combinations(new ArrayList<>(Arrays.asList("a","b","c")), 2);
        assertEquals(expResult, result);
    }
    
}
