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
package com.datumbox.framework.statistics.nonparametrics.independentsamples;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.dataobjects.TransposeDataCollection;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;
import java.util.AbstractMap;
import java.util.Map;

/**
 *
 * @author bbriniotis
 */
public class MannWhitney {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    /**
     * Calculates the p-value of null Hypothesis 
     * 
     * @param transposeDataCollection
     * @return 
     */
    public static double getPvalue(TransposeDataCollection transposeDataCollection) throws IllegalArgumentException {
        if(transposeDataCollection.size()!=2) {
            throw new IllegalArgumentException();
        }
        
        Object[] keys = transposeDataCollection.keySet().toArray();
        
        //IMPORTANT!!! place as n1 the smallest sample!!!!! This is required by the Test.
        
        
        int smallIndex = 0;
        int largeIndex = 1;
        int n1 = transposeDataCollection.get(keys[smallIndex]).size();
        int n2 = transposeDataCollection.get(keys[largeIndex]).size();
        if(n1>n2) {
            int tmp = n1;
            n1 = n2;
            n2 = tmp;
            smallIndex=1;
            largeIndex=0;
        }
        
        //flatten the original internalData table
        AssociativeArray associativeArray = new AssociativeArray();
        
        for(Map.Entry<Object, FlatDataCollection> entry : transposeDataCollection.entrySet()) {
            Object i = entry.getKey();
            FlatDataCollection row = entry.getValue();
            
            Integer j=0;
            for (Object value : row) {
                Map.Entry<Object, Object> i_j = new AbstractMap.SimpleEntry<>(i, (Object)j);
                associativeArray.put(i_j, value);
                ++j;
            }
        }
        
        //converts the values of the flatDataCollection with their Ranks
        Dataset.getRanksFromValues(associativeArray);

        //sum up the scores of the smallest sample
        double MWscore=0.0;
        
        for(Map.Entry<Object,Object> entry : associativeArray.entrySet()) {
            @SuppressWarnings("unchecked")
            Map.Entry<Object, Object> i_j = (Map.Entry<Object, Object>)entry.getKey();
            Object i = i_j.getKey(); //get i and j values
            
            if(i==keys[smallIndex]) { //if it belongs to the FIRST group (small group)
                Double rank = Dataset.toDouble(entry.getValue());
                MWscore+=rank; //add the score
            }
        }
        associativeArray=null;

        double pvalue= scoreToPvalue(MWscore, n1, n2);

        return pvalue;
    }

    /**
     * Tests the rejection of null Hypothesis for a particular confidence level.
     * 
     * @param transposeDataCollection
     * @param is_twoTailed
     * @param aLevel
     * @return 
     */
    public static boolean test(TransposeDataCollection transposeDataCollection, boolean is_twoTailed, double aLevel) {
        double pvalue= getPvalue(transposeDataCollection);

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
     * @param n1
     * @param n2
     * @return 
     */
    protected static double scoreToPvalue(double score, int n1, int n2) {
        if(n1<=10 && n2<=10) {
            //calculate it from tables too small values
            //EXPAND: waiting for tables from Dimaki
        }

        double mean=n1*(n1+n2+1.0)/2.0;
        double variable=n1*n2*(n1+n2+1.0)/12.0;

        double z=(score-mean)/Math.sqrt(variable);

        return ContinuousDistributions.GaussCdf(z);
    }
}
