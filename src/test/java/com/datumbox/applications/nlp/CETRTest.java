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
import com.datumbox.tests.bases.BaseTest;
import com.datumbox.tests.utilities.TestUtils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class CETRTest extends BaseTest {
    
    /**
     * Test of extract method, of class CETR.
     */
    @Test
    public void testExtract() {
        logger.info("extract");
         
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        String dbName = this.getClass().getSimpleName();
        
        String text = null;
        try {
            text = TestUtils.webRequest("http://www.example.org/");
        }
        catch(Exception ex) {
            logger.warn("Unable to download datasets, skipping test.");
            return;
        }
        
        CETR.Parameters parameters = new CETR.Parameters();
        parameters.setNumberOfClusters(2);
        parameters.setAlphaWindowSizeFor2DModel(3);
        parameters.setSmoothingAverageRadius(2);
        CETR instance = new CETR(dbName, dbConf);
        String expResult = "This domain is established to be used for illustrative examples in documents. You may use this domain in examples without prior coordination or asking for permission.";
        String result = instance.extract(text, parameters);
        assertEquals(expResult, result);
        instance=null;
    }
    
}
