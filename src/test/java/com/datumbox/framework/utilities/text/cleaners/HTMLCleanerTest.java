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
package com.datumbox.framework.utilities.text.cleaners;

import com.datumbox.tests.abstracts.AbstractTest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for HTMLCleaner.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class HTMLCleanerTest extends AbstractTest {

    /**
     * Test of replaceImgWithAlt method, of class HTMLCleaner.
     */
    @Test
    public void testReplaceImgWithAlt() {
        logger.info("replaceImgWithAlt");
        String text = "some text <img src=\"s.jpg\" title=\"here\"> and <img src=\"s.jpg\" alt=\"there\"> and everywhere";
        String expResult = "some text  here  and  there  and everywhere";
        String result = HTMLCleaner.replaceImgWithAlt(text);
        assertEquals(expResult, result);
    }

    /**
     * Test of removeComments method, of class HTMLCleaner.
     */
    @Test
    public void testRemoveComments() {
        logger.info("removeComments");
        String text = "some text and <!-- \n these \n are \n comments --> nothing else";
        String expResult = "some text and  nothing else";
        String result = HTMLCleaner.removeComments(text);
        assertEquals(expResult, result);
    }

    /**
     * Test of unsafeRemoveAllTags method, of class HTMLCleaner.
     */
    @Test
    public void testUnsafeRemoveAllTags() {
        logger.info("unsafeRemoveAllTags");
        String text = "some <a href=\"asd\">hyperlink</a> and <br/> some <br> more<br><span>text</span> here";
        String expResult = "some  hyperlink  and   some   more  text  here";
        String result = HTMLCleaner.unsafeRemoveAllTags(text);
        assertEquals(expResult, result);
    }

    /**
     * Test of safeRemoveAllTags method, of class HTMLCleaner.
     */
    @Test
    public void testSafeRemoveAllTags() {
        logger.info("safeRemoveAllTags");
        String text = "<script>hidden</script>some <a href=\"asd\">hyperlink</a> and <br/> some <br> more<br><span>text</span> here";
        String expResult = " some  hyperlink  and   some   more  text  here";
        String result = HTMLCleaner.safeRemoveAllTags(text);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractText method, of class HTMLCleaner.
     */
    @Test
    public void testExtractText() {
        logger.info("extractText");
        String text = "<html><head>INVISIBLE</head><body>"
                + "<!-- INISIBLE -->"
                + "<style>INVISIBLE</style>s"
                + "<style>INVISIBLE</style>"
                + "<script>INVISIBLE</script>"
                + "<object>INVISIBLE</object>"
                + "<embed>INVISIBLE</embed>"
                + "<applet>INVISIBLE</applet>"
                + "<noframes>INVISIBLE</noframes>"
                + "<noscript>INVISIBLE</noscript>"
                + "<noembed>INVISIBLE</noembed>"
                + "<option>INVISIBLE</option>"
                + "<div>visible1&#39;s</div>"
                + "<img src=\"\" alt=\"img\" />"
                + "<img src=\"\" alt=\"img2\" />"
                + "<br>"
                + "test!"
                + "</body></html>";
        String expResult = "s visible1's img img2 test!";
        String result = StringCleaner.removeExtraSpaces(HTMLCleaner.extractText(text));
        assertEquals(expResult, result);
    }

    /**
     * Test of removeNonTextTagsAndAttributes method, of class HTMLCleaner.
     */
    @Test
    public void testRemoveNonTextTagsAndAttributes() {
        logger.info("testRemoveNonTextTagsAndAttributes");
        String text = "<html><head>INVISIBLE</head><body>"
                + "<!-- INISIBLE -->"
                + "<style>INVISIBLE</style>"
                + "<script>INVISIBLE</script>"
                + "<object>INVISIBLE</object>"
                + "<embed>INVISIBLE</embed>"
                + "<applet>INVISIBLE</applet>"
                + "<noframes>INVISIBLE</noframes>"
                + "<noscript>INVISIBLE</noscript>"
                + "<noembed>INVISIBLE</noembed>"
                + "<option>INVISIBLE</option>"
                + "<div>visible1&#39;s</div>"
                + "<img src=\"\" />"
                + "<img src=\"\" />"
                + "<br>"
                + "test!"
                + "</body></html>";
        String expResult = "<html> <body> <div>visible1's</div><img/><img/><br>test!</body></html>";
        String result = StringCleaner.removeExtraSpaces(HTMLCleaner.removeNonTextTagsAndAttributes(text));
        assertEquals(expResult, result);
    }

    /**
     * Test of extractTitle method, of class HTMLCleaner.
     */
    @Test
    public void testExtractTitle() {
        logger.info("extractTitle");
        String text = "<html><head><title>Page&#39;s &amp; \nWebsite&#39;s   title</title></head></html>";
        String expResult = "Page's & Website's title";
        String result = HTMLCleaner.extractTitle(text);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractHyperlinks method, of class HTMLCleaner.
     */
    @Test
    public void testExtractHyperlinks() {
        logger.info("extractHyperlinks");
        String text = "some text <a href=\"url1\">text1</a> <a href=\"url2\"><img /></a> some other text";
        Map<HTMLCleaner.HyperlinkPart, List<String>> expResult = new HashMap<>();
        expResult.put(HTMLCleaner.HyperlinkPart.HTMLTAG, new ArrayList<>());
        expResult.put(HTMLCleaner.HyperlinkPart.URL, new ArrayList<>());
        expResult.put(HTMLCleaner.HyperlinkPart.ANCHORTEXT, new ArrayList<>());
        
        expResult.get(HTMLCleaner.HyperlinkPart.HTMLTAG).add("<a href=\"url1\">text1</a>");
        expResult.get(HTMLCleaner.HyperlinkPart.URL).add("url1");
        expResult.get(HTMLCleaner.HyperlinkPart.ANCHORTEXT).add("text1");        
        
        expResult.get(HTMLCleaner.HyperlinkPart.HTMLTAG).add("<a href=\"url2\"><img /></a>");
        expResult.get(HTMLCleaner.HyperlinkPart.URL).add("url2");
        expResult.get(HTMLCleaner.HyperlinkPart.ANCHORTEXT).add("<img />");
        
        Map<HTMLCleaner.HyperlinkPart, List<String>> result = HTMLCleaner.extractHyperlinks(text);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractMetatags method, of class HTMLCleaner.
     */
    @Test
    public void testExtractMetatags() {
        logger.info("extractMetatags");
        String text = "<html><head>   <meta name  =  \"description\" content  =   \"Some description.\" />\n" +
                      "    <   meta name  =  'keywords' content  =  'Some keywords.' >";
        Map<String, String> expResult = new HashMap<>();
        expResult.put("description", "Some description.");
        expResult.put("keywords", "Some keywords.");
        Map<String, String> result = HTMLCleaner.extractMetatags(text);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractHTMLheaders method, of class HTMLCleaner.
     */
    @Test
    public void testExtractHTMLheaders() {
        logger.info("extractHTMLheaders");
        String text = " <h1>a1</h1> <h1 class='s'>a2</h1> <h2>b1</h2>";
        Map<String, List<String>> expResult = new HashMap<>();
        for(int i=1;i<=6;++i) {
            expResult.put("H"+i, new ArrayList<>());
        }
        expResult.get("H1").add("a1");
        expResult.get("H1").add("a2");
        expResult.get("H2").add("b1");
        
        
        Map<String, List<String>> result = HTMLCleaner.extractHTMLheaders(text);
        assertEquals(expResult, result);
    }
    
}
