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

import com.datumbox.configuration.TestConfiguration;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class DiscreteDistributionsTest {
    
    public DiscreteDistributionsTest() {
    }
    
    /**
     * Test of Bernoulli method, of class DiscreteDistributions.
     */
    @Test
    public void testBernoulli() {
        System.out.println("Bernoulli");
        boolean k = true;
        double p = 0.5;
        double expResult = 0.5;
        double result = DiscreteDistributions.Bernoulli(k, p);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of BernoulliCdf method, of class DiscreteDistributions.
     */
    @Test
    public void testBernoulliCdf() {
        System.out.println("BernoulliCdf");
        int k = 1;
        double p = 0.5;
        double expResult = 1.0;
        double result = DiscreteDistributions.BernoulliCdf(k, p);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of Binomial method, of class DiscreteDistributions.
     */
    @Test
    public void testBinomial() {
        System.out.println("Binomial");
        int k = 3;
        double p = 0.5;
        int n = 10;
        double expResult = 0.11718750001462;
        double result = DiscreteDistributions.Binomial(k, p, n);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of BinomialCdf method, of class DiscreteDistributions.
     */
    @Test
    public void testBinomialCdf() {
        System.out.println("BinomialCdf");
        int k = 3;
        double p = 0.5;
        int n = 10;
        double expResult = 0.17187500002003;
        double result = DiscreteDistributions.BinomialCdf(k, p, n);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of Geometric method, of class DiscreteDistributions.
     */
    @Test
    public void testGeometric() {
        System.out.println("Geometric");
        int k = 3;
        double p = 0.5;
        double expResult = 0.125;
        double result = DiscreteDistributions.Geometric(k, p);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of GeometricCdf method, of class DiscreteDistributions.
     */
    @Test
    public void testGeometricCdf() {
        System.out.println("GeometricCdf");
        int k = 3;
        double p = 0.5;
        double expResult = 0.875;
        double result = DiscreteDistributions.GeometricCdf(k, p);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of NegativeBinomial method, of class DiscreteDistributions.
     */
    @Test
    public void testNegativeBinomial() {
        System.out.println("NegativeBinomial");
        int n = 10;
        int r = 4;
        double p = 0.5;
        double expResult = 0.08203125;
        double result = DiscreteDistributions.NegativeBinomial(n, r, p);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of NegativeBinomialCdf method, of class DiscreteDistributions.
     */
    @Test
    public void testNegativeBinomialCdf() {
        System.out.println("NegativeBinomialCdf");
        int n = 10;
        int r = 4;
        double p = 0.5;
        double expResult = 0.12705078125;
        double result = DiscreteDistributions.NegativeBinomialCdf(n, r, p);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of Uniform method, of class DiscreteDistributions.
     */
    @Test
    public void testUniform() {
        System.out.println("Uniform");
        int n = 10;
        double expResult = 0.1;
        double result = DiscreteDistributions.Uniform(n);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of UniformCdf method, of class DiscreteDistributions.
     */
    @Test
    public void testUniformCdf() {
        System.out.println("UniformCdf");
        int k = 3;
        int n = 10;
        double expResult = 0.3;
        double result = DiscreteDistributions.UniformCdf(k, n);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of Hypergeometric method, of class DiscreteDistributions.
     */
    @Test
    public void testHypergeometric() {
        System.out.println("Hypergeometric");
        int k = 3;
        int n = 10;
        int Kp = 30;
        int Np = 100;
        double expResult = 0.28116339430254;
        double result = DiscreteDistributions.Hypergeometric(k, n, Kp, Np);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of HypergeometricCdf method, of class DiscreteDistributions.
     */
    @Test
    public void testHypergeometricCdf() {
        System.out.println("HypergeometricCdf");
        int k = 3;
        int n = 10;
        int Kp = 30;
        int Np = 100;
        double expResult = 0.65401998866081;
        double result = DiscreteDistributions.HypergeometricCdf(k, n, Kp, Np);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of Poisson method, of class DiscreteDistributions.
     */
    @Test
    public void testPoisson() {
        System.out.println("Poisson");
        int k = 3;
        double lamda = 5.0;
        double expResult = 0.14037389583692;
        double result = DiscreteDistributions.Poisson(k, lamda);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of PoissonCdf method, of class DiscreteDistributions.
     */
    @Test
    public void testPoissonCdf() {
        System.out.println("PoissonCdf");
        int k = 3;
        double lamda = 5.0;
        double expResult = 0.26502591533403;
        double result = DiscreteDistributions.PoissonCdf(k, lamda);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }
    
}
