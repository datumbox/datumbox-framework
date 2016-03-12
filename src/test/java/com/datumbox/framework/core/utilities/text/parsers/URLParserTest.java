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
package com.datumbox.framework.core.utilities.text.parsers;

import com.datumbox.framework.tests.abstracts.AbstractTest;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for URLParser.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class URLParserTest extends AbstractTest {

    /**
     * Test of toAbsolute method, of class URLParser.
     */
    @Test
    public void testToAbsolute_String_String() {
        logger.info("toAbsolute");
        String base = "http://user:password@www.example.com:8080/to/path/document?arg1=val1&arg2=val2#part";
        String relative = "../path2/doc2?a=1&b=2#part2";
        String expResult = "http://user:password@www.example.com:8080/to/path2/doc2?a=1&b=2#part2";
        String result = URLParser.toAbsolute(base, relative);
        assertEquals(expResult, result);
    }

    /**
     * Test of splitURL method, of class URLParser.
     */
    @Test
    public void testSplitURL_String() {
        logger.info("splitURL");
        String URLString = "http://user:password@www.example.com:8080/to/path/document?arg1=val1&arg2=val2#part";
        Map<URLParser.URLParts, String> expResult = new HashMap<>();
        expResult.put(URLParser.URLParts.PROTOCOL, "http");
        expResult.put(URLParser.URLParts.PATH, "/to/path/document");
        expResult.put(URLParser.URLParts.HOST, "www.example.com");
        expResult.put(URLParser.URLParts.PORT, "8080");
        expResult.put(URLParser.URLParts.USERINFO, "user:password");
        expResult.put(URLParser.URLParts.FILENAME, "/to/path/document?arg1=val1&arg2=val2");
        expResult.put(URLParser.URLParts.QUERY, "arg1=val1&arg2=val2");
        expResult.put(URLParser.URLParts.AUTHORITY, "user:password@www.example.com:8080");
        expResult.put(URLParser.URLParts.REF, "part");
        
        Map<URLParser.URLParts, String> result = URLParser.splitURL(URLString);
        assertEquals(expResult, result);
    }

    /**
     * Test of joinURL method, of class URLParser.
     */
    @Test
    public void testJoinURL() {
        logger.info("joinURL");
        Map<URLParser.URLParts, String> urlParts = new HashMap<>();
        urlParts.put(URLParser.URLParts.PROTOCOL, "http");
        urlParts.put(URLParser.URLParts.PATH, "/to/path/document");
        urlParts.put(URLParser.URLParts.HOST, "www.example.com");
        urlParts.put(URLParser.URLParts.PORT, "8080");
        urlParts.put(URLParser.URLParts.USERINFO, "user:password");
        urlParts.put(URLParser.URLParts.FILENAME, "/to/path/document?arg1=val1&arg2=val2");
        urlParts.put(URLParser.URLParts.QUERY, "arg1=val1&arg2=val2");
        urlParts.put(URLParser.URLParts.AUTHORITY, "user:password@www.example.com:8080");
        urlParts.put(URLParser.URLParts.REF, "part");
        
        String expResult = "http://user:password@www.example.com:8080/to/path/document?arg1=val1&arg2=val2#part";
        String result = URLParser.joinURL(urlParts);
        assertEquals(expResult, result);
    }

    /**
     * Test of splitDomain method, of class URLParser.
     */
    @Test
    public void testSplitDomain1() {
        logger.info("splitDomain1");
        String domain = "www.cars.example.co.uk";
        Map<URLParser.DomainParts, String> expResult = new HashMap<>();
        expResult.put(URLParser.DomainParts.TLD, "co.uk");
        expResult.put(URLParser.DomainParts.DOMAINNAME, "example");
        expResult.put(URLParser.DomainParts.SUBDOMAIN, "www.cars");
        
        
        Map<URLParser.DomainParts, String> result = URLParser.splitDomain(domain);
        assertEquals(expResult, result);
    }

    /**
     * Test of splitDomain method, of class URLParser.
     */
    @Test
    public void testSplitDomain2() {
        logger.info("splitDomain2");
        String domain = "example.com";
        Map<URLParser.DomainParts, String> expResult = new HashMap<>();
        expResult.put(URLParser.DomainParts.TLD, "com");
        expResult.put(URLParser.DomainParts.DOMAINNAME, "example");
        expResult.put(URLParser.DomainParts.SUBDOMAIN, null);
        
        
        Map<URLParser.DomainParts, String> result = URLParser.splitDomain(domain);
        assertEquals(expResult, result);
    }

    /**
     * Test of splitDomain method, of class URLParser.
     */
    @Test
    public void testSplitDomain3() {
        logger.info("splitDomain3");
        String domain = "www.example.com";
        Map<URLParser.DomainParts, String> expResult = new HashMap<>();
        expResult.put(URLParser.DomainParts.TLD, "com");
        expResult.put(URLParser.DomainParts.DOMAINNAME, "example");
        expResult.put(URLParser.DomainParts.SUBDOMAIN, "www");
        
        
        Map<URLParser.DomainParts, String> result = URLParser.splitDomain(domain);
        assertEquals(expResult, result);
    }
    
}
