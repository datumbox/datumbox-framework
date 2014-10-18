/* 
 * Copyright (C) 2014 Vasilis Vryniotis <bbriniotis at datumbox.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.datumbox.framework.statistics.parametrics.relatedsamples;

import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.dataobjects.TransposeDataList;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;

/**
 *
 * @author bbriniotis
 */
public class PearsonCorrelation {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    /**
     * Calculates Pearson's Correlation for a given transposed array (2xn table)
     * 
     * @param transposeDataList
     * @return
     * @throws IllegalArgumentException 
     */
    public static double calculateCorrelation(TransposeDataList transposeDataList) throws IllegalArgumentException {
        double pearson = 0.0;
        
        Object[] keys = transposeDataList.keySet().toArray();
        if(keys.length!=2) {
            throw new IllegalArgumentException();
        }
        
        Object keyX = keys[0];
        Object keyY = keys[1];
        
        FlatDataCollection flatDataCollectionX = transposeDataList.get(keyX).toFlatDataCollection();
        FlatDataCollection flatDataCollectionY = transposeDataList.get(keyY).toFlatDataCollection();

        int n = flatDataCollectionX.size();
        if(n<=2 || n!=flatDataCollectionY.size()) {
            throw new IllegalArgumentException();
        }

        double stdX=Descriptives.std(flatDataCollectionX,true);
        double stdY=Descriptives.std(flatDataCollectionY,true);

        double covariance=Descriptives.covariance(transposeDataList,true);

        pearson=covariance/(stdX*stdY);

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
    protected static double scoreToPvalue(double score, int n) {
        double T= score/Math.sqrt((1-score*score)/(n-2));
        return ContinuousDistributions.StudentsCdf(T, n-2);
    }   
}
