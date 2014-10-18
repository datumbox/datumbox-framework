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
package com.datumbox.framework.utilities.text.cleaners;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class StringCleanerTest {
    
    public StringCleanerTest() {
    }

    /**
     * Test of tokenizeURLs method, of class StringCleaner.
     */
    @Test
    public void testTokenizeURLs() {
        System.out.println("tokenizeURLs");
        String text = "Test, test δοκιμή http://wWw.Google.com/page?query=1#hash test test";
        String expResult = "Test, test δοκιμή  PREPROCESSDOC_URL  test test";
        String result = StringCleaner.tokenizeURLs(text);
        assertEquals(expResult, result);
    }

    /**
     * Test of tokenizeSmileys method, of class StringCleaner.
     */
    @Test
    public void testTokenizeSmileys() {
        System.out.println("tokenizeSmileys");
        String text = "Test, test δοκιμή :) :( :] :[ test test";
        String expResult = "Test, test δοκιμή  PREPROCESSDOC_EM1   PREPROCESSDOC_EM3   PREPROCESSDOC_EM8   PREPROCESSDOC_EM9  test test";
        String result = StringCleaner.tokenizeSmileys(text);
        assertEquals(expResult, result);
    }

    /**
     * Test of removeExtraSpaces method, of class StringCleaner.
     */
    @Test
    public void testRemoveExtraSpaces() {
        System.out.println("removeExtraSpaces");
        String text = " test    test  test      test        test\n\n\n\r\n\r\r test\n";
        String expResult = "test test test test test test";
        String result = StringCleaner.removeExtraSpaces(text);
        assertEquals(expResult, result);
    }

    /**
     * Test of removeSymbols method, of class StringCleaner.
     */
    @Test
    public void testRemoveSymbols() {
        System.out.println("removeSymbols");
        String text = "test ` ~ ! @ # $ % ^ & * ( ) _ - + = < , > . ? / \" ' : ; [ { } ] | \\ test `~!@#$%^&*()_-+=<,>.?/\\\"':;[{}]|\\\\ test";
        String expResult = "test _ test _ test";
        String result = StringCleaner.removeExtraSpaces(StringCleaner.removeSymbols(text));
        assertEquals(expResult, result);
    }

    /**
     * Test of unifyTerminators method, of class StringCleaner.
     */
    @Test
    public void testUnifyTerminators() {
        System.out.println("unifyTerminators");
        String text = " This is amazing!!! !    How is this possible?!?!?!?!!!???! ";
        String expResult = "This is amazing. How is this possible.";
        String result = StringCleaner.unifyTerminators(text);
        assertEquals(expResult, result);
    }

    /**
     * Test of removeAccents method, of class StringCleaner.
     */
    @Test
    public void testRemoveAccents() {
        System.out.println("removeAccents");
        String text = "'ά','ό','έ','ί','ϊ','ΐ','ή','ύ','ϋ','ΰ','ώ','à','á','â','ã','ä','ç','è','é','ê','ë','ì','í','î','ï','ñ','ò','ó','ô','õ','ö','ù','ú','û','ü','ý','ÿ','Ά','Ό','Έ','Ί','Ϊ','Ή','Ύ','Ϋ','Ώ','À','Á','Â','Ã','Ä','Ç','È','É','Ê','Ë','Ì','Í','Î','Ï','Ñ','Ò','Ó','Ô','Õ','Ö','Ù','Ú','Û','Ü','Ý'";
        String expResult = "'α','ο','ε','ι','ι','ι','η','υ','υ','υ','ω','a','a','a','a','a','c','e','e','e','e','i','i','i','i','n','o','o','o','o','o','u','u','u','u','y','y','Α','Ο','Ε','Ι','Ι','Η','Υ','Υ','Ω','A','A','A','A','A','C','E','E','E','E','I','I','I','I','N','O','O','O','O','O','U','U','U','U','Y'";
        String result = StringCleaner.removeAccents(text);
        assertEquals(expResult, result);
    }
    
}
