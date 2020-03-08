/**
 * Copyright (C) 2013-2020 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.core.mathematics.discrete;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for combinations and permutations.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Combinatorics {

    /**
     * Returns the permutations of a collection. 
     * Ported from:
     * http://stackoverflow.com/questions/10503392/java-code-for-permutations-of-a-list-of-numbers
     *
     * @param <T>
     * @param elements
     * @return
     */
    public static <T> Collection<List<T>> permutations(Collection<T> elements) {
        Collection<List<T>> result = new ArrayList<>();
        if (elements.isEmpty()) {
            result.add(new LinkedList<>());
            return result;
        }

        List<T> rest = new LinkedList<>(elements);
        T head = rest.remove(0);
        for (List<T> permutations : permutations(rest)) {
            List<List<T>> subLists = new ArrayList<>();
            for (int i = 0; i <= permutations.size(); i++) {
                List<T> subList = new ArrayList<>();
                subList.addAll(permutations);
                subList.add(i, head);
                subLists.add(subList);
            }
            result.addAll(subLists);
        }
        return result;
    }

    /**
     * Returns all the possible combinations of the set.
     *
     * @param elements
     * @param subsetSize
     * @param <T>
     * @return
     */
    public static <T> Set<Set<T>> combinations(Set<T> elements, int subsetSize) {
        return combinationsStream(elements, subsetSize).collect(Collectors.toSet());
    }

    /**
     * Returns all the possible combinations of the set in a stream.
     *
     * Heavily Modified code:
     * http://codereview.stackexchange.com/questions/26854/recursive-method-to-return-a-set-of-all-combinations
     *
     * @param elements
     * @param subsetSize
     * @param <T>
     * @return
     */
    public static <T> Stream<Set<T>> combinationsStream(Set<T> elements, int subsetSize) {
        if (subsetSize == 0) {
            return Stream.of(new HashSet<>());
        }
        else if (subsetSize <= elements.size()) {
            Set<T> remainingElements = elements;

            Iterator<T> it = remainingElements.iterator();
            T X = it.next();
            it.remove();

            Stream<Set<T>> combinations = Stream.concat(
                    combinationsStream(remainingElements, subsetSize), //exclude X
                    combinationsStream(remainingElements, subsetSize-1).map(s -> {s.add(X); return s;}) //include X
            );

            remainingElements.add(X);

            return combinations;
        }
        else {
            return Stream.empty();
        }
    }

    /**
     * Fast and memory efficient way to return an iterator with all the possible combinations of an array.
     *
     * Heavily Modified code:
     * http://hmkcode.com/calculate-find-all-possible-combinations-of-an-array-using-java/
     *
     * @param elements
     * @param subsetSize
     * @param <T>
     * @return
     */
    public static <T> Iterator<T[]> combinationsIterator(final T[] elements, final int subsetSize) {
        return new Iterator<T[]>() {
            /**
             * The index on the combination array.
             */
            private int r = 0;

            /**
             * The index on the elements array.
             */
            private int index = 0;

            /**
             * The indexes of the elements of the combination.
             */
            private final int[] selectedIndexes = new int[subsetSize];

            /**
             * Flag that tells us if there is a next item. If the flag is null then we don't know.
             */
            private Boolean hasNext = null;

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean hasNext() {
                if(hasNext == null) { //if we don't know if there is a next item, we need to try to locate it.
                    hasNext = locateNext();
                }
                return hasNext;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public T[] next() {
                /*
                if(!hasNext()) {
                    throw new java.util.NoSuchElementException();
                }
                */
                hasNext = null; //we retrieved the item so we need to locate a new item next time.

                @SuppressWarnings("unchecked")
                T[] combination =(T[]) Array.newInstance(elements[0].getClass(), subsetSize);
                for(int i = 0; i< subsetSize; i++) {
                    combination[i] = elements[selectedIndexes[i]];
                }
                return combination;
            }

            /**
             * Locates the next item OR informs us that there are no next items.
             *
             * @return
             */
            private boolean locateNext() {
                if(subsetSize == 0) {
                    return false;
                }
                int N = elements.length;
                while(true) {
                    if(index <= (N + (r - subsetSize))) {
                        selectedIndexes[r] = index++;
                        if(r == subsetSize -1) {
                            return true; //we retrieved the next
                        }
                        else {
                            r++;
                        }
                    }
                    else {
                        r--;
                        if(r < 0) {
                            return false; //does not have next
                        }
                        index = selectedIndexes[r]+1;
                    }
                }
            }
        };
    }
}
