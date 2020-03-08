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
package com.datumbox.framework.core.statistics.nonparametrics.independentsamples;

import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.common.dataobjects.FlatDataCollection;
import com.datumbox.framework.common.dataobjects.TransposeDataCollection;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.statistics.descriptivestatistics.Ranks;
import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Kruskal–Wallis one-way analysis of variance.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class KruskalWallis {
    
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
        AssociativeArray tiesCounter = Ranks.getRanksFromValues(associativeArray);
        int n = associativeArray.size();
        
        double C=0;
        //Correct for ties
        if(!tiesCounter.isEmpty()) {
            for(Object value : tiesCounter.values()) {
                double Ti = TypeInference.toDouble(value);
                C+=((Ti*Ti-1)*Ti); //faster than using pow()
            }
            C/=((n*n-1.0)*n); //faster than using pow()
        }
        //tiesCounter = null;

        //Important note! Remember that the "i" value is the number of the group while the j is the number of observation within the group and NOT the other way around.
        Map<Object, Integer> ni = new HashMap<>(); //stores the total number of observations in each group
        Map<Object, Double> Ridot = new HashMap<>(); //stores the sum of Ranks for each group
        
        for(Map.Entry<Object, Object> entry : associativeArray.entrySet()) {
            @SuppressWarnings("unchecked")
            Map.Entry<Object, Object> i_j = (Map.Entry<Object, Object>)entry.getKey();
            
            Object i = i_j.getKey(); //get i and j values
            Double rank = TypeInference.toDouble(entry.getValue());
            
            if(Ridot.containsKey(i)==false) { //if this "i" value is found for first time then define 
                Ridot.put(i, rank);
                ni.put(i, 1);
            }
            else {
                Ridot.put(i, Ridot.get(i)+rank);
                ni.put(i, ni.get(i)+1);
            }
        }
        //associativeArray=null;

        int k=ni.size();

        //Calculate Kruskal Wallis scrore based on the above
        double KWscore=0.0;
        
        for(Map.Entry<Object, Double> entry : Ridot.entrySet()) {
            Object i = entry.getKey();
            Double value = entry.getValue();
            
            KWscore+=value*value/ni.get(i);
        }
        //Ridot=null;
        //ni=null;


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
    private static double scoreToPvalue(double score, int k) {
        /*
        if(k<=3) {
            //calculate it from tables too small values
        }
        */
        return 1.0-ContinuousDistributions.chisquareCdf(score, k-1);
    }
}
