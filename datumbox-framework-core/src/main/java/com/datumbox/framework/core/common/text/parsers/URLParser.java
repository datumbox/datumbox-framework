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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * The URLParser class contains static methods which can be used for parsing the
 * contents of URL strings.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class URLParser {
    
    /**
     * Enum with the parts of a URL string.
     */
    public enum URLParts {
        /**
         * Protocol.
         */
        PROTOCOL,
        
        /**
         * User info.
         */
        USERINFO,
        
        /**
         * Authority.
         */
        AUTHORITY,
        
        /**
         * Host.
         */
        HOST,
        
        /**
         * Port.
         */
        PORT,
        
        /**
         * Path.
         */
        PATH,
        
        /**
         * Query.
         */
        QUERY,
        
        /**
         * Filename.
         */
        FILENAME,
        
        /**
         * Ref.
         */
        REF;
    }
    
    /**
     * Enum with the parts of a domain name.
     */
    public enum DomainParts {
        /**
         * Sub-domain.
         */
        SUBDOMAIN, //www
        
        /**
         * Domain.
         */
        DOMAINNAME, //example
        
        /**
         * Top level domain.
         */
        TLD; //com
    }
    
    //common top level domains
    private static final Set<String> COMMON_TLD = new HashSet<>(Arrays.asList("br", "cn", "tr", "ca", "au", "uk", "ru", "in", "tw", "ve", "eg", "ar", "jp", "pl", "hk", "ua", "my", "mx", "ng", "vn", "pk", "sa", "com", "co", "kw", "bn", "kh", "pe", "za", "sg", "do", "bd", "jm", "net", "nz", "ec", "gi", "ph", "sv", "il", "ni", "lu", "na", "zw", "mz", "sz", "ao", "tz", "at", "uz", "pg", "mm", "py", "uy", "np", "gh", "ls", "lb", "et", "bt", "gr", "sy", "jo", "gt", "cy", "pt", "ma", "ir", "es", "pa", "id", "qa", "mo", "om", "org", "mt", "th", "ro", "cv", "tn", "zm", "ug", "cr", "pf", "sd", "kr", "yu", "mk", "hr", "vi", "bw", "im", "vc", "tj", "ge", "ly", "dz", "sb", "iq", "rs", "af", "ai", "lk", "pr", "sl", "gn", "az", "ye", "lr", "ae", "cu", "bh", "nf", "bo", "by", "slg.br", "pol.tr", "tmp.br", "ppg.br", "trd.br", "tur.br", "vet.br", "psi.br", "far.br", "imb.br", "tel.tr", "rec.br", "qsl.br", "psc.br", "ind.br", "pro.br", "eun.eg", "bio.br", "cng.br", "zlg.br", "agr.br", "biz.tr", "cnt.br", "idv.tw", "art.br", "web.tr", "not.br", "ntr.br", "plc.uk", "nom.br", "no.com", "oop.br", "odo.br", "srv.br", "gw", "csiro.au", "yt", "pm", "name.tr", "conf.au", "jus.br", "gda.pl", "kp", "gq", "mq", "aq", "adv.br", "fk", "wf", "er", "kn", "msk.ru", "nhs.uk", "fnd.br", "inf.br", "mus.br", "fot.br", "fst.br", "gen.tr", "g12.br", "med.br", "jor.br", "res.in", "nic.in", "spb.ru", "lel.br", "mat.br", "ltd.uk", "ggf.br", "gx.cn", "pp.ru", "gd.cn", "hb.cn", "xj.cn", "xz.cn", "av.tr", "yn.cn", "dr.tr", "gr.jp", "sd.cn", "ln.cn", "sx.cn", "tj.cn", "cq.cn", "gs.cn", "qh.cn", "fm.br", "fj.cn", "hl.cn", "ad.jp", "hn.cn", "hk.cn", "hi.cn", "it.ao", "he.cn", "bj.cn", "tw.cn", "gz.cn", "km", "zj.cn", "ha.cn", "ah.cn", "tv.br", "nx.cn", "ne.jp", "me.uk", "ab.ca", "asn.au", "bel.tr", "bc.ca", "mb.ca", "nf.ca", "nb.ca", "eti.br", "ato.br", "adm.br", "bmd.br", "arq.br", "ecn.br", "etc.br", "esp.br", "eng.br", "nl.ca", "ns.ca", "js.cn", "nm.cn", "sh.cn", "jx.cn", "am.br", "sn.cn", "sc.cn", "mo.cn", "jl.cn", "on.ca", "nu.ca", "nt.ca", "pe.ca", "qc.ca", "yk.ca", "sk.ca", "cim.br", "ki", "it", "is", "cx", "ke", "la", "hm", "hu", "cz", "de", "se", "to", "tm", "sh", "eu", "ie", "dk", "fi", "fj", "cc", "no", "nl", "nu", "bg", "ba", "be", "cd", "li", "gb", "fr", "fo", "ee", "gl", "gs", "ac", "tc", "am", "mobi", "coop", "aero", "edu", "int", "mil", "biz", "info", "name", "travel", "museum", "", "arpa", "nato", "asia", "jobs", "gov", "cat", "sk", "si", "tv", "ag", "sm", "al", "su", "va", "as", "pro", "tel", "st", "ws", "vg", "us", "ch", "ck", "cg", "cf", "bs", "ci", "cm", "mr", "io", "bj", "bi", "gy", "gp", "gm", "hn", "ht", "bf", "ad", "gf", "gd", "sr", "so", "an", "aw", "lc", "ax", "cs", "re", "pw", "nc", "mp", "bb", "tf", "ky", "bm", "gg", "ga", "tl", "bz", "kz", "je", "kg", "ml", "mg", "fm", "me", "lv", "lt", "cl", "mc", "md", "ms", "mn", "mu", "mv", "tt", "tk", "tg", "vu", "sn", "dm", "dj", "td", "sc", "ne", "mw", "nr", "pn", "rw", "ps", "tp", "www", "xxx"));
    
    //common second level domains (example: com.gr, gov.uk etc)
    private static final Set<String> COMMON_SLD = new HashSet<>(Arrays.asList("com", "gov", "edu", "org", "net", "co", "gob", "ac", "or", "se", "uk", "gb", "eu", "id", "info", "mil"));
    
    /**
     * Converts a relative URL to absolute URL.
     * 
     * @param baseURL
     * @param relative
     * @return
     */
    public static URL toAbsolute(URL baseURL, String relative) {
        try {
            return new URL(baseURL, relative);
        } 
        catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Converts a relative URL to absolute URL.
     * 
     * @param base
     * @param relative
     * @return
     */
    public static String toAbsolute(String base, String relative) {
        String absolute = null;
        base = base.trim();
        relative = relative.trim();
        
        if(relative.isEmpty()) {
            return absolute;
        }
        
        String rel2lowercase = relative.toLowerCase(Locale.ENGLISH);
        
        if(rel2lowercase.startsWith("mailto:") || rel2lowercase.startsWith("javascript:")) {
            return absolute;
        }
        
        try {
            absolute = toAbsolute(new URL(base), relative).toString();
        } 
        catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
        
        return absolute;
    }
    
    /**
     * This method splits a URL into parts and return a map containing them.
     * 
     * @param URLString
     * @return
     */
    public static Map<URLParts, String> splitURL(String URLString) {
        try {
            return splitURL(new URL(URLString));
        } 
        catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * This method splits a URL into parts and return a map containing them.
     * 
     * @param url
     * @return 
     */
    public static Map<URLParts, String> splitURL(URL url) {
        Map<URLParts, String> urlParts = new HashMap<>();
        
        urlParts.put(URLParts.PROTOCOL, url.getProtocol());
        urlParts.put(URLParts.USERINFO, url.getUserInfo());
        urlParts.put(URLParts.AUTHORITY, url.getAuthority());
        urlParts.put(URLParts.HOST, url.getHost());
        urlParts.put(URLParts.PATH, url.getPath());
        urlParts.put(URLParts.QUERY, url.getQuery());
        urlParts.put(URLParts.FILENAME, url.getFile());
        urlParts.put(URLParts.REF, url.getRef());
        int port = url.getPort();
        if(port!=-1) {
            urlParts.put(URLParts.PORT, String.valueOf(port));
        }
        else {
            urlParts.put(URLParts.PORT, null);
        }
        
        return urlParts;
    }
    
    /**
     * This method can be used to build a URL from its parts.
     * 
     * @param urlParts
     * @return
     */
    public static String joinURL(Map<URLParts, String> urlParts) {
        try {
            URI uri = new URI(urlParts.get(URLParts.PROTOCOL), urlParts.get(URLParts.AUTHORITY), urlParts.get(URLParts.PATH), urlParts.get(URLParts.QUERY), urlParts.get(URLParts.REF));
            return uri.toString();
        } 
        catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Splits a domain name to parts and returns them in a map.
     * 
     * @param domain
     * @return 
     */
    public static Map<DomainParts, String> splitDomain(String domain) {
        Map<DomainParts, String> domainParts = null;
        
        String[] dottedParts = domain.trim().toLowerCase(Locale.ENGLISH).split("\\.");
        
        if(dottedParts.length==2) {
            domainParts = new HashMap<>();
            domainParts.put(DomainParts.SUBDOMAIN, null);
            domainParts.put(DomainParts.DOMAINNAME, dottedParts[0]);
            domainParts.put(DomainParts.TLD, dottedParts[1]);
        }
        else if(dottedParts.length>2) {
            int n = dottedParts.length;
            if( COMMON_TLD.contains(dottedParts[n-2]+"."+dottedParts[n-1]) ||
                (COMMON_TLD.contains(dottedParts[n-1]) && COMMON_SLD.contains(dottedParts[n-2])) ||
                (COMMON_TLD.contains(dottedParts[n-2]) && COMMON_SLD.contains(dottedParts[n-1]))) {
                
                domainParts = new HashMap<>();
                domainParts.put(DomainParts.TLD, dottedParts[n-2]+"."+dottedParts[n-1]);
                domainParts.put(DomainParts.DOMAINNAME, dottedParts[n-3]);
                
                StringBuilder sb = new StringBuilder(dottedParts[0]);
                for(int i=1;i<n-3;++i) {
                    sb.append(".").append(dottedParts[i]);
                }
                domainParts.put(DomainParts.SUBDOMAIN, sb.toString());
            }
            else if(COMMON_TLD.contains(dottedParts[n-1])) {
                domainParts = new HashMap<>();
                domainParts.put(DomainParts.TLD, dottedParts[n-1]);
                domainParts.put(DomainParts.DOMAINNAME, dottedParts[n-2]);
                
                StringBuilder sb = new StringBuilder(dottedParts[0]);
                for(int i=1;i<n-2;++i) {
                    sb.append(".").append(dottedParts[i]);
                }
                domainParts.put(DomainParts.SUBDOMAIN, sb.toString());
            }
        }
        
            
        return domainParts;
    }
}
