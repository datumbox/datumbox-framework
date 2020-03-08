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

import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.common.dataobjects.FlatDataList;
import com.datumbox.framework.common.dataobjects.TransposeDataList;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.statistics.descriptivestatistics.Ranks;
import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;

/**
 * This class provides methods to estimate and test Spearman's Correlation.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class SpearmanCorrelation {
    
    /**
     * Estimates Spearman's Correlation for the provided data.
     * 
     * @param transposeDataList
     * @return
     */
    public static double calculateCorrelation(TransposeDataList transposeDataList) {
        Object[] keys = transposeDataList.keySet().toArray();
        if(keys.length!=2) {
            throw new IllegalArgumentException("The collection must contain observations from 2 groups.");
        }

        Object keyX = keys[0];
        Object keyY = keys[1];

        FlatDataList flatDataListX = transposeDataList.get(keyX).copy();
        FlatDataList flatDataListY = transposeDataList.get(keyY).copy();

        int n = flatDataListX.size();
        if(n<=0 || n!=flatDataListY.size()) {
            throw new IllegalArgumentException("The number of observations in each group must be equal and larger than 0.");
        }


        //converts the values of the X table with its Ranks
        AssociativeArray tiesCounter= Ranks.getRanksFromValues(flatDataListX);

        //Estimate Rx_square
        double Sum_Rx_square=(n*n-1.0)*n;
        for(Object value : tiesCounter.values()) {
            double Ti = TypeInference.toDouble(value);
            Sum_Rx_square-=((Ti*Ti-1.0)*Ti); //faster than using pow()
        }
        Sum_Rx_square/=12.0;
        //tiesCounter = null;



        //converts the values of the Y table with its Ranks
        tiesCounter= Ranks.getRanksFromValues(flatDataListY);

        //Estimate Ry_square
        double Sum_Ry_square=(n*n-1.0)*n;
        for(Object value : tiesCounter.values()) {
            double Ti = TypeInference.toDouble(value);
            Sum_Ry_square-=((Ti*Ti-1.0)*Ti); //faster than using pow()
        }
        Sum_Ry_square/=12.0;
        //tiesCounter = null;
        
        
        //calculate the sum of Di^2
        double Sum_Di_square=0;
        for(int j=0;j<n;++j) {
            double di= flatDataListX.getDouble(j) - flatDataListY.getDouble(j);
            Sum_Di_square+=di*di;
        }

        //Finally we estimate the Spearman Correlation
        double Rs=(Sum_Rx_square+Sum_Ry_square-Sum_Di_square)/(2.0*Math.sqrt(Sum_Rx_square*Sum_Ry_square));

        return Rs;
    }

    /**
     * Tests the rejection of null Hypothesis (no correlation between paired samples/internalData) for a particular confidence level 
 It uses a Transposed DataTable (2xn)
     * 
     * @param transposeDataList
     * @param is_twoTailed
     * @param aLevel
     * @return
     */
    public static boolean test(TransposeDataList transposeDataList, boolean is_twoTailed, double aLevel) {
        if(transposeDataList.isEmpty()) {
            return false;
        }
        
        double SpearmanCorrelation= calculateCorrelation(transposeDataList);
        
        Object firstKey = transposeDataList.keySet().iterator().next();
        int n=transposeDataList.get(firstKey).size();
        double pvalue = scoreToPvalue(SpearmanCorrelation,n);


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
     * Returns the Pvalue for a particular score.
     * 
     * @param score
     * @param n
     * @return 
     */
    private static double scoreToPvalue(double score, int n) {
        double Zs= score*Math.sqrt(n-1.0);
        double Ts= score*Math.sqrt((n-Zs)/(1.0-score*score));
        return ContinuousDistributions.studentsCdf(Ts, n-2);
    }   
}
