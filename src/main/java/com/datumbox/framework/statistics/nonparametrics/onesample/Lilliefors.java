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
import com.datumbox.common.dataobjects.DataTable2D;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author bbriniotis
 */
public class Lilliefors {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    protected static final DataTable2D CRITICAL_VALUES = new DataTable2D(new LinkedHashMap<>()); //maintain the order to the first keys
    static {
        //The first key stores the aLevel (statistical significance).
        //The second one stores the number of observations in sample. The values are from 0-30.
        //NOTE: On second key, on 0 we store the approximation for n>30
        CRITICAL_VALUES.put2d(0.2, 0, 0.736); CRITICAL_VALUES.put2d(0.2, 4, 0.3); CRITICAL_VALUES.put2d(0.2, 5, 0.285); CRITICAL_VALUES.put2d(0.2, 6, 0.265); CRITICAL_VALUES.put2d(0.2, 7, 0.247); CRITICAL_VALUES.put2d(0.2, 8, 0.233); CRITICAL_VALUES.put2d(0.2, 9, 0.223); CRITICAL_VALUES.put2d(0.2, 10, 0.215); CRITICAL_VALUES.put2d(0.2, 11, 0.206); CRITICAL_VALUES.put2d(0.2, 12, 0.199); CRITICAL_VALUES.put2d(0.2, 13, 0.19); CRITICAL_VALUES.put2d(0.2, 14, 0.183); CRITICAL_VALUES.put2d(0.2, 15, 0.177); CRITICAL_VALUES.put2d(0.2, 16, 0.173); CRITICAL_VALUES.put2d(0.2, 17, 0.169); CRITICAL_VALUES.put2d(0.2, 18, 0.166); CRITICAL_VALUES.put2d(0.2, 19, 0.163); CRITICAL_VALUES.put2d(0.2, 20, 0.16); CRITICAL_VALUES.put2d(0.2, 25, 0.142); CRITICAL_VALUES.put2d(0.2, 30, 0.131); 
        CRITICAL_VALUES.put2d(0.15, 0, 0.768); CRITICAL_VALUES.put2d(0.15, 4, 0.319); CRITICAL_VALUES.put2d(0.15, 5, 0.299); CRITICAL_VALUES.put2d(0.15, 6, 0.277); CRITICAL_VALUES.put2d(0.15, 7, 0.258); CRITICAL_VALUES.put2d(0.15, 8, 0.244); CRITICAL_VALUES.put2d(0.15, 9, 0.233); CRITICAL_VALUES.put2d(0.15, 10, 0.224); CRITICAL_VALUES.put2d(0.15, 11, 0.217); CRITICAL_VALUES.put2d(0.15, 12, 0.212); CRITICAL_VALUES.put2d(0.15, 13, 0.202); CRITICAL_VALUES.put2d(0.15, 14, 0.194); CRITICAL_VALUES.put2d(0.15, 15, 0.187); CRITICAL_VALUES.put2d(0.15, 16, 0.182); CRITICAL_VALUES.put2d(0.15, 17, 0.177); CRITICAL_VALUES.put2d(0.15, 18, 0.173); CRITICAL_VALUES.put2d(0.15, 19, 0.169); CRITICAL_VALUES.put2d(0.15, 20, 0.166); CRITICAL_VALUES.put2d(0.15, 25, 0.147); CRITICAL_VALUES.put2d(0.15, 30, 0.136); 
        CRITICAL_VALUES.put2d(0.1, 0, 0.805); CRITICAL_VALUES.put2d(0.1, 4, 0.325); CRITICAL_VALUES.put2d(0.1, 5, 0.315); CRITICAL_VALUES.put2d(0.1, 6, 0.294); CRITICAL_VALUES.put2d(0.1, 7, 0.276); CRITICAL_VALUES.put2d(0.1, 8, 0.261); CRITICAL_VALUES.put2d(0.1, 9, 0.249); CRITICAL_VALUES.put2d(0.1, 10, 0.239); CRITICAL_VALUES.put2d(0.1, 11, 0.23); CRITICAL_VALUES.put2d(0.1, 12, 0.223); CRITICAL_VALUES.put2d(0.1, 13, 0.214); CRITICAL_VALUES.put2d(0.1, 14, 0.207); CRITICAL_VALUES.put2d(0.1, 15, 0.201); CRITICAL_VALUES.put2d(0.1, 16, 0.195); CRITICAL_VALUES.put2d(0.1, 17, 0.189); CRITICAL_VALUES.put2d(0.1, 18, 0.184); CRITICAL_VALUES.put2d(0.1, 19, 0.179); CRITICAL_VALUES.put2d(0.1, 20, 0.174); CRITICAL_VALUES.put2d(0.1, 25, 0.158); CRITICAL_VALUES.put2d(0.1, 30, 0.144); 
        CRITICAL_VALUES.put2d(0.05, 0, 0.886); CRITICAL_VALUES.put2d(0.05, 4, 0.381); CRITICAL_VALUES.put2d(0.05, 5, 0.337); CRITICAL_VALUES.put2d(0.05, 6, 0.319); CRITICAL_VALUES.put2d(0.05, 7, 0.3); CRITICAL_VALUES.put2d(0.05, 8, 0.285); CRITICAL_VALUES.put2d(0.05, 9, 0.271); CRITICAL_VALUES.put2d(0.05, 10, 0.258); CRITICAL_VALUES.put2d(0.05, 11, 0.249); CRITICAL_VALUES.put2d(0.05, 12, 0.242); CRITICAL_VALUES.put2d(0.05, 13, 0.234); CRITICAL_VALUES.put2d(0.05, 14, 0.227); CRITICAL_VALUES.put2d(0.05, 15, 0.22); CRITICAL_VALUES.put2d(0.05, 16, 0.213); CRITICAL_VALUES.put2d(0.05, 17, 0.206); CRITICAL_VALUES.put2d(0.05, 18, 0.2); CRITICAL_VALUES.put2d(0.05, 19, 0.195); CRITICAL_VALUES.put2d(0.05, 20, 0.19); CRITICAL_VALUES.put2d(0.05, 25, 0.173); CRITICAL_VALUES.put2d(0.05, 30, 0.161); 
        CRITICAL_VALUES.put2d(0.01, 0, 1.031); CRITICAL_VALUES.put2d(0.01, 4, 0.417); CRITICAL_VALUES.put2d(0.01, 5, 0.405); CRITICAL_VALUES.put2d(0.01, 6, 0.364); CRITICAL_VALUES.put2d(0.01, 7, 0.348); CRITICAL_VALUES.put2d(0.01, 8, 0.331); CRITICAL_VALUES.put2d(0.01, 9, 0.311); CRITICAL_VALUES.put2d(0.01, 10, 0.294); CRITICAL_VALUES.put2d(0.01, 11, 0.284); CRITICAL_VALUES.put2d(0.01, 12, 0.275); CRITICAL_VALUES.put2d(0.01, 13, 0.268); CRITICAL_VALUES.put2d(0.01, 14, 0.261); CRITICAL_VALUES.put2d(0.01, 15, 0.257); CRITICAL_VALUES.put2d(0.01, 16, 0.25); CRITICAL_VALUES.put2d(0.01, 17, 0.245); CRITICAL_VALUES.put2d(0.01, 18, 0.239); CRITICAL_VALUES.put2d(0.01, 19, 0.235); CRITICAL_VALUES.put2d(0.01, 20, 0.231); CRITICAL_VALUES.put2d(0.01, 25, 0.2); CRITICAL_VALUES.put2d(0.01, 30, 0.187); 
    }
    
    /**
     * Tests the rejection of null Hypothesis for a particular confidence level.
     * 
     * @param flatDataCollection
     * @param cdfMethod
     * @param aLevel
     * @return
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException 
     */
    public static boolean test(FlatDataCollection flatDataCollection, String cdfMethod, double aLevel) throws IllegalArgumentException, SecurityException, NoSuchMethodException, IllegalAccessException, InvocationTargetException { 
        int n=flatDataCollection.size();
        if(n<=0) {
            throw new IllegalArgumentException();
        }

        Double[] numberList = Dataset.copyCollection2DoubleArray(flatDataCollection);
        Arrays.sort(numberList);        
        
        //Calculation of expected Probabilities
        double observedProbabilityIminus1=0;//the exact previous observed probability (i-1)

        double maxDelta=0;
        int rank=1;
                
        //Calculate the parameters of the Distribution based on the sample
        Method method;
        method = Lilliefors.class.getMethod(cdfMethod + "GetParams", FlatDataCollection.class);
        AssociativeArray params = (AssociativeArray)method.invoke(null, flatDataCollection);
        
        //Fetch the method of the distribution.
        method = Lilliefors.class.getMethod(cdfMethod, Double.class, AssociativeArray.class);
        for(int i=0;i<numberList.length;++i) {
            Double x = numberList[i];
            
            double observedProbabilityI=(double)rank/n;

            Object methodResult = method.invoke(null, x, params);
            double expectedProbabilityI = Dataset.toDouble(methodResult);
            
            double delta=Math.max(Math.abs(expectedProbabilityI-observedProbabilityI),Math.abs(expectedProbabilityI-observedProbabilityIminus1));
            if(delta>=maxDelta) {
                maxDelta=delta;
            }

            observedProbabilityIminus1=observedProbabilityI;
            ++rank;
        }

        boolean rejectH0=checkCriticalValue(maxDelta, n, aLevel);

        return rejectH0;
    }
    
    /**
     * Estimate Parameters of Normal based on Sample. This method is called via reflection.
     * 
     * @param flatDataCollection
     * @return 
     */
    public static AssociativeArray normalDistributionGetParams(FlatDataCollection flatDataCollection) {
        AssociativeArray params = new AssociativeArray();
        params.put("mean", Descriptives.mean(flatDataCollection));
        params.put("variance", Descriptives.variance(flatDataCollection, true));
        return params;
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

        return ContinuousDistributions.GaussCdf(z);
    }
    
    /**
     * Checks the Critical Value to determine if the Hypothesis should be rejected
     * 
     * @param score
     * @param n
     * @param aLevel
     * @return 
     */
    protected static boolean checkCriticalValue(double score, int n, double aLevel) {
        boolean rejected=false;

        double criticalValue = Double.MAX_VALUE;
        
        double aLevelKey = aLevel;
        if(!CRITICAL_VALUES.containsKey(aLevelKey)) { //if the particular aLevel is not a default one take the closest one
            double significanceValue = 0.0;
            for(Map.Entry<Object, AssociativeArray> entry : CRITICAL_VALUES.entrySet()) {
                significanceValue = Dataset.toDouble(entry.getKey());
                //AssociativeArray scoreList = entry.getValue();
                
                if(significanceValue<=aLevel) { //add the closest one
                    aLevelKey=significanceValue; 
                    break;
                }
            }
            
            if(!CRITICAL_VALUES.containsKey(aLevelKey)) { //it is possible that we still have not found a proper aLevel. This happens when the requested aLevel is close to 0.99999... In that case we take the largest (last)
                aLevelKey = significanceValue; //get the last available significance value on the CRITICAL_VALUES array
            }
        }

        
        if(CRITICAL_VALUES.containsKey(aLevelKey)) { //the aLevelKey is one of the standards, we can use the tables
            if(CRITICAL_VALUES.get(aLevelKey).containsKey(n)) { //if the n value exists within the table use the exact percentage
                criticalValue = CRITICAL_VALUES.get(aLevelKey).getDouble(n);
            }
            else {
                //the n is too large, use the approximation
                criticalValue=CRITICAL_VALUES.get(aLevelKey).getDouble(0);
                criticalValue/=Math.sqrt(n);
            }
        }


        if(score>criticalValue) {
            rejected=true; 
        }

        return rejected;
    }
    
}
