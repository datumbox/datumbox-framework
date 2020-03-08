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
import com.datumbox.framework.common.dataobjects.DataTable2D;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.statistics.descriptivestatistics.Ranks;
import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Friedman's related sample non-parametric test.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Friedman {
    
    /**
     * Calculates the p-value of null Hypothesis .
     * 
     * @param dataTable
     * @return 
     */
    public static double getPvalue(DataTable2D dataTable) {
        if(dataTable.isValid()==false) {
            throw new IllegalArgumentException("The provided Table does not have a rectangular format.");
        }
        
        Map<Object, Double> columnSum = new HashMap<>(); //it stores the column sums that are required by the algorithm

        List<Integer> tiesCounter = new ArrayList<>(); //it stores the total ties occurred along with the values that were involved in the tie in each case

        //Find Ranks from Data Table. We don't store them to reduce memory consumption
        for(Map.Entry<Object, AssociativeArray> entry1 : dataTable.entrySet()) {
            AssociativeArray row = entry1.getValue(); 
            
            //find the number of tied values and convert values into ranks
            AssociativeArray tiedValuesArray = Ranks.getRanksFromValues(row.copy()); //this copies the data before passing them into the Ranks.
            
            for(Object value : tiedValuesArray.values()) {
                tiesCounter.add( ((Number)value).intValue() );
            }
            
            for(Map.Entry<Object, Object> entry2 : row.entrySet()) {
                Object j = entry2.getKey();
                
                double v = TypeInference.toDouble(entry2.getValue());
                if(columnSum.containsKey(j)==false) {
                    columnSum.put(j, v);
                }
                else {
                    columnSum.put(j, columnSum.get(j)+v);
                }
            }

            //tiedValuesArray = null;
            //row = null;
        }

        int n=dataTable.size();
        int k=columnSum.size();

        double Fscore=0;
        for(Double value : columnSum.values()) {
            Fscore+=value*value;
        }
        //columnSum = null;
        Fscore=(12.0/(n*k*(k+1.0)))*Fscore - 3.0*n*(k+1.0);

        //Correct for ties
        if(!tiesCounter.isEmpty()) {
            double C=0.0;
            for(Integer Ti : tiesCounter) {
                C+=((Ti*Ti-1.0)*Ti); //faster than using pow()
            }
            Fscore/=(1.0-C/(n*k*(k*k-1.0))); //again faster than using pow()
        }

        double pvalue=scoreToPvalue(Fscore, k);

        return pvalue;
    }

    /**
     * Tests the rejection of null Hypothesis for a particular confidence level.
     * 
     * @param dataTable
     * @param aLevel
     * @return 
     */
    public static boolean test(DataTable2D dataTable, double aLevel) {
        double pvalue= getPvalue(dataTable);

        boolean rejectH0=false;
        if(pvalue<=aLevel) {
            rejectH0=true; 
        }

        return rejectH0;
    }
    
    /**
     * Returns the Pvalue for a particular score
     * 
     * @param score
     * @param k
     * @return 
     */
    private static double scoreToPvalue(double score, int k) {
        /*
        if(k<=5) {
            //calculate it from tables too small values
        }
        */
        return 1-ContinuousDistributions.chisquareCdf(score, k-1);
    }    
}
