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

import com.datumbox.framework.common.dataobjects.FlatDataCollection;
import com.datumbox.framework.common.dataobjects.FlatDataList;
import com.datumbox.framework.core.common.utilities.PHPMethods;

import java.util.Iterator;

/**
 * This class provides methods which can be used for performing Systematic Sampling.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class SystematicSampling {
    
    /**
     * Samples n ids by using Systematic Sampling
     * 
     * @param idList
     * @param n
     * @param randomizeRecords
     * @return
     */
    public static FlatDataCollection randomSampling(FlatDataList idList, int n, boolean randomizeRecords) {
        FlatDataList sampledIds = new FlatDataList();
        
        int populationN = idList.size();
        
        Object[] keys = idList.toArray();
        if(randomizeRecords) {
            PHPMethods.<Object>shuffle(keys);
        }
        
        int k = populationN/n; //number of systematics
        if(k<2) {
            throw new IllegalArgumentException("The number of systematics is too small.");
        }
        
        int randomSystematic = PHPMethods.mt_rand(0,k-1);
        
        for(int i=randomSystematic;i<keys.length;i+=k) {
            Object pointID = keys[i];
            sampledIds.add(pointID);
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
        return SimpleRandomSampling.mean(flatDataCollection);
    }
    
    /**
     * Calculates Variance for Xbar
     * 
     * @param flatDataCollection
     * @return
     */
    public static double xbarVariance(FlatDataCollection flatDataCollection) {
        double n = flatDataCollection.size();
        if(n<=1) {
            throw new IllegalArgumentException("The provided collection must have more than 1 elements.");
        } 
        
        //As explained at http://www.fao.org/docrep/003/x6831e/x6831e12.htm
        //we take the first differences
        Double previousValue = null;
        double xbarVariance = 0.0;
        Iterator<Double> it = flatDataCollection.iteratorDouble();
        while(it.hasNext()) {
            Double yi = it.next();
            if(previousValue!=null) {
                double delta = yi - previousValue;
                xbarVariance+=delta*delta;
            }
            previousValue = yi;
        }
        xbarVariance/=(2*n*(n-1));

        return xbarVariance;
    }
    
    /**
     * Calculates Standard Deviation for Xbar
     * 
     * @param flatDataCollection
     * @return 
     */
    public static double xbarStd(FlatDataCollection flatDataCollection) {
        return Math.sqrt(xbarVariance(flatDataCollection));
    }
}
