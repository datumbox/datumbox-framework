/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
package com.datumbox.tests.utilities;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.DataTable2D;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.utilities.TypeConversions;
import com.datumbox.configuration.TestConfiguration;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import static org.junit.Assert.assertEquals;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class TestUtils {
    
    public static void assertDoubleDataTable2D(DataTable2D expResult, DataTable2D result) {
        for (Object key1 : result.keySet()) {
            for (Object key2 : result.get(key1).keySet()) {
                
                double v1 = TypeConversions.toDouble(expResult.get2d(key1, key2));
                double v2 = TypeConversions.toDouble(result.get2d(key1, key2));
                
                assertEquals(v1, v2, TestConfiguration.DOUBLE_ACCURACY_HIGH);
            }
        }
    }
    
    public static void assetDoubleAssociativeArray(AssociativeArray expResult, AssociativeArray result) {
        
        for (Object key : result.keySet()) {
            double v1 = expResult.getDouble(key);
            double v2 = result.getDouble(key);

            assertEquals(v1, v2, TestConfiguration.DOUBLE_ACCURACY_HIGH);
        }
    }
    
    public static URI getRemoteFile(URL url) {
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("datumbox", ".tmp");
        } 
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(url.openStream()));
             DataOutputStream out = new DataOutputStream(new FileOutputStream(tmpFile))) {
            
            int n;
            byte[] buffer = new byte[4096];
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            
            return tmpFile.toURI();
        } 
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static final synchronized void log(Class klass, String msg) {
        LoggerFactory.getLogger(klass).info(msg);
    }
}
