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
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.ConfigurationFactory;
import com.datumbox.common.persistentstorage.inmemory.InMemoryConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.mapdb.MapDBConfiguration;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.configuration.TestConfiguration;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
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
                
                assertEquals(v1, v2, TestConfiguration.DOUBLE_ACCURACY_HIGH);
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

            assertEquals(v1, v2, TestConfiguration.DOUBLE_ACCURACY_HIGH);
        }
    }
    
    /**
     * Downloads a file from a remote URL, it stores it locally in a temporary
     * file and it returns its URI.
     * 
     * @param url
     * @return 
     */
    public static URI getRemoteFile(URL url) {
        try {
            File tmpFile = File.createTempFile("datumbox", ".tmp");
            tmpFile.deleteOnExit();
            URLConnection con = url.openConnection();
            con.setConnectTimeout(3000);
            con.setReadTimeout(5000);
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(con.getInputStream()));
                 DataOutputStream out = new DataOutputStream(new FileOutputStream(tmpFile))) {

                int n;
                byte[] buffer = new byte[4096];
                while ((n = in.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                }

                return tmpFile.toURI();
            } 
        } 
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Gets the contents of a URL and it returns it as a string.
     * 
     * @param urlStr
     * @return 
     */
    public static String webRequest(String urlStr) {
        StringBuilder sb = new StringBuilder(2048);
        try {
            URL url = new URL(urlStr);
            URLConnection con = url.openConnection();
            con.setConnectTimeout(3000);
            con.setReadTimeout(5000);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    sb.append(inputLine);
                }
            }
        } 
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } 
        
        return sb.toString();
    }

    /**
     * Initializes and returns the correct DatabaseConfiguration based on the
     * configuration.
     * 
     * @return 
     */
    public static DatabaseConfiguration getDBConfig() {
        String tmpFolder = System.getProperty("java.io.tmpdir");
        if (TestConfiguration.PERMANENT_STORAGE.equals(InMemoryConfiguration.class)) {
            InMemoryConfiguration dbConf = (InMemoryConfiguration) ConfigurationFactory.INMEMORY.getConfiguration();
            dbConf.setOutputFolder(tmpFolder);
            return dbConf;
        } 
        else if (TestConfiguration.PERMANENT_STORAGE.equals(MapDBConfiguration.class)) {
            MapDBConfiguration dbConf = (MapDBConfiguration) ConfigurationFactory.MAPDB.getConfiguration();
            dbConf.setOutputFolder(tmpFolder);
            dbConf.setHybridized(TestConfiguration.HYBRIDIZED_STORAGE);
            return dbConf;
        }
        return null;
    }
    
    /**
     * Generates a new Record from an Array.
     * 
     * @param <T>
     * @param xArray
     * @param y
     * @return 
     */
    public static <T> Record newDataVector(T[] xArray, Object y) {
        AssociativeArray x = new AssociativeArray();
        for(int i=0;i<xArray.length;++i) {
            x.put(i, xArray[i]);
        }
        return new Record(x, y);
    }
    
}
