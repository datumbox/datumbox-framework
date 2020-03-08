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

import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.common.dataobjects.FlatDataCollection;
import com.datumbox.framework.common.dataobjects.FlatDataList;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.common.utilities.PHPMethods;
import com.datumbox.framework.core.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;

import java.util.Map;

/**
 * This class provides methods which can be used for performing Simple Random Sampling.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class SimpleRandomSampling {
    
    /**
     * Samples n ids based on their a Table which contains weights, probabilities 
     * or frequencies. 
     * 
     * @param weightedTable
     * @param n
     * @param withReplacement
     * @return 
     */
    public static FlatDataCollection weightedSampling(AssociativeArray weightedTable, int n, boolean withReplacement) {
        FlatDataList sampledIds = new FlatDataList();
        
        double sumOfFrequencies = Descriptives.sum(weightedTable.toFlatDataCollection());
        int populationN = weightedTable.size();
        
        for(int i=0;i<n;++i) {
            if(withReplacement==false && populationN<=n) {
                //if replacement is not allowed and we already sampled everything that it can stop
                break;
            }
            
            double randomFrequency = PHPMethods.mt_rand(0.0, sumOfFrequencies);
            
            double cumulativeFrequency=0;
            for(Map.Entry<Object, Object> entry : weightedTable.entrySet()) {
                Object pointID = entry.getKey();
                cumulativeFrequency+= TypeInference.toDouble(entry.getValue());
                if(cumulativeFrequency>=randomFrequency) {
                    if(withReplacement==false && sampledIds.contains(pointID)) {
                        continue;
                    }
                    
                    sampledIds.add(pointID);
                    break;
                }
            }
        }
    
        return sampledIds.toFlatDataCollection();
    }
    
    /**
     * Samples n ids by using SimpleRandomSampling (Simple Random Sampling).
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
            
            int randomPosition = PHPMethods.mt_rand(0, populationN-1);
            
            Object pointID = idList.get(randomPosition);
            
            if(withReplacement==false && sampledIds.contains(pointID)) {
                continue;
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
     */
    public static double xbarVariance(double variance, int sampleN, int populationN) {
        if(populationN<=0 || sampleN<=0 || sampleN>populationN) {
            throw new IllegalArgumentException("All the parameters must be positive and sampleN smaller than populationN.");
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
     */
    public static double pbarVariance(double pbar, int sampleN, int populationN) {
        if(populationN<=0 || sampleN<=0 || sampleN>populationN) {
            throw new IllegalArgumentException("All the parameters must be positive and sampleN smaller than populationN.");
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
    public static int minimumSampleSizeForMaximumXbarStd(double maximumXbarStd, double populationStd, int populationN) {
        if(populationN<=0) {
            throw new IllegalArgumentException("The populationN parameter must be positive.");
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
    public static int minimumSampleSizeForGivenDandMaximumRisk(double d, double aLevel, double populationStd, int populationN) {
        if(populationN<=0 || aLevel<=0 || d<=0) {
            throw new IllegalArgumentException("All the parameters must be positive.");
        }
        
        double a = 1.0 - aLevel/2.0;
        
        double Za = ContinuousDistributions.gaussInverseCdf(a);
        
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
