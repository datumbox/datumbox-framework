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

import com.datumbox.framework.common.dataobjects.FlatDataCollection;
import com.datumbox.framework.common.dataobjects.TransposeDataList;
import com.datumbox.framework.core.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;

/**
 * This class contains methods for estimating and testing Pearson's Correlation.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class PearsonCorrelation {
    
    /**
     * Calculates Pearson's Correlation for a given transposed array (2xn table)
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
        
        FlatDataCollection flatDataCollectionX = transposeDataList.get(keyX).toFlatDataCollection();
        FlatDataCollection flatDataCollectionY = transposeDataList.get(keyY).toFlatDataCollection();

        int n = flatDataCollectionX.size();
        if(n<=2 || n!=flatDataCollectionY.size()) {
            throw new IllegalArgumentException("The number of observations in each group must be equal and larger than 2.");
        }

        double stdX= Descriptives.std(flatDataCollectionX,true);
        double stdY=Descriptives.std(flatDataCollectionY,true);

        double covariance=Descriptives.covariance(transposeDataList,true);

        double pearson=covariance/(stdX*stdY);

        return pearson;
    }
    
    /**
     * Tests the rejection of null Hypothesis (no correlation between paired samples/internalData) for a particular confidence level 
 It uses a Transposed List (2xn)
     * 
     * @param transposeDataList
     * @param is_twoTailed
     * @param aLevel
     * @return 
     */
    public static boolean test(TransposeDataList transposeDataList, boolean is_twoTailed, double aLevel) {
        double pearson=calculateCorrelation(transposeDataList);

        Object[] keys = transposeDataList.keySet().toArray();
        
        int n = transposeDataList.get(keys[0]).size();
        
        double pvalue=scoreToPvalue(pearson,n);

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
        double T= score/Math.sqrt((1-score*score)/(n-2));
        return ContinuousDistributions.studentsCdf(T, n-2);
    }   
}
