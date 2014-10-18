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
package com.datumbox.framework.statistics.distributions;

import com.datumbox.common.utilities.RandomValue;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;

/**
 *
 * @author bbriniotis
 */
public class ContinuousDistributions {
    
    /**
     * Returns the probability from 0 to x of a specific chisquare score and degrees of freedom
     * 
     * @param x
     * @param df
     * @return 
     * @throws IllegalArgumentException 
     */
    public static double ChisquareCdf(double x, int df) throws IllegalArgumentException {
        if(df<=0) {
            throw new IllegalArgumentException();
        }
        
        return GammaCdf(x/2.0, df/2.0);
    }
    
    /**
     * Returns the p-value of a specific z score for Gaussian
     * Ported from C# code posted at http://jamesmccaffrey.wordpress.com/2010/11/05/programmatically-computing-the-area-under-the-normal-curve/
     * 
     * @param z
     * @return 
     */
    public static double GaussCdf(double z) {
        // input = z-value (-inf to +inf)
        // output = p under Normal curve from -inf to z
        // e.g., if z = 0.0, function returns 0.5000
        // ACM Algorithm #209
        double y; // 209 scratch variable
        double p; // result. called ‘z’ in 209
        double w; // 209 scratch variable

        if (z == 0.0) {
            p = 0.0;
        }
        else {
            y = Math.abs(z) / 2.0;
            if (y >= 3.0) {
                p = 1.0;
            }
            else if (y < 1.0) {
                w = y * y;
                p = ((((((((0.000124818987 * w
                  - 0.001075204047) * w + 0.005198775019) * w
                  - 0.019198292004) * w + 0.059054035642) * w
                  - 0.151968751364) * w + 0.319152932694) * w
                  - 0.531923007300) * w + 0.797884560593) * y * 2.0;
            }
            else {
                y = y - 2.0;
                p = (((((((((((((-0.000045255659 * y
                  + 0.000152529290) * y - 0.000019538132) * y
                  - 0.000676904986) * y + 0.001390604284) * y
                  - 0.000794620820) * y - 0.002034254874) * y
                  + 0.006549791214) * y - 0.010557625006) * y
                  + 0.011630447319) * y - 0.009279453341) * y
                  + 0.005353579108) * y - 0.002141268741) * y
                  + 0.000535310849) * y + 0.999936657524;
            }
        }

        if (z > 0.0) {
            return (p + 1.0) / 2.0;
        }
        
        return (1.0 - p) / 2.0;
    }
    
    
    /**
     * It estimates a numeric approximation of gamma function.
     * 
     * @param x     The input x
     * @return      The value of gamma(x)
     */
    public static double gamma(double x) { 
        return Math.exp(LogGamma(x)); 
    }
    
    /**
     * Log Gamma Function
     * 
     * @param Z
     * @return 
     */
    public static double LogGamma(double Z) {
        double S = 1.0 + 76.18009173/Z-86.50532033/(Z+1.0)+24.01409822/(Z+2.0)-1.231739516/(Z+3.0)+0.00120858003/(Z+4.0)-0.00000536382/(Z+5.0);
        double LG = (Z-0.5)*Math.log(Z+4.5)-(Z+4.5)+Math.log(S*2.50662827465);
        
        return LG;
    }
    
    /**
     * Internal function used by StudentCdf
     * 
     * @param x
     * @param A
     * @param B
     * @return 
     */
    protected static double Betinc(double x, double A, double B) {
        double A0=0.0;
        double B0=1.0;
        double A1=1.0;
        double B1=1.0;
        double M9=0.0;
        double A2=0.0;
        double C9=0.0;
        while (Math.abs((A1-A2)/A1)>0.00001) {
            A2=A1;
            C9=-(A+M9)*(A+B+M9)*x/(A+2.0*M9)/(A+2.0*M9+1.0);
            A0=A1+C9*A0;
            B0=B1+C9*B0;
            M9=M9+1;
            C9=M9*(B-M9)*x/(A+2.0*M9-1.0)/(A+2.0*M9);
            A1=A0+C9*A1;
            B1=B0+C9*B1;
            A0=A0/B1;
            B0=B0/B1;
            A1=A1/B1;
            B1=1.0;
        }
        return A1/A; 
    }
    
    /**
     * Calculates the probability from -INF to X under Student's Distribution
     * Ported to PHP from Javascript implementation found at http://www.math.ucla.edu/~tom/distributions/tDist.html
     * 
     * @param x
     * @param df
     * @return 
     * @throws IllegalArgumentException 
     */
    public static double StudentsCdf(double x, int df) throws IllegalArgumentException {
        if(df<=0) {
            throw new IllegalArgumentException();
        }
        
        double tcdf = 0.0;

        double A = df/2.0;
        double S = A+0.5;
        double Z = df/(df + x*x);
        double BT = Math.exp(LogGamma(S)-LogGamma(0.5)-LogGamma(A)+A*Math.log(Z)+0.5*Math.log(1.0-Z));
        double betacdf = 0.0;
        if (Z<(A+1.0)/(S+2.0)) {
            betacdf = BT*Betinc(Z,A,0.5);
        } 
        else {
            betacdf=1-BT*Betinc(1.0-Z,0.5,A);
        }
        if (x<0) {
            tcdf=betacdf/2.0;
        } 
        else {
            tcdf=1.0-betacdf/2.0;
        }

        return tcdf;
    }
    
    /**
     * Calculates the probability from 0 to X under Exponential Distribution
     * 
     * @param x
     * @param lamda
     * @return
     * @throws IllegalArgumentException 
     */
    public static double ExponentialCdf(double x, double lamda) throws IllegalArgumentException {
        if(x<0 || lamda<=0) {
            throw new IllegalArgumentException();
        }
        
        double probability = 1.0 - Math.exp(-lamda*x);
        
        return probability;
    }
    
    /**
     * Calculates the probability from 0 to X under Beta Distribution
     * 
     * @param x
     * @param a
     * @param b
     * @return 
     */
    public static double BetaCdf(double x, double a, double b) throws IllegalArgumentException {
        if(x<0 || a<=0 || b<=0) {
            throw new IllegalArgumentException();
        }
        
        double Bcdf = 0.0;
        
        if(x==0) {
            return Bcdf;
        }
        else if (x>=1) {
            Bcdf=1.0;
            return Bcdf;
        }
        
        double S= a + b;

        double BT = Math.exp(LogGamma(S)-LogGamma(b)-LogGamma(a)+a*Math.log(x)+b*Math.log(1-x));
        if (x<(a+1.0)/(S+2.0)) {
            Bcdf=BT*Betinc(x,a,b);
        } 
        else {
            Bcdf=1.0-BT*Betinc(1.0-x,b,a);
        }

        return Bcdf;
    }
    
    /**
     * Calculates the probability from 0 to X under F Distribution
     * 
     * @param x
     * @param f1
     * @param f2
     * @return
     * @throws IllegalArgumentException 
     */
    public static double FCdf(double x, int f1, int f2) throws IllegalArgumentException {
        if(x<0 || f1<=0 || f2<=0) {
            throw new IllegalArgumentException();
        }
        
        double FCdf=0.0;
        
        double Z = x/(x + (double)f2/f1);
        FCdf = BetaCdf(Z,f1/2.0,f2/2.0);
        
        return FCdf;
    }
    
    /**
     * Internal function used by GammaCdf
     * 
     * @param x
     * @param A
     * @return 
     */
    protected static double Gcf(double x, double A) {
       // Good for X>A+1
       double A0=0;
       double B0=1;
       double A1=1;
       double B1=x;
       double AOLD=0;
       double N=0;
       while (Math.abs((A1-AOLD)/A1)>.00001) {
           AOLD=A1;
           N=N+1;
           A0=A1+(N-A)*A0;
           B0=B1+(N-A)*B0;
           A1=x*A0+N*A1;
           B1=x*B0+N*B1;
           A0=A0/B1;
           B0=B0/B1;
           A1=A1/B1;
           B1=1;
       }
       double Prob=Math.exp(A*Math.log(x)-x-LogGamma(A))*A1;

       return 1.0-Prob;
    }
    
    /**
     * Internal function used by GammaCdf
     * 
     * @param x
     * @param A
     * @return 
     */
    protected static double Gser(double x, double A) {
        // Good for X<A+1.
        double T9=1/A;
        double G=T9;
        double I=1;
        while (T9>G*0.00001) {
            T9=T9*x/(A+I);
            G=G+T9;
            ++I;
        }
        G=G*Math.exp(A*Math.log(x)-x-LogGamma(A));

        return G;
    }
    
    /**
     * Internal function used by GammaCdf
     * 
     * @param x
     * @param a
     * @return
     * @throws IllegalArgumentException 
     */
    protected static double GammaCdf(double x, double a) throws IllegalArgumentException {
        if(x<0) {
            throw new IllegalArgumentException();
        }
        
        double GI=0;
        if (a>200) {
            double z=(x-a)/Math.sqrt(a);
            double y=GaussCdf(z);
            double b1=2/Math.sqrt(a);
            double phiz=0.39894228*Math.exp(-z*z/2);
            double w=y-b1*(z*z-1)*phiz/6;  //Edgeworth1
            double b2=6/a;
            int zXor4=((int)z)^4;
            double u=3*b2*(z*z-3)+b1*b1*(zXor4-10*z*z+15);
            GI=w-phiz*z*u/72;        //Edgeworth2
        } 
        else if (x<a+1) {
            GI=Gser(x,a);
        } 
        else {
            GI=Gcf(x,a);
        }
        
        return GI;
    }
    
    /**
     * Calculates the probability from 0 to X under Gamma Distribution
     * 
     * @param x
     * @param a
     * @param b
     * @return
     * @throws IllegalArgumentException 
     */
    public static double GammaCdf(double x, double a, double b) throws IllegalArgumentException {
        if(a<=0 || b<=0) {
            throw new IllegalArgumentException();
        }
        
        double GammaCdf = GammaCdf(x/b, a);
        
        return GammaCdf;
    }
    
    /**
     * Returns the cumulative probability of Uniform
     * 
     * @param x
     * @param a
     * @param b
     * @return
     * @throws IllegalArgumentException 
     */
    public static double UniformCdf(double x, double a, double b) throws IllegalArgumentException {
        if(a>=b) {
            throw new IllegalArgumentException();
        }
        
        double probabilitySum=0;
        if(x<a) {
            probabilitySum=0;
        }
        else if(x<b) {
            probabilitySum=(x-a)/(b-a);
        }
        else {
            probabilitySum=1;
        }

        return probabilitySum;
    } 
    
    /**
     * Returns the cumulative probability of Kolmogorov
     * 
     * @param z
     * @return 
     */
    public static double Kolmogorov(double z) {
        //Kolmogorov distribution. Error<.0000001
        if (z<0.27) {
            return 0.0;
        } 
        else if (z>3.2) {
            return 1.1;
        } 

        double ks=0;
        double y=-2*z*z;
        for(int i=27;i>=1;i=i-2) {
            ks=Math.exp(i*y)*(1-ks);
        }
        return 1.0-2.0*ks;
    }
    
    /*
        Inverse Cdf functions
    */
    
    /**
     * Returns the z score of a specific pvalue for Gaussian
     * Partially ported from http://home.online.no/~pjacklam/notes/invnorm/impl/karimov/StatUtil.java
     * Other implementations http://home.online.no/~pjacklam/notes/invnorm/index.html#Java
     * 
     * @param p
     * @return 
     */
    public static double GaussInverseCdf(double p) {
        final double P_LOW  = 0.02425D;
        final double P_HIGH = 1.0D - P_LOW;
        final double ICDF_A[] =
            { -3.969683028665376e+01,  2.209460984245205e+02,
              -2.759285104469687e+02,  1.383577518672690e+02,
              -3.066479806614716e+01,  2.506628277459239e+00 };
        final double ICDF_B[] =
            { -5.447609879822406e+01,  1.615858368580409e+02,
              -1.556989798598866e+02,  6.680131188771972e+01,
              -1.328068155288572e+01 };
        final double ICDF_C[] =
            { -7.784894002430293e-03, -3.223964580411365e-01,
              -2.400758277161838e+00, -2.549732539343734e+00,
              4.374664141464968e+00,  2.938163982698783e+00 };
        final double ICDF_D[] =
            { 7.784695709041462e-03,  3.224671290700398e-01,
              2.445134137142996e+00,  3.754408661907416e+00 };
        
        // Define break-points.
        // variable for result
        double z = 0;

        if(p == 0) {
            z = Double.NEGATIVE_INFINITY;
        }
        else if(p == 1) {
            z = Double.POSITIVE_INFINITY;
        }
        else if(Double.isNaN(p) || p < 0 || p > 1) {
            z = Double.NaN;
        }
        else if( p < P_LOW ) { // Rational approximation for lower region:
          double q  = Math.sqrt(-2*Math.log(p));
          z = (((((ICDF_C[0]*q+ICDF_C[1])*q+ICDF_C[2])*q+ICDF_C[3])*q+ICDF_C[4])*q+ICDF_C[5]) / ((((ICDF_D[0]*q+ICDF_D[1])*q+ICDF_D[2])*q+ICDF_D[3])*q+1);
        }
        else if ( P_HIGH < p ) { // Rational approximation for upper region:
          double q  = Math.sqrt(-2*Math.log(1-p));
          z = -(((((ICDF_C[0]*q+ICDF_C[1])*q+ICDF_C[2])*q+ICDF_C[3])*q+ICDF_C[4])*q+ICDF_C[5]) / ((((ICDF_D[0]*q+ICDF_D[1])*q+ICDF_D[2])*q+ICDF_D[3])*q+1);
        } 
        else { // Rational approximation for central region:
          double q = p - 0.5D;
          double r = q * q;
          z = (((((ICDF_A[0]*r+ICDF_A[1])*r+ICDF_A[2])*r+ICDF_A[3])*r+ICDF_A[4])*r+ICDF_A[5])*q / (((((ICDF_B[0]*r+ICDF_B[1])*r+ICDF_B[2])*r+ICDF_B[3])*r+ICDF_B[4])*r+1);
        }
        
        return z;
    }
    
    /**
     * Returns the x score of a specific pvalue and degrees of freedom for Chisquare. It We just do a bisectionsearch for a value within CHI_EPSILON, relying on the monotonicity of ChisquareCdf().
     * Ported from Javascript code posted at http://www.fourmilab.ch/rpkp/experiments/analysis/chiCalc.js
     * 
     * @param p
     * @param df
     * @return 
     */
    public static double ChisquareInverseCdf(double p, int df) {
        final double CHI_EPSILON = 0.000001;   /* Accuracy of critchi approximation */
        final double CHI_MAX = 99999.0;        /* Maximum chi-square value */
        double minchisq = 0.0;
        double maxchisq = CHI_MAX;
        double chisqval = 0.0;

        if (p <= 0.0) {
            return CHI_MAX;
        } 
        else if (p >= 1.0) {
            return 0.0;
        }

        chisqval = df/Math.sqrt(p);    /* fair first value */
        while ((maxchisq - minchisq) > CHI_EPSILON) {
            if (1-ChisquareCdf(chisqval, df) < p) {
                maxchisq = chisqval;
            } 
            else {
                minchisq = chisqval;
            }
            chisqval = (maxchisq + minchisq) * 0.5;
        }
        
        return chisqval;
    }
    
    
    /* 
        Other functions 
    */
    
    /**
     * Compute the quantile function for the normal distribution. For small to moderate probabilities, algorithm referenced
     * below is used to obtain an initial approximation which is polished with a final Newton step. For very large arguments, an algorithm of Wichura is used.
     * Used by ShapiroWilk Test
     * Ported by Javascript implementation found at https://raw.github.com/rniwa/js-shapiro-wilk/master/shapiro-wilk.js
     * Originally ported from http://svn.r-project.org/R/trunk/src/nmath/qnorm.c
     * 
     * @param p
     * @param mu
     * @param sigma
     * @return 
     */
    public static double normalQuantile(double p, double mu, double sigma) throws IllegalArgumentException {
        // The inverse of cdf.
        if(sigma<0) {
            throw new IllegalArgumentException();
        }    
        else if(sigma==0) {
            return mu;
        }
        
        double r=0;
        double val=0;
            
        double q=p - 0.5;

        if (0.075 <= p && p <= 0.925) {
            r = 0.180625 - q * q;
            val = q * (((((((r * 2509.0809287301226727 + 33430.575583588128105) * r + 67265.770927008700853) * r
                + 45921.953931549871457) * r + 13731.693765509461125) * r + 1971.5909503065514427) * r + 133.14166789178437745) * r
                + 3.387132872796366608) / (((((((r * 5226.495278852854561 + 28729.085735721942674) * r + 39307.89580009271061) * r
                + 21213.794301586595867) * r + 5394.1960214247511077) * r + 687.1870074920579083) * r + 42.313330701600911252) * r + 1);
        }
        else { /* closer than 0.075 from {0,1} boundary */
            /* r = min(p, 1-p) < 0.075 */
            if (q > 0) {
                r = 1 - p;
            }
            else {
                r = p;/* = R_DT_Iv(p) ^=  p */
            }
            
            r = Math.sqrt(-Math.log(r)); /* r = sqrt(-log(r))  <==>  min(p, 1-p) = exp( - r^2 ) */

            if (r <= 5.0) { /* <==> min(p,1-p) >= exp(-25) ~= 1.3888e-11 */
                r += -1.6;
                val = (((((((r * 7.7454501427834140764e-4 + 0.0227238449892691845833) * r + 0.24178072517745061177) * r
                    + 1.27045825245236838258) * r + 3.64784832476320460504) * r + 5.7694972214606914055) * r
                    + 4.6303378461565452959) * r + 1.42343711074968357734) / (((((((r * 1.05075007164441684324e-9 + 5.475938084995344946e-4) * r
                    + 0.0151986665636164571966) * r + 0.14810397642748007459) * r + 0.68976733498510000455) * r + 1.6763848301838038494) * r
                    + 2.05319162663775882187) * r + 1.0);
            }
            else { /* very close to  0 or 1 */
                r += -5.0;
                val = (((((((r * 2.01033439929228813265e-7 + 2.71155556874348757815e-5) * r + 0.0012426609473880784386) * r
                    + 0.026532189526576123093) * r + 0.29656057182850489123) * r + 1.7848265399172913358) * r + 5.4637849111641143699) * r
                    + 6.6579046435011037772) / (((((((r * 2.04426310338993978564e-15 + 1.4215117583164458887e-7)* r
                    + 1.8463183175100546818e-5) * r + 7.868691311456132591e-4) * r + 0.0148753612908506148525) * r
                    + 0.13692988092273580531) * r + 0.59983220655588793769) * r + 1.0);
            }

            if (q < 0.0) {
                val = -val;
            }
            /* return (q >= 0.)? r : -r ;*/
        }
        return mu + sigma * val;    
    }
    
    /**
     * Calculates probability pi, ai under dirichlet distribution
     * 
     * @param pi    The vector with probabilities.
     * @param ai    The vector with pseudocounts.
     * @return      The probability
     */
    public static double dirichletPdf(double[] pi, double[] ai) {
        double probability=1.0;
        double sumAi=0.0;
        double productGammaAi=1.0;
        
        double tmp;
        int piLength=pi.length;
        for(int i=0;i<piLength;++i) {
            tmp=ai[i];
            sumAi+= tmp;
            productGammaAi*=gamma(tmp);
            probability*=Math.pow(pi[i], tmp-1);
        }
        
        probability*=gamma(sumAi)/productGammaAi;
        
        return probability;
    }
    
    /**
     * Implementation for single alpha value.
     * 
     * @param pi    The vector with probabilities.
     * @param a     The alpha parameter for all pseudocounts.
     * @return      The probability
     */
    public static double dirichletPdf(double[] pi, double a) {
        double probability=1.0;
        
        int piLength=pi.length;
        
        for(int i=0;i<piLength;++i) {
            probability*=Math.pow(pi[i], a-1);
        }
        
        double sumAi=piLength*a;
        double productGammaAi=Math.pow(gamma(a), piLength);
        probability*=gamma(sumAi)/productGammaAi;
        
        return probability;
    }
    
    
    /**
     * Samples from Multinomial Normal Distribution.
     * 
     * @param mean
     * @param covariance
     * @return              A multinomialGaussianSample from the Multinomial Normal Distribution
     */
    public static double[] multinomialGaussianSample(double[] mean, double[][] covariance) {
        MultivariateNormalDistribution gaussian = new 
            MultivariateNormalDistribution(mean, covariance);
        gaussian.reseedRandomGenerator(RandomValue.randomGenerator.nextLong());
        return gaussian.sample();
    }
    
    /**
     * Calculates the PDF of Multinomial Normal Distribution for a particular x.
     * 
     * @param mean
     * @param covariance
     * @param x             The x record
     * @return              The multinomialGaussianPdf of x
     */
    public static double multinomialGaussianPdf(double[] mean, double[][] covariance, double[] x)  {
        MultivariateNormalDistribution gaussian = new 
            MultivariateNormalDistribution(mean, covariance);
        return gaussian.density(x);
    }
}
