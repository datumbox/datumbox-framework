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
package com.datumbox.framework.utilities.text.analysis;

import com.datumbox.framework.utilities.text.analysis.TextSimilarity;
import com.datumbox.configuration.TestConfiguration;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class TextSimilarityTest {
    
    public TextSimilarityTest() {
    }

    /**
     * Test of oliverSimilarity method, of class TextSimilarity.
     */
    @Test
    public void testOliverSimilarity() {
        System.out.println("oliverSimilarity");
        String text1 = "This book has been written against a background of both reckless optimism and reckless despair. It holds that Progress and Doom are two sides of the same medal; that both are articles of superstition, not of faith. It was written out of the conviction that it should be possible to discover the hidden mechanics by which all traditional elements of our political and spiritual world were dissolved into a conglomeration where everything seems to have lost specific value, and has become unrecognizable for human comprehension, unusable for human purpose. Hannah Arendt, The Origins of Totalitarianism (New York: Harcourt Brace Jovanovich, Inc., 1973 ed.), p.vii, Preface to the First Edition.";
        String text2 = "The first edition of The Origins of Totalitarianism was written in 1950. Soon after the Second World War, this was a time of both reckless optimism and reckless despair. During this time, Dr. Arendt argues, the traditional elements of the political and spiritual world were dissolved into a conglomeration where everything seems to have lost specific value. In particular, the separation between the State and Society seems to have been destroyed. In this book, she seeks to disclose the hidden mechanics by which this transformation occurred.";
        double expResult = 0.4064777327935223;
        double result = TextSimilarity.oliverSimilarity(text1, text2);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of shinglerSimilarity method, of class TextSimilarity.
     */
    @Test
    public void testShinglerSimilarity() {
        System.out.println("shinglerSimilarity");
        String text1 = "This book has been written against a background of both reckless optimism and reckless despair. It holds that Progress and Doom are two sides of the same medal; that both are articles of superstition, not of faith. It was written out of the conviction that it should be possible to discover the hidden mechanics by which all traditional elements of our political and spiritual world were dissolved into a conglomeration where everything seems to have lost specific value, and has become unrecognizable for human comprehension, unusable for human purpose. Hannah Arendt, The Origins of Totalitarianism (New York: Harcourt Brace Jovanovich, Inc., 1973 ed.), p.vii, Preface to the First Edition.";
        String text2 = "The first edition of The Origins of Totalitarianism was written in 1950. Soon after the Second World War, this was a time of both reckless optimism and reckless despair. During this time, Dr. Arendt argues, the traditional elements of the political and spiritual world were dissolved into a conglomeration where everything seems to have lost specific value. In particular, the separation between the State and Society seems to have been destroyed. In this book, she seeks to disclose the hidden mechanics by which this transformation occurred.";
        int w = 3;
        double expResult = 0.15151515151515152;
        double result = TextSimilarity.shinglerSimilarity(text1, text2, w);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }
    
}
