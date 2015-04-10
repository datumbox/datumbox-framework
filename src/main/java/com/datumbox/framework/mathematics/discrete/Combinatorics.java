/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
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

import com.google.common.collect.Collections2;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Combinatorics {
    
    /**
     * Returns the permutations of a collection. Uses Guava library's Collection2.
     * 
     * @param <T>
     * @param elements
     * @return 
     */
    public static <T> Collection<List<T>> permutations(Collection<T> elements) {
        Collection<List<T>> permutations = Collections2.permutations(elements);
        
        List<List<T>> result = new ArrayList<>();
        for(List<T> internalList : permutations) {
            result.add(new ArrayList<>(internalList));
        }
        
        return result;
    }

    
    /**
     * Possible combinations of a list. 
     * Ported from:
     * http://codereview.stackexchange.com/questions/26854/recursive-method-to-return-a-set-of-all-combinations
     * 
     * @param <T>
     * @param elements
     * @param elementCount
     * @return 
     */
    public static <T> Collection<List<T>> combinations(List<T> elements, int elementCount) {
        Set<Set<T>> combinations = getCombinationsFor(elements, elementCount);
        
        Collection<List<T>> result = new ArrayList<>();
        for(Set<T> linkedset : combinations) {
            result.add(new ArrayList<>(linkedset));
        }
        
        return result;
    }
    
    private static <T> Set<Set<T>> getCombinationsFor(List<T> group, int subsetSize) {
        Set<Set<T>> resultingCombinations = new LinkedHashSet<> ();
        int totalSize=group.size();
        if (subsetSize == 0) {
            resultingCombinations.add(new LinkedHashSet<>());
        } 
        else if (subsetSize <= totalSize) {
            List<T> remainingElements = new ArrayList<> (group);
            T X = remainingElements.remove(remainingElements.size()-1);

            Set<Set<T>> combinationsExclusiveX = getCombinationsFor(remainingElements, subsetSize);
            Set<Set<T>> combinationsInclusiveX = getCombinationsFor(remainingElements, subsetSize-1);
            for (Set<T> combination : combinationsInclusiveX) {
                combination.add(X);
            }
            resultingCombinations.addAll(combinationsExclusiveX);
            resultingCombinations.addAll(combinationsInclusiveX);
        }
        return resultingCombinations;
    }
}
