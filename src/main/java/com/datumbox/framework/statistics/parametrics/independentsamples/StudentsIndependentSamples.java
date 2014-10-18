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
package com.datumbox.framework.statistics.parametrics.independentsamples;

import com.datumbox.framework.statistics.distributions.ContinuousDistributions;

/**
 *
 * @author bbriniotis
 */
public class StudentsIndependentSamples {
    /**
     * The dataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the dataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
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
     * @throws IllegalArgumentException 
     */
    public static boolean testMeansUnknownNotEqualVars(double xbar, double ybar, int n, int m, double stdx, double stdy, boolean is_twoTailed, double aLevel) throws IllegalArgumentException {
        //also known as Welch's T-test: http://en.wikipedia.org/wiki/Welch's_t_test
        if(n<=1 || stdx<=0 || m<=1 || stdy<=0) {
            throw new IllegalArgumentException();
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
     * @throws IllegalArgumentException 
     */
    public static boolean testMeansUnknownEqualVars(double xbar, double ybar, int n, int m, double stdx, double stdy, boolean is_twoTailed, double aLevel) throws IllegalArgumentException {
        if(n<=1 || stdx<=0 || m<=1 || stdy<=0) {
            throw new IllegalArgumentException();
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
