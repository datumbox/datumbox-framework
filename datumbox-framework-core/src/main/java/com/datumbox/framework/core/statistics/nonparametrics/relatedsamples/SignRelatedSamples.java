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

import com.datumbox.framework.common.dataobjects.FlatDataList;
import com.datumbox.framework.common.dataobjects.TransposeDataList;
import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;

/**
 * Sign Related Sample test.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class SignRelatedSamples {
    
    /**
     * Estimates the p-value of the sign test for the provided data.
     * 
     * @param transposeDataList
     * @return
     */
    public static double getPvalue(TransposeDataList transposeDataList) {
        Object[] keys = transposeDataList.keySet().toArray();
        if(keys.length!=2) {
            throw new IllegalArgumentException("The collection must contain observations from 2 groups.");
        }
        
        Object keyX = keys[0];
        Object keyY = keys[1];
        
        FlatDataList flatDataListX = transposeDataList.get(keyX);
        FlatDataList flatDataListY = transposeDataList.get(keyY);

        int n = flatDataListX.size();
        if(n<=0 || n!=flatDataListY.size()) {
            throw new IllegalArgumentException("The number of observations in each group must be equal and larger than 0.");
        }

        int Tplus=0;
        
        for(int j=0;j<n;++j) {
            double delta= flatDataListX.getDouble(j) - flatDataListY.getDouble(j);
            if(delta==0) {
                continue; //don't count it at all
            }
            if(delta>0) {
                ++Tplus;
            }
        }

        double pvalue = scoreToPvalue(Tplus, n);

        return pvalue;
    }

    /**
     * Tests the rejection of null Hypothesis for a particular confidence level.
     * 
     * @param transposeDataList
     * @param is_twoTailed
     * @param aLevel
     * @return
     */
    public static boolean test(TransposeDataList transposeDataList, boolean is_twoTailed, double aLevel) {
        if(transposeDataList.size()!=2) {
            throw new IllegalArgumentException("The collection must contain observations from 2 groups.");
        }

        double pvalue=getPvalue(transposeDataList);

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
