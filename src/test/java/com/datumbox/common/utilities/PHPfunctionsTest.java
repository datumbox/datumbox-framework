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
package com.datumbox.common.utilities;

import com.datumbox.configuration.TestConfiguration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class PHPfunctionsTest {
    
    public PHPfunctionsTest() {
    }
    
    /**
     * Test of asort method, of class PHPfunctions.
     */
    @Test
    public void testAsort() {
        System.out.println("asort");
        Double[] array = {1.1, 1.2, 1.3, 1.4, 1.0};
        Integer[] expResult = {4, 0, 1, 2, 3};
        Integer[] result = PHPfunctions.<Double>asort(array);
        assertArrayEquals(expResult, result);
    } 

    /**
     * Test of arsort method, of class PHPfunctions.
     */
    @Test
    public void testArsort() {
        System.out.println("arsort");
        Double[] array = {1.1, 1.2, 1.3, 1.4, 1.0};
        Integer[] expResult = {3, 2, 1, 0, 4};
        Integer[] result = PHPfunctions.<Double>arsort(array);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of ltrim method, of class PHPfunctions.
     */
    @Test
    public void testLtrim() {
        System.out.println("ltrim");
        String s = "  test";
        String expResult = "test";
        String result = PHPfunctions.ltrim(s);
        assertEquals(expResult, result);
    }

    /**
     * Test of rtrim method, of class PHPfunctions.
     */
    @Test
    public void testRtrim() {
        System.out.println("rtrim");
        String s = "test   ";
        String expResult = "test";
        String result = PHPfunctions.rtrim(s);
        assertEquals(expResult, result);
    }


    /**
     * Test of substr_count method, of class PHPfunctions.
     */
    @Test
    public void testSubstr_count_String_String() {
        System.out.println("substr_count");
        String string = "test tester";
        String substring = "test";
        int expResult = 2;
        int result = PHPfunctions.substr_count(string, substring);
        assertEquals(expResult, result);
    }

    /**
     * Test of substr_count method, of class PHPfunctions.
     */
    @Test
    public void testSubstr_count_String_char() {
        System.out.println("substr_count");
        String string = "test test";
        char character = 't';
        int expResult = 4;
        int result = PHPfunctions.substr_count(string, character);
        assertEquals(expResult, result);
    }

    /**
     * Test of round method, of class PHPfunctions.
     */
    @Test
    public void testRound() {
        System.out.println("round");
        double d = 3.1415;
        int i = 2;
        double expResult = 3.14;
        double result = PHPfunctions.round(d, i);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of round method, of class PHPfunctions.
     */
    @Test
    public void testLog() {
        System.out.println("log");
        double d = 100.0;
        double base = 10.0;
        double expResult = 2.0;
        double result = PHPfunctions.log(d, base);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }

    /**
     * Test of mt_rand method, of class PHPfunctions.
     */
    @Test
    public void testMt_rand_0args() {
        System.out.println("mt_rand");
        RandomValue.randomGenerator = new Random(42);
        int expResult = -1562431105;
        int result = PHPfunctions.mt_rand();
        assertEquals(expResult, result);
    }

    /**
     * Test of mt_rand method, of class PHPfunctions.
     */
    @Test
    public void testMt_rand_int_int() {
        System.out.println("mt_rand");
        RandomValue.randomGenerator = new Random(42);
        int min = 0;
        int max = 10;
        int expResult = 8;
        int result = PHPfunctions.mt_rand(min, max);
        assertEquals(expResult, result);
    }

    /**
     * Test of shuffle method, of class PHPfunctions.
     */
    @Test
    public void testArray_flip() {
        System.out.println("array_flip");
        Map<String, Integer> original = new HashMap<>();
        original.put("1", 10);
        original.put("2", 11);
        original.put("3", 12);
        
        Map<Integer, String> expResult = new HashMap<>();
        expResult.put(10, "1");
        expResult.put(11, "2");
        expResult.put(12, "3");
        
        
        Map<Integer, String> result = PHPfunctions.array_flip(original);
        assertEquals(expResult, result);
    }

    /**
     * Test of shuffle method, of class PHPfunctions.
     */
    @Test
    public void testShuffle() {
        System.out.println("shuffle");
        RandomValue.randomGenerator = new Random(42);
        Integer[] result = {1,2,3,4,5};
        Integer[] expResult = {2,3,4,5,1};
        PHPfunctions.shuffle(result);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of preg_replace method, of class PHPfunctions.
     */
    @Test
    public void testPreg_replace_3args_1() {
        System.out.println("preg_replace");
        String regex = "\\s+";
        String replacement = " ";
        String subject = "a   a";
        String expResult = "a a";
        String result = PHPfunctions.preg_replace(regex, replacement, subject);
        assertEquals(expResult, result);
    }

    /**
     * Test of preg_replace method, of class PHPfunctions.
     */
    @Test
    public void testPreg_replace_3args_2() {
        System.out.println("preg_replace");
        Pattern pattern = Pattern.compile("\\s+");
        String replacement = " ";
        String subject = "a   a";
        String expResult = "a a";
        String result = PHPfunctions.preg_replace(pattern, replacement, subject);
        assertEquals(expResult, result);
    }

    /**
     * Test of preg_match method, of class PHPfunctions.
     */
    @Test
    public void testPreg_match_String_String() {
        System.out.println("preg_match");
        String regex = "\\s{2,}";
        String subject = "a   a  a";
        int expResult = 2;
        int result = PHPfunctions.preg_match(regex, subject);
        assertEquals(expResult, result);
    }

    /**
     * Test of preg_match method, of class PHPfunctions.
     */
    @Test
    public void testPreg_match_Pattern_String() {
        System.out.println("preg_match");
        Pattern pattern = Pattern.compile("\\s{2,}");
        String subject = "a   a  a";
        int expResult = 2;
        int result = PHPfunctions.preg_match(pattern, subject);
        assertEquals(expResult, result);
    }
    
}
