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
package com.datumbox.framework.core.statistics.nonparametrics.onesample;

import com.datumbox.framework.common.dataobjects.AssociativeArray2D;
import com.datumbox.framework.common.dataobjects.FlatDataCollection;
import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;

import java.util.HashMap;
import java.util.Map;

/**
 * Wald-Wolfowitz runs test.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class WaldWolfowitz {

    private static final AssociativeArray2D CRITICAL_VALUES = new AssociativeArray2D();
    
    static {
        CRITICAL_VALUES.put2d(5, 4, "2,9"); CRITICAL_VALUES.put2d(5, 5, "2,10"); 
        CRITICAL_VALUES.put2d(6, 3, "2,"); CRITICAL_VALUES.put2d(6, 4, "2,9"); CRITICAL_VALUES.put2d(6, 5, "3,10"); CRITICAL_VALUES.put2d(6, 6, "3,11"); 
        CRITICAL_VALUES.put2d(7, 3, "2,"); CRITICAL_VALUES.put2d(7, 4, "2,"); CRITICAL_VALUES.put2d(7, 5, "3,11"); CRITICAL_VALUES.put2d(7, 6, "3,12"); CRITICAL_VALUES.put2d(7, 7, "3,13"); 
        CRITICAL_VALUES.put2d(8, 3, "2,"); CRITICAL_VALUES.put2d(8, 4, "3,"); CRITICAL_VALUES.put2d(8, 5, "3,11"); CRITICAL_VALUES.put2d(8, 6, "3,12"); CRITICAL_VALUES.put2d(8, 7, "4,13"); CRITICAL_VALUES.put2d(8, 8, "4,14"); 
        CRITICAL_VALUES.put2d(9, 3, "2,"); CRITICAL_VALUES.put2d(9, 4, "3,"); CRITICAL_VALUES.put2d(9, 5, "3,"); CRITICAL_VALUES.put2d(9, 6, "4,13"); CRITICAL_VALUES.put2d(9, 7, "4,14"); CRITICAL_VALUES.put2d(9, 8, "5,14"); CRITICAL_VALUES.put2d(9, 9, "5,15"); 
        CRITICAL_VALUES.put2d(10, 3, "2,"); CRITICAL_VALUES.put2d(10, 4, "3,"); CRITICAL_VALUES.put2d(10, 5, "3,"); CRITICAL_VALUES.put2d(10, 6, "4,13"); CRITICAL_VALUES.put2d(10, 7, "5,14"); CRITICAL_VALUES.put2d(10, 8, "5,15"); CRITICAL_VALUES.put2d(10, 9, "5,16"); CRITICAL_VALUES.put2d(10, 10, "6,16"); 
        CRITICAL_VALUES.put2d(11, 3, "2,"); CRITICAL_VALUES.put2d(11, 4, "3,"); CRITICAL_VALUES.put2d(11, 5, "4,"); CRITICAL_VALUES.put2d(11, 6, "4,13"); CRITICAL_VALUES.put2d(11, 7, "5,14"); CRITICAL_VALUES.put2d(11, 8, "5,15"); CRITICAL_VALUES.put2d(11, 9, "6,16"); CRITICAL_VALUES.put2d(11, 10, "6,17"); CRITICAL_VALUES.put2d(11, 11, "7,17"); 
        CRITICAL_VALUES.put2d(12, 2, "2,"); CRITICAL_VALUES.put2d(12, 3, "2,"); CRITICAL_VALUES.put2d(12, 4, "3,"); CRITICAL_VALUES.put2d(12, 5, "4,"); CRITICAL_VALUES.put2d(12, 6, "4,13"); CRITICAL_VALUES.put2d(12, 7, "5,14"); CRITICAL_VALUES.put2d(12, 8, "6,16"); CRITICAL_VALUES.put2d(12, 9, "6,16"); CRITICAL_VALUES.put2d(12, 10, "7,17"); CRITICAL_VALUES.put2d(12, 11, "7,18"); CRITICAL_VALUES.put2d(12, 12, "7,19"); 
        CRITICAL_VALUES.put2d(13, 2, "2,"); CRITICAL_VALUES.put2d(13, 3, "2,"); CRITICAL_VALUES.put2d(13, 4, "3,"); CRITICAL_VALUES.put2d(13, 5, "4,"); CRITICAL_VALUES.put2d(13, 6, "5,"); CRITICAL_VALUES.put2d(13, 7, "5,15"); CRITICAL_VALUES.put2d(13, 8, "6,16"); CRITICAL_VALUES.put2d(13, 9, "6,17"); CRITICAL_VALUES.put2d(13, 10, "7,18"); CRITICAL_VALUES.put2d(13, 11, "7,19"); CRITICAL_VALUES.put2d(13, 12, "8,19"); CRITICAL_VALUES.put2d(13, 13, "8,20"); 
        CRITICAL_VALUES.put2d(14, 2, "2,"); CRITICAL_VALUES.put2d(14, 3, "2,"); CRITICAL_VALUES.put2d(14, 4, "3,"); CRITICAL_VALUES.put2d(14, 5, "4,"); CRITICAL_VALUES.put2d(14, 6, "5,"); CRITICAL_VALUES.put2d(14, 7, "5,15"); CRITICAL_VALUES.put2d(14, 8, "6,16"); CRITICAL_VALUES.put2d(14, 9, "7,17"); CRITICAL_VALUES.put2d(14, 10, "7,18"); CRITICAL_VALUES.put2d(14, 11, "8,19"); CRITICAL_VALUES.put2d(14, 12, "8,20"); CRITICAL_VALUES.put2d(14, 13, "9,20"); CRITICAL_VALUES.put2d(14, 14, "9,21"); 
        CRITICAL_VALUES.put2d(15, 2, "2,"); CRITICAL_VALUES.put2d(15, 3, "3,"); CRITICAL_VALUES.put2d(15, 4, "3,"); CRITICAL_VALUES.put2d(15, 5, "4,"); CRITICAL_VALUES.put2d(15, 6, "5,"); CRITICAL_VALUES.put2d(15, 7, "6,15"); CRITICAL_VALUES.put2d(15, 8, "6,16"); CRITICAL_VALUES.put2d(15, 9, "7,18"); CRITICAL_VALUES.put2d(15, 10, "7,18"); CRITICAL_VALUES.put2d(15, 11, "8,19"); CRITICAL_VALUES.put2d(15, 12, "8,20"); CRITICAL_VALUES.put2d(15, 13, "9,21"); CRITICAL_VALUES.put2d(15, 14, "9,22"); CRITICAL_VALUES.put2d(15, 15, "10,22"); 
        CRITICAL_VALUES.put2d(16, 2, "2,"); CRITICAL_VALUES.put2d(16, 3, "3,"); CRITICAL_VALUES.put2d(16, 4, "4,"); CRITICAL_VALUES.put2d(16, 5, "4,"); CRITICAL_VALUES.put2d(16, 6, "5,"); CRITICAL_VALUES.put2d(16, 7, "6,"); CRITICAL_VALUES.put2d(16, 8, "6,17"); CRITICAL_VALUES.put2d(16, 9, "7,18"); CRITICAL_VALUES.put2d(16, 10, "8,19"); CRITICAL_VALUES.put2d(16, 11, "8,20"); CRITICAL_VALUES.put2d(16, 12, "9,21"); CRITICAL_VALUES.put2d(16, 13, "9,21"); CRITICAL_VALUES.put2d(16, 14, "10,22"); CRITICAL_VALUES.put2d(16, 15, "10,23"); CRITICAL_VALUES.put2d(16, 16, "11,23"); 
        CRITICAL_VALUES.put2d(17, 2, "2,"); CRITICAL_VALUES.put2d(17, 3, "3,"); CRITICAL_VALUES.put2d(17, 4, "4,"); CRITICAL_VALUES.put2d(17, 5, "4,"); CRITICAL_VALUES.put2d(17, 6, "5,"); CRITICAL_VALUES.put2d(17, 7, "6,"); CRITICAL_VALUES.put2d(17, 8, "7,17"); CRITICAL_VALUES.put2d(17, 9, "7,18"); CRITICAL_VALUES.put2d(17, 10, "8,19"); CRITICAL_VALUES.put2d(17, 11, "9,20"); CRITICAL_VALUES.put2d(17, 12, "9,21"); CRITICAL_VALUES.put2d(17, 13, "10,22"); CRITICAL_VALUES.put2d(17, 14, "10,23"); CRITICAL_VALUES.put2d(17, 15, "11,23"); CRITICAL_VALUES.put2d(17, 16, "11,24"); CRITICAL_VALUES.put2d(17, 17, "11,25"); 
        CRITICAL_VALUES.put2d(18, 2, "2,"); CRITICAL_VALUES.put2d(18, 3, "3,"); CRITICAL_VALUES.put2d(18, 4, "4,"); CRITICAL_VALUES.put2d(18, 5, "5,"); CRITICAL_VALUES.put2d(18, 6, "5,"); CRITICAL_VALUES.put2d(18, 7, "6,"); CRITICAL_VALUES.put2d(18, 8, "7,17"); CRITICAL_VALUES.put2d(18, 9, "8,18"); CRITICAL_VALUES.put2d(18, 10, "8,19"); CRITICAL_VALUES.put2d(18, 11, "9,20"); CRITICAL_VALUES.put2d(18, 12, "9,21"); CRITICAL_VALUES.put2d(18, 13, "10,22"); CRITICAL_VALUES.put2d(18, 14, "10,23"); CRITICAL_VALUES.put2d(18, 15, "11,24"); CRITICAL_VALUES.put2d(18, 16, "11,25"); CRITICAL_VALUES.put2d(18, 17, "12,25"); CRITICAL_VALUES.put2d(18, 18, "12,26"); 
        CRITICAL_VALUES.put2d(19, 2, "2,"); CRITICAL_VALUES.put2d(19, 3, "3,"); CRITICAL_VALUES.put2d(19, 4, "4,"); CRITICAL_VALUES.put2d(19, 5, "5,"); CRITICAL_VALUES.put2d(19, 6, "6,"); CRITICAL_VALUES.put2d(19, 7, "6,"); CRITICAL_VALUES.put2d(19, 8, "7,17"); CRITICAL_VALUES.put2d(19, 9, "8,18"); CRITICAL_VALUES.put2d(19, 10, "8,20"); CRITICAL_VALUES.put2d(19, 11, "9,21"); CRITICAL_VALUES.put2d(19, 12, "10,22"); CRITICAL_VALUES.put2d(19, 13, "10,23"); CRITICAL_VALUES.put2d(19, 14, "11,23"); CRITICAL_VALUES.put2d(19, 15, "11,24"); CRITICAL_VALUES.put2d(19, 16, "12,25"); CRITICAL_VALUES.put2d(19, 17, "12,26"); CRITICAL_VALUES.put2d(19, 18, "13,26"); CRITICAL_VALUES.put2d(19, 19, "13,27"); 
        CRITICAL_VALUES.put2d(20, 2, "2,"); CRITICAL_VALUES.put2d(20, 3, "3,"); CRITICAL_VALUES.put2d(20, 4, "4,"); CRITICAL_VALUES.put2d(20, 5, "5,"); CRITICAL_VALUES.put2d(20, 6, "6,"); CRITICAL_VALUES.put2d(20, 7, "6,"); CRITICAL_VALUES.put2d(20, 8, "7,17"); CRITICAL_VALUES.put2d(20, 9, "8,18"); CRITICAL_VALUES.put2d(20, 10, "9,20"); CRITICAL_VALUES.put2d(20, 11, "9,21"); CRITICAL_VALUES.put2d(20, 12, "10,22"); CRITICAL_VALUES.put2d(20, 13, "10,23"); CRITICAL_VALUES.put2d(20, 14, "11,24"); CRITICAL_VALUES.put2d(20, 15, "12,25"); CRITICAL_VALUES.put2d(20, 16, "12,25"); CRITICAL_VALUES.put2d(20, 17, "13,26"); CRITICAL_VALUES.put2d(20, 18, "13,27"); CRITICAL_VALUES.put2d(20, 19, "13,27"); CRITICAL_VALUES.put2d(20, 20, "14,28");
    }
    
    /**
     * Tests the rejection of null Hypothesis for a particular confidence level.
     * 
     * @param flatDataCollection
     * @param aLevel
     * @return
     */
    public static boolean test(FlatDataCollection flatDataCollection, double aLevel) {       
            //Note! This test works only for 2 valued internalData. If you have other types of internalData you must transform them into 2 valued internalData.
            int U=0;
            
            Map<Object, Integer> Ni = new HashMap<>();

            Object previousValue=null;
            //Estimate the number of Runs
            for(Object x : flatDataCollection) {
                if(previousValue!=null && !previousValue.equals(x)) {
                    ++U; //if the next value changes count it as different run
                }
                previousValue=x;

                if(Ni.containsKey(x)==false) {
                    Ni.put(x, 1);
                }
                else {
                    Ni.put(x, Ni.get(x) + 1);//keep track of the occurences in each category
                }
            }
            
            //the internalData must be two valued in this test
            if(Ni.size()!=2) {
                throw new IllegalArgumentException("The collection must contain observations from 2 groups.");
            }
            
            Object[] keys = Ni.keySet().toArray();
            int n1= Ni.get(keys[0]);
            int n2= Ni.get(keys[1]);
            //keys = null;
            //Ni = null;

            boolean rejectH0= checkCriticalValue((double)U, n1, n2, aLevel);

            return rejectH0;
    }   

    /**
     * Checks the Critical Value to determine if the Hypothesis should be rejected.
     * 
     * @param score
     * @param n1
     * @param n2
     * @param aLevel
     * @return 
     */
    private static boolean checkCriticalValue(double score, int n1, int n2, double aLevel) {
            boolean rejected=false;

            int n=n1+n2;

            if(n1<=20 && n2<=20 && aLevel==0.05) { //This works only if we have low values on n1 and n2 and for specific levels of a
                int key1=Math.max(n1,n2); //put the maximum in the first key (this is due to the way the table is contructed)
                int key2=Math.min(n1,n2); //put the minimum in the second key

                Object value = CRITICAL_VALUES.get2d(key1, key2);
                if(value!=null) { //check if we have the value in the table

                    String[] lowuplimit=String.valueOf(value).split(",");

                    int low = Integer.parseInt(lowuplimit[0]);
                    int high = n;
                    if(lowuplimit.length==2) {
                        high = Integer.parseInt(lowuplimit[1]);
                    }

                    if(score<=low || score>=high) { //if the score is outside the confidence intervals reject null hypothesis
                        rejected=true;
                    }

                    return rejected;
                }
            }


            //Estimate the mean and Variance under Null Hypothesis
            double mean= 2.0*n1*n2/((double)n1+n2) +1.0;
            double variance = 2.0*n1*n2*(2.0*n1*n2 - n1 - n2)/(n*n*(n-1.0));

            //Normalize U
            double z=(score-mean)/Math.sqrt(variance);

            //Get its pvalue
            double pvalue=ContinuousDistributions.gaussCdf(z);

            double a=aLevel/2; //always tailed test, so split the statistical significance in half
            if(pvalue<=a || pvalue>=(1-a)) {
                    rejected=true; 
            }

            return rejected;
    }

}
