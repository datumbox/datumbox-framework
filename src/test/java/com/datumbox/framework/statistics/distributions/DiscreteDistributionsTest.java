/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.statistics.distributions;


import com.datumbox.configuration.TestConfiguration;
import com.datumbox.tests.bases.BaseTest;
import com.datumbox.tests.utilities.TestUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class DiscreteDistributionsTest extends BaseTest {
    
    /**
     * Test of Bernoulli method, of class DiscreteDistributions.
     */
    @Test
    public void testBernoulli() {
        TestUtils.log(this.getClass(), "Bernoulli");
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
        TestUtils.log(this.getClass(), "BernoulliCdf");
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
        TestUtils.log(this.getClass(), "Binomial");
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
        TestUtils.log(this.getClass(), "BinomialCdf");
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
        TestUtils.log(this.getClass(), "Geometric");
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
        TestUtils.log(this.getClass(), "GeometricCdf");
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
        TestUtils.log(this.getClass(), "NegativeBinomial");
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
        TestUtils.log(this.getClass(), "NegativeBinomialCdf");
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
        TestUtils.log(this.getClass(), "Uniform");
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
        TestUtils.log(this.getClass(), "UniformCdf");
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
        TestUtils.log(this.getClass(), "Hypergeometric");
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
        TestUtils.log(this.getClass(), "HypergeometricCdf");
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
        TestUtils.log(this.getClass(), "Poisson");
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
        TestUtils.log(this.getClass(), "PoissonCdf");
        int k = 3;
        double lamda = 5.0;
        double expResult = 0.26502591533403;
        double result = DiscreteDistributions.PoissonCdf(k, lamda);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }
    
}
