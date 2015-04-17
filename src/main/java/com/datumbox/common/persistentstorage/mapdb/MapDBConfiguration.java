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
package com.datumbox.common.persistentstorage.mapdb;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import java.util.Properties;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MapDBConfiguration implements DatabaseConfiguration {
    
    //Mandatory constants
    private static final String DBNAME_SEPARATOR = "_"; //NOT permitted characters are: <>:"/\|?*

    //DB specific properties
    private String outputFolder = "./";
    
    private int cacheSize = 10000;
    
    private boolean compressed = true;
    
    private boolean transacted = false;

    @Override
    public DatabaseConnector getConnector(String database) {
        return new MapDBConnector(database, this);
    }

    @Override
    public String getDBnameSeparator() {
        return DBNAME_SEPARATOR;
    }

    @Override
    public void load(Properties properties) {
        outputFolder = properties.getProperty("dbConfig.MapDBConfiguration.outputFolder");
        cacheSize = Integer.valueOf(properties.getProperty("dbConfig.MapDBConfiguration.cacheSize"));
        compressed = "true".equals(properties.getProperty("dbConfig.MapDBConfiguration.compressed").toLowerCase());
        transacted = "true".equals(properties.getProperty("dbConfig.MapDBConfiguration.transacted").toLowerCase());
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    public boolean isTransacted() {
        return transacted;
    }

    public void setTransacted(boolean transacted) {
        this.transacted = transacted;
    }
    
    
}
