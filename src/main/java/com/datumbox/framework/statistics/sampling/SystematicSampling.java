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

import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.utilities.PHPfunctions;
import java.util.Iterator;

/**
 * Systematic Sampling
 * @author bbriniotis
 */
public class SystematicSampling {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    /**
     * Samples n ids by using Systematic Sampling
     * 
     * @param idList
     * @param n
     * @param randomizeRecords
     * @return
     * @throws IllegalArgumentException 
     */
    public static FlatDataCollection randomSampling(FlatDataList idList, int n, boolean randomizeRecords) throws IllegalArgumentException {
        FlatDataList sampledIds = new FlatDataList();
        
        int populationN = idList.size();
        
        Object[] keys = idList.toArray();
        if(randomizeRecords) {
            PHPfunctions.<Object>shuffle(keys);
        }
        
        int k = populationN/n; //number of systematics
        if(k<2) {
            throw new IllegalArgumentException();
        }
        
        int randomSystematic = PHPfunctions.mt_rand(0,k-1);
        
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
        return SRS.mean(flatDataCollection);
    }
    
    /**
     * Calculates Variance for Xbar
     * 
     * @param flatDataCollection
     * @return
     * @throws IllegalArgumentException 
     */
    public static double xbarVariance(FlatDataCollection flatDataCollection) throws IllegalArgumentException {
        double n = flatDataCollection.size();
        if(n<=1) {
            throw new IllegalArgumentException();
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
