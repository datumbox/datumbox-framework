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
import java.util.Map;

/**
 * Mann-Whitney non-parametric test for independent samples.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MannWhitney {
    
    /**
     * Calculates the p-value of null Hypothesis 
     * 
     * @param transposeDataCollection
     * @return 
     */
    public static double getPvalue(TransposeDataCollection transposeDataCollection) {
        if(transposeDataCollection.size()!=2) {
            throw new IllegalArgumentException("The collection must contain observations from 2 groups.");
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
            //largeIndex=0;
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
        Ranks.getRanksFromValues(associativeArray);

        //sum up the scores of the smallest sample
        double MWscore=0.0;
        
        for(Map.Entry<Object,Object> entry : associativeArray.entrySet()) {
            @SuppressWarnings("unchecked")
            Map.Entry<Object, Object> i_j = (Map.Entry<Object, Object>)entry.getKey();
            Object i = i_j.getKey(); //get i and j values
            
            if(i.equals(keys[smallIndex])) { //if it belongs to the FIRST group (small group)
                Double rank = TypeInference.toDouble(entry.getValue());
                MWscore+=rank; //add the score
            }
        }
        //associativeArray=null;

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
    private static double scoreToPvalue(double score, int n1, int n2) {
        /*
        if(n1<=10 && n2<=10) {
            //calculate it from tables too small values
        }
        */
        double mean=n1*(n1+n2+1.0)/2.0;
        double variable=n1*n2*(n1+n2+1.0)/12.0;

        double z=(score-mean)/Math.sqrt(variable);

        return ContinuousDistributions.gaussCdf(z);
    }
}
