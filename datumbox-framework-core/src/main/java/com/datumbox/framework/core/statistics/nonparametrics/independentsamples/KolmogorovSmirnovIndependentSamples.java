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
package com.datumbox.framework.core.statistics.nonparametrics.independentsamples;

import com.datumbox.framework.common.dataobjects.DataTable2D;
import com.datumbox.framework.common.dataobjects.TransposeDataList;
import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * kolmogorov-Smirnov's test for independent samples.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class KolmogorovSmirnovIndependentSamples {
    
    /**
     * Tests the rejection of null Hypothesis for a particular confidence level
     * 
     * @param transposeDataList
     * @param is_twoTailed
     * @param aLevel
     * @return
     */
    public static boolean test(TransposeDataList transposeDataList, boolean is_twoTailed, double aLevel) {
        int k=transposeDataList.size();
        if(k!=2) {
            throw new IllegalArgumentException("The collection must contain observations from 2 groups.");
        }
        
        Object[] keys = transposeDataList.keySet().toArray();
        Set<Double> allUniqueValues = new TreeSet<>(); //sorted set
        for(int j=0;j<k;++j) {
            Iterator<Double> it = transposeDataList.get(keys[j]).iteratorDouble();
            while(it.hasNext()) {
                allUniqueValues.add(it.next());
            }
        }
        
        DataTable2D distributionAndValue2Probability = new DataTable2D();
        for(int j=0;j<k;++j) {
            Object keyj = keys[j];
            int nj=transposeDataList.get(keyj).size();
            if(nj<=0) {
                throw new IllegalArgumentException("The number of observations in each group but be larger than 0.");
            }
            
            
            int rank = 1;
            for(Double value : allUniqueValues) {
                Object objValue = value;
                if(!transposeDataList.get(keyj).contains(objValue)) { //if this is a missing value
                    //add the probability that matches the previous rank
                    distributionAndValue2Probability.put2d(keyj, objValue, (rank-1.0)/nj);
                    continue;
                }
                
                if(distributionAndValue2Probability.get2d(keyj, objValue)==null) { //keeps the lowest probability in case of ties
                    distributionAndValue2Probability.put2d(keyj, objValue, (double)rank/nj);
                }
                ++rank;
            }
        }
        //allUniqueValues=null;
        
        double maxDelta=0.0;
        for(Object key : distributionAndValue2Probability.get(keys[0]).keySet()) {
            //get the 2 probabilities from the 2 dataTables and find max delta
            
            double v1 = distributionAndValue2Probability.get(keys[0]).getDouble(key);
            double v2 = distributionAndValue2Probability.get(keys[1]).getDouble(key);
            
            double delta=Math.abs(v2-v1);
            if(delta>maxDelta) {
                maxDelta=delta;
            }   
        }
        //distributionAndValue2Probability = null;

        int n1 = transposeDataList.get(keys[0]).size();
        int n2 = transposeDataList.get(keys[1]).size();
        //keys = null;
        
        boolean rejectH0 = checkCriticalValue(maxDelta, is_twoTailed, n1, n2, aLevel);
        
        return rejectH0;
    }

    /**
     * Checks the Critical Value to determine if the Hypothesis should be rejected
     * 
     * @param score
     * @param is_twoTailed
     * @param n1
     * @param n2
     * @param aLevel
     * @return 
     */
    private static boolean checkCriticalValue(double score, boolean is_twoTailed, int n1, int n2, double aLevel) {
        boolean rejected=false;

        double criticalValue= calculateCriticalValue(is_twoTailed,n1,n2,aLevel);


        if(score>criticalValue) {
            rejected=true; 
        }

        return rejected;
    }
    

    /**
     * Calculate Critical Value for a particular $n and $aLevel combination
     * 
     * @param is_twoTailed
     * @param n1
     * @param n2
     * @param aLevel
     * @return 
     */
    protected static double calculateCriticalValue(boolean is_twoTailed, int n1, int n2, double aLevel) {
        double a=aLevel;
        if(is_twoTailed) {
            a=aLevel/2.0;
        }
        double one_minus_a=1.0-a;

        double Ka=1.36;//start by this value and go either up or down until you pass the desired level of significance

        int direction=1;//go up
        if(ContinuousDistributions.kolmogorov(Ka)>one_minus_a) {
            direction=-1;//go down
        }
        for(int i=0;i<110;++i) { //Why maximum 110 steps? Because the minimum value before kolmogorov goes to 0 is 0.27 and the maximum (empirically) is about 2.5. Both of them are about 110 steps of 0.01 distance away 
            Ka+=(direction*0.01);

            double sign=(one_minus_a-ContinuousDistributions.kolmogorov(Ka))*direction;
            //this changes sign ONLY when we just passed the value
            if(sign<=0) {
                break;
            }
        }

        double criticalValue=Ka*Math.sqrt(((double)n1+n2)/(n1*n2));

        return criticalValue;
    }
}
