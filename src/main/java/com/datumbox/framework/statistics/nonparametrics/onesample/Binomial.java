/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
package com.datumbox.framework.statistics.nonparametrics.onesample;

import com.datumbox.framework.statistics.distributions.ContinuousDistributions;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class Binomial {
    /**
     * The dataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the dataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    /**
     * Tests the rejection of null Hypothesis for a particular confidence level 
     * 
     * @param k
     * @param n
     * @param p
     * @param is_twoTailed
     * @param aLevel
     * @return
     * @throws IllegalArgumentException 
     */
    public static boolean test(int k, int n, double p, boolean is_twoTailed, double aLevel) throws IllegalArgumentException {        
        if(k<0 || n<=0 || p<0) {
            throw new IllegalArgumentException();
        }
        double pvalue = scoreToPvalue((double)k, n, p);

        boolean rejectH0=false;

        double a=aLevel;
        if(is_twoTailed) { //if to tailed test then split the statistical significance in half
            a=aLevel/2.0;
        }
        if(pvalue<=a || pvalue>=(1.0-a)) {
            rejectH0=true;
        }
        
        return rejectH0;
    }
    
    /**
     * Returns the Pvalue for a particular $score
     * 
     * @param score
     * @param n
     * @param p
     * @return 
     */
    private static double scoreToPvalue(double score, int n, double p) {
        if(n<=20) {
            //calculate it from binomial distribution
            //EXPAND: use binomial distribution
        }

        double z=(score+0.5-n*p)/Math.sqrt(n*p*(1.0-p));

        return ContinuousDistributions.GaussCdf(z);
    }
    
}
