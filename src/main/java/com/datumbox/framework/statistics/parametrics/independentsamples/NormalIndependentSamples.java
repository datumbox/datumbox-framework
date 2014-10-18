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
public class NormalIndependentSamples {
    /**
     * The dataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the dataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
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
     * @throws IllegalArgumentException 
     */
    public static boolean testMeans(double xbar, double ybar, int n, int m, double stdx, double stdy, boolean is_twoTailed, double aLevel) throws IllegalArgumentException {
        if(n<=0 || stdx<=0 || m<=0 || stdy<=0) {
            throw new IllegalArgumentException();
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
     * @throws IllegalArgumentException 
     */
    public static boolean testPercentages(double p1bar, double p2bar, int n1, int n2, boolean is_twoTailed, double aLevel) throws IllegalArgumentException {
        if(n1<=0 || p1bar<0 || n2<=0 || p2bar<0) {
            throw new IllegalArgumentException();
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
     * @throws IllegalArgumentException 
     */
    public static boolean testOddsRatio(int n11, int n12, int n21, int n22, boolean is_twoTailed, double aLevel) throws IllegalArgumentException {
        if(n11<=0 || n12<=0 || n21<=0 || n22<=0) {
            throw new IllegalArgumentException();
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
    protected static boolean checkCriticalValue(double score, boolean is_twoTailed, double aLevel) {
        double probability=ContinuousDistributions.GaussCdf(score);

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
