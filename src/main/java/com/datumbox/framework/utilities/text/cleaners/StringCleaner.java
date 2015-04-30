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

import com.datumbox.common.utilities.PHPfunctions;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This is a utility class which contains several methods which are commonly 
 * applied to clean-up a string before text classification.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class StringCleaner {
    private static final Pattern URL_PATTERN = Pattern.compile("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    
    /**
     * All URLs are replaced within the text by using the TOKENIZED_URL constant.
     */
    public static final String TOKENIZED_URL = " PREPROCESSDOC_URL ";
    
    /**
     * Mapping between smileys and their tokenized representation.
     */
    public static final Map<String, String> smileys = new HashMap<>();
    
    static {
        smileys.put(":\\)", " PREPROCESSDOC_EM1 ");
        smileys.put(":-\\)", " PREPROCESSDOC_EM2 ");
        smileys.put(":\\(", " PREPROCESSDOC_EM3 ");
        smileys.put(":-\\(", " PREPROCESSDOC_EM4 ");
        smileys.put(":d", " PREPROCESSDOC_EM5 ");
        smileys.put(";\\)", " PREPROCESSDOC_EM6 ");
        smileys.put(":o\\)", " PREPROCESSDOC_EM7 ");
        smileys.put(":\\]", " PREPROCESSDOC_EM8 ");
        smileys.put(":\\[", " PREPROCESSDOC_EM9 ");
        smileys.put(":p", " PREPROCESSDO,C_EM10 ");
        smileys.put(":-p", " PREPROCESSDOC_EM11 ");
        smileys.put("8-\\)", " PREPROCESSDOC_EM12 ");
        smileys.put("=\\)", " PREPROCESSDOC_EM13 ");
        smileys.put("=\\(", " PREPROCESSDOC_EM14 ");
    }
    
    /**
     * Replaces all the URLs within the text with a token.
     * 
     * @param text
     * @return 
     */
    public static String tokenizeURLs(String text) {
        return PHPfunctions.preg_replace(URL_PATTERN, TOKENIZED_URL, text);
    }
    
    /**
     * Replaces all the smileys within the text with their tokens.
     * 
     * @param text
     * @return 
     */
    public static String tokenizeSmileys(String text) {
        for(Map.Entry<String, String> smiley : smileys.entrySet()) {
            text = text.replaceAll(smiley.getKey(), smiley.getValue());
        }
        return text;
    }
    
    /**
     * Replaces all extra spaces with one space.
     * 
     * @param text
     * @return 
     */
    public static String removeExtraSpaces(String text) {
        text = text.trim().replaceAll("\\s+", " ");
        return text;
    }
    
    /**
     * Removes all the non-alphanumeric symbols from the text.
     * 
     * @param text
     * @return 
     */
    public static String removeSymbols(String text) {
        //text = text.replaceAll("[^\\w\\s]","");
        text = text.replaceAll("[^\\p{L}\\p{Z}_]","");
        return text;
    }
    
    /**
     * Replaces all terminators with space or dots. The final string will contain only
     * alphanumerics and dots.
     * 
     * @param text
     * @return 
     */
    public static String unifyTerminators(String text) {
        text = text.replaceAll("[\",:;()\\-]+", " "); // Replace commas, hyphens, quotes etc (count them as spaces)
        text = text.replaceAll("[\\.!?]", "."); // Unify terminators
        text = text.replaceAll("\\.[\\. ]+", "."); // Check for duplicated terminators
        text = text.replaceAll("\\s*\\.\\s*", ". "); // Pad sentence terminators
        return text.trim();
    }
    
    /**
     * Removes all accepts from the text.
     * 
     * @param text
     * @return 
     */
    public static String removeAccents(String text) {
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return text;
    }
    
    /**
     * Convenience method which tokenizes the URLs and the smileys, removes accents 
     * and symbols and eliminates the extra spaces from the provided text.
     * 
     * @param text
     * @return 
     */
    public static String clear(String text) {
        text = StringCleaner.tokenizeURLs(text);
        text = StringCleaner.tokenizeSmileys(text);
        text = StringCleaner.removeAccents(text);
        text = StringCleaner.removeSymbols(text);
        text = StringCleaner.removeExtraSpaces(text);
        
        return text.toLowerCase();
    }
}
