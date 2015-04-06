/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
package com.datumbox.common.utilities;

import com.datumbox.configuration.TestConfiguration;
import com.datumbox.tests.utilities.TestUtils;
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
        TestUtils.log(this.getClass(), "asort");
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
        TestUtils.log(this.getClass(), "arsort");
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
        TestUtils.log(this.getClass(), "ltrim");
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
        TestUtils.log(this.getClass(), "rtrim");
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
        TestUtils.log(this.getClass(), "substr_count");
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
        TestUtils.log(this.getClass(), "substr_count");
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
        TestUtils.log(this.getClass(), "round");
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
        TestUtils.log(this.getClass(), "log");
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
        TestUtils.log(this.getClass(), "mt_rand");
        RandomValue.setRandomGenerator(new Random(TestConfiguration.RANDOM_SEED));
        int expResult = -1562431105;
        int result = PHPfunctions.mt_rand();
        assertEquals(expResult, result);
    }

    /**
     * Test of mt_rand method, of class PHPfunctions.
     */
    @Test
    public void testMt_rand_int_int() {
        TestUtils.log(this.getClass(), "mt_rand");
        RandomValue.setRandomGenerator(new Random(TestConfiguration.RANDOM_SEED));
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
        TestUtils.log(this.getClass(), "array_flip");
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
        TestUtils.log(this.getClass(), "shuffle");
        RandomValue.setRandomGenerator(new Random(TestConfiguration.RANDOM_SEED));
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
        TestUtils.log(this.getClass(), "preg_replace");
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
        TestUtils.log(this.getClass(), "preg_replace");
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
        TestUtils.log(this.getClass(), "preg_match");
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
        TestUtils.log(this.getClass(), "preg_match");
        Pattern pattern = Pattern.compile("\\s{2,}");
        String subject = "a   a  a";
        int expResult = 2;
        int result = PHPfunctions.preg_match(pattern, subject);
        assertEquals(expResult, result);
    }
    
}
