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
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.utilities.RandomValue;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;
import java.util.Map;

/**
 * Simple Random Sampling
 * @author bbriniotis
 */
public class SRS {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    /**
     * Samples n ids based on their Probability Table
     * 
     * @param probabilityTable
     * @param n
     * @param withReplacement
     * @return 
     */
    public static FlatDataCollection weightedProbabilitySampling(AssociativeArray probabilityTable, int n, boolean withReplacement) {
        AssociativeArray frequencyTable = new AssociativeArray();
        
        for(Map.Entry<Object, Object> entry : probabilityTable.entrySet()) {
            Object id = entry.getKey();
            Double value = Dataset.toDouble(entry.getValue());
            frequencyTable.put(id, value*n);
        }
        
        return weightedSampling(frequencyTable, n, withReplacement);
    }
    
    /**
     * Samples n ids based on their Frequency Table
     * 
     * @param frequencyTable
     * @param n
     * @param withReplacement
     * @return 
     */
    public static FlatDataCollection weightedSampling(AssociativeArray frequencyTable, int n, boolean withReplacement) {
        FlatDataList sampledIds = new FlatDataList();
        
        double sumOfFrequencies = Descriptives.sum(frequencyTable.toFlatDataCollection());
        int populationN = frequencyTable.size();
        
        for(int i=0;i<n;++i) {
            if(withReplacement==false && populationN<=n) {
                //if replacement is not allowed and we already sampled everything that it can stop
                break;
            }
            
            double randomFrequency = RandomValue.doubleRand(0.0, sumOfFrequencies);
            
            double cumulativeFrequency=0;
            for(Map.Entry<Object, Object> entry : frequencyTable.entrySet()) {
                Object pointID = entry.getKey();
                cumulativeFrequency+= Dataset.toDouble(entry.getValue());
                if(cumulativeFrequency>=randomFrequency) {
                    if(withReplacement==false) {
                        /* if replacement is not allowed check if the point already exists */
                        if(sampledIds.contains(pointID)) {
                            continue;
                        }
                    }
                    
                    sampledIds.add(pointID);
                    break;
                }
            }
        }
    
        return sampledIds.toFlatDataCollection();
    }
    
    /**
     * Samples n ids by using SRS (Simple Random Sampling).
     * 
     * @param idList
     * @param n
     * @param withReplacement
     * @return 
     */
    public static FlatDataCollection randomSampling(FlatDataList idList, int n, boolean withReplacement) {
        FlatDataList sampledIds = new FlatDataList();
        
        int populationN = idList.size();
        
        for(int i=0;i<n;) {
            if(withReplacement==false && populationN<=n) {
                /* if replacement is not allowed and we already sampled everything that it can stop */
                break;
            }
            
            int randomPosition = RandomValue.intRand(0, populationN-1);
            
            Object pointID = idList.get(randomPosition);
            
            if(withReplacement==false) {
                /* if replacement is not allowed check if the point already exists */
                if(sampledIds.contains(pointID)) {
                    continue;
                }
            }

            sampledIds.add(pointID);
            ++i;
            
            
        }
    
        return sampledIds.toFlatDataCollection();
    }
    
    /**
     * Calculate the mean from the sample
     * 
     * @param flatDataCollection
     * @return 
     */
    public static double mean(FlatDataCollection flatDataCollection) {
        return Descriptives.mean(flatDataCollection);
    }
    
    /**
     * Calculate the variance from the sample
     * 
     * @param flatDataCollection
     * @return 
     */
    public static double variance(FlatDataCollection flatDataCollection) {
        return Descriptives.variance(flatDataCollection,true);
    }
    
    /**
     * Calculate the standard deviation of the sample
     * 
     * @param flatDataCollection
     * @return 
     */
    public static double std(FlatDataCollection flatDataCollection) {
        return Math.sqrt(variance(flatDataCollection));
    }
    
    /**
     * Calculates Variance for Xbar for infinite population size
     * 
     * @param variance
     * @param sampleN
     * @return 
     */
    public static double xbarVariance(double variance, int sampleN) {
        return xbarVariance(variance, sampleN, Integer.MAX_VALUE);
    }
    
    /**
     * Calculates Variance for Xbar for a finite population size
     * 
     * @param variance
     * @param sampleN
     * @param populationN
     * @return
     * @throws IllegalArgumentException 
     */
    public static double xbarVariance(double variance, int sampleN, int populationN) throws IllegalArgumentException {
        if(populationN<=0 || sampleN<=0 || sampleN>populationN) {
            throw new IllegalArgumentException();
        }
        
        double xbarVariance=(1.0 - (double)sampleN/populationN)*variance/sampleN;
        
        return xbarVariance;
    }
    
    /**
     * Calculates Standard Deviation for Xbar for infinite population size
     * 
     * @param std
     * @param sampleN
     * @return 
     */
    public static double xbarStd(double std, int sampleN) {
        return Math.sqrt(xbarVariance(std*std, sampleN, Integer.MAX_VALUE));
    }
    
    /**
     * Calculates Standard Deviation for Xbar for finite population size
     * 
     * @param std
     * @param sampleN
     * @param populationN
     * @return 
     */
    public static double xbarStd(double std, int sampleN, int populationN) {
        return Math.sqrt(xbarVariance(std*std, sampleN, populationN));
    }
    
    /**
     * Calculates Variance for Pbar for infinite population size
     * 
     * @param pbar
     * @param sampleN
     * @return 
     */
    public static double pbarVariance(double pbar, int sampleN) {
        return pbarVariance(pbar, sampleN, Integer.MAX_VALUE);
    }
    
    /**
     * Calculates Variance for Pbar for a finite population size
     * 
     * @param pbar
     * @param sampleN
     * @param populationN
     * @return
     * @throws IllegalArgumentException 
     */
    public static double pbarVariance(double pbar, int sampleN, int populationN) throws IllegalArgumentException {
        if(populationN<=0 || sampleN<=0 || sampleN>populationN) {
            throw new IllegalArgumentException();
        }
        double f = (double)sampleN/populationN;
        double pbarVariance=((1.0 - f)*pbar*(1.0 - pbar))/(sampleN-1.0);
        
        return pbarVariance;
    }
    
    /**
     * Calculates Standard Deviation for Pbar for infinite population size
     * 
     * @param pbar
     * @param sampleN
     * @return 
     */
    public static double pbarStd(double pbar, int sampleN) {
        return Math.sqrt(pbarVariance(pbar, sampleN, Integer.MAX_VALUE));
    }
    
    /**
     * Calculates Standard Deviation for Pbar for finite population size
     * 
     * @param pbar
     * @param sampleN
     * @param populationN
     * @return 
     */
    public static double pbarStd(double pbar, int sampleN, int populationN) {
        return Math.sqrt(pbarVariance(pbar, sampleN, populationN));
    }
    
    /**
     * Returns the minimum required sample size when we set a specific maximum Xbar STD Error for infinite population size.
     * 
     * @param maximumXbarStd
     * @param populationStd
     * @return 
     */
    public static int minimumSampleSizeForMaximumXbarStd(double maximumXbarStd, double populationStd) {
        return minimumSampleSizeForMaximumXbarStd(maximumXbarStd, populationStd, Integer.MAX_VALUE);
    }
    
    /**
     * Returns the minimum required sample size when we set a specific maximum Xbar STD Error for finite population size.
     * 
     * @param maximumXbarStd
     * @param populationStd
     * @param populationN
     * @return 
     */
    public static int minimumSampleSizeForMaximumXbarStd(double maximumXbarStd, double populationStd, int populationN) throws IllegalArgumentException {
        if(populationN<=0) {
            throw new IllegalArgumentException();
        }
        
        double minimumSampleN = 1.0/(Math.pow(maximumXbarStd/populationStd,2) + 1.0/populationN);

        return (int)Math.ceil(minimumSampleN);
    }
    
    /**
     * Returns the minimum required sample size when we set a predifined limit d and a maximum probability Risk a for infinite population size
     * 
     * @param d
     * @param aLevel
     * @param populationStd
     * @return 
     */
    public static int minimumSampleSizeForGivenDandMaximumRisk(double d, double aLevel, double populationStd) {
        return minimumSampleSizeForGivenDandMaximumRisk(d, aLevel, populationStd, Integer.MAX_VALUE);
    }
    
    /**
     * Returns the minimum required sample size when we set a predefined limit d and a maximum probability Risk a for finite population size
     * 
     * @param d
     * @param aLevel
     * @param populationStd
     * @param populationN
     * @return 
     */
    public static int minimumSampleSizeForGivenDandMaximumRisk(double d, double aLevel, double populationStd, int populationN) throws IllegalArgumentException {
        if(populationN<=0 || aLevel<=0 || d<=0) {
            throw new IllegalArgumentException();
        }
        
        double a = 1.0 - aLevel/2.0;
        
        double Za = ContinuousDistributions.GaussInverseCdf(a);
        
        double V = Math.pow(d/Za,2);
        double Ssquare = populationStd*populationStd;
        double minimumSampleN = (Ssquare/V) * (1.0 / (1.0 + Ssquare/(populationN*V)) );
        /*
        double minimumSampleN=populationN/(
            1.0+populationN*pow(d/(Za*populationStd),2)
        );
        */
        return (int)Math.ceil(minimumSampleN);
    }
}
