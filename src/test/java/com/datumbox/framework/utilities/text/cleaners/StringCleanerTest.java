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
package com.datumbox.framework.utilities.text.cleaners;

import com.datumbox.tests.bases.BaseTest;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class StringCleanerTest extends BaseTest {

    /**
     * Test of tokenizeURLs method, of class StringCleaner.
     */
    @Test
    public void testTokenizeURLs() {
        logger.info("tokenizeURLs");
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
        logger.info("tokenizeSmileys");
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
        logger.info("removeExtraSpaces");
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
        logger.info("removeSymbols");
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
        logger.info("unifyTerminators");
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
        logger.info("removeAccents");
        String text = "'ά','ό','έ','ί','ϊ','ΐ','ή','ύ','ϋ','ΰ','ώ','à','á','â','ã','ä','ç','è','é','ê','ë','ì','í','î','ï','ñ','ò','ó','ô','õ','ö','ù','ú','û','ü','ý','ÿ','Ά','Ό','Έ','Ί','Ϊ','Ή','Ύ','Ϋ','Ώ','À','Á','Â','Ã','Ä','Ç','È','É','Ê','Ë','Ì','Í','Î','Ï','Ñ','Ò','Ó','Ô','Õ','Ö','Ù','Ú','Û','Ü','Ý'";
        String expResult = "'α','ο','ε','ι','ι','ι','η','υ','υ','υ','ω','a','a','a','a','a','c','e','e','e','e','i','i','i','i','n','o','o','o','o','o','u','u','u','u','y','y','Α','Ο','Ε','Ι','Ι','Η','Υ','Υ','Ω','A','A','A','A','A','C','E','E','E','E','I','I','I','I','N','O','O','O','O','O','U','U','U','U','Y'";
        String result = StringCleaner.removeAccents(text);
        assertEquals(expResult, result);
    }
    
}
