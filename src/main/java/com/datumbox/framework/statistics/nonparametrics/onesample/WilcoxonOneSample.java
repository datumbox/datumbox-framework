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
package com.datumbox.framework.statistics.nonparametrics.onesample;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author bbriniotis
 */
public class WilcoxonOneSample {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    /**
     * Calculates the p-value of null Hypothesis 
     * 
     * @param flatDataCollection
     * @param median
     * @return
     * @throws IllegalArgumentException 
     */
    public static double getPvalue(FlatDataCollection flatDataCollection, double median) throws IllegalArgumentException {
        int n=0;
        AssociativeArray Di = new AssociativeArray(new LinkedHashMap<>());
        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            double delta=it.next()-median;

            if(delta==0.0) {
                continue; //don't count it at all
            }

            String key="+";
            if(delta<0) {
                key="-";
            }
            Di.put(key+String.valueOf(n), Math.abs(delta));
            ++n;
        }
        if(n<=0) {
            throw new IllegalArgumentException();
        }

        //converts the values of the table with its Ranks
        Di.toFlatDataList();
        Dataset.getRanksFromValues(Di);
        double W=0.0;
        for(Map.Entry<Object, Object> entry : Di.entrySet()) {
            String key = entry.getKey().toString();
            Double rank = Dataset.toDouble(entry.getValue());
        
            if(key.charAt(0)=='+') {
                W+=rank;
            }
        }

        double pvalue= scoreToPvalue(W, n);

        return pvalue;
    }
    
    /**
     * Tests the rejection of null Hypothesis for a particular confidence level 
     * 
     * @param flatDataCollection
     * @param median
     * @param is_twoTailed
     * @param aLevel
     * @return 
     */
    public static boolean test(FlatDataCollection flatDataCollection, double median, boolean is_twoTailed, double aLevel) {        
        double pvalue= getPvalue(flatDataCollection, median);

        boolean rejectH0=false;

        double a=aLevel;
        if(is_twoTailed) { //if to tailed test then split the statistical significance in half
            a=aLevel/2.0;
        }
        if(pvalue<=a || pvalue>=(1.0-a)) {
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
        if(n<=20) {
            //calculate it from binomial distribution
            //EXPAND: waiting for tables from Dimaki
        }

        double mean=n*(n+1.0)/4.0;
        double variable=n*(n+1.0)*(2.0*n+1.0)/24.0;

        double z=(score-mean)/Math.sqrt(variable);

        return ContinuousDistributions.GaussCdf(z);
    }
}
