/**
 * Copyright (C) 2013-2016 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.core.statistics.survival.nonparametrics.independentsamples;

import com.datumbox.framework.common.dataobjects.*;
import com.datumbox.framework.core.statistics.descriptivestatistics.CensoredDescriptives;
import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Implementation of the Logrank non-parametric test.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Logrank {
    
    /**
     * Calculates the p-value of null Hypothesis.
     * 
     * @param transposeDataCollection
     * @return
     */
    public static double getPvalue(TransposeDataCollection transposeDataCollection) {
        if(transposeDataCollection.size()!=2) {
            throw new IllegalArgumentException("The collection must contain observations from 2 groups.");
        }
        
        Object[] keys = transposeDataCollection.keySet().toArray();
        
        //counter of uncencored internalData in each group
        Map<Object, Integer> n = new HashMap<>();
        n.put(keys[0], 0);
        n.put(keys[1], 0);
        
        Queue<Double> censoredData = new PriorityQueue<>();
        Queue<Double> uncensoredData = new PriorityQueue<>();
        for(Map.Entry<Object, FlatDataCollection> entry : transposeDataCollection.entrySet()) {
            Object j = entry.getKey();
            FlatDataCollection flatDataCollection = entry.getValue();
            
            for(Object value : flatDataCollection) {
                String str = value.toString();
                if(str.endsWith(CensoredDescriptives.CENSORED_NUMBER_POSTFIX)) {
                    //censored internalData encoded as 4.3+ or -4.3+
                    censoredData.add(Double.valueOf(str.substring(0,str.length()-CensoredDescriptives.CENSORED_NUMBER_POSTFIX.length()))); //remove the trailing char and convert it to double
                }
                else {
                    //uncensored internalData
                    uncensoredData.add(TypeInference.toDouble(value)); //convert it to double
                }
                n.put(j, n.get(j)+1);
            }
        }

        
        Double currentCensored = null;
        Double currentUncensored = null;
        AssociativeArray2D testTable = new AssociativeArray2D();
        
        do {
            if(currentCensored==null) {
                currentCensored=censoredData.poll();
            }
            if(currentUncensored==null) {
                currentUncensored=uncensoredData.poll();
            }
            
            Double ti;
            String key;
            if(currentUncensored == null) {
                key=currentCensored.toString().concat((CensoredDescriptives.CENSORED_NUMBER_POSTFIX));
                ti = currentCensored;
                currentCensored = null;
            }
            else if(currentCensored == null) {
                key=currentUncensored.toString();
                ti = currentUncensored;
                currentUncensored = null;
            }
            else if(currentCensored<currentUncensored) { //NOT EQUAL! Uncensored internalData of the same value are always larger
                key=currentCensored.toString().concat(CensoredDescriptives.CENSORED_NUMBER_POSTFIX);
                ti = currentCensored;
                currentCensored = null;
            }
            else {
                key=currentUncensored.toString();
                ti = currentUncensored;
                currentUncensored = null;
            }
            
            Object value = testTable.get2d(key, "mi");
            if(value==null) {
                testTable.put2d(key, "mi", 1);
                testTable.put2d(key, "rti", 0);
            }
            else {
                testTable.put2d(key, "mi", ((Integer)value) +1); 
                continue; //continue in order not to count twice the r*ti below
            }
            
            for(Map.Entry<Object, FlatDataCollection> entry : transposeDataCollection.entrySet()) {
                Object j = entry.getKey();
                FlatDataCollection flatDataCollection = entry.getValue();
                
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
                    
                    if(v>=ti) {
                        testTable.put2d(key, "rti", (Integer)testTable.get2d(key, "rti") +1);
                    }
                }
            }
                
        } 
        while(currentCensored!=null || currentUncensored!=null || 
              !censoredData.isEmpty() || !uncensoredData.isEmpty());
        
        //censoredData=null;
        //uncensoredData=null;
        

        double VarS=0.0;

        Object previousUncencoredKey = null;
        for(Map.Entry<Object, AssociativeArray> entry : testTable.entrySet()) {
            Object ti = entry.getKey();
            AssociativeArray testRow = entry.getValue();

            double previousUncencoredValue=0;
            
            Object tmp = testTable.get2d(previousUncencoredKey, "eti");
            if(tmp!=null) {
                previousUncencoredValue = TypeInference.toDouble(tmp);
            }
            
            if(!ti.toString().endsWith(CensoredDescriptives.CENSORED_NUMBER_POSTFIX)) { //uncensored
                double mi = testRow.getDouble("mi");
                double rti = testRow.getDouble("rti");
                double eti = previousUncencoredValue+mi/rti;
                
                testRow.put("eti", eti);
                testRow.put("wi", 1-eti);
                previousUncencoredKey=ti;
            }
            else { //censored
                testRow.put("wi", -previousUncencoredValue);
            }

            double wi = testRow.getDouble("wi");
            VarS+= testRow.getDouble("mi")*wi*wi;
        }


        double S=0.0;
        for(Object value : transposeDataCollection.get(keys[0])) { //if ti belongs to the first group
            Object key; //we must first convert the number into to double and then append the + if necessary. This is why it's converted like this.
            String str = value.toString();
            if(str.endsWith(CensoredDescriptives.CENSORED_NUMBER_POSTFIX)) {
                //censored internalData encoded as 4.3+ or -4.3+
                Double v = Double.valueOf(str.substring(0,str.length()-CensoredDescriptives.CENSORED_NUMBER_POSTFIX.length())); //remove the trailing char and convert it to double
                key = v.toString()+CensoredDescriptives.CENSORED_NUMBER_POSTFIX;
            }
            else {
                //uncensored internalData
                Double v = TypeInference.toDouble(value); //convert it to double
                key = v.toString();
            }
            double wi = TypeInference.toDouble(testTable.get2d(key, "wi"));
            S+= wi;
        }
        //testTable = null;
        
        double n0 = n.get(keys[0]).doubleValue();
        double n1 = n.get(keys[1]).doubleValue();
        
        VarS*=n0*n1/((n0+n1)*(n0+n1-1.0));

        double Z=S/Math.sqrt(VarS);
        
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
    private static double scoreToPvalue(double score) {
        return ContinuousDistributions.gaussCdf(score);
    }
    
}
