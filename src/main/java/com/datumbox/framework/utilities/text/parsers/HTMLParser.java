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
package com.datumbox.framework.utilities.text.parsers;

import com.datumbox.common.utilities.StringCleaner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;

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
        
        html = StringEscapeUtils.unescapeHtml4(html);
        
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
        
        html = StringEscapeUtils.unescapeHtml4(html);
        
        return html;
    }
    
    private static String clear(String html) {
        return StringCleaner.removeExtraSpaces(StringEscapeUtils.unescapeHtml4(unsafeRemoveAllTags(html)));
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
