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
package com.datumbox.framework.statistics.nonparametrics.relatedsamples;

import com.datumbox.framework.statistics.distributions.ContinuousDistributions;

/**
 *
 * @author bbriniotis
 */
public class McNemar {
    /**
     * The dataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the dataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    /**
     * Calculates the p-value of null Hypothesis 
     * 
     * @param n11
     * @param n12
     * @param n21
     * @param n22
     * @return 
     */
    public static double getPvalue(int n11, int n12, int n21, int n22) {
        double Chisquare=Math.pow(Math.abs(n12-n21) - 0.5,2)/(n12+n21); //McNemar with Yates's correction for continuity

        double pvalue= scoreToPvalue(Chisquare);

        return pvalue;
    }
    
    /**
     * Tests the rejection of null Hypothesis for a particular confidence level 
     * 
     * @param n11
     * @param n12
     * @param n21
     * @param n22
     * @param is_twoTailed
     * @param aLevel
     * @return 
     */
    public static boolean test(int n11, int n12, int n21, int n22, boolean is_twoTailed, double aLevel) {
        double pvalue= getPvalue(n11,n12,n21,n22);

        boolean rejectH0=false;

        double a=aLevel;
        if(is_twoTailed) { //if to tailed test then split the statistical significance in half
            a=aLevel/2;
        }
        if(pvalue<=a || pvalue>=(1-a)) {
            rejectH0=true; 
        }

        return rejectH0;
    }
    
    /**
     * Returns the Pvalue for a particular score
     * 
     * @param score
     * @return 
     */
    protected static double scoreToPvalue(double score) {
        return 1.0-ContinuousDistributions.ChisquareCdf(score, 1);
    }
    
}
