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
package com.datumbox.framework.common.persistentstorage.inmemory;

import com.datumbox.framework.common.persistentstorage.abstracts.AbstractDatabaseConnector;
import com.datumbox.framework.common.persistentstorage.abstracts.AbstractFileDBConnector;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.framework.common.utilities.DeepCopy;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;


/**
 * The InMemoryConnector is responsible for saving and loading data in memory,
 * creating BigMaps and persisting data. The InMemoryConnector loads all the
 * data in memory and persists all data in serialized files.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class InMemoryConnector extends AbstractFileDBConnector<InMemoryConfiguration> {

    /**
     * The catalog stores weak references to all the items that are stored in the db (objects and big maps).
     */
    private Map<String, WeakReference<?>> catalog = new HashMap<>();

    /** 
     * @param dbName
     * @param dbConf
     * @see AbstractDatabaseConnector#AbstractDatabaseConnector(String, DatabaseConfiguration)
     */
    protected InMemoryConnector(String dbName, InMemoryConfiguration dbConf) {
        super(dbName, dbConf);
    }

    /** {@inheritDoc} */
    @Override
    public boolean rename(String newDBName) {
        assertConnectionOpen();
        if(dbName.equals(newDBName)) {
            return false;
        }

        catalog.clear();

        try {
            moveDirectory(getRootPath(dbName), getRootPath(newDBName));
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        logger.trace("Renamed db {} to {}", dbName, newDBName);
        dbName = newDBName;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsObject(String name) {
        assertConnectionOpen();
        if(catalog.containsKey(name)) {
            return true;
        }
        return new File(getRootPath(dbName).toFile(), name).exists();
    }
    
    /** {@inheritDoc} */
    @Override
    public <T extends Serializable> void saveObject(String name, T serializableObject) {
        assertConnectionOpen();
        try { 
            Path rootPath = getRootPath(dbName);
            createDirectoryIfNotExists(rootPath);

            Path objectPath = new File(rootPath.toFile(), name).toPath();
            Files.write(objectPath, DeepCopy.serialize(serializableObject));
        } 
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        catalog.put(name, new WeakReference<>(serializableObject));
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Serializable> T loadObject(String name, Class<T> klass) throws NoSuchElementException {
        assertConnectionOpen();

        if(!existsObject(name)) {
            throw new NoSuchElementException("Can't find any object with name '"+name+"'");
        }

        T obj;
        try {
            Path objectPath = new File(getRootPath(dbName).toFile(), name).toPath();
            Object serializableObject = DeepCopy.deserialize(Files.readAllBytes(objectPath));
            obj = klass.cast(serializableObject);
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        catalog.put(name, new WeakReference<>(obj));
        return obj;
    }
    
    /** {@inheritDoc} */
    @Override
    public void close() {
        if(isClosed()){
            return; 
        }
        super.close();
        catalog = null;
        logger.trace("Closed db {}", dbName);
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        assertConnectionOpen();
        catalog.clear();
        try {
            deleteDirectory(getRootPath(dbName), true);
        } 
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public <K,V> Map<K,V> getBigMap(String name, Class<K> keyClass, Class<V> valueClass, MapType type, StorageHint storageHint, boolean isConcurrent, boolean isTemporary) {
        assertConnectionOpen();

        Map<K,V> m;
        if(MapType.HASHMAP.equals(type)) {
            m = isConcurrent?new ConcurrentHashMap<>():new HashMap<>();
        }
        else if(MapType.TREEMAP.equals(type)) {
            m = isConcurrent?new ConcurrentSkipListMap<>():new TreeMap<>();
        }
        else {
            throw new IllegalArgumentException("Unsupported MapType.");
        }

        catalog.put(name, new WeakReference<>(m));
        return m;
    }  
    
    /** {@inheritDoc} */
    @Override
    public <T extends Map> void dropBigMap(String name, T map) {
        assertConnectionOpen();
        map.clear();
        catalog.remove(name);
    }

}
