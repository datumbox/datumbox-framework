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
package com.datumbox.framework.common.persistentstorage.interfaces;

import java.io.Serializable;
import java.util.Map;

/**
 * DatabaseConnectors that give access to a permanent storage should 
 * implement this interface. The connector should open the connection on its
 * constructor and be responsible for managing and storing the data on the
 * permanent storage.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public interface DatabaseConnector extends AutoCloseable {
    
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
         * This option will hint the storage engine that you wish to keep part 
         * of the map in the cache and the rest in the disk.
         */
        IN_CACHE,
        
        /**
         * This option will hint the storage engine that you wish to keep the 
         * map in disk.
         */
        IN_DISK;
    }
    
    /**
     * Checks if the connector is closed.
     * 
     * @return 
     */
    public boolean isClosed();
    
    /**
     * Clears all the data stored in the database while keeping the connection open.
     */
    public void clear();
    
    /**
     * Stores a serializable object in the database.
     * 
     * @param <T>
     * @param name
     * @param serializableObject 
     */
    public <T extends Serializable> void saveObject(String name, T serializableObject);
    
    /**
     * Loads a serializable object from the database.
     * 
     * @param <T>
     * @param name
     * @param klass
     * @return 
     */
    public <T extends Serializable> T loadObject(String name, Class<T> klass);

    /**
     * Creates or loads a Big Map collection. 
     *
     * @param name
     * @param keyClass
     * @param valueClass
     * @param type
     * @param storageHint
     * @param isConcurrent
     * @param isTemporary
     * @param <K>
     * @param <V>
     * @return
     */
    public <K,V> Map<K,V> getBigMap(String name, Class<K> keyClass, Class<V> valueClass, MapType type, StorageHint storageHint, boolean isConcurrent, boolean isTemporary);
    
    /**
     * Drops the Big Map.
     * 
     * @param <T>
     * @param name
     * @param map 
     */
    public <T extends Map> void dropBigMap(String name, T map);
    
    /**
     * Returns the name of the database.
     * 
     * @return 
     */
    public String getDatabaseName();
}
