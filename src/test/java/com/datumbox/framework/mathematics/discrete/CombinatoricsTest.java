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
package com.datumbox.framework.mathematics.discrete;

import com.datumbox.tests.bases.BaseTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class CombinatoricsTest extends BaseTest {

    /**
     * Test of permutations method, of class Combinatorics.
     */
    @Test
    public void testPermutations() {
        logger.info("permutations");
        Collection<List<String>> expResult = new ArrayList<>();
        expResult.add(new ArrayList<>(Arrays.asList("a","b","c")));
        expResult.add(new ArrayList<>(Arrays.asList("b","a","c")));
        expResult.add(new ArrayList<>(Arrays.asList("b","c","a")));
        expResult.add(new ArrayList<>(Arrays.asList("a","c","b")));
        expResult.add(new ArrayList<>(Arrays.asList("c","a","b")));
        expResult.add(new ArrayList<>(Arrays.asList("c","b","a")));
        Collection<List<String>> result = Combinatorics.<String>permutations(new ArrayList<>(Arrays.asList("a","b","c")));
        assertEquals(expResult, result);
    }

    /**
     * Test of combinations method, of class Combinatorics.
     */
    @Test
    public void testCombinations() {
        logger.info("combinations");
        Collection<List<String>> expResult = new ArrayList<>();
        expResult.add(new ArrayList<>(Arrays.asList("a","b")));
        expResult.add(new ArrayList<>(Arrays.asList("a","c")));
        expResult.add(new ArrayList<>(Arrays.asList("b","c")));
        Collection<List<String>> result = Combinatorics.combinations(new ArrayList<>(Arrays.asList("a","b","c")), 2);
        assertEquals(expResult, result);
    }
    
}
