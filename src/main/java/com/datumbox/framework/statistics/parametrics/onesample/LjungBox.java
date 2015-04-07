/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
package com.datumbox.framework.statistics.parametrics.onesample;

import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;
import java.util.Iterator;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class LjungBox {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;

    /**
     * Autocorrelation test for many pk
     * 
     * @param pkList
     * @param n
     * @param aLevel
     * @return
     * @throws IllegalArgumentException 
     */
    public static boolean testAutocorrelation(FlatDataCollection pkList, int n, double aLevel) throws IllegalArgumentException {
        int h=pkList.size();
        if(n<=h) {
            throw new IllegalArgumentException();
        }

        double Q=0;
        int k=1;

        Iterator<Double> it = pkList.iteratorDouble();
        while(it.hasNext()) {
            Double pkValue = it.next();
            Q+=(pkValue*pkValue)/(n-k);
            ++k;
        }

        Q*=n*(n+2);

        boolean rejectH0=checkCriticalValue(Q, h, aLevel);

        return rejectH0;
    }
    
    /**
     * Checks the Critical Value to determine if the Hypothesis should be rejected
     * 
     * @param score
     * @param h
     * @param aLevel
     * @return 
     */
    protected static boolean checkCriticalValue(double score, int h, double aLevel) {
        return checkCriticalValue(score, h, aLevel, 0, 0);
    }
    
    /**
     * Checks the Critical Value to determine if the Hypothesis should be rejected
     * 
     * @param score
     * @param h
     * @param aLevel
     * @param p
     * @param q
     * @return 
     */
    protected static boolean checkCriticalValue(double score, int h, double aLevel, int p, int q) { //p and q are used only in ARIMA Models (p,0,q).
        double probability=ContinuousDistributions.ChisquareCdf(score,h - p - q);

        boolean rejectH0=false;

        double a=aLevel;
        if(probability>=(1-a)) {
            rejectH0=true;
        }

        return rejectH0;
    }
}
