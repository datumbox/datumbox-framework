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

import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.common.dataobjects.DataTable2D;
import com.datumbox.framework.common.dataobjects.FlatDataCollection;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * kolmogorov–Smirnov's test for equality of distributions.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class KolmogorovSmirnovOneSample {
    
    private static final DataTable2D CRITICAL_VALUES = new DataTable2D();
    
    static {
        //The first key stores the aLevel (statistical significance).
        //The second one stores the number of observations in sample. The values are from 0-40.
        //NOTE: On second key, on 0 we store the approximation for n>40
        CRITICAL_VALUES.put2d(0.2, 0, 1.07); CRITICAL_VALUES.put2d(0.2, 1, 0.9); CRITICAL_VALUES.put2d(0.2, 2, 0.684); CRITICAL_VALUES.put2d(0.2, 3, 0.565); CRITICAL_VALUES.put2d(0.2, 4, 0.493); CRITICAL_VALUES.put2d(0.2, 5, 0.447); CRITICAL_VALUES.put2d(0.2, 6, 0.41); CRITICAL_VALUES.put2d(0.2, 7, 0.381); CRITICAL_VALUES.put2d(0.2, 8, 0.358); CRITICAL_VALUES.put2d(0.2, 9, 0.339); CRITICAL_VALUES.put2d(0.2, 10, 0.323); CRITICAL_VALUES.put2d(0.2, 11, 0.308); CRITICAL_VALUES.put2d(0.2, 12, 0.296); CRITICAL_VALUES.put2d(0.2, 13, 0.285); CRITICAL_VALUES.put2d(0.2, 14, 0.275); CRITICAL_VALUES.put2d(0.2, 15, 0.266); CRITICAL_VALUES.put2d(0.2, 16, 0.258); CRITICAL_VALUES.put2d(0.2, 17, 0.25); CRITICAL_VALUES.put2d(0.2, 18, 0.244); CRITICAL_VALUES.put2d(0.2, 19, 0.237); CRITICAL_VALUES.put2d(0.2, 20, 0.232); CRITICAL_VALUES.put2d(0.2, 21, 0.226); CRITICAL_VALUES.put2d(0.2, 22, 0.221); CRITICAL_VALUES.put2d(0.2, 23, 0.216 ); CRITICAL_VALUES.put2d(0.2, 24, 0.212); CRITICAL_VALUES.put2d(0.2, 25, 0.208); CRITICAL_VALUES.put2d(0.2, 26, 0.204); CRITICAL_VALUES.put2d(0.2, 27, 0.2); CRITICAL_VALUES.put2d(0.2, 28, 0.197); CRITICAL_VALUES.put2d(0.2, 29, 0.193); CRITICAL_VALUES.put2d(0.2, 30, 0.19); CRITICAL_VALUES.put2d(0.2, 31, 0.187); CRITICAL_VALUES.put2d(0.2, 32, 0.184); CRITICAL_VALUES.put2d(0.2, 33, 0.182); CRITICAL_VALUES.put2d(0.2, 34, 0.179); CRITICAL_VALUES.put2d(0.2, 35, 0.177); CRITICAL_VALUES.put2d(0.2, 36, 0.174); CRITICAL_VALUES.put2d(0.2, 37, 0.172); CRITICAL_VALUES.put2d(0.2, 38, 0.17); CRITICAL_VALUES.put2d(0.2, 39, 0.168); CRITICAL_VALUES.put2d(0.2, 40, 0.165);
        CRITICAL_VALUES.put2d(0.1, 0, 1.22); CRITICAL_VALUES.put2d(0.1, 1, 0.95); CRITICAL_VALUES.put2d(0.1, 2, 0.776); CRITICAL_VALUES.put2d(0.1, 3, 0.636); CRITICAL_VALUES.put2d(0.1, 4, 0.565); CRITICAL_VALUES.put2d(0.1, 5, 0.509); CRITICAL_VALUES.put2d(0.1, 6, 0.468); CRITICAL_VALUES.put2d(0.1, 7, 0.436); CRITICAL_VALUES.put2d(0.1, 8, 0.41); CRITICAL_VALUES.put2d(0.1, 9, 0.387); CRITICAL_VALUES.put2d(0.1, 10, 0.369); CRITICAL_VALUES.put2d(0.1, 11, 0.352); CRITICAL_VALUES.put2d(0.1, 12, 0.338); CRITICAL_VALUES.put2d(0.1, 13, 0.325); CRITICAL_VALUES.put2d(0.1, 14, 0.314); CRITICAL_VALUES.put2d(0.1, 15, 0.304); CRITICAL_VALUES.put2d(0.1, 16, 0.295); CRITICAL_VALUES.put2d(0.1, 17, 0.286); CRITICAL_VALUES.put2d(0.1, 18, 0.279); CRITICAL_VALUES.put2d(0.1, 19, 0.271); CRITICAL_VALUES.put2d(0.1, 20, 0.265); CRITICAL_VALUES.put2d(0.1, 21, 0.259); CRITICAL_VALUES.put2d(0.1, 22, 0.253); CRITICAL_VALUES.put2d(0.1, 23, 0.247); CRITICAL_VALUES.put2d(0.1, 24, 0.242); CRITICAL_VALUES.put2d(0.1, 25, 0.238); CRITICAL_VALUES.put2d(0.1, 26, 0.233); CRITICAL_VALUES.put2d(0.1, 27, 0.229); CRITICAL_VALUES.put2d(0.1, 28, 0.225); CRITICAL_VALUES.put2d(0.1, 29, 0.221); CRITICAL_VALUES.put2d(0.1, 30, 0.218); CRITICAL_VALUES.put2d(0.1, 31, 0.214); CRITICAL_VALUES.put2d(0.1, 32, 0.211); CRITICAL_VALUES.put2d(0.1, 33, 0.208); CRITICAL_VALUES.put2d(0.1, 34, 0.205); CRITICAL_VALUES.put2d(0.1, 35, 0.202); CRITICAL_VALUES.put2d(0.1, 36, 0.199); CRITICAL_VALUES.put2d(0.1, 37, 0.196); CRITICAL_VALUES.put2d(0.1, 38, 0.194); CRITICAL_VALUES.put2d(0.1, 39, 0.191); CRITICAL_VALUES.put2d(0.1, 40, 0.189); 
        CRITICAL_VALUES.put2d(0.05, 0, 1.36); CRITICAL_VALUES.put2d(0.05, 1, 0.975); CRITICAL_VALUES.put2d(0.05, 2, 0.842); CRITICAL_VALUES.put2d(0.05, 3, 0.708); CRITICAL_VALUES.put2d(0.05, 4, 0.624); CRITICAL_VALUES.put2d(0.05, 5, 0.563); CRITICAL_VALUES.put2d(0.05, 6, 0.519); CRITICAL_VALUES.put2d(0.05, 7, 0.483); CRITICAL_VALUES.put2d(0.05, 8, 0.454); CRITICAL_VALUES.put2d(0.05, 9, 0.43); CRITICAL_VALUES.put2d(0.05, 10, 0.409); CRITICAL_VALUES.put2d(0.05, 11, 0.391); CRITICAL_VALUES.put2d(0.05, 12, 0.375); CRITICAL_VALUES.put2d(0.05, 13, 0.361); CRITICAL_VALUES.put2d(0.05, 14, 0.349); CRITICAL_VALUES.put2d(0.05, 15, 0.338); CRITICAL_VALUES.put2d(0.05, 16, 0.327); CRITICAL_VALUES.put2d(0.05, 17, 0.318); CRITICAL_VALUES.put2d(0.05, 18, 0.309); CRITICAL_VALUES.put2d(0.05, 19, 0.301); CRITICAL_VALUES.put2d(0.05, 20, 0.294); CRITICAL_VALUES.put2d(0.05, 21, 0.287); CRITICAL_VALUES.put2d(0.05, 22, 0.281); CRITICAL_VALUES.put2d(0.05, 23, 0.275); CRITICAL_VALUES.put2d(0.05, 24, 0.269); CRITICAL_VALUES.put2d(0.05, 25, 0.264); CRITICAL_VALUES.put2d(0.05, 26, 0.259); CRITICAL_VALUES.put2d(0.05, 27, 0.254); CRITICAL_VALUES.put2d(0.05, 28, 0.25); CRITICAL_VALUES.put2d(0.05, 29, 0.246); CRITICAL_VALUES.put2d(0.05, 30, 0.242); CRITICAL_VALUES.put2d(0.05, 31, 0.238); CRITICAL_VALUES.put2d(0.05, 32, 0.234); CRITICAL_VALUES.put2d(0.05, 33, 0.231); CRITICAL_VALUES.put2d(0.05, 34, 0.227); CRITICAL_VALUES.put2d(0.05, 35, 0.224); CRITICAL_VALUES.put2d(0.05, 36, 0.221); CRITICAL_VALUES.put2d(0.05, 37, 0.218); CRITICAL_VALUES.put2d(0.05, 38, 0.215); CRITICAL_VALUES.put2d(0.05, 39, 0.213); CRITICAL_VALUES.put2d(0.05, 40, 0.21); 
        CRITICAL_VALUES.put2d(0.02, 0, 1.52); CRITICAL_VALUES.put2d(0.02, 1, 0.99); CRITICAL_VALUES.put2d(0.02, 2, 0.9); CRITICAL_VALUES.put2d(0.02, 3, 0.785); CRITICAL_VALUES.put2d(0.02, 4, 0.689); CRITICAL_VALUES.put2d(0.02, 5, 0.627); CRITICAL_VALUES.put2d(0.02, 6, 0.577); CRITICAL_VALUES.put2d(0.02, 7, 0.538); CRITICAL_VALUES.put2d(0.02, 8, 0.507); CRITICAL_VALUES.put2d(0.02, 9, 0.48); CRITICAL_VALUES.put2d(0.02, 10, 0.457); CRITICAL_VALUES.put2d(0.02, 11, 0.437); CRITICAL_VALUES.put2d(0.02, 12, 0.419); CRITICAL_VALUES.put2d(0.02, 13, 0.404); CRITICAL_VALUES.put2d(0.02, 14, 0.39); CRITICAL_VALUES.put2d(0.02, 15, 0.377); CRITICAL_VALUES.put2d(0.02, 16, 0.366); CRITICAL_VALUES.put2d(0.02, 17, 0.355); CRITICAL_VALUES.put2d(0.02, 18, 0.346); CRITICAL_VALUES.put2d(0.02, 19, 0.337); CRITICAL_VALUES.put2d(0.02, 20, 0.329); CRITICAL_VALUES.put2d(0.02, 21, 0.321); CRITICAL_VALUES.put2d(0.02, 22, 0.314); CRITICAL_VALUES.put2d(0.02, 23, 0.307); CRITICAL_VALUES.put2d(0.02, 24, 0.301); CRITICAL_VALUES.put2d(0.02, 25, 0.295); CRITICAL_VALUES.put2d(0.02, 26, 0.29); CRITICAL_VALUES.put2d(0.02, 27, 0.284); CRITICAL_VALUES.put2d(0.02, 28, 0.279); CRITICAL_VALUES.put2d(0.02, 29, 0.275); CRITICAL_VALUES.put2d(0.02, 30, 0.27); CRITICAL_VALUES.put2d(0.02, 31, 0.266); CRITICAL_VALUES.put2d(0.02, 32, 0.262); CRITICAL_VALUES.put2d(0.02, 33, 0.258); CRITICAL_VALUES.put2d(0.02, 34, 0.254); CRITICAL_VALUES.put2d(0.02, 35, 0.251); CRITICAL_VALUES.put2d(0.02, 36, 0.247); CRITICAL_VALUES.put2d(0.02, 37, 0.244); CRITICAL_VALUES.put2d(0.02, 38, 0.241); CRITICAL_VALUES.put2d(0.02, 39, 0.238); CRITICAL_VALUES.put2d(0.02, 40, 0.235); 
        CRITICAL_VALUES.put2d(0.01, 0, 1.63); CRITICAL_VALUES.put2d(0.01, 1, 0.995); CRITICAL_VALUES.put2d(0.01, 2, 0.929); CRITICAL_VALUES.put2d(0.01, 3, 0.829); CRITICAL_VALUES.put2d(0.01, 4, 0.734); CRITICAL_VALUES.put2d(0.01, 5, 0.669); CRITICAL_VALUES.put2d(0.01, 6, 0.617); CRITICAL_VALUES.put2d(0.01, 7, 0.576); CRITICAL_VALUES.put2d(0.01, 8, 0.542); CRITICAL_VALUES.put2d(0.01, 9, 0.513); CRITICAL_VALUES.put2d(0.01, 10, 0.489); CRITICAL_VALUES.put2d(0.01, 11, 0.468); CRITICAL_VALUES.put2d(0.01, 12, 0.449); CRITICAL_VALUES.put2d(0.01, 13, 0.432); CRITICAL_VALUES.put2d(0.01, 14, 0.418); CRITICAL_VALUES.put2d(0.01, 15, 0.404); CRITICAL_VALUES.put2d(0.01, 16, 0.392); CRITICAL_VALUES.put2d(0.01, 17, 0.381); CRITICAL_VALUES.put2d(0.01, 18, 0.371); CRITICAL_VALUES.put2d(0.01, 19, 0.361); CRITICAL_VALUES.put2d(0.01, 20, 0.352); CRITICAL_VALUES.put2d(0.01, 21, 0.344); CRITICAL_VALUES.put2d(0.01, 22, 0.337); CRITICAL_VALUES.put2d(0.01, 23, 0.33); CRITICAL_VALUES.put2d(0.01, 24, 0.323); CRITICAL_VALUES.put2d(0.01, 25, 0.317); CRITICAL_VALUES.put2d(0.01, 26, 0.311); CRITICAL_VALUES.put2d(0.01, 27, 0.305); CRITICAL_VALUES.put2d(0.01, 28, 0.3); CRITICAL_VALUES.put2d(0.01, 29, 0.295); CRITICAL_VALUES.put2d(0.01, 30, 0.29); CRITICAL_VALUES.put2d(0.01, 31, 0.285); CRITICAL_VALUES.put2d(0.01, 32, 0.281); CRITICAL_VALUES.put2d(0.01, 33, 0.277); CRITICAL_VALUES.put2d(0.01, 34, 0.273); CRITICAL_VALUES.put2d(0.01, 35, 0.269); CRITICAL_VALUES.put2d(0.01, 36, 0.265); CRITICAL_VALUES.put2d(0.01, 37, 0.262); CRITICAL_VALUES.put2d(0.01, 38, 0.258); CRITICAL_VALUES.put2d(0.01, 39, 0.255); CRITICAL_VALUES.put2d(0.01, 40, 0.252);
    }
    /**
     * Tests the rejection of null Hypothesis for a particular confidence level.
     * 
     * @param flatDataCollection
     * @param cdfMethod
     * @param params
     * @param is_twoTailed
     * @param aLevel
     * @return
     */
    public static boolean test(FlatDataCollection flatDataCollection, String cdfMethod, AssociativeArray params, boolean is_twoTailed, double aLevel) {
        double[] doubleArray = flatDataCollection.stream().filter(x -> x!=null).mapToDouble(TypeInference::toDouble).toArray();
        int n = doubleArray.length;
        if(n<=0) {
            throw new IllegalArgumentException("The provided collection can't be empty.");
        }
        Arrays.sort(doubleArray);

        //Calculation of expected Probabilities
        double observedProbabilityIminus1=0;//the exact previous observed probability (i-1)

        double maxDelta=0;
        int rank=1;

        try {    
            Method method = KolmogorovSmirnovOneSample.class.getMethod(cdfMethod, Double.class, AssociativeArray.class);
            for(int i=0;i<doubleArray.length;++i) {
                double x = doubleArray[i];
                
                double observedProbabilityI=rank/(double)n;
                
                Object methodResult = method.invoke(null, x, params);
                double expectedProbabilityI = TypeInference.toDouble(methodResult);
                
                double delta=Math.max(Math.abs(expectedProbabilityI-observedProbabilityI),Math.abs(expectedProbabilityI-observedProbabilityIminus1));
                if(delta>=maxDelta) {
                    maxDelta=delta;
                }
                
                observedProbabilityIminus1=observedProbabilityI;
                ++rank;
            }
            
            boolean rejectH0=checkCriticalValue(maxDelta, is_twoTailed, n, aLevel);
            
            return rejectH0;
        } 
        catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new IllegalArgumentException(ex);
        } 
    }
    
    /**
     * Cumulative Normal Distribution Method. This method is called via reflection.
     * 
     * @param x
     * @param params : AssociativeArray("mean"=>mean,"variance"=>variance)
     * @return 
     */
    public static double normalDistribution(Double x, AssociativeArray params) {
        double mean= params.getDouble("mean");
        double variance= params.getDouble("variance");

        //standardize the x value
        double z=(x-mean)/Math.sqrt(variance);

        return ContinuousDistributions.gaussCdf(z);
    }
    
    /**
     * Checks the Critical Value to determine if the Hypothesis should be rejected
     * 
     * @param score
     * @param is_twoTailed
     * @param n
     * @param aLevel
     * @return 
     */
    private static boolean checkCriticalValue(double score, boolean is_twoTailed, int n, double aLevel) {
        boolean rejected=false;

        double criticalValue;

        if(CRITICAL_VALUES.containsKey(aLevel)) { //the aLevel is one of the standards, we can use the tables
            if(CRITICAL_VALUES.get(aLevel).containsKey(n)) { //if the n value exists within the table use the exact percentage
                criticalValue = CRITICAL_VALUES.get(aLevel).getDouble(n);
            }
            else {
                //the n is too large, use the approximation
                criticalValue=CRITICAL_VALUES.get(aLevel).getDouble(0);
                criticalValue/=Math.sqrt(n+Math.sqrt(n/10.0));
            }
        }
        else {
            //estimate dynamically the critical value from the kolmogorov distribution
            criticalValue=calculateCriticalValue(is_twoTailed,n,aLevel);
        }


        if(score>criticalValue) {
            rejected=true; 
        }

        return rejected;
    }
    
    /**
     * Calculate Critical Value for a particular $n and $aLevel combination
     * 
     * @param is_twoTailed
     * @param n
     * @param aLevel
     * @return 
     */
    protected static double calculateCriticalValue(boolean is_twoTailed, int n,double aLevel) {
        double a=aLevel;
        if(is_twoTailed) {
            a=aLevel/2.0;
        }
        double one_minus_a=1.0-a;

        double Ka=1.36;//start by this value and go either up or down until you pass the desired level of significance

        int direction=1;//go up
        if(ContinuousDistributions.kolmogorov(Ka)>one_minus_a) {
            direction=-1;//go down
        }
        for(int i=0;i<110;++i) { //Why maximum 110 steps? Because the minimum value before kolmogorov goes to 0 is 0.27 and the maximum (empirically) is about 2.5. Both of them are about 110 steps of 0.01 distance away 
            Ka+=(direction*0.01);

            double sign=(one_minus_a-ContinuousDistributions.kolmogorov(Ka))*direction;
            //this changes sign ONLY when we just passed the value
            if(sign<=0) {
                break;
            }
        }

        double criticalValue=Ka/Math.sqrt(n+Math.sqrt(n/10.0));

        return criticalValue;
    }
}
