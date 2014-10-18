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

import com.datumbox.framework.mathematics.discrete.ArithmeticMath;

/**
 *
 * @author bbriniotis
 */
public class DiscreteDistributions {
    /**
     * Returns the probability of k and p
     * 
     * @param k
     * @param p
     * @return
     * @throws IllegalArgumentException 
     */
    public static double Bernoulli(boolean k, double p) throws IllegalArgumentException {
        if(p<0) {
            throw new IllegalArgumentException();
        }
        
        return (k)?p:(1-p);
    }
    
    /**
     * Returns the cumulative probability under Bernoulli
     * 
     * @param k
     * @param p
     * @return
     * @throws IllegalArgumentException 
     */
    public static double BernoulliCdf(int k, double p) throws IllegalArgumentException {
        if(p<0) {
            throw new IllegalArgumentException();
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
     * @throws IllegalArgumentException 
     */
    public static double Binomial(int k, double p, int n) throws IllegalArgumentException {
        if(k<0 ||  p<0 || n<1) {
            throw new IllegalArgumentException();
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
     * @throws IllegalArgumentException 
     */
    public static double BinomialCdf(int k, double p, int n) throws IllegalArgumentException {
        if(k<0 ||  p<0 || n<1) {
            throw new IllegalArgumentException();
        }
        
        k = Math.min(k, n);
        
        double probabilitySum=0.0;
        
        /*
        //slow!
        for($i=0;$i<=$k;++$i) {
            $probabilitySum+=self::Binomial($i,$p,$n);
        }
        */

        probabilitySum = approxBinomialCdf(k,p,n); 
        
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
    protected static double approxBinomialCdf(int k, double p, int n) {
        //use an approximation as described at http://www.math.ucla.edu/~tom/distributions/binomial.html
        double Z = p;
        double A=k+1;
        double B=n-k;
        double S=A+B;
        double BT=Math.exp(ContinuousDistributions.LogGamma(S)-ContinuousDistributions.LogGamma(B)-ContinuousDistributions.LogGamma(A)+A*Math.log(Z)+B*Math.log(1-Z));
        
        double probabilitySum=0.0;
        if (Z<(A+1)/(S+2)) {
            probabilitySum=BT*ContinuousDistributions.Betinc(Z,A,B);
        } 
        else {
            probabilitySum=1.0-BT*ContinuousDistributions.Betinc(1.0-Z,B,A);
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
     * @throws IllegalArgumentException 
     */
    public static double Geometric(int k, double p) throws IllegalArgumentException {
        if(k<=0 || p<0) {
            throw new IllegalArgumentException();
        }
        
        double probability = Math.pow(1-p,k-1)*p;
        
        return probability;
    }
    
    /**
     * Returns the cumulative probability of Geometric
     * 
     * @param k
     * @param p
     * @return
     * @throws IllegalArgumentException 
     */
    public static double GeometricCdf(int k, double p) throws IllegalArgumentException {
        if(k<=0 || p<0) {
            throw new IllegalArgumentException();
        }
        
        double probabilitySum = 0.0;
        for(int i=1;i<=k;++i) {
            probabilitySum += Geometric(i, p);
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
     * @throws IllegalArgumentException 
     */
    public static double NegativeBinomial(int n, int r, double p) throws IllegalArgumentException {
        //tested its validity with http://www.mathcelebrity.com/binomialneg.php
        if(n<0 || r<0 || p<0) {
            throw new IllegalArgumentException();
        }
        n = Math.max(n,r);//obvisouly the total number of tries must be larger than the number of required successes
        
        double probability = ArithmeticMath.combination(n-1, r-1)*Math.pow(1-p,n-r)*Math.pow(p,r);
        
        return probability;
    }
    
    /**
     * Returns the cumulative probability of NegativeBinomial
     * 
     * @param n
     * @param r
     * @param p
     * @return
     * @throws IllegalArgumentException 
     */
    public static double NegativeBinomialCdf(int n, int r, double p) throws IllegalArgumentException {
        if(n<0 || r<0 || p<0) {
            throw new IllegalArgumentException();
        }
        n = Math.max(n,r);
        
        double probabilitySum = 0.0;
        for(int i=0;i<=r;++i) {
            probabilitySum += NegativeBinomial(n, i, p);
        }
        
        return probabilitySum;
    }

    /*
    //does not work because we consider the number of success R and not the number of failures as R
    protected static function approxNegativeBinomialCdf($n,$r,$p) {
        //use an approximation as described at http://www.math.ucla.edu/~tom/distributions/Negbinomial.html
        $Xoriginal=$r;
        $Roriginal=$n-$r;

        $Z=1-$p;
        $A=$Xoriginal+1; //SUCCESSES
        $B=$Roriginal; //FAILURES
        $S=$A+$B;        
        $BT=exp(ContinuousDistributions::_LogGamma($S)-ContinuousDistributions::_LogGamma($B)-ContinuousDistributions::_LogGamma($A)+$A*log($Z)+$B*log(1-$Z));
        if ($Z<($A+1)/($S+2)) {
            $probabilitySum=$BT*ContinuousDistributions::_Betinc($Z,$A,$B);
        } 
        else {
            $probabilitySum=1-$BT*ContinuousDistributions::_Betinc(1-$Z,$B,$A);
        }
        $probabilitySum=1-$probabilitySum;

        return $probabilitySum;
    }
    */

    /**
     * Returns the probability of uniform discrete distribution with parameter n
     * 
     * @param n
     * @return
     * @throws IllegalArgumentException 
     */
    public static double Uniform(int n) throws IllegalArgumentException {
        if(n<1) {
            throw new IllegalArgumentException();
        }
        
        double probability = 1.0/n;
        
        return probability;
    }
    
    /**
     * Returns the cumulative probability of Uniform
     * 
     * @param k
     * @param n
     * @return
     * @throws IllegalArgumentException 
     */
    public static double UniformCdf(int k, int n) throws IllegalArgumentException {
        if(k<0 || n<1) {
            throw new IllegalArgumentException();
        }
        k = Math.min(k, n);
        
        double probabilitySum = k*Uniform(n);
        
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
     * @throws IllegalArgumentException 
     */
    public static double Hypergeometric(int k, int n, int Kp, int Np) throws IllegalArgumentException {
        if(k<0 || n<0 || Kp<0 || Np<0) {
            throw new IllegalArgumentException();
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
     * Returns the cumulative probability of Hypergeometric
     * 
     * @param k
     * @param n
     * @param Kp
     * @param Np
     * @return
     * @throws IllegalArgumentException 
     */
    public static double HypergeometricCdf(int k, int n, int Kp, int Np) throws IllegalArgumentException {
        if(k<0 || n<0 || Kp<0 || Np<0) {
            throw new IllegalArgumentException();
        }
        Kp = Math.max(k, Kp);
        Np = Math.max(n, Np);
        
        /*
        //slow!
        $probabilitySum=0;
        for($i=0;$i<=$k;++$i) {
            $probabilitySum+=self::Hypergeometric($i,$n,$Kp,$Np);
        }
        */

        //fast and can handle large numbers
        //Cdf(k)-Cdf(k-1)
        double probabilitySum = approxHypergeometricCdf(k,n,Kp,Np);
        
        return probabilitySum;
    }
    
    protected static double approxHypergeometricCdf(double x,double n,double m,double nn) {
        //Approximate estimation as described at http://www.math.ucla.edu/~tom/distributions/Hypergeometric.html?
        double Prob=0;
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

    protected static double hyp(double x, double n, double m, double nn) {
        double nz=0;
        double mz=0;
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
     * @throws IllegalArgumentException 
     */
    public static double Poisson(int k, double lamda) throws IllegalArgumentException {
        if(k<0 || lamda<0) {
            throw new IllegalArgumentException();
        }
        
        /*
        //Slow
        $probability=pow($lamda,$k)*exp(-$lamda)/StatsUtilities::factorial($k);
        */

        //fast and can handle large numbers
        //Cdf(k)-Cdf(k-1)
        double probability = PoissonCdf(k,lamda);
        if(k>0) {
            probability -= PoissonCdf(k-1,lamda);
        }
        
        return probability;
    }
    
    /**
     * Returns the cumulative probability of Poisson
     * 
     * @param k
     * @param lamda
     * @return
     * @throws IllegalArgumentException 
     */
    public static double PoissonCdf(int k, double lamda) throws IllegalArgumentException {
        if(k<0 || lamda<0) {
            throw new IllegalArgumentException();
        }
        
        /*
        //Slow!
        $probabilitySum=0;
        for($i=0;$i<=$k;++$i) {
            $probabilitySum+=self::Poisson($i,$lamda);
        }
        */

        //Faster solution as described at: http://www.math.ucla.edu/~tom/distributions/poisson.html?
        double probabilitySum = 1.0 - ContinuousDistributions.GammaCdf(lamda,k+1);
        
        return probabilitySum;
    }
}
