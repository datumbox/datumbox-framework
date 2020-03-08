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

import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.common.dataobjects.FlatDataCollection;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.statistics.descriptivestatistics.Ranks;
import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;

import java.util.Iterator;
import java.util.Map;

/**
 * One sample Wilcoxon test.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class WilcoxonOneSample {
    
    /**
     * Calculates the p-value of null Hypothesis 
     * 
     * @param flatDataCollection
     * @param median
     * @return
     */
    public static double getPvalue(FlatDataCollection flatDataCollection, double median) {
        int n=0;
        AssociativeArray Di = new AssociativeArray();
        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            double delta=it.next()-median;

            if(delta==0.0) {
                continue; //don't count it at all
            }

            String key="+";
            if(delta<0) {
                key="-";
            }
            Di.put(key+String.valueOf(n), Math.abs(delta));
            ++n;
        }
        if(n<=0) {
            throw new IllegalArgumentException("The provided collection can't be empty.");
        }

        //converts the values of the table with its Ranks
        Ranks.getRanksFromValues(Di);
        double W=0.0;
        for(Map.Entry<Object, Object> entry : Di.entrySet()) {
            String key = entry.getKey().toString();
            Double rank = TypeInference.toDouble(entry.getValue());
        
            if(key.charAt(0)=='+') {
                W+=rank;
            }
        }

        double pvalue= scoreToPvalue(W, n);

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
     * @return 
     */
    private static double scoreToPvalue(double score, int n) {
        /*
        if(n<=20) {
            //calculate it from binomial distribution
        }
        */
        
        double mean=n*(n+1.0)/4.0;
        double variable=n*(n+1.0)*(2.0*n+1.0)/24.0;

        double z=(score-mean)/Math.sqrt(variable);

        return ContinuousDistributions.gaussCdf(z);
    }
}
