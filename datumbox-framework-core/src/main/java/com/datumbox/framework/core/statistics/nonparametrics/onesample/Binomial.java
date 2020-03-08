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
package com.datumbox.framework.core.statistics.nonparametrics.onesample;

import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;

/**
 * Binomial exact test.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Binomial {
    
    /**
     * Tests the rejection of null Hypothesis for a particular confidence level 
     * 
     * @param k
     * @param n
     * @param p
     * @param is_twoTailed
     * @param aLevel
     * @return
     */
    public static boolean test(int k, int n, double p, boolean is_twoTailed, double aLevel) {        
        if(k<0 || n<=0 || p<0) {
            throw new IllegalArgumentException("All the parameters must be positive.");
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
     * Returns the Pvalue for a particular score
     * 
     * @param score
     * @param n
     * @param p
     * @return 
     */
    private static double scoreToPvalue(double score, int n, double p) {
        /*
        if(n<=20) {
            //calculate it from binomial distribution
        }
        */

        double z=(score+0.5-n*p)/Math.sqrt(n*p*(1.0-p));

        return ContinuousDistributions.gaussCdf(z);
    }
    
}
