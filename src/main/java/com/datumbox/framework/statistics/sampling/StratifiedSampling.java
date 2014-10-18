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
package com.datumbox.framework.statistics.sampling;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.AssociativeArray2D;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.dataobjects.TransposeDataList;
import com.datumbox.common.dataobjects.TransposeDataCollection;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.Map;

/**
 * Stratified Sampling
 * @author bbriniotis
 */
public class StratifiedSampling {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
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
            
            sampledIds.put(strata, SRS.weightedProbabilitySampling(entry.getValue(), sampleN.intValue(), withReplacement));
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
            
            sampledIds.put(strata, SRS.randomSampling(entry.getValue(), sampleN.intValue(), withReplacement));
        }
        
        return sampledIds;
    }
    
    /**
     * Calculate the mean from the sample
     * 
     * @param sampleDataCollection
     * @param populationNh
     * @return
     * @throws IllegalArgumentException 
     */
    public static double mean(TransposeDataCollection sampleDataCollection, AssociativeArray populationNh) throws IllegalArgumentException {
        double populationN = Descriptives.sum(populationNh.toFlatDataCollection());
        
        if(populationN<=0) {
            throw new IllegalArgumentException();
        }
        
        double mean = 0.0;
        
        for(Map.Entry<Object, FlatDataCollection> entry : sampleDataCollection.entrySet()) {
            Object strata = entry.getKey();
            Integer strataPopulation = ((Number)populationNh.get(strata)).intValue();
        
            if(strataPopulation==null) {
                throw new IllegalArgumentException();
            }
            
            mean += strataPopulation*SRS.mean(entry.getValue())/populationN;
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
    public static double variance(TransposeDataCollection sampleDataCollection, AssociativeArray populationNh) throws IllegalArgumentException {
        double variance = 0.0;
        
        int populationN = 0;
        
        double mean = mean(sampleDataCollection, populationNh);
        
        for(Map.Entry<Object, FlatDataCollection> entry : sampleDataCollection.entrySet()) {
            Object strata = entry.getKey();
            Integer strataPopulation = ((Number)populationNh.get(strata)).intValue();
        
            if(strataPopulation==null) {
                throw new IllegalArgumentException();
            }
            
            populationN+=strataPopulation;
            
            //Analysis of Variance
            
            //Within Strata
            variance+=(strataPopulation-1)*SRS.variance(entry.getValue());
            //Between Strata
            variance+=strataPopulation*Math.pow(SRS.mean(entry.getValue())-mean,2);
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
     * @throws IllegalArgumentException 
     */
    public static double xbarVariance(TransposeDataCollection sampleDataCollection, AssociativeArray nh, AssociativeArray populationNh) throws IllegalArgumentException {
        double populationN = Descriptives.sum(populationNh.toFlatDataCollection());
        
        if(populationN<=0) {
            throw new IllegalArgumentException();
        }
        
        double variance = 0.0;
        
        for(Map.Entry<Object, FlatDataCollection> entry : sampleDataCollection.entrySet()) {
            Object strata = entry.getKey();
            Integer strataPopulation = ((Number)populationNh.get(strata)).intValue();
            Integer strataSample = ((Number)nh.get(strata)).intValue();
        
            if(strataPopulation==null || strataSample==null) {
                throw new IllegalArgumentException();
            }
            
            double Wh = strataPopulation/populationN;
                
            //this is the formula when we do SRS in each strata. nevertheless in order to keep our code DRY, instead of writing directly the formula of SRS here, we will call SRS xbarVariance function to estimate it.
            //$fh=$nh[$strata]/$populationNh[$strata];
            //$variance+=$Wh*$Wh*SRS::variance($flatDataCollection)*(1-$fh)/$nh[$strata]; 
            variance+= Wh*Wh* SRS.xbarVariance(SRS.variance(entry.getValue()), strataSample, strataPopulation);
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
    public static AssociativeArray optimumSampleSize(int n, AssociativeArray populationNh, AssociativeArray populationStdh) throws IllegalArgumentException {
        AssociativeArray nh = new AssociativeArray();
        
        double sumNhSh = 0.0;
        for(Map.Entry<Object, Object> entry : populationNh.entrySet()) {
            Object strata = entry.getKey();
            Integer populationInStrata = ((Number)entry.getValue()).intValue();
            Double populationStd = populationStdh.getDouble(strata);
            
            if(populationStd == null || populationInStrata <=0.0) {
                throw new IllegalArgumentException();
            }
            
            double NhSh = populationInStrata*populationStd;
            
            sumNhSh+=NhSh;

            nh.put(strata, n*NhSh);
        }
        
        if(sumNhSh<=0) {
            throw new IllegalArgumentException();
        }
        
        for(Map.Entry<Object, Object> entry : nh.entrySet()) {
            Object strata = entry.getKey();
            nh.put(strata, Dataset.toDouble(entry.getValue())/sumNhSh);
        }
        
        return nh;
    }
}
