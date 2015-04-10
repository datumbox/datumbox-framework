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
package com.datumbox;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Manifest;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Datumbox {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String[] versionInfo = getVersionBuild();
        System.out.println("Datumbox Machine Learning Framework "+versionInfo[0]+" "+versionInfo[1]);
    }
    
    private static String[] getVersionBuild() {
        String version;
        String build;
        
        URLClassLoader cl = (URLClassLoader) Datumbox.class.getClassLoader();
        URL url = cl.findResource("META-INF/MANIFEST.MF");
        try (InputStream in = url.openStream()) {
            Manifest manifest = new Manifest(in);
            version = manifest.getMainAttributes().getValue("Implementation-Version");
            build = manifest.getMainAttributes().getValue("Implementation-Build");
        } 
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
        return new String[]{version, build};
    }
}