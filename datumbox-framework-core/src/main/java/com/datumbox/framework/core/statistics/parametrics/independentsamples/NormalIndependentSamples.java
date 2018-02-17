/**
 * Copyright (C) 2013-2018 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.core.statistics.parametrics.independentsamples;

import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;

/**
 * Normal independent samples mean test.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class NormalIndependentSamples {
    
    /**
     * Independent Samples Mean Test for Normal.
     * Requirements: Normal with known Variances, Not Normal with large sample and know/unknown variances
     * 
     * @param xbar
     * @param ybar
     * @param n
     * @param m
     * @param stdx
     * @param stdy
     * @param is_twoTailed
     * @param aLevel
     * @return
     */
    public static boolean testMeans(double xbar, double ybar, int n, int m, double stdx, double stdy, boolean is_twoTailed, double aLevel) {
        if(n<=0 || stdx<=0 || m<=0 || stdy<=0) {
            throw new IllegalArgumentException("All the parameters must be positive.");
        }

        //standardize it
        double z=(xbar-ybar)/Math.sqrt(stdx*stdx/n+stdy*stdy/m);

        boolean rejectH0= checkCriticalValue(z, is_twoTailed, aLevel);

        return rejectH0;
    }

    /**
     * Independent Samples Percentage Test for Normal.
     * Requirements: Percentages
     * 
     * @param p1bar
     * @param p2bar
     * @param n1
     * @param n2
     * @param is_twoTailed
     * @param aLevel
     * @return
     */
    public static boolean testPercentages(double p1bar, double p2bar, int n1, int n2, boolean is_twoTailed, double aLevel) {
        if(n1<=0 || p1bar<0 || n2<=0 || p2bar<0) {
            throw new IllegalArgumentException("All the parameters must be positive.");
        }

        //standardize it
        double z=(p1bar-p2bar)/Math.sqrt(p1bar*(1.0-p1bar)/n1+p2bar*(1.0-p2bar)/n2);

        boolean rejectH0=checkCriticalValue(z, is_twoTailed, aLevel);

        return rejectH0;
    }
    
    /**
     * Independent Samples, 2x2 Tables Odds Ratio Test for Normal.
     * Requirements: Percentages
     * 
     * @param n11
     * @param n12
     * @param n21
     * @param n22
     * @param is_twoTailed
     * @param aLevel
     * @return
     */
    public static boolean testOddsRatio(int n11, int n12, int n21, int n22, boolean is_twoTailed, double aLevel) {
        if(n11<=0 || n12<=0 || n21<=0 || n22<=0) {
            throw new IllegalArgumentException("All the parameters must be positive.");
        }

        //standardize it
        double thita=((double)n11*n22)/(n12*n21);

        double z=Math.log(thita)/Math.sqrt(1.0/n11+1.0/n12+1.0/n21+1.0/n22);

        boolean rejectH0=checkCriticalValue(z, is_twoTailed, aLevel);

        return rejectH0;
    }
    
    /**
     * Checks the Critical Value to determine if the Hypothesis should be rejected
     * 
     * @param score
     * @param is_twoTailed
     * @param aLevel
     * @return 
     */
    private static boolean checkCriticalValue(double score, boolean is_twoTailed, double aLevel) {
        double probability= ContinuousDistributions.gaussCdf(score);

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
