/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.statistics.survival.nonparametrics.independentsamples;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.AssociativeArray2D;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.dataobjects.TransposeDataCollection;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.framework.statistics.descriptivestatistics.CensoredDescriptives;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the Cox-Mantel non-parametric test.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class CoxMantel {
    
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
        Collections.sort(mergedUncensoredData, (Object o1, Object o2) -> {
            double v1 = TypeInference.toDouble(o1);
            double v2 = TypeInference.toDouble(o2);
            if(v1>v2) {
                return 1;
            }
            else if(v1<v2) {
                return -1;
            }
            return 0;
        });

        AssociativeArray2D testTable = new AssociativeArray2D();
        for(Object ti : mergedUncensoredData) {
            Double tiValue = TypeInference.toDouble(ti);
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
                
                int jIndex = (j.equals(keys[1]))?1:0;
                
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
                        v = TypeInference.toDouble(value2); //convert it to double
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
