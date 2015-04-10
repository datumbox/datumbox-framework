/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.statistics.parametrics.relatedsamples;

import com.datumbox.framework.statistics.distributions.ContinuousDistributions;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class NormalRelatedSamples {
    /**
     * The dataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the dataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    /**
     * Related Samples (Paired) Mean Test for Normal.
     * Requirements: Not Normal with large sample and known or unknown variances
     * 
     * @param dbar
     * @param n
     * @param dbarStd
     * @param is_twoTailed
     * @param aLevel
     * @return
     * @throws IllegalArgumentException 
     */
    public static boolean testMean(double dbar, int n, double dbarStd, boolean is_twoTailed, double aLevel) throws IllegalArgumentException {
        if(n<=0 || dbarStd<=0) {
            throw new IllegalArgumentException();
        }

        //standardize it
        double z=(dbar)/(dbarStd/Math.sqrt(n));

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
