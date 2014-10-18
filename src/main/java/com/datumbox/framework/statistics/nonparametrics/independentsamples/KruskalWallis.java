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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author bbriniotis
 */
public class KruskalWallis {
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
    public static double getPvalue(TransposeDataCollection transposeDataCollection) {
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
        AssociativeArray tiesCounter = Dataset.getRanksFromValues(associativeArray);
        int n = associativeArray.size();
        
        double C=0;
        //Correct for ties
        if(!tiesCounter.isEmpty()) {
            for(Object value : tiesCounter.values()) {
                double Ti = Dataset.toDouble(value);
                C+=((Ti*Ti-1)*Ti); //faster than using pow()
            }
            C/=((n*n-1.0)*n); //faster than using pow()
        }
        tiesCounter = null;

        //Important note! Remember that the "i" value is the number of the group while the j is the number of observation within the group and NOT the other way around.
        Map<Object, Integer> ni = new HashMap<>(); //stores the total number of observations in each group
        Map<Object, Double> Ridot = new HashMap<>(); //stores the sum of Ranks for each group
        
        for(Map.Entry<Object, Object> entry : associativeArray.entrySet()) {
            @SuppressWarnings("unchecked")
            Map.Entry<Object, Object> i_j = (Map.Entry<Object, Object>)entry.getKey();
            
            Object i = i_j.getKey(); //get i and j values
            Double rank = Dataset.toDouble(entry.getValue());
            
            if(Ridot.containsKey(i)==false) { //if this "i" value is found for first time then define 
                Ridot.put(i, rank);
                ni.put(i, 1);
            }
            else {
                Ridot.put(i, Ridot.get(i)+rank);
                ni.put(i, ni.get(i)+1);
            }
        }
        associativeArray=null;

        int k=ni.size();

        //Calculate Kruskal Wallis scrore based on the above
        double KWscore=0.0;
        
        for(Map.Entry<Object, Double> entry : Ridot.entrySet()) {
            Object i = entry.getKey();
            Double value = entry.getValue();
            
            KWscore+=value*value/ni.get(i);
        }
        Ridot=null;
        ni=null;


        KWscore=(12.0/(n*(n+1.0)))*KWscore - 3.0*(n+1.0);

        KWscore/=(1.0-C); //correction for ties

        double pvalue= scoreToPvalue(KWscore, k);

        return pvalue;
    }

    /**
     * Tests the rejection of null Hypothesis for a particular confidence level
     * 
     * @param transposeDataCollection
     * @param aLevel
     * @return 
     */
    public static boolean test(TransposeDataCollection transposeDataCollection, double aLevel) {
        double pvalue = getPvalue(transposeDataCollection);

        boolean rejectH0=false;
        if(pvalue<=aLevel) {
            rejectH0=true; 
        }

        return rejectH0;
    }
    
    /**
     * Returns the Pvalue for a particular score.
     * 
     * @param score
     * @param k
     * @return 
     */
    protected static double scoreToPvalue(double score, int k) {
        if(k<=3) {
            //calculate it from tables too small values
            //EXPAND: waiting for tables from Dimaki
        }
        return 1.0-ContinuousDistributions.ChisquareCdf(score, k-1);
    }
}
