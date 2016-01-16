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
package com.datumbox.tests.utilities;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.DataTable2D;
import com.datumbox.common.persistentstorage.inmemory.InMemoryConfiguration;
import com.datumbox.common.Configuration;
import com.datumbox.common.persistentstorage.mapdb.MapDBConfiguration;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.tests.Constants;
import static org.junit.Assert.assertEquals;

/**
 * Utility methods used only by the JUnit tests.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class TestUtils {
    
    /**
     * Assert method for DataTable2D data structure which stores double values.
     * 
     * @param expResult
     * @param result 
     */
    public static void assertDoubleDataTable2D(DataTable2D expResult, DataTable2D result) {
        for (Object key1 : result.keySet()) {
            for (Object key2 : result.get(key1).keySet()) {
                
                double v1 = TypeInference.toDouble(expResult.get2d(key1, key2));
                double v2 = TypeInference.toDouble(result.get2d(key1, key2));
                
                assertEquals(v1, v2, Constants.DOUBLE_ACCURACY_HIGH);
            }
        }
    }
    
    /**
     * Assert method for AssociativeArray data structure which stores double values.
     * 
     * @param expResult
     * @param result 
     */
    public static void assetDoubleAssociativeArray(AssociativeArray expResult, AssociativeArray result) {
        
        for (Object key : result.keySet()) {
            double v1 = expResult.getDouble(key);
            double v2 = result.getDouble(key);

            assertEquals(v1, v2, Constants.DOUBLE_ACCURACY_HIGH);
        }
    }

    /**
     * Initializes and returns the correct DatabaseConfiguration based on the
     * configuration.
     * 
     * @return 
     */
    public static Configuration getConfig() {
        String tmpFolder = System.getProperty("java.io.tmpdir");
        Configuration conf = Configuration.getConfiguration();
        
        if (conf.getDbConfig().getClass().equals(InMemoryConfiguration.class)) {
            InMemoryConfiguration dbConf = (InMemoryConfiguration) conf.getDbConfig();
            dbConf.setOutputFolder(tmpFolder);
            conf.setDbConfig(dbConf);
        } 
        else if (conf.getDbConfig().getClass().equals(MapDBConfiguration.class)) {
            MapDBConfiguration dbConf = (MapDBConfiguration) conf.getDbConfig();
            dbConf.setOutputFolder(tmpFolder);
            conf.setDbConfig(dbConf);
        }
        
        return conf;
    }
    
}
