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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class HTMLCleaner {
    private final static Pattern IMG_ALT_TITLE_PATTERN = Pattern.compile("<[\\s]*img[^>]*[alt|title]=[\\s]*[\\\"']?([^>\\\"']+)[\\\"']?[^>]*>", Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
    private final static Pattern NON_TEXT_TAGS_PATTERN = Pattern.compile("<[\\s]*(head|style|script|object|embed|applet|noframes|noscript|noembed|option)[^>]*?>.*?</\\1>", Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
    private final static Pattern REMOVE_ATTRIBUTES_PATTERN = Pattern.compile("<([a-z!][a-z0-9]*)[^>]*?(/?)>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);    
    private final static Pattern TITLE_PATTERN = Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);    
    private final static Pattern HYPERLINK_PATTERN = Pattern.compile("<[\\s]*a[^>]*href[\\s]*=[\\s]*[\\\"']([^\\\"']*)[\\\"'][^>]*>(.*?)</a>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);    
    private final static Pattern METATAG_PATTERN = Pattern.compile("<[\\s]*meta[^>]*name[\\s]*=[\\s]*[\\\"']([^\\\"']*)[\\\"'][^>]*content[\\s]*=[\\s]*[\\\"']([^\\\"']*)[\\\"'][^>]*>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);    
    private final static Pattern HX_PATTERN = Pattern.compile("<[\\s]*(H[1-6])[^>]*?>(.*?)</\\1>", Pattern.DOTALL|Pattern.CASE_INSENSITIVE);    
    
    public static String replaceImgWithAlt(String text) {
        Matcher m = IMG_ALT_TITLE_PATTERN.matcher(text);
        if (m.find()) {
            return m.replaceAll(" $1 ");
        }
        return text;
    }
    
    public static String removeComments(String text) {
        return text.replaceAll("(?s)<!--.*?-->", "");
    }
    
    public static String unsafeRemoveAllTags(String text) {
        return text.replaceAll("\\<.*?>"," ");
    }
    
    public static String safeRemoveAllTags(String text) {
        text = removeNonTextTags(text);
        text = unsafeRemoveAllTags(text);
        return text;
    }
    
    protected static String removeNonTextTags(String text) {
        text = removeComments(text);
        Matcher m = NON_TEXT_TAGS_PATTERN.matcher(text);
        if(m.find()) {
            text = m.replaceAll(" ");
        }
        
        return text;
    }
    
    public static String removeNonTextTagsAndAttributes(String text) {
        text = removeNonTextTags(text);
        
        Matcher m = REMOVE_ATTRIBUTES_PATTERN.matcher(text);
        if(m.find()) {
            text = m.replaceAll("<$1$2>");
        }
        
        text = StringEscapeUtils.unescapeHtml4(text);
        
        return text;
    }
    
    public static String extractText(String text) {
        //return Jsoup.parse(text).text();
        text = replaceImgWithAlt(text);
        text = safeRemoveAllTags(text);
        
        text = StringEscapeUtils.unescapeHtml4(text);
        
        return text;
    }
    
    protected static String clear(String text) {
        return StringCleaner.removeExtraSpaces(StringEscapeUtils.unescapeHtml4(unsafeRemoveAllTags(text)));
    }
    
    public static String extractTitle(String text) {
        Matcher m = TITLE_PATTERN.matcher(text);
        if (m.find()) {
            return clear(m.group(0));
        }
        return null;
    }
    
    public enum HyperlinkPart {
        HTMLTAG, 
        URL,
        ANCHORTEXT
    }
    
    public static Map<HyperlinkPart, List<String>> extractHyperlinks(String text) {
        Map<HyperlinkPart, List<String>> hyperlinksMap = new HashMap<>();
        hyperlinksMap.put(HyperlinkPart.HTMLTAG, new ArrayList<>());
        hyperlinksMap.put(HyperlinkPart.URL, new ArrayList<>());
        hyperlinksMap.put(HyperlinkPart.ANCHORTEXT, new ArrayList<>());
                
        Matcher m = HYPERLINK_PATTERN.matcher(text);
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
    
    public static Map<String, String> extractMetatags(String text) {
        Map<String, String> metatagsMap = new HashMap<>();
                
        Matcher m = METATAG_PATTERN.matcher(text);
        while (m.find()) {
            if(m.groupCount()==2) {
                String name = m.group(1);
                String content = m.group(2);
                metatagsMap.put(clear(name), clear(content));
            }
        }
        return metatagsMap;
    }
    
    public static Map<String, List<String>> extractHTMLheaders(String text) {
        Map<String, List<String>> hxtagsMap = new HashMap<>();
        for(Integer i=1;i<=6;++i) {
            hxtagsMap.put("H"+i.toString(), new ArrayList<>());
        }
                
        Matcher m = HX_PATTERN.matcher(text);
        while (m.find()) {
            if(m.groupCount()==2) {
                String tagType = m.group(1).toUpperCase();
                String content = m.group(2);
                hxtagsMap.get(tagType).add(clear(content));
            }
        }
        return hxtagsMap;
    }
}
