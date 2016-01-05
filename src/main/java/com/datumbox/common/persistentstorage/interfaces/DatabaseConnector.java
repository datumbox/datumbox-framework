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
package com.datumbox.common.persistentstorage.interfaces;

import java.io.Serializable;
import java.util.Map;

/**
 * DB connectors that permanently store the parameters of the models should 
 * implement this interface. This interface defines the methods which are 
 * required to initialize db-backed collections. It is also responsible for 
 * connecting, clearing and managing the databases.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public interface DatabaseConnector {
    
    /**
     * The supported MapTypes.
     */
    public enum MapType {
        /**
         * HashMap Type.
         */
        HASHMAP,
        
        /**
         * TreeMap Type.
         */
        TREEMAP;
    }
    
    /**
     * A hint on where to ideally store the map. There are no guarantees over how
     * the DatabaseConnector will actually store the data, but some engines will 
     * use this info to keep as many important data in memory as possible.
     */
    public enum StorageHint {
        /**
         * This option will hint the storage engine that you wish to keep the 
         * map in memory.
         */
        IN_MEMORY,
        
        /**
         * This option will hint the storage engine that you wish to keep the 
         * map in disk.
         */
        IN_DISK;
    }
    
    /**
     * This method is responsible for storing serializable objects in the
     * database.
     * 
     * @param <T>
     * @param name
     * @param serializableObject 
     */
    public <T extends Serializable> void save(String name, T serializableObject);
    
    /**
     * Loads serializable objects from the database.
     * 
     * @param <T>
     * @param name
     * @param klass
     * @return 
     */
    public <T extends Serializable> T load(String name, Class<T> klass);
    
    /**
     * Closes the connection and clean ups the resources.
     */
    public void close();
    
    /**
     * Checks if the connector is closed.
     * 
     * @return 
     */
    public boolean isClosed();
    
    /**
     * Checks if a particular database exists.
     * 
     * @return 
     */
    public boolean existsDatabase();
    
    /**
     * Drops the particular database.
     */
    public void dropDatabase();
    
    /**
     * Creates or loads a Big Map which is capable of storing large number of 
     * records. 
     * 
     * @param <K>
     * @param <V>
     * @param name
     * @param type
     * @param storageHint
     * @param isTemporary
     * @return 
     */
    public <K,V> Map<K,V> getBigMap(String name, MapType type, StorageHint storageHint, boolean isTemporary);
    
    /**
     * Drops a particular Big Map.
     * 
     * @param <T>
     * @param name
     * @param map 
     */
    public <T extends Map> void dropBigMap(String name, T map);
    
}
