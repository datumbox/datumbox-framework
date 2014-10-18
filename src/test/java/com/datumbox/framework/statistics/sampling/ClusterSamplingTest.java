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
import com.datumbox.configuration.TestConfiguration;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class ClusterSamplingTest {
    
    public ClusterSamplingTest() {
    }
    
    protected TransposeDataList generateClusterIdList() {
        TransposeDataList clusterIdList = new TransposeDataList();
        clusterIdList.put("cluster1", new FlatDataList(Arrays.asList(new Object[]{0,1,2,3,4,5,6,7,8,9})));
        clusterIdList.put("cluster2", new FlatDataList(Arrays.asList(new Object[]{10,11,12,13,14,15,16,17,18,19})));
        clusterIdList.put("cluster3", new FlatDataList(Arrays.asList(new Object[]{20,21,22,23,24,25,26,27,28,29})));
        clusterIdList.put("cluster4", new FlatDataList(Arrays.asList(new Object[]{30,31,32,33,34,35,36,37,38,39})));
        
        return clusterIdList;
    }
    
    protected TransposeDataCollection generateSampleDataCollection() {
        TransposeDataCollection sampleDataCollection = new TransposeDataCollection();
        sampleDataCollection.put("cluster1", new FlatDataCollection(Arrays.asList(new Object[]{21,12,23,14,25,16,27,18,29,10})));
        sampleDataCollection.put("cluster2", new FlatDataCollection(Arrays.asList(new Object[]{11,12,13,14,15,16,17,18,19,20})));
        //sampleDataCollection.internalData.put("cluster3", new FlatDataCollection(Arrays.asList(new Object[]{31,2,33,4,35,6,37,8,39,0})));
        //sampleDataCollection.internalData.put("cluster4", new FlatDataCollection(Arrays.asList(new Object[]{1,32,3,34,5,36,7,38,9,30})));
        
        return sampleDataCollection;
    }
    
    /**
     * Test of Nbar method, of class ClusterSampling.
     */
    @Test
    public void testNbar() {
        System.out.println("Nbar");
        TransposeDataList clusterIdList = generateClusterIdList();
        
        double expResult = 10.0;
        double result = ClusterSampling.Nbar(clusterIdList);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of randomSampling method, of class ClusterSampling.
     */
    @Test
    public void testRandomSampling() {
        System.out.println("randomSampling");
        TransposeDataList clusterIdList = generateClusterIdList();
        int sampleM = 2;
        double expResult = sampleM;
        TransposeDataCollection sampledIds = ClusterSampling.randomSampling(clusterIdList, sampleM);
        double result = sampledIds.size();
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of mean method, of class ClusterSampling.
     */
    @Test
    public void testMean() {
        System.out.println("mean");
        TransposeDataCollection sampleDataCollection = generateSampleDataCollection();
        double expResult = 17.5;
        double result = ClusterSampling.mean(sampleDataCollection);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of xbarVariance method, of class ClusterSampling.
     */
    @Test
    public void testXbarVariance() {
        System.out.println("xbarVariance");
        TransposeDataCollection sampleDataCollection = generateSampleDataCollection();
        int populationM = 4;
        double Nbar = 10.0;
        double expResult = 2.0;
        double result = ClusterSampling.xbarVariance(sampleDataCollection, populationM, Nbar);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of xbarStd method, of class ClusterSampling.
     */
    @Test
    public void testXbarStd() {
        System.out.println("xbarStd");
        TransposeDataCollection sampleDataCollection = generateSampleDataCollection();
        int populationM = 4;
        double Nbar = 10.0;
        double expResult = 1.4142135623731;
        double result = ClusterSampling.xbarStd(sampleDataCollection, populationM, Nbar);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }
    
}
