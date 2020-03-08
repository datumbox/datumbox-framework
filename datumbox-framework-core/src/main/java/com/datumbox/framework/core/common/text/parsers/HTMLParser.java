/**
 * Copyright (C) 2013-2020 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.core.common.text.parsers;

import com.datumbox.framework.core.common.text.StringCleaner;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The HTMLParser class is a utility class that provides a list of helpful methods
 which can be used to sanitize, clean up and manipulate HTML documents.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class HTMLParser {
    private static final Pattern IMG_ALT_TITLE_PATTERN = Pattern.compile("<[\\s]*img[^>]*[alt|title]=[\\s]*[\\\"']?([^>\\\"']+)[\\\"']?[^>]*>", Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
    private static final Pattern NON_TEXT_TAGS_PATTERN = Pattern.compile("<[\\s]*(head|style|script|object|embed|applet|noframes|noscript|noembed|option)[^>]*?>.*?</\\1>", Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
    private static final Pattern REMOVE_ATTRIBUTES_PATTERN = Pattern.compile("<([a-z!][a-z0-9]*)[^>]*?(/?)>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);    
    private static final Pattern TITLE_PATTERN = Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);    
    private static final Pattern HYPERLINK_PATTERN = Pattern.compile("<[\\s]*a[^>]*href[\\s]*=[\\s]*[\\\"']([^\\\"']*)[\\\"'][^>]*>(.*?)</a>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);    
    private static final Pattern METATAG_PATTERN = Pattern.compile("<[\\s]*meta[^>]*name[\\s]*=[\\s]*[\\\"']([^\\\"']*)[\\\"'][^>]*content[\\s]*=[\\s]*[\\\"']([^\\\"']*)[\\\"'][^>]*>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);    
    private static final Pattern HX_PATTERN = Pattern.compile("<[\\s]*(H[1-6])[^>]*?>(.*?)</\\1>", Pattern.DOTALL|Pattern.CASE_INSENSITIVE);

    private static final String[][] ESCAPES = {
            {"\"",     "quot"}, // " - double-quote
            {"&",      "amp"}, // & - ampersand
            {"<",      "lt"}, // < - less-than
            {">",      "gt"}, // > - greater-than

            // Mapping to escape ISO-8859-1 characters to their named HTML 3.x equivalents.
            {"\u00A0", "nbsp"}, // non-breaking space
            {"\u00A1", "iexcl"}, // inverted exclamation mark
            {"\u00A2", "cent"}, // cent sign
            {"\u00A3", "pound"}, // pound sign
            {"\u00A4", "curren"}, // currency sign
            {"\u00A5", "yen"}, // yen sign = yuan sign
            {"\u00A6", "brvbar"}, // broken bar = broken vertical bar
            {"\u00A7", "sect"}, // section sign
            {"\u00A8", "uml"}, // diaeresis = spacing diaeresis
            {"\u00A9", "copy"}, // © - copyright sign
            {"\u00AA", "ordf"}, // feminine ordinal indicator
            {"\u00AB", "laquo"}, // left-pointing double angle quotation mark = left pointing guillemet
            {"\u00AC", "not"}, // not sign
            {"\u00AD", "shy"}, // soft hyphen = discretionary hyphen
            {"\u00AE", "reg"}, // ® - registered trademark sign
            {"\u00AF", "macr"}, // macron = spacing macron = overline = APL overbar
            {"\u00B0", "deg"}, // degree sign
            {"\u00B1", "plusmn"}, // plus-minus sign = plus-or-minus sign
            {"\u00B2", "sup2"}, // superscript two = superscript digit two = squared
            {"\u00B3", "sup3"}, // superscript three = superscript digit three = cubed
            {"\u00B4", "acute"}, // acute accent = spacing acute
            {"\u00B5", "micro"}, // micro sign
            {"\u00B6", "para"}, // pilcrow sign = paragraph sign
            {"\u00B7", "middot"}, // middle dot = Georgian comma = Greek middle dot
            {"\u00B8", "cedil"}, // cedilla = spacing cedilla
            {"\u00B9", "sup1"}, // superscript one = superscript digit one
            {"\u00BA", "ordm"}, // masculine ordinal indicator
            {"\u00BB", "raquo"}, // right-pointing double angle quotation mark = right pointing guillemet
            {"\u00BC", "frac14"}, // vulgar fraction one quarter = fraction one quarter
            {"\u00BD", "frac12"}, // vulgar fraction one half = fraction one half
            {"\u00BE", "frac34"}, // vulgar fraction three quarters = fraction three quarters
            {"\u00BF", "iquest"}, // inverted question mark = turned question mark
            {"\u00C0", "Agrave"}, // А - uppercase A, grave accent
            {"\u00C1", "Aacute"}, // Б - uppercase A, acute accent
            {"\u00C2", "Acirc"}, // В - uppercase A, circumflex accent
            {"\u00C3", "Atilde"}, // Г - uppercase A, tilde
            {"\u00C4", "Auml"}, // Д - uppercase A, umlaut
            {"\u00C5", "Aring"}, // Е - uppercase A, ring
            {"\u00C6", "AElig"}, // Ж - uppercase AE
            {"\u00C7", "Ccedil"}, // З - uppercase C, cedilla
            {"\u00C8", "Egrave"}, // И - uppercase E, grave accent
            {"\u00C9", "Eacute"}, // Й - uppercase E, acute accent
            {"\u00CA", "Ecirc"}, // К - uppercase E, circumflex accent
            {"\u00CB", "Euml"}, // Л - uppercase E, umlaut
            {"\u00CC", "Igrave"}, // М - uppercase I, grave accent
            {"\u00CD", "Iacute"}, // Н - uppercase I, acute accent
            {"\u00CE", "Icirc"}, // О - uppercase I, circumflex accent
            {"\u00CF", "Iuml"}, // П - uppercase I, umlaut
            {"\u00D0", "ETH"}, // Р - uppercase Eth, Icelandic
            {"\u00D1", "Ntilde"}, // С - uppercase N, tilde
            {"\u00D2", "Ograve"}, // Т - uppercase O, grave accent
            {"\u00D3", "Oacute"}, // У - uppercase O, acute accent
            {"\u00D4", "Ocirc"}, // Ф - uppercase O, circumflex accent
            {"\u00D5", "Otilde"}, // Х - uppercase O, tilde
            {"\u00D6", "Ouml"}, // Ц - uppercase O, umlaut
            {"\u00D7", "times"}, // multiplication sign
            {"\u00D8", "Oslash"}, // Ш - uppercase O, slash
            {"\u00D9", "Ugrave"}, // Щ - uppercase U, grave accent
            {"\u00DA", "Uacute"}, // Ъ - uppercase U, acute accent
            {"\u00DB", "Ucirc"}, // Ы - uppercase U, circumflex accent
            {"\u00DC", "Uuml"}, // Ь - uppercase U, umlaut
            {"\u00DD", "Yacute"}, // Э - uppercase Y, acute accent
            {"\u00DE", "THORN"}, // Ю - uppercase THORN, Icelandic
            {"\u00DF", "szlig"}, // Я - lowercase sharps, German
            {"\u00E0", "agrave"}, // а - lowercase a, grave accent
            {"\u00E1", "aacute"}, // б - lowercase a, acute accent
            {"\u00E2", "acirc"}, // в - lowercase a, circumflex accent
            {"\u00E3", "atilde"}, // г - lowercase a, tilde
            {"\u00E4", "auml"}, // д - lowercase a, umlaut
            {"\u00E5", "aring"}, // е - lowercase a, ring
            {"\u00E6", "aelig"}, // ж - lowercase ae
            {"\u00E7", "ccedil"}, // з - lowercase c, cedilla
            {"\u00E8", "egrave"}, // и - lowercase e, grave accent
            {"\u00E9", "eacute"}, // й - lowercase e, acute accent
            {"\u00EA", "ecirc"}, // к - lowercase e, circumflex accent
            {"\u00EB", "euml"}, // л - lowercase e, umlaut
            {"\u00EC", "igrave"}, // м - lowercase i, grave accent
            {"\u00ED", "iacute"}, // н - lowercase i, acute accent
            {"\u00EE", "icirc"}, // о - lowercase i, circumflex accent
            {"\u00EF", "iuml"}, // п - lowercase i, umlaut
            {"\u00F0", "eth"}, // р - lowercase eth, Icelandic
            {"\u00F1", "ntilde"}, // с - lowercase n, tilde
            {"\u00F2", "ograve"}, // т - lowercase o, grave accent
            {"\u00F3", "oacute"}, // у - lowercase o, acute accent
            {"\u00F4", "ocirc"}, // ф - lowercase o, circumflex accent
            {"\u00F5", "otilde"}, // х - lowercase o, tilde
            {"\u00F6", "ouml"}, // ц - lowercase o, umlaut
            {"\u00F7", "divide"}, // division sign
            {"\u00F8", "oslash"}, // ш - lowercase o, slash
            {"\u00F9", "ugrave"}, // щ - lowercase u, grave accent
            {"\u00FA", "uacute"}, // ъ - lowercase u, acute accent
            {"\u00FB", "ucirc"}, // ы - lowercase u, circumflex accent
            {"\u00FC", "uuml"}, // ь - lowercase u, umlaut
            {"\u00FD", "yacute"}, // э - lowercase y, acute accent
            {"\u00FE", "thorn"}, // ю - lowercase thorn, Icelandic
            {"\u00FF", "yuml"}, // я - lowercase y, umlaut
    };

    private static final int MIN_ESCAPE = 2;
    private static final int MAX_ESCAPE = 6;

    private static final HashMap<String, CharSequence> LOOKUP_MAP;
    static {
        LOOKUP_MAP = new HashMap<>();
        for (final CharSequence[] seq : ESCAPES) {
            LOOKUP_MAP.put(seq[1].toString(), seq[0]);
        }
    }

    /**
     * Unescapes HTML3 chars from a string.
     *
     * Modified version of http://stackoverflow.com/questions/994331/java-how-to-decode-html-character-entities-in-java-like-httputility-htmldecode/24575417#24575417.
     *
     * @param input
     * @return
     */
    private static String unescapeHtml(final String input) {
        StringBuilder writer = null;
        int len = input.length();
        int i = 1;
        int st = 0;
        while (true) {
            // look for '&'
            while (i < len && input.charAt(i-1) != '&') {
                i++;
            }
            if (i >= len) {
                break;
            }

            // found '&', look for ';'
            int j = i;
            while (j < len && j < i + MAX_ESCAPE + 1 && input.charAt(j) != ';') {
                j++;
            }
            if (j == len || j < i + MIN_ESCAPE || j == i + MAX_ESCAPE + 1) {
                i++;
                continue;
            }

            // found escape
            if (input.charAt(i) == '#') {
                // numeric escape
                int k = i + 1;
                int radix = 10;

                final char firstChar = input.charAt(k);
                if (firstChar == 'x' || firstChar == 'X') {
                    k++;
                    radix = 16;
                }

                try {
                    int entityValue = Integer.parseInt(input.substring(k, j), radix);

                    if (writer == null) {
                        writer = new StringBuilder(input.length());
                    }
                    writer.append(input.substring(st, i - 1));

                    if (entityValue > 0xFFFF) {
                        final char[] chrs = Character.toChars(entityValue);
                        writer.append(chrs[0]);
                        writer.append(chrs[1]);
                    }
                    else if(entityValue == 39) {
                        writer.append('\'');
                    }
                    else {
                        writer.append(entityValue);
                    }

                }
                catch (NumberFormatException ex) {
                    i++;
                    continue;
                }
            }
            else {
                // named escape
                CharSequence value = LOOKUP_MAP.get(input.substring(i, j));
                if (value == null) {
                    i++;
                    continue;
                }

                if (writer == null) {
                    writer = new StringBuilder(input.length());
                }
                writer.append(input.substring(st, i - 1));

                writer.append(value);
            }

            // skip escape
            st = j + 1;
            i = st;
        }

        if (writer != null) {
            writer.append(input.substring(st, len));
            return writer.toString();
        }
        return input;
    }
    
    /**
     * Replaces the img tags with their alt text.
     * 
     * @param html
     * @return 
     */
    public static String replaceImgWithAlt(String html) {
        Matcher m = IMG_ALT_TITLE_PATTERN.matcher(html);
        if (m.find()) {
            return m.replaceAll(" $1 ");
        }
        return html;
    }
    
    /**
     * Removes all HTML comments from string.
     * 
     * @param html
     * @return 
     */
    public static String removeComments(String html) {
        return html.replaceAll("(?s)<!--.*?-->", "");
    }
    
    /**
     * A fast way but unsafe way to remove the tags from an HTML string. The method
     * does not remove javascript, css and other non text blocks and thus it should
     * be used with caution.
     * 
     * @param html
     * @return 
     */
    public static String unsafeRemoveAllTags(String html) {
        return html.replaceAll("\\<.*?>"," ");
    }
    
    /**
     * A safe way to remove the tags from an HTML string. The method removes first
     * javascript, css and other non text blocks and then removes all HTML tags.
     * 
     * @param html
     * @return 
     */
    public static String safeRemoveAllTags(String html) {
        html = removeNonTextTags(html);
        html = unsafeRemoveAllTags(html);
        return html;
    }
    
    private static String removeNonTextTags(String html) {
        html = removeComments(html);
        Matcher m = NON_TEXT_TAGS_PATTERN.matcher(html);
        if(m.find()) {
            html = m.replaceAll(" ");
        }
        
        return html;
    }
    
    /**
     * Removes all non-text tags (Javascript, css etc) from a string along with
     * all the attributes from the tags.
     * 
     * @param html
     * @return 
     */
    public static String removeNonTextTagsAndAttributes(String html) {
        html = removeNonTextTags(html);
        
        Matcher m = REMOVE_ATTRIBUTES_PATTERN.matcher(html);
        if(m.find()) {
            html = m.replaceAll("<$1$2>");
        }
        
        html = unescapeHtml(html);
        
        return html;
    }
    
    /**
     * Extracts the text from an HTML page.
     * 
     * @param html
     * @return 
     */
    public static String extractText(String html) {
        //return Jsoup.parse(text).text();
        html = replaceImgWithAlt(html);
        html = safeRemoveAllTags(html);
        
        html = unescapeHtml(html);
        
        return html;
    }
    
    private static String clear(String html) {
        return StringCleaner.removeExtraSpaces(unescapeHtml(unsafeRemoveAllTags(html)));
    }
    
    /**
     * Extracts the title of the page.
     * 
     * @param html
     * @return 
     */
    public static String extractTitle(String html) {
        Matcher m = TITLE_PATTERN.matcher(html);
        if (m.find()) {
            return clear(m.group(0));
        }
        return null;
    }
    
    /**
     * Enum with the various components of a hyperlink.
     */
    public enum HyperlinkPart {
        /**
         * The entire HTML tag.
         */
        HTMLTAG, 
        
        /**
         * The URL address.
         */
        URL,
        
        /***
         * The Anchor Text.
         */
        ANCHORTEXT
    }
    
    /**
     * Extracts the hyperlinks from an html string and returns their components 
     * in a map.
     * 
     * @param html
     * @return 
     */
    public static Map<HyperlinkPart, List<String>> extractHyperlinks(String html) {
        Map<HyperlinkPart, List<String>> hyperlinksMap = new HashMap<>();
        hyperlinksMap.put(HyperlinkPart.HTMLTAG, new ArrayList<>());
        hyperlinksMap.put(HyperlinkPart.URL, new ArrayList<>());
        hyperlinksMap.put(HyperlinkPart.ANCHORTEXT, new ArrayList<>());
                
        Matcher m = HYPERLINK_PATTERN.matcher(html);
        while (m.find()) {
            if(m.groupCount()==2) {
                String tag = m.group(0);
                String url = m.group(1);
                String anchortext = m.group(2);
                hyperlinksMap.get(HyperlinkPart.HTMLTAG).add(tag);
                hyperlinksMap.get(HyperlinkPart.URL).add(url);
                hyperlinksMap.get(HyperlinkPart.ANCHORTEXT).add(anchortext);
            }
        }
        return hyperlinksMap;
    }
    
    /**
     * Extracts the meta tags from an HTML page and returns them in a map.
     * 
     * @param html
     * @return 
     */
    public static Map<String, String> extractMetatags(String html) {
        Map<String, String> metatagsMap = new HashMap<>();
                
        Matcher m = METATAG_PATTERN.matcher(html);
        while (m.find()) {
            if(m.groupCount()==2) {
                String name = m.group(1);
                String content = m.group(2);
                metatagsMap.put(clear(name), clear(content));
            }
        }
        return metatagsMap;
    }
    
    /**
     * Extracts the HTML headers (h1-h6 tags) from an HTML page.
     * 
     * @param html
     * @return 
     */
    public static Map<String, List<String>> extractHTMLheaders(String html) {
        Map<String, List<String>> hxtagsMap = new HashMap<>();
        for(int i=1;i<=6;++i) {
            hxtagsMap.put("H"+i, new ArrayList<>());
        }
                
        Matcher m = HX_PATTERN.matcher(html);
        while (m.find()) {
            if(m.groupCount()==2) {
                String tagType = m.group(1).toUpperCase(Locale.ENGLISH);
                String content = m.group(2);
                hxtagsMap.get(tagType).add(clear(content));
            }
        }
        return hxtagsMap;
    }
}
