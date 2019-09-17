/**
 * Copyright (C) 2013-2019 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.core.common.text.analyzers;

import com.datumbox.framework.tests.Constants;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for ReadabilityStatistics.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ReadabilityStatisticsTest extends AbstractTest {
    
    private static final String TEST_STRING = "if you can keep your head when all about you are losing theirs and blaming it on you, if you can trust yourself when all men doubt you but make allowance for their doubting too, if you can wait and not be tired by waiting, or being lied about, don't deal in lies, or being hated, don't give way to hating, and yet don't look too good, nor talk too wise: if you can dream - and not make dreams your master, if you can think - and not make thoughts your aim; if you can meet with triumph and disaster and treat those two impostors just the same; if you can bear to hear the truth you've spoken twisted by knaves to make a trap for fools, or watch the things you gave your life to, broken, and stoop and build 'em up with worn-out tools: if you can make one heap of all your winnings and risk it all on one turn of pitch-and-toss, and lose, and start again at your beginnings and never breath a word about your loss; if you can force your heart and nerve and sinew to serve your turn long after they are gone, and so hold on when there is nothing in you except the will which says to them: \\\"hold on\\\" if you can talk with crowds and keep your virtue, or walk with kings - nor lose the common touch, if neither foes nor loving friends can hurt you; if all men count with you, but none too much, if you can fill the unforgiving minute with sixty seconds' worth of distance run, yours is the earth and everything that's in it, and - which is more - you'll be a man, my son!";
    
    /**
     * Test of fleschKincaidReadingEase method, of class ReadabilityStatistics.
     */
    @Test
    public void testFlesch_kincaid_reading_ease() {
        logger.info("flesch_kincaid_reading_ease");
        String strText = TEST_STRING;
        double expResult = -188.4;
        double result = ReadabilityStatistics.fleschKincaidReadingEase(strText);
        Assert.assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of fleschKincaidGradeLevel method, of class ReadabilityStatistics.
     */
    @Test
    public void testFlesch_kincaid_grade_level() {
        logger.info("flesch_kincaid_grade_level");
        String strText = TEST_STRING;
        double expResult = 112.3;
        double result = ReadabilityStatistics.fleschKincaidGradeLevel(strText);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of gunningFogScore method, of class ReadabilityStatistics.
     */
    @Test
    public void testGunning_fog_score() {
        logger.info("gunning_fog_score");
        String strText = TEST_STRING;
        double expResult = 118.0;
        double result = ReadabilityStatistics.gunningFogScore(strText);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of colemanLiauIndex method, of class ReadabilityStatistics.
     */
    @Test
    public void testColeman_liau_index() {
        logger.info("coleman_liau_index");
        String strText = TEST_STRING;
        double expResult = 6.8;
        double result = ReadabilityStatistics.colemanLiauIndex(strText);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of smogIndex method, of class ReadabilityStatistics.
     */
    @Test
    public void testSmog_index() {
        logger.info("smog_index");
        String strText = TEST_STRING;
        double expResult = 14.1;
        double result = ReadabilityStatistics.smogIndex(strText);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of automatedReadabilityIndex method, of class ReadabilityStatistics.
     */
    @Test
    public void testAutomated_readability_index() {
        logger.info("automated_readability_index");
        String strText = TEST_STRING;
        double expResult = 143.2;
        double result = ReadabilityStatistics.automatedReadabilityIndex(strText);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of daleChallScore method, of class ReadabilityStatistics.
     */
    @Test
    public void testDale_chall_score() {
        logger.info("dale_chall_score");
        String strText = TEST_STRING;
        double expResult = 20.486603754266213;
        double result = ReadabilityStatistics.daleChallScore(strText);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of daleChallGrade method, of class ReadabilityStatistics.
     */
    @Test
    public void testDale_chall_grade() {
        logger.info("dale_chall_grade");
        String strText = TEST_STRING;
        double expResult = 16.0;
        double result = ReadabilityStatistics.daleChallGrade(strText);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of spacheScore method, of class ReadabilityStatistics.
     */
    @Test
    public void testSpache_score() {
        logger.info("spache_score");
        String strText = TEST_STRING;
        double expResult = 41.523999999999994;
        double result = ReadabilityStatistics.spacheScore(strText);
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
    }
    
}
