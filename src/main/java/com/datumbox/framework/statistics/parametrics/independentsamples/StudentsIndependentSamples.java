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
package com.datumbox.framework.statistics.parametrics.independentsamples;

import com.datumbox.framework.statistics.distributions.ContinuousDistributions;

/**
 * Student's Independent Sample Mean test.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class StudentsIndependentSamples {
    
    /**
     * Independent Samples Mean Test for Students.
     * Requirements: Normal with unknown Variances, not equal variances
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
    public static boolean testMeansUnknownNotEqualVars(double xbar, double ybar, int n, int m, double stdx, double stdy, boolean is_twoTailed, double aLevel) {
        //also known as Welch's T-test: http://en.wikipedia.org/wiki/Welch's_t_test
        if(n<=1 || stdx<=0 || m<=1 || stdy<=0) {
            throw new IllegalArgumentException("The values of the provided parameters are not within the permitted range.");
        }

        double varx=stdx*stdx;
        double vary=stdy*stdy;

        //standardize it
        double t = (xbar-ybar)/Math.sqrt(varx/n+vary/m);

        int df = (int)Math.round(Math.pow(varx/n+vary/m,2)/(Math.pow(varx/n,2)/(n-1) + Math.pow(vary/m,2)/(m-1)));

        boolean rejectH0=checkCriticalValue(t, df, is_twoTailed, aLevel);

        return rejectH0;
    }
    
    /**
     * Independent Samples Mean Test for Students.
     * Requirements: Normal with unknown Variances, equal variances
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
    public static boolean testMeansUnknownEqualVars(double xbar, double ybar, int n, int m, double stdx, double stdy, boolean is_twoTailed, double aLevel) {
        if(n<=1 || stdx<=0 || m<=1 || stdy<=0) {
            throw new IllegalArgumentException("The values of the provided parameters are not within the permitted range.");
        }

        double PooledVariance=((n-1)*stdx*stdx+(m-1)*stdy*stdy)/(n+m-2.0);

        //standardize it
        double t=(xbar-ybar)/Math.sqrt(PooledVariance/n+PooledVariance/m);

        int df=n+m-2;

        boolean rejectH0=checkCriticalValue(t, df, is_twoTailed, aLevel);

        return rejectH0;
    }

    /**
     * Checks the Critical Value to determine if the Hypothesis should be rejected
     * 
     * @param score
     * @param df
     * @param is_twoTailed
     * @param aLevel
     * @return 
     */
    protected static boolean checkCriticalValue(double score, int df, boolean is_twoTailed, double aLevel) {
        double probability=ContinuousDistributions.StudentsCdf(score,df);

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
