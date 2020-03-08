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
package com.datumbox.framework.core.statistics.sampling;

import com.datumbox.framework.common.dataobjects.*;
import com.datumbox.framework.core.statistics.descriptivestatistics.Descriptives;

import java.util.Map;

/**
 * This class provides methods which can be used for performing Stratified Sampling.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class StratifiedSampling {
    
    /**
     * Samples nh ids from each strata based on their Frequency Table
     * 
     * @param strataFrequencyTable
     * @param nh
     * @param withReplacement
     * @return 
     */
    public static TransposeDataCollection weightedProbabilitySampling(AssociativeArray2D strataFrequencyTable, AssociativeArray nh, boolean withReplacement) {
        TransposeDataCollection sampledIds = new TransposeDataCollection(); 
    
        for(Map.Entry<Object, AssociativeArray> entry : strataFrequencyTable.entrySet()) {
            Object strata = entry.getKey();
            
            Number sampleN =  ((Number)nh.get(strata));
            if(sampleN==null) {
                continue;
            }
            
            sampledIds.put(strata, SimpleRandomSampling.weightedSampling(entry.getValue(), sampleN.intValue(), withReplacement));
        }
        
        return sampledIds;
    }
    
    /**
     * Samples nh ids from each strata by using Stratified Sampling
     * 
     * @param strataIdList
     * @param nh
     * @param withReplacement
     * @return 
     */
    public static TransposeDataCollection randomSampling(TransposeDataList strataIdList, AssociativeArray nh, boolean withReplacement) {
        TransposeDataCollection sampledIds = new TransposeDataCollection();
    
        for(Map.Entry<Object, FlatDataList> entry : strataIdList.entrySet()) {
            Object strata = entry.getKey();
            
            Number sampleN =  ((Number)nh.get(strata));
            if(sampleN==null) {
                continue;
            }
            
            sampledIds.put(strata, SimpleRandomSampling.randomSampling(entry.getValue(), sampleN.intValue(), withReplacement));
        }
        
        return sampledIds;
    }
    
    /**
     * Calculate the mean from the sample
     * 
     * @param sampleDataCollection
     * @param populationNh
     * @return
     */
    public static double mean(TransposeDataCollection sampleDataCollection, AssociativeArray populationNh) {
        double populationN = Descriptives.sum(populationNh.toFlatDataCollection());
        
        if(populationN<=0) {
            throw new IllegalArgumentException("The populationN parameter must be positive.");
        }
        
        double mean = 0.0;
        
        for(Map.Entry<Object, FlatDataCollection> entry : sampleDataCollection.entrySet()) {
            Object strata = entry.getKey();
            Integer strataPopulation = ((Number)populationNh.get(strata)).intValue();
        
            if(strataPopulation==null) {
                throw new IllegalArgumentException("Invalid strata population size.");
            }
            
            mean += strataPopulation*SimpleRandomSampling.mean(entry.getValue())/populationN;
        }
        
        return mean;
    }
    
    /**
     * Calculate the variance from the sample
     * 
     * @param sampleDataCollection
     * @param populationNh
     * @return 
     */
    public static double variance(TransposeDataCollection sampleDataCollection, AssociativeArray populationNh) {
        double variance = 0.0;
        
        int populationN = 0;
        
        double mean = mean(sampleDataCollection, populationNh);
        
        for(Map.Entry<Object, FlatDataCollection> entry : sampleDataCollection.entrySet()) {
            Object strata = entry.getKey();
            Integer strataPopulation = ((Number)populationNh.get(strata)).intValue();
        
            if(strataPopulation==null) {
                throw new IllegalArgumentException("Invalid strata population size.");
            }
            
            populationN+=strataPopulation;
            
            //Analysis of Variance
            
            //Within Strata
            variance+=(strataPopulation-1)*SimpleRandomSampling.variance(entry.getValue());
            //Between Strata
            variance+=strataPopulation*Math.pow(SimpleRandomSampling.mean(entry.getValue())-mean,2);
        }
        
        variance/=(populationN-1);

        return variance;
    }
    
    /**
     * Calculate the standard deviation of the sample
     * 
     * @param sampleDataCollection
     * @param populationNh
     * @return 
     */
    public static double std(TransposeDataCollection sampleDataCollection, AssociativeArray populationNh) {
        return Math.sqrt(variance(sampleDataCollection, populationNh));
    }
    
    /**
     * Calculates Variance for Xbar
     * 
     * @param sampleDataCollection
     * @param nh
     * @param populationNh
     * @return
     */
    public static double xbarVariance(TransposeDataCollection sampleDataCollection, AssociativeArray nh, AssociativeArray populationNh) {
        double populationN = Descriptives.sum(populationNh.toFlatDataCollection());
        
        if(populationN<=0) {
            throw new IllegalArgumentException("The populationN parameter must be positive.");
        }
        
        double variance = 0.0;
        
        for(Map.Entry<Object, FlatDataCollection> entry : sampleDataCollection.entrySet()) {
            Object strata = entry.getKey();
            Integer strataPopulation = ((Number)populationNh.get(strata)).intValue();
            Integer strataSample = ((Number)nh.get(strata)).intValue();
        
            if(strataPopulation==null || strataSample==null) {
                throw new IllegalArgumentException("Invalid strata population or sample size.");
            }
            
            double Wh = strataPopulation/populationN;
                
            //this is the formula when we do SimpleRandomSampling in each strata. nevertheless in order to keep our code DRY, instead of writing directly the formula of SimpleRandomSampling here, we will call SimpleRandomSampling xbarVariance function to estimate it.
            //$fh=$nh[$strata]/$populationNh[$strata];
            //$variance+=$Wh*$Wh*SimpleRandomSampling::variance($flatDataCollection)*(1-$fh)/$nh[$strata]; 
            variance+= Wh*Wh* SimpleRandomSampling.xbarVariance(SimpleRandomSampling.variance(entry.getValue()), strataSample, strataPopulation);
        }
        
        return variance;
    }
    
    /**
     * Calculates Standard Deviation for Xbar
     * 
     * @param sampleDataCollection
     * @param nh
     * @param populationNh
     * @return 
     */
    public static double xbarStd(TransposeDataCollection sampleDataCollection, AssociativeArray nh, AssociativeArray populationNh) {
        return Math.sqrt(xbarVariance(sampleDataCollection, nh, populationNh));
    }
    
    /**
     * Returns the optimum sample size per strata under Neyman Allocation
     * 
     * @param n
     * @param populationNh
     * @param populationStdh
     * @return 
     */
    public static AssociativeArray optimumSampleSize(int n, AssociativeArray populationNh, AssociativeArray populationStdh) {
        AssociativeArray nh = new AssociativeArray();
        
        double sumNhSh = 0.0;
        for(Map.Entry<Object, Object> entry : populationNh.entrySet()) {
            Object strata = entry.getKey();
            Integer populationInStrata = ((Number)entry.getValue()).intValue();
            Double populationStd = populationStdh.getDouble(strata);
            
            if(populationStd == null || populationInStrata <=0.0) {
                throw new IllegalArgumentException("Invalid strata population or strata std.");
            }
            
            double NhSh = populationInStrata*populationStd;
            
            sumNhSh+=NhSh;

            nh.put(strata, n*NhSh);
        }
        
        if(sumNhSh<=0) {
            throw new IllegalArgumentException("Invalid strata populations.");
        }
        
        for(Map.Entry<Object, Object> entry : nh.entrySet()) {
            Object strata = entry.getKey();
            nh.put(strata, TypeInference.toDouble(entry.getValue())/sumNhSh);
        }
        
        return nh;
    }
}
