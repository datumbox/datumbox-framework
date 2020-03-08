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
package com.datumbox.framework.core.statistics.parametrics.onesample;

import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;

/**
 * Student's One sample parametric tests.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class StudentsOneSample {
    
    /**
     * One Sample Mean Test for Students.
     * Requirements: Normal with unknown variance
     * 
     * @param xbar
     * @param n
     * @param H0mean
     * @param std
     * @param is_twoTailed
     * @param aLevel
     * @return
     */
    public static boolean testMean(double xbar, int n, double H0mean, double std, boolean is_twoTailed, double aLevel) {
        if(n<=0 || std<=0) {
            throw new IllegalArgumentException("All the parameters must be positive.");
        }

        //standardize it
        double t=(xbar-H0mean)/(std/Math.sqrt(n));

        boolean rejectH0=checkCriticalValue(t, n, is_twoTailed, aLevel);

        return rejectH0;
    }
    
    /**
     * Autocorrelation test
     * 
     * @param pk
     * @param n
     * @param is_twoTailed
     * @param aLevel
     * @return
     */
    public static boolean testAutocorrelation(double pk, int n, boolean is_twoTailed, double aLevel) {
        if(n<=0) {
            throw new IllegalArgumentException("All the parameters must be positive.");
        }

        //standardize it
        double t=Math.sqrt(n)*pk;

        boolean rejectH0=checkCriticalValue(t, n, is_twoTailed, aLevel);

        return rejectH0;
    }
    
    /**
     * Checks the Critical Value to determine if the Hypothesis should be rejected
     * 
     * @param score
     * @param n
     * @param is_twoTailed
     * @param aLevel
     * @return 
     */
    private static boolean checkCriticalValue(double score, int n, boolean is_twoTailed, double aLevel) {
        double probability=ContinuousDistributions.studentsCdf(score,n-1);

        boolean rejectH0=false;

        double a=aLevel;
        if(is_twoTailed) { //if to tailed test then split the statistical significance in half
            a=aLevel/2.0;
        }
        if(probability<=a || probability>=(1-a)) {
            rejectH0=true;
        }

        return rejectH0;
    }
}
