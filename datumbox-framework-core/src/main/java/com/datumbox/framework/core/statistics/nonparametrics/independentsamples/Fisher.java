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
package com.datumbox.framework.core.statistics.nonparametrics.independentsamples;

import com.datumbox.framework.core.mathematics.discrete.Arithmetics;

import java.util.HashSet;
import java.util.Set;

/**
 * Fisher's exact test.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Fisher {
    
    /**
     * Calculates the p-value of null Hypothesis 
     * 
     * @param n11
     * @param ndot1
     * @param ndot2
     * @param n1dot
     * @param n2dot
     * @return 
     */
    public static double getPvalue(int n11, int ndot1, int ndot2, int n1dot, int n2dot) {
        int originalN11=n11;

        int n=ndot1+ndot2;

        int min=Math.max(0,n1dot+ndot1-n);
        int max=Math.min(n1dot,ndot1);

        Set<Integer> combinations = new HashSet<>();
        combinations.add(originalN11);
        for(int r=min;r<=max;++r) {
            combinations.add(r);
        }

        double standardPart = Arithmetics.factorial(n1dot)*Arithmetics.factorial(n2dot)*Arithmetics.factorial(ndot1)*Arithmetics.factorial(ndot2)/Arithmetics.factorial(n);

        double originalPvalue=0.0;
        double Psum=0;
        for(Integer r : combinations) {
            n11=r;

            int n21=ndot1-n11;
            int n12=n1dot-n11;
            int n22=n2dot-n21;

            double pvalue=standardPart/(Arithmetics.factorial(n11)*Arithmetics.factorial(n12)*Arithmetics.factorial(n21)*Arithmetics.factorial(n22));

            if(r==originalN11) {
                originalPvalue=pvalue;
            }

            if(pvalue<=originalPvalue) {
                Psum+=pvalue;
            }
        }
        //combinations = null;    

        return Psum;
    }
    
    /**
     * Tests the rejection of null Hypothesis for a particular confidence level.
     * 
     * @param n11
     * @param n12
     * @param n21
     * @param n22
     * @param aLevel
     * @return 
     */
    public static boolean test(int n11, int n12, int n21, int n22, double aLevel) {
        int ndot1=n11+n21;
        int ndot2=n12+n22;
        int n1dot=n11+n12;
        int n2dot=n21+n22;

        double pvalue= getPvalue(n11,ndot1,ndot2,n1dot,n2dot);

        boolean rejectH0=false;
        if(pvalue<=aLevel) {
            rejectH0=true; 
        }

        return rejectH0;
    }
}
