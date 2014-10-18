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

import com.datumbox.framework.utilities.text.analysis.ReadabilityStatistics;
import com.datumbox.configuration.TestConfiguration;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class ReadabilityStatisticsTest {
    
    protected static String TEST_STRING = "if you can keep your head when all about you are losing theirs and blaming it on you, if you can trust yourself when all men doubt you but make allowance for their doubting too, if you can wait and not be tired by waiting, or being lied about, don't deal in lies, or being hated, don't give way to hating, and yet don't look too good, nor talk too wise: if you can dream - and not make dreams your master, if you can think - and not make thoughts your aim; if you can meet with triumph and disaster and treat those two impostors just the same; if you can bear to hear the truth you've spoken twisted by knaves to make a trap for fools, or watch the things you gave your life to, broken, and stoop and build 'em up with worn-out tools: if you can make one heap of all your winnings and risk it all on one turn of pitch-and-toss, and lose, and start again at your beginnings and never breath a word about your loss; if you can force your heart and nerve and sinew to serve your turn long after they are gone, and so hold on when there is nothing in you except the will which says to them: \\\"hold on\\\" if you can talk with crowds and keep your virtue, or walk with kings - nor lose the common touch, if neither foes nor loving friends can hurt you; if all men count with you, but none too much, if you can fill the unforgiving minute with sixty seconds' worth of distance run, yours is the earth and everything that's in it, and - which is more - you'll be a man, my son!";
    
    public ReadabilityStatisticsTest() {
    }

    /**
     * Test of flesch_kincaid_reading_ease method, of class ReadabilityStatistics.
     */
    @Test
    public void testFlesch_kincaid_reading_ease() {
        System.out.println("flesch_kincaid_reading_ease");
        String strText = TEST_STRING;
        double expResult = -188.4;
        double result = ReadabilityStatistics.flesch_kincaid_reading_ease(strText);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of flesch_kincaid_grade_level method, of class ReadabilityStatistics.
     */
    @Test
    public void testFlesch_kincaid_grade_level() {
        System.out.println("flesch_kincaid_grade_level");
        String strText = TEST_STRING;
        double expResult = 112.3;
        double result = ReadabilityStatistics.flesch_kincaid_grade_level(strText);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of gunning_fog_score method, of class ReadabilityStatistics.
     */
    @Test
    public void testGunning_fog_score() {
        System.out.println("gunning_fog_score");
        String strText = TEST_STRING;
        double expResult = 118.0;
        double result = ReadabilityStatistics.gunning_fog_score(strText);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of coleman_liau_index method, of class ReadabilityStatistics.
     */
    @Test
    public void testColeman_liau_index() {
        System.out.println("coleman_liau_index");
        String strText = TEST_STRING;
        double expResult = 6.8;
        double result = ReadabilityStatistics.coleman_liau_index(strText);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of smog_index method, of class ReadabilityStatistics.
     */
    @Test
    public void testSmog_index() {
        System.out.println("smog_index");
        String strText = TEST_STRING;
        double expResult = 14.1;
        double result = ReadabilityStatistics.smog_index(strText);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of automated_readability_index method, of class ReadabilityStatistics.
     */
    @Test
    public void testAutomated_readability_index() {
        System.out.println("automated_readability_index");
        String strText = TEST_STRING;
        double expResult = 143.2;
        double result = ReadabilityStatistics.automated_readability_index(strText);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of dale_chall_score method, of class ReadabilityStatistics.
     */
    @Test
    public void testDale_chall_score() {
        System.out.println("dale_chall_score");
        String strText = TEST_STRING;
        double expResult = 20.486603754266213;
        double result = ReadabilityStatistics.dale_chall_score(strText);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of dale_chall_grade method, of class ReadabilityStatistics.
     */
    @Test
    public void testDale_chall_grade() {
        System.out.println("dale_chall_grade");
        String strText = TEST_STRING;
        double expResult = 16.0;
        double result = ReadabilityStatistics.dale_chall_grade(strText);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of spache_score method, of class ReadabilityStatistics.
     */
    @Test
    public void testSpache_score() {
        System.out.println("spache_score");
        String strText = TEST_STRING;
        double expResult = 41.523999999999994;
        double result = ReadabilityStatistics.spache_score(strText);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }
    
}
