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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class URLParser {
    
    public enum URLParts {
        PROTOCOL,
        USERINFO,
        AUTHORITY,
        HOST,
        PORT,
        PATH,
        QUERY,
        FILENAME,
        REF
    }
    
    public enum DomainParts {
        SUBDOMAIN, //www
        DOMAINNAME, //example
        TLD //com
    }
    
    //common top level domains
    public static final Set<String> commonTLDs = new HashSet<>(Arrays.asList("br", "cn", "tr", "ca", "au", "uk", "ru", "in", "tw", "ve", "eg", "ar", "jp", "pl", "hk", "ua", "my", "mx", "ng", "vn", "pk", "sa", "com", "co", "kw", "bn", "kh", "pe", "za", "sg", "do", "bd", "jm", "net", "nz", "ec", "gi", "ph", "sv", "il", "ni", "lu", "na", "zw", "mz", "sz", "ao", "tz", "at", "uz", "pg", "mm", "py", "uy", "np", "gh", "ls", "lb", "et", "bt", "gr", "sy", "jo", "gt", "cy", "pt", "ma", "ir", "es", "pa", "id", "qa", "mo", "om", "org", "mt", "th", "ro", "cv", "tn", "zm", "ug", "cr", "pf", "sd", "kr", "yu", "mk", "hr", "vi", "bw", "im", "vc", "tj", "ge", "ly", "dz", "sb", "iq", "rs", "af", "ai", "lk", "pr", "sl", "gn", "az", "ye", "lr", "ae", "cu", "bh", "nf", "bo", "by", "slg.br", "pol.tr", "tmp.br", "ppg.br", "trd.br", "tur.br", "vet.br", "psi.br", "far.br", "imb.br", "tel.tr", "rec.br", "qsl.br", "psc.br", "ind.br", "pro.br", "eun.eg", "bio.br", "cng.br", "zlg.br", "agr.br", "biz.tr", "cnt.br", "idv.tw", "art.br", "web.tr", "not.br", "ntr.br", "plc.uk", "nom.br", "no.com", "oop.br", "odo.br", "srv.br", "gw", "csiro.au", "yt", "pm", "name.tr", "conf.au", "jus.br", "gda.pl", "kp", "gq", "mq", "aq", "adv.br", "fk", "wf", "er", "kn", "msk.ru", "nhs.uk", "fnd.br", "inf.br", "mus.br", "fot.br", "fst.br", "gen.tr", "g12.br", "med.br", "jor.br", "res.in", "nic.in", "spb.ru", "lel.br", "mat.br", "ltd.uk", "ggf.br", "gx.cn", "pp.ru", "gd.cn", "hb.cn", "xj.cn", "xz.cn", "av.tr", "yn.cn", "dr.tr", "gr.jp", "sd.cn", "ln.cn", "sx.cn", "tj.cn", "cq.cn", "gs.cn", "qh.cn", "fm.br", "fj.cn", "hl.cn", "ad.jp", "hn.cn", "hk.cn", "hi.cn", "it.ao", "he.cn", "bj.cn", "tw.cn", "gz.cn", "km", "zj.cn", "ha.cn", "ah.cn", "tv.br", "nx.cn", "ne.jp", "me.uk", "ab.ca", "asn.au", "bel.tr", "bc.ca", "mb.ca", "nf.ca", "nb.ca", "eti.br", "ato.br", "adm.br", "bmd.br", "arq.br", "ecn.br", "etc.br", "esp.br", "eng.br", "nl.ca", "ns.ca", "js.cn", "nm.cn", "sh.cn", "jx.cn", "am.br", "sn.cn", "sc.cn", "mo.cn", "jl.cn", "on.ca", "nu.ca", "nt.ca", "pe.ca", "qc.ca", "yk.ca", "sk.ca", "cim.br", "ki", "it", "is", "cx", "ke", "la", "hm", "hu", "cz", "de", "se", "to", "tm", "sh", "eu", "ie", "dk", "fi", "fj", "cc", "no", "nl", "nu", "bg", "ba", "be", "cd", "li", "gb", "fr", "fo", "ee", "gl", "gs", "ac", "tc", "am", "mobi", "coop", "aero", "edu", "int", "mil", "biz", "info", "name", "travel", "museum", "", "arpa", "nato", "asia", "jobs", "gov", "cat", "sk", "si", "tv", "ag", "sm", "al", "su", "va", "as", "pro", "tel", "st", "ws", "vg", "us", "ch", "ck", "cg", "cf", "bs", "ci", "cm", "mr", "io", "bj", "bi", "gy", "gp", "gm", "hn", "ht", "bf", "ad", "gf", "gd", "sr", "so", "an", "aw", "lc", "ax", "cs", "re", "pw", "nc", "mp", "bb", "tf", "ky", "bm", "gg", "ga", "tl", "bz", "kz", "je", "kg", "ml", "mg", "fm", "me", "lv", "lt", "cl", "mc", "md", "ms", "mn", "mu", "mv", "tt", "tk", "tg", "vu", "sn", "dm", "dj", "td", "sc", "ne", "mw", "nr", "pn", "rw", "ps", "tp", "www", "xxx"));
    
    //common second level domains (example: com.gr, gov.uk etc)
    public static final Set<String> commonSLDs = new HashSet<>(Arrays.asList("com", "gov", "edu", "org", "net", "co", "gob", "ac", "or", "se", "uk", "gb", "eu", "id", "info", "mil"));
    
    public static URL toAbsolute(URL baseURL, String relative) throws MalformedURLException {
        return new URL(baseURL, relative);
    }
    
    public static String toAbsolute(String base, String relative) throws MalformedURLException {
        String absolute = null;
        base = base.trim();
        relative = relative.trim();
        
        if(relative.isEmpty()) {
            return absolute;
        }
        
        String rel2lowercase = relative.toLowerCase();
        
        if(rel2lowercase.startsWith("mailto:") || rel2lowercase.startsWith("javascript:")) {
            return absolute;
        }
        
        absolute = toAbsolute(new URL(base), relative).toString();
        
        return absolute;
    }
    
    public static Map<URLParts, String> splitURL(String URLString) throws MalformedURLException {
        return splitURL(new URL(URLString));
    }
    
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
    
    public static String joinURL(Map<URLParts, String> urlParts) throws URISyntaxException {
        URI uri = new URI(urlParts.get(URLParts.PROTOCOL), urlParts.get(URLParts.AUTHORITY), urlParts.get(URLParts.PATH), urlParts.get(URLParts.QUERY), urlParts.get(URLParts.REF));
        return uri.toString();
    }
    
    public static Map<DomainParts, String> splitDomain(String domain) {
        Map<DomainParts, String> domainParts = null;
        
        String[] dottedParts = domain.trim().toLowerCase().split("\\.");
        
        if(dottedParts.length==2) {
            domainParts = new HashMap<>();
            domainParts.put(DomainParts.SUBDOMAIN, null);
            domainParts.put(DomainParts.DOMAINNAME, dottedParts[0]);
            domainParts.put(DomainParts.TLD, dottedParts[1]);
        }
        else if(dottedParts.length>2) {
            int n = dottedParts.length;
            if( commonTLDs.contains(dottedParts[n-2]+"."+dottedParts[n-1]) ||
                (commonTLDs.contains(dottedParts[n-1]) && commonSLDs.contains(dottedParts[n-2])) ||
                (commonTLDs.contains(dottedParts[n-2]) && commonSLDs.contains(dottedParts[n-1]))) {
                
                domainParts = new HashMap<>();
                domainParts.put(DomainParts.TLD, dottedParts[n-2]+"."+dottedParts[n-1]);
                domainParts.put(DomainParts.DOMAINNAME, dottedParts[n-3]);
                
                StringBuilder sb = new StringBuilder(dottedParts[0]);
                for(int i=1;i<n-3;++i) {
                    sb.append(".").append(dottedParts[i]);
                }
                domainParts.put(DomainParts.SUBDOMAIN, sb.toString());
            }
            else if(commonTLDs.contains(dottedParts[n-1])) {
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
