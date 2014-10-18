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
import com.datumbox.common.dataobjects.DataTable2D;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author bbriniotis
 */
public class Chisquare {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    /**
     * Calculates the score of Chisquare test.
     * 
     * @param dataTable
     * @return 
     * @throws IllegalArgumentException 
     */
    public static AssociativeArray getScore(DataTable2D dataTable) throws IllegalArgumentException {
        if(dataTable.isValid()==false) {
            throw new IllegalArgumentException();
        }
        //Estimate marginal scores and sum
        Map<Object, Double> XdotJ = new HashMap<>();
        Map<Object, Double> XIdot = new HashMap<>();
        double Xdotdot=0.0;
        
        for(Map.Entry<Object, AssociativeArray> entry1 : dataTable.entrySet()) {
            Object i = entry1.getKey();
            AssociativeArray row = entry1.getValue();
            
            for(Map.Entry<Object, Object> entry2 : row.entrySet()) {
                Object j = entry2.getKey();
                Object value = entry2.getValue();
                
                double v = Dataset.toDouble(value);
                
                //Summing the columns
                if(XdotJ.containsKey(j)==false) {
                    XdotJ.put(j, v);
                }
                else {
                    XdotJ.put(j, XdotJ.get(j) + v);
                }

                //Summing the rows
                if(XIdot.containsKey(i)==false) {
                    XIdot.put(i, v);
                }
                else {
                    XIdot.put(i, XIdot.get(i) + v);
                }
                
                Xdotdot+=v;
            }
        }

        int k=XdotJ.size();
        int n=XIdot.size();

        //Calculating Chisquare score
        double ChisquareScore=0.0;        
        if(k==2 && n==2) { //if 2x2 then perform the Yates correction. Make this check outside the loops to make it faster
            for(Map.Entry<Object, AssociativeArray> entry1 : dataTable.entrySet()) {
                Object i = entry1.getKey();
                AssociativeArray row = entry1.getValue();

                for(Map.Entry<Object, Object> entry2 : row.entrySet()) {
                    Object j = entry2.getKey();
                    Object value = entry2.getValue();

                    double v = Dataset.toDouble(value);

                    //expected value under null hypothesis
                    double eij = XIdot.get(i)*XdotJ.get(j)/Xdotdot;
                    if(eij==0) {
                        continue;
                    }
                    ChisquareScore+=Math.pow((Math.abs(v-eij)-0.5),2)/eij;
                }
            }
        }
        else {
            for(Map.Entry<Object, AssociativeArray> entry1 : dataTable.entrySet()) {
                Object i = entry1.getKey();
                AssociativeArray row = entry1.getValue();

                for(Map.Entry<Object, Object> entry2 : row.entrySet()) {
                    Object j = entry2.getKey();
                    Object value = entry2.getValue();
            
                    double v = Dataset.toDouble(value);

                    //expected value under null hypothesis
                    double eij = XIdot.get(i)*XdotJ.get(j)/Xdotdot;

                    ChisquareScore+=Math.pow((v-eij),2)/eij;
                }
            }
        }
        XdotJ = null;
        XIdot = null;

        AssociativeArray result = new AssociativeArray();
        result.put("k", k);
        result.put("n", n);
        result.put("score", ChisquareScore);
        
        return result;
    }
    
    /**
     * Convenience method to get the score of Chisquare.
     * @param dataTable
     * @return 
     */
    public static double getScoreValue(DataTable2D dataTable) {

        AssociativeArray result = getScore(dataTable);
        double score = result.getDouble("score");

        return score;
    }
    
    /**
     * Calculates the p-value of null Hypothesis 
     * 
     * @param dataTable
     * @return 
     */
    public static double getPvalue(DataTable2D dataTable) {

        AssociativeArray result = getScore(dataTable);
        double score = result.getDouble("score");
        int n = result.getDouble("n").intValue();
        int k = result.getDouble("k").intValue();
        
        double pvalue = scoreToPvalue(score, n, k);

        return pvalue;
    }

    /**
     * Tests the rejection of null Hypothesis for a particular confidence level 
     * 
     * @param dataTable
     * @param aLevel
     * @return 
     */
    public static boolean test(DataTable2D dataTable, double aLevel) {
        double pvalue = getPvalue(dataTable);

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
     * @param n
     * @param k
     * @return 
     */
    protected static double scoreToPvalue(double score, int n, int k) {
        return 1.0-ContinuousDistributions.ChisquareCdf(score, (n-1)*(k-1));
    }
    
}
