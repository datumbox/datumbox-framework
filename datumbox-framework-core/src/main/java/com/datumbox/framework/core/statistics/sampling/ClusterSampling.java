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
import com.datumbox.framework.common.dataobjects.TransposeDataCollection;
import com.datumbox.framework.common.dataobjects.TransposeDataList;
import com.datumbox.framework.core.common.utilities.PHPMethods;
import com.datumbox.framework.core.statistics.descriptivestatistics.Descriptives;

import java.util.Iterator;
import java.util.Map;

/**
 * This class provides methods which can be used for performing Cluster Sampling.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ClusterSampling {
    
    /**
     * Returns the mean cluster size.
     * 
     * @param clusterIdList
     * @return 
     */
    public static double nBar(TransposeDataList clusterIdList) {
        int populationM = clusterIdList.size();
        
        double nBar = 0.0;
        for(Map.Entry<Object, FlatDataList> entry : clusterIdList.entrySet()) {
            nBar += (double)entry.getValue().size()/populationM;
        }

        return nBar;
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
        PHPMethods.<Object>shuffle(selectedClusters);
        
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
