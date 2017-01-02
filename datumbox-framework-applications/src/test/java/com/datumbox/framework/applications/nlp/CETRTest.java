/**
 * Copyright (C) 2013-2017 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.applications.nlp;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for CETR.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class CETRTest extends AbstractTest {
    
    /**
     * Test of extract method, of class CETR.
     */
    @Test
    public void testExtract() {
        logger.info("extract");
         
        Configuration configuration = Configuration.getConfiguration();
        
        String text;        
        try {
            List<String> lines = Files.readAllLines(Paths.get(this.getClass().getClassLoader().getResource("datasets/example.com.html").toURI()), StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder();
            for(String line: lines){
                sb.append(line);
                sb.append("\r\n");
            }
            text = sb.toString().trim();
        }
        catch(IOException | URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        
        CETR.Parameters parameters = new CETR.Parameters();
        parameters.setNumberOfClusters(2);
        parameters.setAlphaWindowSizeFor2DModel(3);
        parameters.setSmoothingAverageRadius(2);
        CETR instance = new CETR(configuration);
        String expResult = "This domain is established to be used for illustrative examples in documents. You may use this domain in examples without prior coordination or asking for permission.";
        String result = instance.extract(text, parameters);
        assertEquals(expResult, result);
    }
    
}
