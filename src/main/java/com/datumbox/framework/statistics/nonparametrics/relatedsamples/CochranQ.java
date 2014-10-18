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
public class CochranQ {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
        
    /**
     * Calculates the p-value of null Hypothesis.
     * 
     * @param dataTable
     * @return 
     * @throws IllegalArgumentException 
     */
    public static double getPvalue(DataTable2D dataTable) throws IllegalArgumentException {
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
                
                double v = Dataset.toDouble(entry2.getValue());
                
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

        //Calculating Qscore        
        double SumOfSquaredXdotJ=0.0;
        for(Double value : XdotJ.values()) {
            SumOfSquaredXdotJ+=value*value;
        }
        XdotJ=null;

        double SumOfSquaredXIdot=0;
        double SumOfXIdot=0;
        for(Double value : XIdot.values()) {
            SumOfSquaredXIdot+=value*value;
            SumOfXIdot+=value;
        }
        XIdot=null;

        double Qscore=(k-1.0)*(k*SumOfSquaredXdotJ-Xdotdot*Xdotdot)/(k*SumOfXIdot-SumOfSquaredXIdot);

        double pvalue = scoreToPvalue(Qscore, n, k);

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
     * @param n
     * @param k
     * @return 
     */
    protected static double scoreToPvalue(double score, int n, int k) {
        if(n<4 || n*k<24) {
            //calculate it from tables too small values
            //EXPAND: waiting for tables from Dimaki
        }
        return 1.0-ContinuousDistributions.ChisquareCdf(score, k-1);
    }
}
