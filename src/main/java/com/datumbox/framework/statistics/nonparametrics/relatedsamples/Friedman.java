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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author bbriniotis
 */
public class Friedman {
    /**
     * The internalDataCollections that are passed in this function are modified after the analysis. 
     * Don't pass directly the internalDataCollection unless you don't need them afterwards
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = false;
    
    /**
     * Calculates the p-value of null Hypothesis .
     * 
     * @param dataTable
     * @return 
     * @throws IllegalArgumentException 
     */
    public static double getPvalue(DataTable2D dataTable) throws IllegalArgumentException {
        if(dataTable.isValid()==false) {
            throw new IllegalArgumentException();
        }
        
        Map<Object, Double> columnSum = new HashMap<>(); //it stores the column sums that are required by the algorithm

        List<Integer> tiesCounter = new ArrayList<>(); //it stores the total ties occurred along with the values that were involved in the tie in each case

        //Find Ranks from Data Table. We don't store them to reduce memory consumption
        for(Map.Entry<Object, AssociativeArray> entry1 : dataTable.entrySet()) {
            Object i = entry1.getKey();
            //AssociativeArray row = DeepCopy.<AssociativeArray>cloneObject(entry1.getValue()); //copy to avoid changes on the structure of the original table!
            AssociativeArray row = entry1.getValue(); 
            
            //find the number of tied values and convert values into ranks
            AssociativeArray tiedValuesArray = Dataset.getRanksFromValues(row);
            
            for(Object value : tiedValuesArray.values()) {
                tiesCounter.add( ((Number)value).intValue() );
            }
            
            for(Map.Entry<Object, Object> entry2 : row.entrySet()) {
                Object j = entry2.getKey();
                
                double v = Dataset.toDouble(entry2.getValue());
                if(columnSum.containsKey(j)==false) {
                    columnSum.put(j, v);
                }
                else {
                    columnSum.put(j, columnSum.get(j)+v);
                }
            }

            tiedValuesArray = null;
            row = null;
        }

        int n=dataTable.size();
        int k=columnSum.size();

        double Fscore=0;
        for(Double value : columnSum.values()) {
            Fscore+=value*value;
        }
        columnSum = null;
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
    protected static double scoreToPvalue(double score, int k) {
        if(k<=5) {
            //calculate it from tables too small values
            //EXPAND: waiting for tables from Dimaki
        }
        return 1-ContinuousDistributions.ChisquareCdf(score, k-1);
    }    
}
