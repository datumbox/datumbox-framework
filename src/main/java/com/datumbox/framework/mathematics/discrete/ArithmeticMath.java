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

/**
 *
 * @author bbriniotis
 */
public class ArithmeticMath {
    /**
     * The dataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the dataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    public static double factorial(int k) {
        double factorial=1.0;
        while(k>0) {
            factorial*=k;
            --k;
        }
        
        return factorial;
    }
    
    public static double combination(int n, int k) throws IllegalArgumentException {
        if(n<k) {
            throw new IllegalArgumentException();
        }
        return factorial(n)/(factorial(k)*factorial(n-k));
    }
    
    
}
