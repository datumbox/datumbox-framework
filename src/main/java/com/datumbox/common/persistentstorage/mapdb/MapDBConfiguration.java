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
package com.datumbox.common.persistentstorage.mapdb;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import java.util.Properties;

/**
 * The MapDBConfiguration class is used to configure the MapDB persistence
 * storage and generate new storage connections. MapDB storage uses collections 
 * which are backed by file and thus it does not load all the data in memory. 
 * The data are persisted in MapDB files.
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

    private boolean hybridized = false;
    
    /**
     * Default Constructor.
     */
    public MapDBConfiguration() {
        
    }
    
    /** {@inheritDoc} */
    @Override
    public DatabaseConnector getConnector(String database) {
        return new MapDBConnector(database, this);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDBnameSeparator() {
        return DBNAME_SEPARATOR;
    }
    
    /** {@inheritDoc} */
    @Override
    public void load(Properties properties) {
        outputFolder = properties.getProperty("dbConfig.MapDBConfiguration.outputFolder");
        cacheSize = Integer.parseInt(properties.getProperty("dbConfig.MapDBConfiguration.cacheSize"));
        compressed = "true".equalsIgnoreCase(properties.getProperty("dbConfig.MapDBConfiguration.compressed"));
        hybridized = "true".equalsIgnoreCase(properties.getProperty("dbConfig.MapDBConfiguration.hybridized"));
    }

    /**
     * Getter for the output folder where the MapDB data files are stored.
     * 
     * @return 
     */
    public String getOutputFolder() {
        return outputFolder;
    }

    /**
     * Setter for the output folder where the MapDB data files are stored.
     * 
     * @param outputFolder 
     */
    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }
    
    /**
     * Getter for the size of items stored in the LRU cache by MapDB.
     * 
     * @return 
     */
    public int getCacheSize() {
        return cacheSize;
    }
    
    /**
     * Setter for the size of items stored in LRU cache by MapDB. Set it to 0 to
     * turn off caching.
     * 
     * @param cacheSize 
     */
    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    /**
     * Getter for the compression option.
     * 
     * @return 
     */
    public boolean isCompressed() {
        return compressed;
    }
    
    /**
     * Setter for the compression option. If turned on the records will be compressed.
     * It is turned on by default.
     * 
     * @param compressed 
     */
    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }
    
    /**
     * Getter for the Hybridized option. 
     * 
     * @return 
     */
    public boolean isHybridized() {
        return hybridized;
    }
    
    /**
     * Setter for the Hybridized option. If turned on, it will take into 
     * account the storage hints provided during Map initialization and it will 
     * move more objects out of MapDB and into main Memory. This will lead to
     * significantly improved speed but also higher memory utilization.
     * 
     * @param hybridized 
     */
    public void setHybridized(boolean hybridized) {
        this.hybridized = hybridized;
    }
    
}
