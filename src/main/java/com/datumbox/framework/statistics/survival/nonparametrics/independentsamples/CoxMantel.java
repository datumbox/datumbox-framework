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
package com.datumbox.framework.statistics.survival.nonparametrics.independentsamples;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.AssociativeArray2D;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.dataobjects.TransposeDataCollection;
import com.datumbox.framework.statistics.descriptivestatistics.CensoredDescriptives;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author bbriniotis
 */
public class CoxMantel {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    /**
     * Calculates the p-value of null Hypothesis.
     * 
     * @param transposeDataCollection
     * @return
     * @throws IllegalArgumentException 
     */
    public static double getPvalue(TransposeDataCollection transposeDataCollection) throws IllegalArgumentException {
        if(transposeDataCollection.size()!=2) {
            throw new IllegalArgumentException();
        }
        
        Object[] keys = transposeDataCollection.keySet().toArray();
        
        //counter of uncencored internalData in each group
        Map<Object, Integer> r = new HashMap<>();
        r.put(keys[0], 0);
        r.put(keys[1], 0);
        
        List<Object> mergedUncensoredData = new ArrayList<>();
        
        for(Map.Entry<Object, FlatDataCollection> entry : transposeDataCollection.entrySet()) {
            Object j = entry.getKey();
            FlatDataCollection flatDataCollection = entry.getValue();
            
            for(Object value : flatDataCollection) {
                if(!value.toString().endsWith(CensoredDescriptives.CENSORED_NUMBER_POSTFIX)) { //only the uncencored internalData
                    mergedUncensoredData.add(value);
                    r.put(j, r.get(j)+1);
                }
            }
        }
        Collections.sort(mergedUncensoredData,new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                double v1 = Dataset.toDouble(o1);
                double v2 = Dataset.toDouble(o2);
                if(v1>v2) {
                    return 1;
                }
                else if(v1<v2) {
                    return -1;
                }
                return 0;
            }
        });

        AssociativeArray2D testTable = new AssociativeArray2D();
        for(Object ti : mergedUncensoredData) {
            Double tiValue = Dataset.toDouble(ti);
            Object value = testTable.get2d(ti, "mi");
            if(value==null) {
                testTable.put2d(ti, "mi", 1);
                testTable.put2d(ti, "r0ti", 0);
                testTable.put2d(ti, "r1ti", 0);
            }
            else {
                testTable.put2d(ti, "mi", ((Integer)value) +1); 
                continue; //continue in order not to count twice the r*ti below
            }

            //calculate the r*ti: the number of observations (both cencored and uncencored) which are larger than ti
            for(Map.Entry<Object, FlatDataCollection> entry : transposeDataCollection.entrySet()) {
                Object j = entry.getKey();
                FlatDataCollection flatDataCollection = entry.getValue();
                
                int jIndex = (j == keys[1])?1:0;
                
                //calculate the r*ti: the number of observations (both cencored and uncencored) which are larger than $ti
                String rJtiKey="r"+String.valueOf(jIndex)+"ti";
                for(Object value2 : flatDataCollection) {
                    
                    double v;
                    String str = value2.toString();
                    if(str.endsWith(CensoredDescriptives.CENSORED_NUMBER_POSTFIX)) {
                        //censored internalData encoded as 4.3+ or -4.3+
                        v = Double.valueOf(str.substring(0,str.length()-CensoredDescriptives.CENSORED_NUMBER_POSTFIX.length())); //remove the trailing char and convert it to double
                    }
                    else {
                        //uncensored internalData
                        v = Dataset.toDouble(value2); //convert it to double
                    }
                    
                    if(v>=tiValue) {
                        testTable.put2d(ti, rJtiKey, (Integer)testTable.get2d(ti, rJtiKey) +1);
                    }
                }
            }
        }
        mergedUncensoredData = null;

        double U = (double)r.get(keys[1]);
        double VarU =0.0;
        for(Map.Entry<Object, AssociativeArray> entry : testTable.entrySet()) {
            //Object key = entry.getKey();
            AssociativeArray testRow = entry.getValue();
            
            double r0ti=testRow.getDouble("r0ti");
            double r1ti=testRow.getDouble("r1ti");
            double mi = testRow.getDouble("mi");
            double rti= r0ti+r1ti;
            double Ai= r1ti/rti;
            U-=mi*Ai;

            VarU+=((mi*(rti-mi))/(rti-1.0))*Ai*(1.0-Ai);
        }
        testTable = null;

        double Z=U/Math.sqrt(VarU);

        double pvalue = scoreToPvalue(Z);

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
        double pvalue = getPvalue(transposeDataCollection);

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
     * Returns the Pvalue for a particular score.
     * 
     * @param score
     * @return 
     */
    protected static double scoreToPvalue(double score) {
        return ContinuousDistributions.GaussCdf(score);
    }
    
}
