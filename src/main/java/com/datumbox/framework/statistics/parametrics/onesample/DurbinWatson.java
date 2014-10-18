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

import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;

/**
 *
 * @author bbriniotis
 */
public class DurbinWatson {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;

    /**
     * Test for Autocorrelation for k explanatory variables
     * 
     * @param errorList
     * @param k
     * @param is_twoTailed
     * @param aLevel
     * @return
     * @throws IllegalArgumentException 
     */
    public static boolean test(FlatDataList errorList, int k, boolean is_twoTailed, double aLevel) throws IllegalArgumentException {
        int n= errorList.size();
        if(n<=0) {
            throw new IllegalArgumentException();
        }

        double DW=calculateScore(errorList);

        boolean rejectH0=checkCriticalValue(DW, n, k, is_twoTailed, aLevel);

        return rejectH0;
    }
    
    /**
     * Calculates DW score
     * 
     * @param errorList
     * @return 
     */
    public static double calculateScore(FlatDataList errorList) {
        double DWdeltasquare=0;
        double DWetsquare=0;

        int n = errorList.size();
        for(int i=0;i<n;++i) {
            Double error = errorList.getDouble(i);
            if(i>=1) {
                Double errorPrevious = errorList.getDouble(i-1);
                if(errorPrevious!=null) {
                    DWdeltasquare+=Math.pow(error - errorPrevious,2);        
                }
            }
            DWetsquare+=error*error;
        }
        double DW=DWdeltasquare/DWetsquare;

        return DW;
    }
    
    /**
     * Checks the Critical Value to determine if the Hypothesis should be rejected
     * 
     * @param score
     * @param n
     * @param k
     * @param is_twoTailed
     * @param aLevel
     * @return 
     */
    public static boolean checkCriticalValue(double score, int n, int k, boolean is_twoTailed, double aLevel) {
        if(n<=200 && k<=20) {
            //Calculate it from tables
            //http://www3.nd.edu/~wevans1/econ30331/Durbin_Watson_tables.pdf
        }
        //Follows normal distribution for large samples
        //References: http://econometrics.com/guide/dwdist.htm
        double z=(score-2.0)/Math.sqrt(4.0/n);

        double probability=ContinuousDistributions.GaussCdf(z);

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
