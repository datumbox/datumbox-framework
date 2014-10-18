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
package com.datumbox.framework.statistics.parametrics.onesample;

import com.datumbox.framework.statistics.distributions.ContinuousDistributions;

/**
 *
 * @author bbriniotis
 */
public class NormalOneSample {
    /**
     * The dataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the dataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    /**
     * One Sample Mean Test for Normal.
     * Requirements: Normal with known variance, Not Normal with large sample and known/unknown variance
     * 
     * @param xbar
     * @param n
     * @param H0mean
     * @param std
     * @param is_twoTailed
     * @param aLevel
     * @return
     * @throws IllegalArgumentException 
     */
    public static boolean testMean(double xbar, int n, double H0mean, double std, boolean is_twoTailed, double aLevel) throws IllegalArgumentException {
        if(n<=0 || std<=0) {
            throw new IllegalArgumentException();
        }
        
        double z = (xbar-H0mean)/(std/Math.sqrt(n));
        
        boolean rejectH0 = checkCriticalValue(z, is_twoTailed, aLevel);
        
        return rejectH0;
    }
    
    /**
     * One Sample Sum Test for Normal.
     * Requirements: Large Sample with known variance
     * 
     * @param xsum
     * @param n
     * @param H0mean
     * @param std
     * @param is_twoTailed
     * @param aLevel
     * @return
     * @throws IllegalArgumentException 
     */
    public static boolean testSum(double xsum, int n, double H0mean, double std, boolean is_twoTailed, double aLevel) throws IllegalArgumentException {
        if(n<=0 || std<=0) {
            throw new IllegalArgumentException();
        }
        
        double z = (xsum-n*H0mean)/(std*Math.sqrt(n));
        
        boolean rejectH0 = checkCriticalValue(z, is_twoTailed, aLevel);
        
        return rejectH0;
    }
    
    /**
     * One Sample Percentage Test for Normal.
     * Requirements: Percentage
     * 
     * @param pbar
     * @param n
     * @param H0p
     * @param is_twoTailed
     * @param aLevel
     * @return
     * @throws IllegalArgumentException 
     */
    public static boolean testPercentage(double pbar, int n, double H0p, boolean is_twoTailed, double aLevel) throws IllegalArgumentException {
        if(n<=0 || H0p<=0 || pbar<0) {
            throw new IllegalArgumentException();
        }
        
        double z = (pbar-H0p)/Math.sqrt((H0p*(1.0 - H0p)/n));
        
        boolean rejectH0 = checkCriticalValue(z, is_twoTailed, aLevel);
        
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
        double probability = ContinuousDistributions.GaussCdf(score);
        
        boolean rejectH0=false;
        
        double a=aLevel;
        if(is_twoTailed) { //if to tailed test then split the statistical significance in half
            a=aLevel/2.0;
        }
        if(probability<=a || probability>=(1.0-a)) {
            rejectH0=true;
        }
        
        return rejectH0;
    }
}
