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
package com.datumbox.framework.core.statistics.nonparametrics.relatedsamples;

import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;

/**
 * McNemar's test for paired nominal data.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class McNemar {
    
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
    private static double scoreToPvalue(double score) {
        return 1.0-ContinuousDistributions.chisquareCdf(score, 1);
    }
    
}
