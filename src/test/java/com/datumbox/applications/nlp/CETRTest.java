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
package com.datumbox.applications.nlp;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.RandomValue;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.tests.utilities.TestUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
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
        TestUtils.log(this.getClass(), "extract");
        RandomValue.setRandomGenerator(new Random(TestConfiguration.RANDOM_SEED)); 
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        String dbName = "JUnitClusterer";
        
        String text = null;
        try {
            text = CETRTest.webRequest("http://www.example.org/");
        }
        catch(Exception ex) {
            TestUtils.log(this.getClass(), "Unable to download datasets, skipping test.");
            return;
        }
        
        CETR.Parameters parameters = new CETR.Parameters();
        parameters.setNumberOfClusters(2);
        parameters.setAlphaWindowSizeFor2DModel(3);
        parameters.setSmoothingAverageRadius(2);
        CETR instance = new CETR(dbName, dbConf);
        String expResult = "This domain is established to be used for illustrative examples in documents. You may use this domain in examples without prior coordination or asking for permission.";
        String result = instance.extract(text, parameters);
        instance=null;
        assertEquals(expResult, result);
    }
    
}
