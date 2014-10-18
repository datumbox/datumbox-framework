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
package com.datumbox.framework.statistics.nonparametrics.relatedsamples;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.TransposeDataList;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;

/**
 *
 * @author bbriniotis
 */
public class SpearmanCorrelation {
    /**
     * The internalDataCollections that are passed in this function are modified after the analysis. 
     * Don't pass directly the internalDataCollection unless you don't need them afterwards
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = false;

    public static double calculateCorrelation(TransposeDataList transposeDataList) throws IllegalArgumentException { //unsafe internalData pointer. modifying the variable internalData
        Object[] keys = transposeDataList.keySet().toArray();
        if(keys.length!=2) {
            throw new IllegalArgumentException();
        }

        Object keyX = keys[0];
        Object keyY = keys[1];

        FlatDataList flatDataListX = transposeDataList.get(keyX);
        FlatDataList flatDataListY = transposeDataList.get(keyY);

        int n = flatDataListX.size();
        if(n<=0 || n!=flatDataListY.size()) {
            throw new IllegalArgumentException();
        }


        //converts the values of the X table with its Ranks
        AssociativeArray tiesCounter= Dataset.getRanksFromValues(flatDataListX);

        //Estimate Rx_square
        double Sum_Rx_square=(n*n-1.0)*n;
        for(Object value : tiesCounter.values()) {
            double Ti = Dataset.toDouble(value);
            Sum_Rx_square-=((Ti*Ti-1.0)*Ti); //faster than using pow()
        }
        Sum_Rx_square/=12.0;
        tiesCounter = null;



        //converts the values of the Y table with its Ranks
        tiesCounter= Dataset.getRanksFromValues(flatDataListY);

        //Estimate Ry_square
        double Sum_Ry_square=(n*n-1.0)*n;
        for(Object value : tiesCounter.values()) {
            double Ti = Dataset.toDouble(value);
            Sum_Ry_square-=((Ti*Ti-1.0)*Ti); //faster than using pow()
        }
        Sum_Ry_square/=12.0;
        tiesCounter = null;
        
        
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
     * @throws IllegalArgumentException 
     */
    public static boolean test(TransposeDataList transposeDataList, boolean is_twoTailed, double aLevel) throws IllegalArgumentException {
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
    protected static double scoreToPvalue(double score, int n) {
        double Zs= score*Math.sqrt(n-1.0);
        double Ts= score*Math.sqrt((n-Zs)/(1.0-score*score));
        return ContinuousDistributions.StudentsCdf(Ts, n-2);
    }   
}
