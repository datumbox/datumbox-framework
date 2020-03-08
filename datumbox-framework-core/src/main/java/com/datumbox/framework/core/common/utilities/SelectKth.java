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
package com.datumbox.framework.core.common.utilities;

import java.util.*;

/**
 * The SelectKth class provides a fast implementation of a Selection algorithm.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */

public class SelectKth {
    
    /**
     * Selects the kth largest element from an iterable object.
     * 
     * @param elements
     * @param k
     * @return 
     */
    public static Double largest(Iterator<Double> elements, int k) {
        Iterator<Double> oppositeElements = new Iterator<Double>() {
            /** {@inheritDoc} */
            @Override
            public boolean hasNext() {
                return elements.hasNext();
            }
            
            /** {@inheritDoc} */
            @Override
            public Double next() {
                return -elements.next(); //reverse the sign of every value
            }
        };
        return -smallest(oppositeElements,k); //reverse the sign of the retrieved value
    }
    
    /**
     * Selects the kth smallest element from an iterable object.
     * 
     * @param elements
     * @param k
     * @return 
     */
    /*
     * This method is adapted from Guava. Original method leastOf().
     * 
     * Copyright 2007 Google Inc.
     * Licensed under Apache License, Version 2.0
     */
    public static Double smallest(Iterator <Double> elements, int k) {
        if (k <= 0 || !elements.hasNext()) {
            return null;
        } 
        else if (k >= Integer.MAX_VALUE / 2) {
            List <Double> list = new ArrayList <> ();
            while (elements.hasNext()) {
                list.add(elements.next());
            }
            Collections.sort(list);
            return list.get(k - 1);
        }

        int bufferCap = k * 2;
        Double[] buffer = new Double[bufferCap];
        Double threshold = elements.next();
        buffer[0] = threshold;
        int bufferSize = 1;

        while (bufferSize < k && elements.hasNext()) {
            Double e = elements.next();
            buffer[bufferSize++] = e;
            threshold = Math.max(threshold, e);
        }

        while (elements.hasNext()) {
            Double e = elements.next();
            if (e >= threshold) {
                continue;
            }

            buffer[bufferSize++] = e;
            if (bufferSize == bufferCap) {

                int left = 0;
                int right = bufferCap - 1;

                int minThresholdPosition = 0;

                while (left < right) {
                    int pivotIndex = (left + right + 1) >>> 1;
                    
                    //--- partition
                    Double pivotValue = buffer[pivotIndex];

                    buffer[pivotIndex] = buffer[right];
                    buffer[right] = pivotValue;

                    int pivotNewIndex = left;
                    for (int l = left; l < right; l++) {
                        if (buffer[l] < pivotValue) {
                            Double temp = buffer[pivotNewIndex];
                            buffer[pivotNewIndex] = buffer[l];
                            buffer[l] = temp;
                            pivotNewIndex++;
                        }
                    }

                    Double temp = buffer[right];
                    buffer[right] = buffer[pivotNewIndex];
                    buffer[pivotNewIndex] = temp;
                    //---
        
                    if (pivotNewIndex > k) {
                        right = pivotNewIndex - 1;
                    } 
                    else if (pivotNewIndex < k) {
                        left = Math.max(pivotNewIndex, left + 1);
                        minThresholdPosition = pivotNewIndex;
                    } 
                    else {
                        break;
                    }
                }
                bufferSize = k;

                threshold = buffer[minThresholdPosition];
                for (int i = minThresholdPosition + 1; i < bufferSize; i++) {
                    threshold = Math.max(threshold, buffer[i]);
                }
            }
        }
        
        Arrays.sort(buffer, 0, bufferSize);

        k = Math.min(bufferSize, k);
        
        return buffer[k-1];
    }
}
