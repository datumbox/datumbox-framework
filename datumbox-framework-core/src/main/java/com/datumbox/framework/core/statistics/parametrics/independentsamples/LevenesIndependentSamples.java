/**
 * Copyright (C) 2013-2019 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.core.statistics.parametrics.independentsamples;

import com.datumbox.framework.common.dataobjects.FlatDataCollection;
import com.datumbox.framework.common.dataobjects.TransposeDataCollection;
import com.datumbox.framework.core.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Levene's test for assessing the equality of variances.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class LevenesIndependentSamples {
    
    /**
     * Independent Samples Variance Test for F as described at http://en.wikipedia.org/wiki/Levene's_test
     * 
     * @param transposeDataCollection
     * @param aLevel
     * @return
     */
    public static boolean testVariances(TransposeDataCollection transposeDataCollection, double aLevel) {
        int n=0;
        int k = transposeDataCollection.size();
        if(k<=1) {
            throw new IllegalArgumentException("The collection must contain observations from at least 2 groups.");
        }

        Map<Object, Integer> nj = new HashMap<>();
        Map<Object, Double> Yjmean = new HashMap<>();
        for(Map.Entry<Object, FlatDataCollection> entry : transposeDataCollection.entrySet()) {
            Object j = entry.getKey();
            FlatDataCollection flatDataCollection = entry.getValue();
            
            int tmp = flatDataCollection.size();
            if(tmp==0) {
                throw new IllegalArgumentException("The number of observations in each group but be larger than 0.");
            }
            nj.put(j, tmp); //get the number of observation for this category
            
            n+=tmp;

            Yjmean.put(j, Descriptives.sum(flatDataCollection)/tmp);
        }

        double zdotdot = 0.0;
        Map<Object, Double> zjdot = new HashMap<>();
        Map<Object, Map<Object, Double>> zji = new HashMap<>();
        for(Map.Entry<Object, FlatDataCollection> entry : transposeDataCollection.entrySet()) {
            Object j = entry.getKey();
            FlatDataCollection flatDataCollection =  entry.getValue();
            
            
            zji.put(j, new HashMap<>());
            
            double zjdotSum=0.0;
            int i = 0;
            Iterator<Double> it = flatDataCollection.iteratorDouble();
            while(it.hasNext()) {
                Double Yji = it.next();
                double tmp = Math.abs( Yji - Yjmean.get(j) );
                zji.get(j).put(i, tmp);
                zdotdot += tmp;
                zjdotSum += tmp;
                ++i;
            }
            
            zjdot.put(j, zjdotSum/nj.get(j));
        }
        zdotdot/=n;
        //Yjmean = null;

        //W = [(N-k)*SUM(i=1 to k)(N(i)*(ZBAR(i.) - ZBAR(..))**2]/[(k-1)*SUM(i=1 to k)(SUM(j=1 to N(i))(Z(ij) - ZBAR(i.))**2]
        double numerator=0;
        double denominator=0;
        for(Map.Entry<Object, FlatDataCollection> entry : transposeDataCollection.entrySet()) {
            Object j = entry.getKey();
            FlatDataCollection flatDataCollection =  entry.getValue();
            
            numerator+=nj.get(j)*Math.pow(zjdot.get(j)-zdotdot, 2);
            int len = flatDataCollection.size();
            for(int i = 0; i < len; i++) {
                denominator+= Math.pow(zji.get(j).get(i)-zjdot.get(j), 2);
            }   
        }
        //zjdot = null;
        //zji = null;
        //nj = null;

        if(denominator==0) {
            throw new IllegalArgumentException("The values of the input collection are invalid.");
        }

        double W = ((n-k)/(k-1.0))*(numerator/denominator);

        boolean rejectH0 = checkCriticalValue(W, n, k, aLevel);

        return rejectH0;
    }
    
    /**
     * Checks the Critical Value to determine if the Hypothesis should be rejected.
     * 
     * @param score
     * @param n
     * @param k
     * @param aLevel
     * @return 
     */
    private static boolean checkCriticalValue(double score, int n, int k, double aLevel) {
        double probability=ContinuousDistributions.fCdf(score,k-1,n-k);

        boolean rejectH0=false;

        if(probability<=aLevel || probability>=(1-aLevel)) {
            rejectH0=true;
        }

        return rejectH0;
    }

}
