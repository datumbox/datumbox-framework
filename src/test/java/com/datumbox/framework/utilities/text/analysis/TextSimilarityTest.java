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
package com.datumbox.framework.utilities.text.analysis;

import com.datumbox.configuration.TestConfiguration;
import com.datumbox.tests.bases.BaseTest;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class TextSimilarityTest extends BaseTest {

    /**
     * Test of oliverSimilarity method, of class TextSimilarity.
     */
    @Test
    public void testOliverSimilarity() {
        logger.info("oliverSimilarity");
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
        logger.info("shinglerSimilarity");
        String text1 = "This book has been written against a background of both reckless optimism and reckless despair. It holds that Progress and Doom are two sides of the same medal; that both are articles of superstition, not of faith. It was written out of the conviction that it should be possible to discover the hidden mechanics by which all traditional elements of our political and spiritual world were dissolved into a conglomeration where everything seems to have lost specific value, and has become unrecognizable for human comprehension, unusable for human purpose. Hannah Arendt, The Origins of Totalitarianism (New York: Harcourt Brace Jovanovich, Inc., 1973 ed.), p.vii, Preface to the First Edition.";
        String text2 = "The first edition of The Origins of Totalitarianism was written in 1950. Soon after the Second World War, this was a time of both reckless optimism and reckless despair. During this time, Dr. Arendt argues, the traditional elements of the political and spiritual world were dissolved into a conglomeration where everything seems to have lost specific value. In particular, the separation between the State and Society seems to have been destroyed. In this book, she seeks to disclose the hidden mechanics by which this transformation occurred.";
        int w = 3;
        double expResult = 0.15151515151515152;
        double result = TextSimilarity.shinglerSimilarity(text1, text2, w);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }
    
}
