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

import com.datumbox.framework.common.dataobjects.FlatDataCollection;
import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;

import java.util.Iterator;

/**
 * One sample Sign test.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class SignOneSample {
    
    /**
     * Calculates the p-value of null Hypothesis.
     * 
     * @param flatDataCollection
     * @param median
     * @return
     */
    public static double getPvalue(FlatDataCollection flatDataCollection, double median) {
        int n=flatDataCollection.size();
        if(n<=0) {
            throw new IllegalArgumentException("The provided collection can't be empty.");
        }
        
        int Tplus=0;
        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            double v = it.next();
            if(Math.abs(v-median) < 0.0000001) {
                continue; //don't count it at all
            }
            if(v>median) {
                ++Tplus;
            }
        }


        double pvalue= scoreToPvalue(Tplus, n);

        return pvalue;
    }    
    
    /**
     * Tests the rejection of null Hypothesis for a particular confidence level
     * 
     * @param flatDataCollection
     * @param median
     * @param is_twoTailed
     * @param aLevel
     * @return 
     */
    public static boolean test(FlatDataCollection flatDataCollection, double median, boolean is_twoTailed, double aLevel) {        
        double pvalue= getPvalue(flatDataCollection, median);

        boolean rejectH0=false;

        double a=aLevel;
        if(is_twoTailed) { //if to tailed test then split the statistical significance in half
            a=aLevel/2.0;
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
     * @param n
     * @return 
     */
    private static double scoreToPvalue(double score, int n) {
        /*
        if(n<10) {
            //calculate it from binomial distribution
        }
        */

        double mean=n/2.0;
        double variable=n/4.0;

        double z=(score-mean)/Math.sqrt(variable);

        return ContinuousDistributions.gaussCdf(z);
    }
}
