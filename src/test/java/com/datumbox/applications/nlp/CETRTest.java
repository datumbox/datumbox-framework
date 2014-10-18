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
package com.datumbox.applications.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class CETRTest {
    
    public CETRTest() {
    }

    public static String webRequest(String url) {
        StringBuilder sb = new StringBuilder();
        try {
            URL yahoo = new URL(url);
            URLConnection yc = yahoo.openConnection();
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    sb.append(inputLine);
                }
            }
        } 
        catch (IOException ex) {
            throw new RuntimeException(ex);
        } 
        
        return sb.toString();
    }
    
    /**
     * Test of extract method, of class CETR.
     */
    @Test
    public void testExtract() {
        System.out.println("extract");
        String text = CETRTest.webRequest("http://www.example.org/");
        CETR.Parameters parameters = new CETR.Parameters();
        parameters.setNumberOfClusters(2);
        parameters.setAlphaWindowSizeFor2DModel(3);
        parameters.setSmoothingAverageRadius(2);
        CETR instance = new CETR();
        String expResult = "This domain is established to be used for illustrative examples in documents. You may use this domain in examples without prior coordination or asking for permission.";
        String result = instance.extract(text, parameters);
        instance=null;
        assertEquals(expResult, result);
    }
    
}
