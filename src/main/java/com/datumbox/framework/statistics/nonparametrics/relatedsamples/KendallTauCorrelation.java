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
public class KendallTauCorrelation {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;

    /**
     * Calculates Kendall Tau's Correlation for a given transposed array (2xn table)
     * 
     * @param transposeDataList
     * @return
     * @throws IllegalArgumentException 
     */
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

        //The following loops calculate twice the scores. That the check both if A>B and B<A and count it twice
        int numberOfConcordant=0;
        int numberOfDisconcordant=0;
        
        
        for(int i=0;i<n;++i) {
            for(int j=0;j<n;++j) {
                double xi = flatDataListX.getDouble(i);
                double xj = flatDataListX.getDouble(j);
                double yi = flatDataListY.getDouble(i);
                double yj = flatDataListY.getDouble(j);
                
                double sign=(xi-xj)*(yi-yj); //if this has a positive sign then they are concordant
                if(sign>0) {
                    ++numberOfConcordant;
                }
                else if(sign<0) {
                    ++numberOfDisconcordant;
                }
            }
        }
        /*
        foreach(transposeDataList[0] as i=>&xi) {
            foreach(transposeDataList[0] as j=>&xj) {
            }
        }
        */
        numberOfConcordant/=2; //divide by 2 because we counted them twice
        numberOfDisconcordant/=2; //divide by 2 because we counted them twice

        double R=(numberOfConcordant-numberOfDisconcordant)/(n*(n-1.0)/2.0);

        return R;
    }
    
    /**
     * Tests the rejection of null Hypothesis for a particular confidence level 
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
        
        double KendallTauCorrelation= calculateCorrelation(transposeDataList);
        
        Object firstKey = transposeDataList.keySet().iterator().next();
        int n=transposeDataList.get(firstKey).size();
        double pvalue= scoreToPvalue(KendallTauCorrelation,n);


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
        double variance=2.0*(2.0*n+5.0)/(9.0*n*(n-1.0));

        double Z=score/Math.sqrt(variance); //follows approximately Normal with 0 mean and variance as calculated above

        return ContinuousDistributions.GaussCdf(Z);
    }       

}
