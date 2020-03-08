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
 * This class provides methods for estimating and testing Kendall Tau's Correlation.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class KendallTauCorrelation {

    /**
     * Calculates Kendall Tau's Correlation for a given transposed array (2xn table)
     * 
     * @param transposeDataList
     * @return
     */
    public static double calculateCorrelation(TransposeDataList transposeDataList) { //unsafe internalData pointer. modifying the variable internalData
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
    private static double scoreToPvalue(double score, int n) {
        double variance=2.0*(2.0*n+5.0)/(9.0*n*(n-1.0));

        double Z=score/Math.sqrt(variance); //follows approximately Normal with 0 mean and variance as calculated above

        return ContinuousDistributions.gaussCdf(Z);
    }       

}
