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
package com.datumbox.framework.core.statistics.distributions;

import com.datumbox.framework.core.mathematics.discrete.Arithmetics;

/**
 * This class provides methods for the CDFs and PDFs of the most common discrete 
 * distributions.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class DiscreteDistributions {
    /**
     * Returns the probability of k and p
     * 
     * @param k
     * @param p
     * @return
     */
    public static double bernoulli(boolean k, double p) {
        if(p<0) {
            throw new IllegalArgumentException("The probability p can't be negative.");
        }
        
        return (k)?p:(1-p);
    }
    
    /**
     * Returns the cumulative probability under bernoulli
     * 
     * @param k
     * @param p
     * @return
     */
    public static double bernoulliCdf(int k, double p) {
        if(p<0) {
            throw new IllegalArgumentException("The probability p can't be negative.");
        }
        
        double probabilitySum=0.0;
        if(k<0) {
            
        }
        else if(k<1) { //aka k==0
            probabilitySum=(1-p);
        }
        else { //k>=1 aka k==1
            probabilitySum=1.0;
        }
        
        return probabilitySum;
    }
    
    /**
     * Returns the probability of k of a specific number of tries n and probability p
     * 
     * @param k
     * @param p
     * @param n
     * @return
     */
    public static double binomial(int k, double p, int n) {
        if(k<0 ||  p<0 || n<1) {
            throw new IllegalArgumentException("All the parameters must be positive and n larger than 1.");
        }
        
        k = Math.min(k, n); 
        
        /*
        //Slow and can't handle large numbers
        $probability=StatsUtilities::combination($n,$k)*pow($p,$k)*pow(1-$p,$n-$k);
        */

        //fast and can handle large numbers
        //Cdf(k)-Cdf(k-1)
        double probability = approxBinomialCdf(k,p,n); 
        if(k>0) {
            probability -= approxBinomialCdf(k-1,p,n);
        }
        
        return probability;
    }
    
    /**
     * Returns the cumulative probability of k of a specific number of tries n and probability p
     * 
     * @param k
     * @param p
     * @param n
     * @return
     */
    public static double binomialCdf(int k, double p, int n) {
        if(k<0 ||  p<0 || n<1) {
            throw new IllegalArgumentException("All the parameters must be positive and n larger than 1.");
        }
        
        k = Math.min(k, n);
        
        double probabilitySum = approxBinomialCdf(k,p,n); 
        
        return probabilitySum;
    }
    
    /**
     * Returns the a good approximation of cumulative probability of k of a specific number of tries n and probability p
     * 
     * @param k
     * @param p
     * @param n
     * @return 
     */
    private static double approxBinomialCdf(int k, double p, int n) {
        //use an approximation as described at http://www.math.ucla.edu/~tom/distributions/binomial.html
        double Z = p;
        double A=k+1;
        double B=n-k;
        double S=A+B;
        double BT=Math.exp(ContinuousDistributions.logGamma(S)-ContinuousDistributions.logGamma(B)-ContinuousDistributions.logGamma(A)+A*Math.log(Z)+B*Math.log(1-Z));
        
        double probabilitySum;
        if (Z<(A+1)/(S+2)) {
            probabilitySum=BT*ContinuousDistributions.betinc(Z,A,B);
        } 
        else {
            probabilitySum=1.0-BT*ContinuousDistributions.betinc(1.0-Z,B,A);
        }
        probabilitySum=1.0-probabilitySum;

        return probabilitySum;
    }
    
    /**
     * Returns the probability that the first success requires k trials with probability of success p
     * 
     * @param k
     * @param p
     * @return
     */
    public static double geometric(int k, double p) {
        if(k<=0 || p<0) {
            throw new IllegalArgumentException("All the parameters must be positive.");
        }
        
        double probability = Math.pow(1-p,k-1)*p;
        
        return probability;
    }
    
    /**
     * Returns the cumulative probability of geometric
     * 
     * @param k
     * @param p
     * @return
     */
    public static double geometricCdf(int k, double p) {
        if(k<=0 || p<0) {
            throw new IllegalArgumentException("All the parameters must be positive.");
        }
        
        double probabilitySum = 0.0;
        for(int i=1;i<=k;++i) {
            probabilitySum += geometric(i, p);
        }
        
        return probabilitySum;
    }
    
    /**
     * Returns the probability of requiring n tries to achieve r successes with probability of success p
     * 
     * @param n
     * @param r
     * @param p
     * @return
     */
    public static double negativeBinomial(int n, int r, double p) {
        //tested its validity with http://www.mathcelebrity.com/binomialneg.php
        if(n<0 || r<0 || p<0) {
            throw new IllegalArgumentException("All the parameters must be positive.");
        }
        n = Math.max(n,r);//obvisouly the total number of tries must be larger than the number of required successes
        
        double probability = Arithmetics.combination(n-1, r-1)*Math.pow(1-p,n-r)*Math.pow(p,r);
        
        return probability;
    }
    
    /**
     * Returns the cumulative probability of negativeBinomial
     * 
     * @param n
     * @param r
     * @param p
     * @return
     */
    public static double negativeBinomialCdf(int n, int r, double p) {
        if(n<0 || r<0 || p<0) {
            throw new IllegalArgumentException("All the parameters must be positive.");
        }
        n = Math.max(n,r);
        
        double probabilitySum = 0.0;
        for(int i=0;i<=r;++i) {
            probabilitySum += negativeBinomial(n, i, p);
        }
        
        return probabilitySum;
    }

    /**
     * Returns the probability of uniform discrete distribution with parameter n
     * 
     * @param n
     * @return
     */
    public static double uniform(int n) {
        if(n<1) {
            throw new IllegalArgumentException("The n must be larger than 1.");
        }
        
        double probability = 1.0/n;
        
        return probability;
    }
    
    /**
     * Returns the cumulative probability of uniform
     * 
     * @param k
     * @param n
     * @return
     */
    public static double uniformCdf(int k, int n) {
        if(k<0 || n<1) {
            throw new IllegalArgumentException("All the parameters must be positive and n larger than 1.");
        }
        k = Math.min(k, n);
        
        double probabilitySum = k*uniform(n);
        
        return probabilitySum;
    }
    
    /**
     * Returns the probability of finding k successes on a sample of n, from a population with Kp successes and size Np
     * 
     * @param k
     * @param n
     * @param Kp
     * @param Np
     * @return
     */
    public static double hypergeometric(int k, int n, int Kp, int Np) {
        if(k<0 || n<0 || Kp<0 || Np<0) {
            throw new IllegalArgumentException("All the parameters must be positive.");
        }
        Kp = Math.max(k, Kp);
        Np = Math.max(n, Np);
        
        /*
        //slow!
        $probability=StatsUtilities::combination($Kp,$k)*StatsUtilities::combination($Np-$Kp,$n-$k)/StatsUtilities::combination($Np,$n);
        */

        //fast and can handle large numbers
        //Cdf(k)-Cdf(k-1)
        double probability = approxHypergeometricCdf(k,n,Kp,Np);
        if(k>0) {
            probability -= approxHypergeometricCdf(k-1,n,Kp,Np);
        }

        
        return probability;
    }
    
    /**
     * Returns the cumulative probability of hypergeometric
     * 
     * @param k
     * @param n
     * @param Kp
     * @param Np
     * @return
     */
    public static double hypergeometricCdf(int k, int n, int Kp, int Np) {
        if(k<0 || n<0 || Kp<0 || Np<0) {
            throw new IllegalArgumentException("All the parameters must be positive.");
        }
        Kp = Math.max(k, Kp);
        Np = Math.max(n, Np);
        
        /*
        //slow!
        $probabilitySum=0;
        for($i=0;$i<=$k;++$i) {
            $probabilitySum+=self::hypergeometric($i,$n,$Kp,$Np);
        }
        */

        //fast and can handle large numbers
        //Cdf(k)-Cdf(k-1)
        double probabilitySum = approxHypergeometricCdf(k,n,Kp,Np);
        
        return probabilitySum;
    }
    
    private static double approxHypergeometricCdf(double x,double n,double m,double nn) {
        //Approximate estimation as described at http://www.math.ucla.edu/~tom/distributions/Hypergeometric.html?
        double Prob;
        if (2*m>nn) {
            if (2*n>nn) {
                Prob=hyp(nn-m-n+x,nn-n,nn-m,nn);
            } 
            else {
                Prob=1-hyp(n-x-1,n,nn-m,nn);
            }
        } 
        else if (2*n>nn) {
            Prob=1-hyp(m-x-1,m,nn-n,nn);
        } 
        else {
            Prob=hyp(x,n,m,nn);
        }
        return Prob;
    }

    private static double hyp(double x, double n, double m, double nn) {
        double nz;
        double mz;
        if (m<n) {         //best to have n<m
            nz=m;
            mz=n;
        } 
        else {
            nz=n;
            mz=m;
        }
        double h=1;
        double s=1;
        double k=0;
        double i=0;
        while (i<x) {
            while ((s>1)&&(k<nz)) {
                h=h*(1-mz/(nn-k));
                s=s*(1-mz/(nn-k));
                ++k;
            }
            h=h*(nz-i)*(mz-i)/(i+1)/(nn-nz-mz+i+1);
            s=s+h;
            ++i;
        }
        while (k<nz) {
            s=s*(1-mz/(nn-k));
            ++k;
        }
        return s;
    }
    
    /**
     * Returns the probability of k occurrences when rate is lamda
     * 
     * @param k
     * @param lamda
     * @return
     */
    public static double poisson(int k, double lamda) {
        if(k<0 || lamda<0) {
            throw new IllegalArgumentException("All the parameters must be positive.");
        }
        
        /*
        //Slow
        $probability=pow($lamda,$k)*exp(-$lamda)/StatsUtilities::factorial($k);
        */

        //fast and can handle large numbers
        //Cdf(k)-Cdf(k-1)
        double probability = poissonCdf(k,lamda);
        if(k>0) {
            probability -= poissonCdf(k-1,lamda);
        }
        
        return probability;
    }
    
    /**
     * Returns the cumulative probability of poisson
     * 
     * @param k
     * @param lamda
     * @return
     */
    public static double poissonCdf(int k, double lamda) {
        if(k<0 || lamda<0) {
            throw new IllegalArgumentException("All the parameters must be positive.");
        }
        
        /*
        //Slow!
        $probabilitySum=0;
        for($i=0;$i<=$k;++$i) {
            $probabilitySum+=self::poisson($i,$lamda);
        }
        */

        //Faster solution as described at: http://www.math.ucla.edu/~tom/distributions/poisson.html?
        double probabilitySum = 1.0 - ContinuousDistributions.gammaCdf(lamda,k+1);
        
        return probabilitySum;
    }
}
