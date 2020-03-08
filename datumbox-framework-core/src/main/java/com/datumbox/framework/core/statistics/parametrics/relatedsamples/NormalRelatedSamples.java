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
package com.datumbox.framework.core.statistics.parametrics.relatedsamples;

import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;

/**
 * Normal Related Samples parametric tests.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class NormalRelatedSamples {
    
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
     */
    public static boolean testMean(double dbar, int n, double dbarStd, boolean is_twoTailed, double aLevel) {
        if(n<=0 || dbarStd<=0) {
            throw new IllegalArgumentException("All the parameters must be positive.");
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
    private static boolean checkCriticalValue(double score, boolean is_twoTailed, double aLevel) {
        double probability=ContinuousDistributions.gaussCdf(score);

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
