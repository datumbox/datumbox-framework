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
package com.datumbox.framework.common.storageengines.mapdb;

import com.datumbox.framework.common.storageengines.abstracts.AbstractFileStorageEngine;
import com.datumbox.framework.common.storageengines.abstracts.AbstractStorageEngine;
import com.datumbox.framework.common.storageengines.interfaces.StorageConfiguration;
import com.datumbox.framework.common.storageengines.interfaces.StorageEngine;
import org.mapdb.*;
import org.mapdb.serializer.GroupSerializer;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;


/**
 * The MapDBEngine is responsible for saving and loading data from MapDB files,
 * creating BigMaps which are backed by files and storing data. The MapDBEngine
 * does not load all the contents of BigMaps in memory, maintains an LRU cache
 * to speed up data retrieval and stores all data in MapDB files.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MapDBEngine extends AbstractFileStorageEngine<MapDBConfiguration> {
    
    /**
     * Enum class which stores the Storage Type used for every collection.
     */
    private enum StorageType {
        /**
         * Primary storage stores all the cached BigMaps and atomic variables which will
         * be stored after the connection closes. The storage maintains a separate
         * LRU cache to speed up the operations.
         */
        PRIMARY_STORAGE,

        /**
         * Secondary storage stores all the uncached BigMaps which will be stored
         * after the connection closes. The storage does not maintain any LRU cache.
         */
        SECONDARY_STORAGE,
        
        /**
         * Temp primary storage is a cached storage used to store temporary medium-sized
         * BigMaps which will not be stored after the connection closes. The storage
         * maintains an separate LRU cache to speed up the operations.
         */
        TEMP_PRIMARY_STORAGE,

        /**
         * Temp secondary storage is an uncached storage used to store temporary
         * large-sized BigMaps which will not be stored after the connection closes.
         * The storage does not maintain any LRU cache.
         */
        TEMP_SECONDARY_STORAGE;
    }
    
    /**
     * This list stores all the storage objects which are used to store the data. This
     * library uses one default and one temporary storage.
     */
    private final Map<StorageType, DB> storageRegistry = new HashMap<>();
    
    /** 
     * @param storageName
     * @param storageConfiguration
     * @see AbstractStorageEngine#AbstractStorageEngine(String, StorageConfiguration)
     */
    protected MapDBEngine(String storageName, MapDBConfiguration storageConfiguration) {
        super(storageName, storageConfiguration);
    }

    /** {@inheritDoc} */
    @Override
    public boolean rename(String newStorageName) {
        assertConnectionOpen();
        if(storageName.equals(newStorageName)) {
            return false;
        }

        safeStorageClose(StorageType.PRIMARY_STORAGE);
        safeStorageClose(StorageType.SECONDARY_STORAGE);

        try {
            moveDirectory(getRootPath(storageName), getRootPath(newStorageName));
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        logger.trace("Renamed storage {} to {}", storageName, newStorageName);
        storageName = newStorageName;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsObject(String name) {
        assertConnectionOpen();
        DB storage = openStorage(StorageType.PRIMARY_STORAGE);

        return storage.exists(name);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Serializable> void saveObject(String name, T serializableObject) {
        assertConnectionOpen();
        DB storage = openStorage(StorageType.PRIMARY_STORAGE);
        Atomic.Var<Object> atomicVar = storage.atomicVar(name).createOrOpen();

        Map<String, Object> objRefs = preSerializer(serializableObject);

        atomicVar.set(serializableObject);
        storage.commit();

        postSerializer(serializableObject, objRefs);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Serializable> T loadObject(String name, Class<T> klass) throws NoSuchElementException {
        assertConnectionOpen();

        if(!existsObject(name)) {
            throw new NoSuchElementException("Can't find any object with name '"+name+"'");
        }

        DB storage = openStorage(StorageType.PRIMARY_STORAGE);
        Atomic.Var<Object> atomicVar = storage.atomicVar(name).createOrOpen();
        T serializableObject = klass.cast(atomicVar.get());

        postDeserializer(serializableObject);

        return serializableObject;
    }
    
    /** {@inheritDoc} */
    @Override
    public void close() {
        if(isClosed()){
            return;
        }
        super.close();
        
        closeStorageRegistry();
        logger.trace("Closed storage {}", storageName);
    }
    
    /** {@inheritDoc} */
    @Override
    public void clear() {
        assertConnectionOpen();
        
        closeStorageRegistry();
        
        try {
            deleteDirectory(getRootPath(storageName), true);
        } 
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public <K,V> Map<K,V> getBigMap(String name, Class<K> keyClass, Class<V> valueClass, StorageEngine.MapType type, StorageEngine.StorageHint storageHint, boolean isConcurrent, boolean isTemporary) {
        assertConnectionOpen();
        
        if(storageHint == StorageEngine.StorageHint.IN_MEMORY && storageConfiguration.isHybridized()) {
            //store in memory
            if(StorageEngine.MapType.HASHMAP.equals(type)) {
                return isConcurrent?new ConcurrentHashMap<>():new HashMap<>();
            }
            else if(StorageEngine.MapType.TREEMAP.equals(type)) {
                return isConcurrent?new ConcurrentSkipListMap<>():new TreeMap<>();
            }
            else {
                throw new IllegalArgumentException("Unsupported MapType.");
            }
        }
        else {
            //store in disk with optional LRU cache
            
            //first find if the particular collection exists and retrieve its storageType
            StorageType storageType = getStorageTypeFromName(name);
            
            if(storageType == null) {
                //the map does not exist. Find where it should be created.
                if(isTemporary == false) {
                    if(storageHint == StorageEngine.StorageHint.IN_MEMORY || storageHint == StorageEngine.StorageHint.IN_CACHE) {
                        //we will use the LRU cache option
                        storageType = StorageType.PRIMARY_STORAGE;
                    }
                    else if(storageHint == StorageEngine.StorageHint.IN_DISK) {
                        //no cache at all
                        storageType = StorageType.SECONDARY_STORAGE;
                    }
                    else {
                        throw new IllegalArgumentException("Unsupported StorageHint.");
                    }
                }
                else {
                    if(storageHint == StorageEngine.StorageHint.IN_MEMORY || storageHint == StorageEngine.StorageHint.IN_CACHE) {
                        //we will use the LRU cache option
                        storageType = StorageType.TEMP_PRIMARY_STORAGE;
                    }
                    else if(storageHint == StorageEngine.StorageHint.IN_DISK) {
                        //no cache at all
                        storageType = StorageType.TEMP_SECONDARY_STORAGE;
                    }
                    else {
                        throw new IllegalArgumentException("Unsupported StorageHint.");
                    }
                }
            }

            //ensure the storage is open
            DB storage = openStorage(storageType);
            
            //return the appropriate type
            Map<K,V> map;
            if(StorageEngine.MapType.HASHMAP.equals(type)) {
                map = (Map<K,V>)storage.hashMap(name)
                .counterEnable()
                .keySerializer(getSerializerFromClass(keyClass))
                .valueSerializer(getSerializerFromClass(valueClass))
                .createOrOpen();
            }
            else if(StorageEngine.MapType.TREEMAP.equals(type)) {
                map = (Map<K,V>)storage.treeMap(name)
                .valuesOutsideNodesEnable()
                .counterEnable()
                .keySerializer(getSerializerFromClass(keyClass))
                .valueSerializer(getSerializerFromClass(valueClass))
                .createOrOpen();
            }
            else {
                throw new IllegalArgumentException("Unsupported MapType.");
            }
            return map;
        }
    }   
    
    /** {@inheritDoc} */
    @Override
    public <T extends Map> void dropBigMap(String name, T map) {
        assertConnectionOpen();
        
        StorageType storageType = getStorageTypeFromName(name);
        
        if(storageType != null) {
            DB storage = storageRegistry.get(storageType);
            if(isOpenStorage(storage)) {
                storage.delete(name);
            }
        }
        else {
            //The storageType can be null in two cases: a) the map was never created
            //or b) it was stored in memory. In either case just clear the map.
            map.clear();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected Set<Class> nonSerializableBigMaps() {
        return new HashSet<>(Arrays.asList(HTreeMap.class, BTreeMap.class));
    }

    //private methods of storage engine class

    /**
     * Returns the appropriate Serializer (if one exists) else ELSA.
     *
     * @param klass
     * @return
     */
    private GroupSerializer<?> getSerializerFromClass(Class<?> klass) {
        if(klass == Integer.class) {
            return Serializer.INTEGER;
        }
        else if(klass == Long.class) {
            return Serializer.LONG;
        }
        else if(klass == Boolean.class) {
            return Serializer.BOOLEAN;
        }
        else if(klass == Double.class) {
            return Serializer.DOUBLE;
        }
        else if(klass == String.class) {
            return Serializer.STRING;
        }
        else if(klass == Byte.class) {
            return Serializer.BYTE;
        }
        else if(klass == Short.class) {
            return Serializer.SHORT;
        }
        else if(klass == Character.class) {
            return Serializer.CHAR;
        }
        else if(klass == Float.class) {
            return Serializer.FLOAT;
        }
        return Serializer.ELSA; //Default POJO serializer
    }

    private boolean isOpenStorage(DB storage) {
        return !(storage == null || storage.isClosed());
    }
    
    /**
     * Opens the storage (if not already open) and returns the storage object.
     * 
     * @param storageType
     * @return 
     */
    private DB openStorage(StorageType storageType) {
        DB storage = storageRegistry.get(storageType);
        if(!isOpenStorage(storage)) {
            DBMaker.Maker m;
            if(storageType == StorageType.PRIMARY_STORAGE || storageType == StorageType.SECONDARY_STORAGE) {
                //main storage
                Path rootPath = getRootPath(storageName);
                try {
                    createDirectoryIfNotExists(rootPath);
                }
                catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }

                m = DBMaker.fileDB(new File(rootPath.toFile(), storageType.toString()));
            }
            else if(storageType == StorageType.TEMP_PRIMARY_STORAGE || storageType == StorageType.TEMP_SECONDARY_STORAGE) {
                //temporary storage
                m = DBMaker.tempFileDB().fileDeleteAfterClose();
            }
            else {
                throw new IllegalArgumentException("Unsupported StorageType.");
            }
            
            if(storageConfiguration.isCompressed()) {
                //m = m.compressionEnable(); //TODO: restore once implemented
            }

            boolean permitCaching = storageType == StorageType.PRIMARY_STORAGE || storageType == StorageType.TEMP_PRIMARY_STORAGE;
            if(permitCaching && storageConfiguration.getCacheSize()>0) {
                //m = m.cacheLRUEnable().cacheSize(storageConfiguration.getCacheSize()); //TODO: restore once implemented
            }

            if(storageConfiguration.isAsynchronous()) {
                //m = m.asyncWriteEnable(); //TODO: restore once implemented
            }
            
            m = m.closeOnJvmShutdown();
            
            storage = m.make();
            storageRegistry.put(storageType, storage);
        }
        return storage;
    }
    
    /**
     * Returns the StorageType using the name of the map. It assumes that names
     * are unique across all StorageType. If not found null is returned.
     * 
     * @param name
     * @return 
     */
    private StorageType getStorageTypeFromName(String name) {
        for(Map.Entry<StorageType, DB> entry : storageRegistry.entrySet()) {
            DB storage = entry.getValue();
            if(isOpenStorage(storage) && storage.exists(name)) {
                return entry.getKey();
            }
        }
        
        return null; //either the Map has not created yet OR it is in memory
    }
    
    /**
     * It closes all the storageengines in the registry.
     */
    private void closeStorageRegistry() {
        for(DB storage : storageRegistry.values()) {
            if(isOpenStorage(storage)) {
                storage.close();
            }
        }
        storageRegistry.clear();
    }

    /**
     * Commits all changes and closes the provided storage. It should be used when we move the storage to a different
     * location. Returns true if the storage needed to be closed and false if it was not necessary.
     *
     * @param storageType
     * @return
     */
    private boolean safeStorageClose(StorageType storageType) {
        DB storage = storageRegistry.get(storageType);
        if(isOpenStorage(storage)) {
            storage.commit();

            storage.close();

            return true;
        }
        else {
            return false;
        }
    }

}
