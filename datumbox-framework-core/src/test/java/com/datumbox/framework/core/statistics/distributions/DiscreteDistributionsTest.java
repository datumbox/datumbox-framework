/**
 * Copyright (C) 2013-2016 Vasilis Vryniotis <bbriniotis@datumbox.com>
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


import com.datumbox.framework.tests.Constants;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for DiscreteDistributions.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class DiscreteDistributionsTest extends AbstractTest {
    
    /**
     * Test of bernoulli method, of class DiscreteDistributions.
     */
    @Test
    public void testBernoulli() {
        logger.info("Bernoulli");
        boolean k = true;
        double p = 0.5;
        double expResult = 0.5;
        double result = DiscreteDistributions.bernoulli(k, p);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of bernoulliCdf method, of class DiscreteDistributions.
     */
    @Test
    public void testBernoulliCdf() {
        logger.info("BernoulliCdf");
        int k = 1;
        double p = 0.5;
        double expResult = 1.0;
        double result = DiscreteDistributions.bernoulliCdf(k, p);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of binomial method, of class DiscreteDistributions.
     */
    @Test
    public void testBinomial() {
        logger.info("Binomial");
        int k = 3;
        double p = 0.5;
        int n = 10;
        double expResult = 0.11718750001462;
        double result = DiscreteDistributions.binomial(k, p, n);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of binomialCdf method, of class DiscreteDistributions.
     */
    @Test
    public void testBinomialCdf() {
        logger.info("BinomialCdf");
        int k = 3;
        double p = 0.5;
        int n = 10;
        double expResult = 0.17187500002003;
        double result = DiscreteDistributions.binomialCdf(k, p, n);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of geometric method, of class DiscreteDistributions.
     */
    @Test
    public void testGeometric() {
        logger.info("Geometric");
        int k = 3;
        double p = 0.5;
        double expResult = 0.125;
        double result = DiscreteDistributions.geometric(k, p);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of geometricCdf method, of class DiscreteDistributions.
     */
    @Test
    public void testGeometricCdf() {
        logger.info("GeometricCdf");
        int k = 3;
        double p = 0.5;
        double expResult = 0.875;
        double result = DiscreteDistributions.geometricCdf(k, p);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of negativeBinomial method, of class DiscreteDistributions.
     */
    @Test
    public void testNegativeBinomial() {
        logger.info("NegativeBinomial");
        int n = 10;
        int r = 4;
        double p = 0.5;
        double expResult = 0.08203125;
        double result = DiscreteDistributions.negativeBinomial(n, r, p);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of negativeBinomialCdf method, of class DiscreteDistributions.
     */
    @Test
    public void testNegativeBinomialCdf() {
        logger.info("NegativeBinomialCdf");
        int n = 10;
        int r = 4;
        double p = 0.5;
        double expResult = 0.12705078125;
        double result = DiscreteDistributions.negativeBinomialCdf(n, r, p);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of uniform method, of class DiscreteDistributions.
     */
    @Test
    public void testUniform() {
        logger.info("Uniform");
        int n = 10;
        double expResult = 0.1;
        double result = DiscreteDistributions.uniform(n);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of uniformCdf method, of class DiscreteDistributions.
     */
    @Test
    public void testUniformCdf() {
        logger.info("UniformCdf");
        int k = 3;
        int n = 10;
        double expResult = 0.3;
        double result = DiscreteDistributions.uniformCdf(k, n);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of hypergeometric method, of class DiscreteDistributions.
     */
    @Test
    public void testHypergeometric() {
        logger.info("Hypergeometric");
        int k = 3;
        int n = 10;
        int Kp = 30;
        int Np = 100;
        double expResult = 0.28116339430254;
        double result = DiscreteDistributions.hypergeometric(k, n, Kp, Np);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of hypergeometricCdf method, of class DiscreteDistributions.
     */
    @Test
    public void testHypergeometricCdf() {
        logger.info("HypergeometricCdf");
        int k = 3;
        int n = 10;
        int Kp = 30;
        int Np = 100;
        double expResult = 0.65401998866081;
        double result = DiscreteDistributions.hypergeometricCdf(k, n, Kp, Np);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of poisson method, of class DiscreteDistributions.
     */
    @Test
    public void testPoisson() {
        logger.info("Poisson");
        int k = 3;
        double lamda = 5.0;
        double expResult = 0.14037389583692;
        double result = DiscreteDistributions.poisson(k, lamda);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of poissonCdf method, of class DiscreteDistributions.
     */
    @Test
    public void testPoissonCdf() {
        logger.info("PoissonCdf");
        int k = 3;
        double lamda = 5.0;
        double expResult = 0.26502591533403;
        double result = DiscreteDistributions.poissonCdf(k, lamda);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }
    
}
