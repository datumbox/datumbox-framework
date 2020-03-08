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

/**
 * Utility class with useful arithmetic methods.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Arithmetics {
    
    /**
     * It estimates the factorial of an integer.
     * 
     * @param k
     * @return 
     */
    public static double factorial(int k) {
        double factorial=1.0;
        while(k>0) {
            factorial*=k;
            --k;
        }
        
        return factorial;
    }
    
    /**
     * It estimates the number of k-combinations of n objects.
     * 
     * @param n
     * @param k
     * @return
     */
    public static double combination(int n, int k) {
        if(n<k) {
            throw new IllegalArgumentException("The n can't be smaller than k.");
        }
        double combinations=1.0;
        double lowerBound = n-k;
        for(int i=n;i>lowerBound;i--) {
            combinations *= i/(i-lowerBound);
        }
        return combinations;
    }
    
    
}
