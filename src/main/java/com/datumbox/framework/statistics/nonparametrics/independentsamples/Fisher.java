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
package com.datumbox.framework.statistics.nonparametrics.independentsamples;

import com.datumbox.framework.mathematics.discrete.ArithmeticMath;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author bbriniotis
 */
public class Fisher {
    /**
     * The dataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the dataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
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

        double standardPart = ArithmeticMath.factorial(n1dot)*ArithmeticMath.factorial(n2dot)*ArithmeticMath.factorial(ndot1)*ArithmeticMath.factorial(ndot2)/ArithmeticMath.factorial(n);

        double originalPvalue=0.0;
        double Psum=0;
        for(Integer r : combinations) {
            n11=r;

            int n21=ndot1-n11;
            int n12=n1dot-n11;
            int n22=n2dot-n21;

            double pvalue=standardPart/(ArithmeticMath.factorial(n11)*ArithmeticMath.factorial(n12)*ArithmeticMath.factorial(n21)*ArithmeticMath.factorial(n22));

            if(r==originalN11) {
                originalPvalue=pvalue;
            }

            if(pvalue<=originalPvalue) {
                Psum+=pvalue;
            }
        }
        combinations = null;    

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
