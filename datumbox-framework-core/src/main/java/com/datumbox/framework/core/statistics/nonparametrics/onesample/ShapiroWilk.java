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
package com.datumbox.framework.core.statistics.nonparametrics.onesample;

import com.datumbox.framework.common.dataobjects.FlatDataCollection;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;

import java.util.Arrays;

/**
 * Shapiro-Wilk test for normality.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ShapiroWilk {
    
    /**
     * Tests the rejection of null Hypothesis for a particular confidence level
     * 
     * @param flatDataCollection
     * @param aLevel
     * @return 
     */
    public static boolean test(FlatDataCollection flatDataCollection, double aLevel) {
        boolean rejectH0=false;

        double probability = shapiroWilkW(flatDataCollection.stream().filter(x -> x!=null).mapToDouble(TypeInference::toDouble).toArray());

        double a=aLevel;
        if(probability<=a || probability>=(1.0-a)) {
            rejectH0=true; 
        }

        return rejectH0; 
    }
    

    /**
    * 
    * The following code is ported from Javascript.
    * Original script: https://github.com/rniwa/js-shapiro-wilk/blob/master/shapiro-wilk.js
    */
    
    /**
     * Used internally by ShapiroWilkW().
     * 
     * @param cc
     * @param nord
     * @param x
     * @return 
     */
    private static double poly(double[] cc, int nord, double x) {
        /* Algorithm AS 181.2    Appl. Statist.    (1982) Vol. 31, No. 2
        Calculates the algebraic polynomial of order nord-1 with array of coefficients cc.
        Zero order coefficient is cc(1) = cc[0] */
        
        double ret_val = cc[0];
        if (nord > 1) {
            double p = x * cc[nord-1];
            for (int j = nord - 2; j > 0; --j) {
                p = (p + cc[j]) * x;
            }
            ret_val += p;
        }
        return ret_val;
    }
    
    /**
     * Used internally by ShapiroWilkW()
     * 
     * @param x
     * @return 
     */
    private static int sign(double x) {
        if (x == 0) {
            return 0;
        }
        return (x > 0)? 1 : -1;
    }
    
    /**
     * Calculates P-value for ShapiroWilk Test
     * 
     * @param x
     * @return
     */
    private static double shapiroWilkW(double[] x) {
        Arrays.sort(x);
        
        int n = x.length;
        if(n<3) {
            throw new IllegalArgumentException("The provided collection must have more than 2 elements.");
        }
        if (n > 5000) {
            throw new IllegalArgumentException("The provided collection must have less or equal to 5000 elements.");
        }
        
        int nn2 = n/2;
        double[] a = new double[nn2+1]; /* 1-based */

        /*    
            ALGORITHM AS R94 APPL. STATIST. (1995) vol.44, no.4, 547-551.
            Calculates the Shapiro-Wilk W test and its significance level
        */
        double small = 1e-19;

        /* polynomial coefficients */
        double g[] = { -2.273, 0.459 };
        double c1[] = { 0.0, 0.221157, -0.147981, -2.07119, 4.434685, -2.706056 };
        double c2[] = { 0.0, 0.042981, -0.293762, -1.752461, 5.682633, -3.582633 };
        double c3[] = { 0.544, -0.39978, 0.025054, -6.714e-4 };
        double c4[] = { 1.3822, -0.77857, 0.062767, -0.0020322 };
        double c5[] = { -1.5861, -0.31082, -0.083751, 0.0038915 };
        double c6[] = { -0.4803, -0.082676, 0.0030302 };

        /* Local variables */
        int i, j, i1;

        double ssassx, summ2, ssumm2, gamma, range;
        double a1, a2, an, m, s, sa, xi, sx, xx, y, w1;
        double fac, asa, an25, ssa, sax, rsn, ssx, xsx;

        double pw;
        an = (double)n;

        if (n == 3) {
            a[1] = 0.70710678;/* = sqrt(1/2) */
        }
        else {
            an25 = an + 0.25;
            summ2 = 0.0;
            for (i = 1; i <= nn2; i++) {
                a[i] = ContinuousDistributions.normalQuantile((i - 0.375) / an25, 0, 1); // p(X <= x), 
                summ2 += a[i] * a[i];
            }
            summ2 *= 2.0;
            ssumm2 = Math.sqrt(summ2);
            rsn = 1.0 / Math.sqrt(an);
            a1 = poly(c1, 6, rsn) - a[1] / ssumm2;

            /* Normalize a[] */
            if (n > 5) {
                i1 = 3;
                a2 = -a[2] / ssumm2 + poly(c2, 6, rsn);
                fac = Math.sqrt((summ2 - 2.0 * (a[1] * a[1]) - 2.0 * (a[2] * a[2])) / (1.0 - 2.0 * (a1 * a1) - 2.0 * (a2 * a2)));
                a[2] = a2;
            } 
            else {
                i1 = 2;
                fac = Math.sqrt((summ2 - 2.0 * (a[1] * a[1])) / ( 1.0  - 2.0 * (a1 * a1)));
            }
            a[1] = a1;
            for (i = i1; i <= nn2; i++) {
                a[i] /= - fac;
            }
        }

        /* Check for zero range */

        range = x[n-1] - x[0];
        if (range < small) {
            throw new IllegalArgumentException("The range is too small.");
        }


        /* Check for correct sort order on range - scaled X */

        xx = x[0] / range;
        sx = xx;
        sa = -a[1];
        for (i = 1, j = n - 1; i < n; j--) {
            xi = x[i] / range;
            if (xx - xi > small) {
                throw new IllegalArgumentException("The xx - xi is too big.");
            }
            sx += xi;
            i++;
            if (i != j) {
                sa += sign(i - j) * a[Math.min(i, j)];
            }
            xx = xi;
        }


        /* Calculate W statistic as squared correlation
        between data and coefficients */

        sa /= n;
        sx /= n;
        ssa = ssx = sax = 0.;
        for (i = 0, j = n - 1; i < n; i++, j--) {
            if (i != j) {
                asa = sign(i - j) * a[1 + Math.min(i, j)] - sa;
            }
            else {
                asa = -sa;
            }
            xsx = x[i] / range - sx;
            ssa += asa * asa;
            ssx += xsx * xsx;
            sax += asa * xsx;
        }

        /* W1 equals (1-W) calculated to avoid excessive rounding error
        for W very near 1 (a potential problem in very large samples) */

        ssassx = Math.sqrt(ssa * ssx);
        w1 = (ssassx - sax) * (ssassx + sax) / (ssa * ssx);
        double w = 1.0 - w1;

        /* Calculate significance level for W */

        if (n == 3) {/* exact P value : */
            double pi6 = 6.0/Math.PI; /* 1.90985931710274 = 6/pi */ 
            double stqr = Math.PI/3.0; /* 1.04719755119660 = asin(sqrt(3/4)) */
            pw = pi6 * (Math.asin(Math.sqrt(w)) - stqr);
            if (pw < 0.) {
                pw = 0;
            }
            //return w;
            return pw;
        }
        y = Math.log(w1);
        xx = Math.log(an);
        if (n <= 11) {
            gamma = poly(g, 2, an);
            if (y >= gamma) {
                pw = 1e-99; /* an "obvious" value, was 'small' which was 1e-19f */
                return pw;
            }
            y = -Math.log(gamma - y);
            m = poly(c3, 4, an);
            s = Math.exp(poly(c4, 4, an));
        } 
        else { /* n >= 12 */
            m = poly(c5, 4, xx);
            s = Math.exp(poly(c6, 3, xx));
        }

        pw = alnorm((y-m)/s);

        return pw;
    }

    /**
     * Converts score to p-value.
     * Algorithm AS 66, Journal of the Royal Statistical Society Series C (Applied Statistics) vol. 22, pp. 424-427 (1973)
     *
     * @param x
     * @return
     */
    private static double alnorm(double x) {
        boolean up = true;
        if (x < 0.0) {
            up = false;
            x = -x;
        }

        double alnorm = 0.0;
        if (x <= 7.0 || (up && x <= 18.66)) {
            double y = 0.5 * x * x;
            if (x > 1.28) {
                alnorm = 0.398942280385 * Math.exp(-y) / (x + -3.8052E-8 + 1.00000615302 / (x + 3.98064794E-4 + 1.98615381364 / (x + -0.151679116635 + 5.29330324926
                        / (x + 4.8385912808 + -15.1508972451 / (x + 0.742380924027 + 30.789933034 / (x + 3.99019417011))))));
            }
            else {
                alnorm = 0.5 - x * (0.398942280444 - 0.39990348504 * y/(y + 5.75885480458 + -29.8213557807 /(y + 2.62433121679 + 48.6959930692/(y + 5.92885724438))));
            }
        }

        if(!up) {
            alnorm = 1.0 - alnorm;
        }
        return alnorm;
    }
}
