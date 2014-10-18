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

import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.TransposeDataList;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;

/**
 *
 * @author bbriniotis
 */
public class SignRelatedSamples {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;

    public static double getPvalue(TransposeDataList transposeDataList) throws IllegalArgumentException {
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

        if(n<=0) {
            throw new IllegalArgumentException();
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
     * @throws IllegalArgumentException 
     */
    public static boolean test(TransposeDataList transposeDataList, boolean is_twoTailed, double aLevel) throws IllegalArgumentException {
        if(transposeDataList.size()!=2) {
            throw new IllegalArgumentException();
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
    protected static double scoreToPvalue(double score, int n) {
        if(n<10) {
            //calculate it from binomial distribution
            //EXPAND: implement binomial
        }

        double mean=n/2.0;
        double variable=n/4.0;

        double z=(score-mean)/Math.sqrt(variable);

        return ContinuousDistributions.GaussCdf(z);
    }
}
