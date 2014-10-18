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
public class ContinuousDistributionsTest {
    
    public ContinuousDistributionsTest() {
    }
    
    /**
     * Test of ChisquareCdf method, of class ContinuousDistributions.
     */
    @Test
    public void testChisquareCdf() {
        System.out.println("ChisquareCdf");
        double x = 3.0;
        int df = 10;
        double expResult = 0.018575928421771;
        double result = ContinuousDistributions.ChisquareCdf(x, df);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of GaussCdf method, of class ContinuousDistributions.
     */
    @Test
    public void testGaussCdf() {
        System.out.println("GaussCdf");
        double z = 3.0;
        double expResult = 0.9986501025724;
        double result = ContinuousDistributions.GaussCdf(z);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of LogGamma method, of class ContinuousDistributions.
     */
    @Test
    public void testLogGamma() {
        System.out.println("LogGamma");
        double Z = 3.0;
        double expResult = 0.69314718044741;
        double result = ContinuousDistributions.LogGamma(Z);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of StudentsCdf method, of class ContinuousDistributions.
     */
    @Test
    public void testStudentsCdf() {
        System.out.println("StudentsCdf");
        double x = 3.0;
        int df = 10;
        double expResult = 0.99332817273466;
        double result = ContinuousDistributions.StudentsCdf(x, df);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of ExponentialCdf method, of class ContinuousDistributions.
     */
    @Test
    public void testExponentialCdf() {
        System.out.println("ExponentialCdf");
        double x = 3.0;
        double lamda = 10.0;
        double expResult = 0.99999999999991;
        double result = ContinuousDistributions.ExponentialCdf(x, lamda);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of BetaCdf method, of class ContinuousDistributions.
     */
    @Test
    public void testBetaCdf() {
        System.out.println("BetaCdf");
        double x = 0.9;
        double a = 10.0;
        double b = 2.0;
        double expResult = 0.69735688048532;
        double result = ContinuousDistributions.BetaCdf(x, a, b);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of FCdf method, of class ContinuousDistributions.
     */
    @Test
    public void testFCdf() {
        System.out.println("FCdf");
        double x = 3.0;
        int f1 = 10;
        int f2 = 2;
        double expResult = 0.7241964339413;
        double result = ContinuousDistributions.FCdf(x, f1, f2);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of GammaCdf method, of class ContinuousDistributions.
     */
    @Test
    public void testGammaCdf() {
        System.out.println("GammaCdf");
        double x = 9.0;
        double a = 10.0;
        double b = 2.0;
        double expResult = 0.017092718679951;
        double result = ContinuousDistributions.GammaCdf(x, a, b);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of UniformCdf method, of class ContinuousDistributions.
     */
    @Test
    public void testUniformCdf() {
        System.out.println("UniformCdf");
        double x = 3.0;
        double a = 2.0;
        double b = 10.0;
        double expResult = 0.125;
        double result = ContinuousDistributions.UniformCdf(x, a, b);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of Kolmogorov method, of class ContinuousDistributions.
     */
    @Test
    public void testKolmogorov() {
        System.out.println("Kolmogorov");
        double z = 2.0;
        double expResult = 0.99932907474422;
        double result = ContinuousDistributions.Kolmogorov(z);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of GaussInverseCdf method, of class ContinuousDistributions.
     */
    @Test
    public void testGaussInverseCdf() {
        System.out.println("GaussInverseCdf");
        double p = 0.32;
        double expResult = -0.46769879942903;
        double result = ContinuousDistributions.GaussInverseCdf(p);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of ChisquareInverseCdf method, of class ContinuousDistributions.
     */
    @Test
    public void testChisquareInverseCdf() {
        System.out.println("ChisquareInverseCdf");
        double p = 0.32;
        int df = 10;
        double expResult = 11.498788492223;
        double result = ContinuousDistributions.ChisquareInverseCdf(p, df);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of normalQuantile method, of class ContinuousDistributions.
     */
    @Test
    public void testNormalQuantile() {
        System.out.println("normalQuantile");
        double p = 0.32;
        double mu = 1.0;
        double sigma = 1.0;
        double expResult = 0.53230120088549;
        double result = ContinuousDistributions.normalQuantile(p, mu, sigma);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }
    
}
