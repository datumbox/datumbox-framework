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
import com.datumbox.common.dataobjects.TransposeDataList;
import com.datumbox.common.dataobjects.TransposeDataCollection;
import com.datumbox.common.utilities.PHPfunctions;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.Iterator;
import java.util.Map;

/**
 * Cluster Sampling
 * 
 * @author bbriniotis
 */
public class ClusterSampling {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    /**
     * Returns the mean cluster size
     * 
     * @param clusterIdList
     * @return 
     */
    public static double Nbar(TransposeDataList clusterIdList) {
        int populationM = clusterIdList.size();
        
        double Nbar = 0.0;
        for(Map.Entry<Object, FlatDataList> entry : clusterIdList.entrySet()) {
            Nbar += (double)entry.getValue().size()/populationM;
        }

        return Nbar;
    }
    
    /**
     * Samples m clusters by using Cluster Sampling
     * 
     * @param clusterIdList
     * @param sampleM
     * @return 
     */
    public static TransposeDataCollection randomSampling(TransposeDataList clusterIdList, int sampleM) {
        TransposeDataCollection sampledIds = new TransposeDataCollection(); 
        
        Object[] selectedClusters = clusterIdList.keySet().toArray();
        PHPfunctions.<Object>shuffle(selectedClusters);
        
        for(int i = 0; i<sampleM; ++i) {
            Object cluster = selectedClusters[i];
            sampledIds.put(cluster, clusterIdList.get(cluster).toFlatDataCollection());
        }
        
        
        return sampledIds;
    }
    
    /**
     * Calculate the mean from the sample
     * 
     * @param sampleDataCollection
     * @return 
     */
    public static double mean(TransposeDataCollection sampleDataCollection) {
        double mean = 0.0;
        
        int totalSampleN = 0;
        for(Map.Entry<Object, FlatDataCollection> entry : sampleDataCollection.entrySet()) {
            mean += Descriptives.sum(entry.getValue());
            
            totalSampleN += entry.getValue().size();
        }
        
        mean/=totalSampleN;
        
        return mean;
    }
    
    /**
     * Calculates Variance for Xbar
     * 
     * @param sampleDataCollection
     * @param populationM
     * @param Nbar
     * @return 
     */
    public static double xbarVariance(TransposeDataCollection sampleDataCollection, int populationM, double Nbar) {
        double xbarVariance = 0.0;
        
        int sampleM = sampleDataCollection.size();
        
        double mean = mean(sampleDataCollection);
        
        for(Map.Entry<Object, FlatDataCollection> entry : sampleDataCollection.entrySet()) {
            double sum = 0.0;
            Iterator<Double> it = entry.getValue().iteratorDouble();
            while(it.hasNext()) {
                sum+=(it.next() - mean);
            }
            xbarVariance+= sum*sum/(sampleM-1);
        }
        
        xbarVariance *= (populationM-sampleM)/(populationM*sampleM*Nbar*Nbar);
        
        return xbarVariance;
    }
    
    /**
     * Calculates Standard Deviation for Xbar
     * 
     * @param sampleDataCollection
     * @param populationM
     * @param Nbar
     * @return 
     */
    public static double xbarStd(TransposeDataCollection sampleDataCollection, int populationM, double Nbar) {
        return Math.sqrt(xbarVariance(sampleDataCollection, populationM, Nbar));
    }
}
