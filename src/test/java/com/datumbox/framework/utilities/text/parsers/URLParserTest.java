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
package com.datumbox.framework.utilities.text.parsers;

import com.datumbox.framework.utilities.text.parsers.URLParser;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class URLParserTest {
    
    public URLParserTest() {
    }

    /**
     * Test of toAbsolute method, of class URLParser.
     */
    @Test
    public void testToAbsolute_String_String() throws Exception {
        System.out.println("toAbsolute");
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
    public void testSplitURL_String() throws Exception {
        System.out.println("splitURL");
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
    public void testJoinURL() throws Exception {
        System.out.println("joinURL");
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
        System.out.println("splitDomain1");
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
        System.out.println("splitDomain2");
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
        System.out.println("splitDomain3");
        String domain = "www.example.com";
        Map<URLParser.DomainParts, String> expResult = new HashMap<>();
        expResult.put(URLParser.DomainParts.TLD, "com");
        expResult.put(URLParser.DomainParts.DOMAINNAME, "example");
        expResult.put(URLParser.DomainParts.SUBDOMAIN, "www");
        
        
        Map<URLParser.DomainParts, String> result = URLParser.splitDomain(domain);
        assertEquals(expResult, result);
    }
    
}
