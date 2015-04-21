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
package com.datumbox.framework.statistics.parametrics.independentsamples;

import com.datumbox.framework.statistics.distributions.ContinuousDistributions;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class FIndependentSamples {
    
    /**
     * Independent Samples Variance Test for F.
     * Requirements: Normal with known variances
     * 
     * @param stdbarx
     * @param stdbary
     * @param n
     * @param m
     * @param is_twoTailed
     * @param aLevel
     * @return
     * @throws IllegalArgumentException 
     */
    public static boolean testVariances(double stdbarx, double stdbary, int n, int m, boolean is_twoTailed, double aLevel) throws IllegalArgumentException {
        if(n<=1 || stdbarx<=0 || m<=1 || stdbary<=0) {
            throw new IllegalArgumentException();
        }

        //standardize it
        double F=(stdbarx*stdbarx)/(stdbary*stdbary);

        boolean rejectH0=checkCriticalValue(F, n, m, is_twoTailed, aLevel);

        return rejectH0;
    }
    
    /**
     * Checks the Critical Value to determine if the Hypothesis should be rejected
     * 
     * @param score
     * @param n
     * @param m
     * @param is_twoTailed
     * @param aLevel
     * @return 
     */
    protected static boolean checkCriticalValue(double score, int n, int m, boolean is_twoTailed, double aLevel) {
        double probability=ContinuousDistributions.FCdf(score,n-1,m-1);

        boolean rejectH0=false;

        double a=aLevel;
        if(is_twoTailed) { //if to tailed test then split the statistical significance in half
            a=aLevel/2;
        }
        if(probability<=a || probability>=(1-a)) {
            rejectH0=true;
        }

        return rejectH0;
    }
}
