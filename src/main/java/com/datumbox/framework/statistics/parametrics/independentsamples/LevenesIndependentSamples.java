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
package com.datumbox.framework.statistics.parametrics.independentsamples;

import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.dataobjects.TransposeDataCollection;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author bbriniotis
 */
public class LevenesIndependentSamples {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    /**
     * Independent Samples Variance Test for F as described at http://en.wikipedia.org/wiki/Levene's_test
     * 
     * @param transposeDataCollection
     * @param aLevel
     * @return
     * @throws IllegalArgumentException 
     */
    public static boolean testVariances(TransposeDataCollection transposeDataCollection, double aLevel) throws IllegalArgumentException {
        int n=0;
        int k = transposeDataCollection.size();
        if(k<=1) {
            throw new IllegalArgumentException();
        }

        Map<Object, Integer> nj = new HashMap<>();
        Map<Object, Double> Yjmean = new HashMap<>();
        for(Map.Entry<Object, FlatDataCollection> entry : transposeDataCollection.entrySet()) {
            Object j = entry.getKey();
            FlatDataCollection flatDataCollection = entry.getValue();
            
            int tmp = flatDataCollection.size();
            if(tmp==0) {
                throw new IllegalArgumentException();
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
        Yjmean = null;

        //W = [(N-k)*SUM(i=1 to k)(N(i)*(ZBAR(i.) - ZBAR(..))**2]/[(k-1)*SUM(i=1 to k)(SUM(j=1 to N(i))(Z(ij) - ZBAR(i.))**2]
        double numerator=0;
        double denominator=0;
        for(Map.Entry<Object, FlatDataCollection> entry : transposeDataCollection.entrySet()) {
            Object j = entry.getKey();
            FlatDataCollection flatDataCollection =  entry.getValue();
            
            numerator+=nj.get(j)*Math.pow(zjdot.get(j)-zdotdot, 2);
            int i = 0;
            for(Object Yji : flatDataCollection) {
                denominator+= Math.pow(zji.get(j).get(i)-zjdot.get(j), 2);
                ++i;
            }   
        }
        zjdot = null;
        zji = null;
        nj = null;

        if(denominator==0) {
            throw new InvalidParameterException();
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
    protected static boolean checkCriticalValue(double score, int n, int k, double aLevel) {
        double probability=ContinuousDistributions.FCdf(score,k-1,n-k);

        boolean rejectH0=false;

        if(probability<=aLevel || probability>=(1-aLevel)) {
            rejectH0=true;
        }

        return rejectH0;
    }

}
