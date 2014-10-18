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

import com.google.common.collect.Collections2;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
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
