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

import com.datumbox.common.persistentstorage.abstracts.AbstractAutoCloseConnector;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;


/**
 * The MapDBConnector is responsible for saving and loading data from MapDB files,
 * creating BigMaps which are backed by files and persisting data. The MapDBConnector 
 * does not load all the contents of BigMaps in memory, maintains an LRU cache
 * to speed up data retrieval and persists all data in MapDB files.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MapDBConnector extends AbstractAutoCloseConnector {
    
    private final String database;
    private final MapDBConfiguration dbConf;
    
    /**
     * Enum class which stores the Database Type used for every collection.
     */
    private enum DBType {
        /**
         * Primary DB stores all the BigMaps maps and atomic variables which will 
         * be persisted after the connection closes; the DB maintains an separate 
         * LRU cache to speed up the operations.
         */
        PRIMARY_DB,
        
        /**
         * Temp DB cached is used to store temporary medium-sized BigMaps which 
         * will not be persisted after the connection closes; the DB maintains 
         * an separate LRU cache to speed up the operations.
         */
        TEMP_DB_CACHED,
        
        /**
         * Temp DB uncached is used to store temporary large-sized BigMaps which 
         * will not be persisted after the connection closes; the DB does not
         * maintain any cache.
         */
        TEMP_DB_UNCACHED;
    }
    
    /**
     * This list stores all the DB objects which are used to persist the data. This
     * library uses one default and one temporary db.
     */
    private final Map<DBType, DB> dbRegistry = new HashMap<>(); 
    
    /** 
     * @param database
     * @param dbConf
     * @see com.datumbox.common.persistentstorage.abstracts.AbstractAutoCloseConnector#AbstractAutoCloseConnector()   
     */
    protected MapDBConnector(String database, MapDBConfiguration dbConf) {  
        super();
        this.database = database;
        this.dbConf = dbConf;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Serializable> void saveObject(String name, T serializableObject) {
        assertConnectionOpen();
        DB db = openDB(DBType.PRIMARY_DB);
        Atomic.Var<T> atomicVar = db.getAtomicVar(name);
        atomicVar.set(serializableObject);
        db.commit();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Serializable> T loadObject(String name, Class<T> klass) {
        assertConnectionOpen();
        DB db = openDB(DBType.PRIMARY_DB);
        Atomic.Var<T> atomicVar = db.getAtomicVar(name);
        return klass.cast(atomicVar.get());
    }
    
    /** {@inheritDoc} */
    @Override
    public void close() {
        super.close();
        
        closeDBRegistry();
    }
    
    /** {@inheritDoc} */
    @Override
    public void clear() {
        assertConnectionOpen();
        
        closeDBRegistry();
        
        try {
            Path defaultPath = getDefaultPath();
            Files.deleteIfExists(defaultPath);
            Files.deleteIfExists(Paths.get(defaultPath.toString()+".p"));
            Files.deleteIfExists(Paths.get(defaultPath.toString()+".t"));
        } 
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public <K,V> Map<K,V> getBigMap(String name, MapType type, StorageHint storageHint, boolean isConcurrent, boolean isTemporary) {
        assertConnectionOpen();
        
        if(storageHint == StorageHint.IN_MEMORY && dbConf.isHybridized()) {
            //store in memory
            if(MapType.HASHMAP.equals(type)) {
                return isConcurrent?new ConcurrentHashMap<>():new HashMap<>();
            }
            else if(MapType.TREEMAP.equals(type)) {
                return isConcurrent?new ConcurrentSkipListMap<>():new TreeMap<>();
            }
            else {
                throw new IllegalArgumentException("Unsupported MapType.");
            }
        }
        else {
            //store in disk with optional LRU cache
            
            //first find if the particular collection exists and retrieve its dbType
            DBType dbType = getDatabaseTypeFromName(name);
            
            if(dbType == null) {
                //the map does not exist. Find where it should be created.
                if(isTemporary == false) {
                    dbType = DBType.PRIMARY_DB;
                }
                else {
                    if(storageHint == StorageHint.IN_MEMORY || storageHint == StorageHint.IN_CACHE) {
                        //we will use the LRU cache option
                        dbType = DBType.TEMP_DB_CACHED;
                    }
                    else if(storageHint == StorageHint.IN_DISK) {
                        //no cache at all
                        dbType = DBType.TEMP_DB_UNCACHED;
                    }
                    else {
                        throw new IllegalArgumentException("Unsupported StorageHint.");
                    }
                }
            }

            //ensure the DB is open 
            DB db = openDB(dbType);
            
            //return the appropriate type
            if(MapType.HASHMAP.equals(type)) {
                return db.createHashMap(name)
                .counterEnable()
                .makeOrGet();
            }
            else if(MapType.TREEMAP.equals(type)) {
                return db.createTreeMap(name)
                .valuesOutsideNodesEnable()
                .counterEnable()
                .makeOrGet();
            }
            else {
                throw new IllegalArgumentException("Unsupported MapType.");
            }
        }
    }   
    
    /** {@inheritDoc} */
    @Override
    public <T extends Map> void dropBigMap(String name, T map) {
        assertConnectionOpen();
        
        DBType dbType = getDatabaseTypeFromName(name);
        
        if(dbType != null) {
            DB db = dbRegistry.get(dbType);
            if(isOpenDB(db)) {
                db.delete(name);
            }
        }
        else {
            //The dbType can be null in two cases: a) the map was never created 
            //or b) it was stored in memory. In either case just clear the map.
            map.clear();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDatabaseName() {
        return database;
    }
    
    //private methods of connector class
    
    private boolean isOpenDB(DB db) {
        return !(db == null || db.isClosed());
    }
    
    /**
     * Opens the DB (if not already open) and returns the DB object.
     * 
     * @param dbType
     * @return 
     */
    private DB openDB(DBType dbType) {
        DB db = dbRegistry.get(dbType);
        if(!isOpenDB(db)) {
            DBMaker m;
            
            boolean permitCaching = true;
            if(dbType == DBType.PRIMARY_DB) {
                //main storage
                m = DBMaker.newFileDB(getDefaultPath().toFile());
            }
            else if(dbType == DBType.TEMP_DB_CACHED || dbType == DBType.TEMP_DB_UNCACHED) {
                //temporary storage
                m = DBMaker.newTempFileDB().deleteFilesAfterClose();
                
                if(dbType == DBType.TEMP_DB_UNCACHED) {
                    permitCaching = false;
                }
            }
            else {
                throw new IllegalArgumentException("Unsupported DatabaseType.");
            }
            
            if(dbConf.isCompressed()) {
                m = m.compressionEnable();
            }
            
            if(permitCaching && dbConf.getCacheSize()>0) {
                m = m.cacheLRUEnable().cacheSize(dbConf.getCacheSize()) ;
            }
            else {
                m = m.cacheDisable();
            }
            
            m = m.transactionDisable();
            m = m.asyncWriteEnable();
            m = m.closeOnJvmShutdown();
            
            db = m.make();
            dbRegistry.put(dbType, db);
        }
        return db;
    }
    
    /**
     * Returns the DatabaseType using the name of the map. It assumes that names 
     * are unique across all DatabaseType. If not found null is returned.
     * 
     * @param name
     * @return 
     */
    private DBType getDatabaseTypeFromName(String name) {
        for(Map.Entry<DBType, DB> entry : dbRegistry.entrySet()) {
            DB db = entry.getValue();
            if(isOpenDB(db) && db.exists(name)) {
                return entry.getKey();
            }
        }
        
        return null; //either the Map has not created yet OR it is in memory
    }
    
    /**
     * It closes all the DBs stored in the registry.
     */
    private void closeDBRegistry() {
        //close all dbs stored in dbRegistry
        for(DB db : dbRegistry.values()) {
            if(isOpenDB(db)) {
                db.close();
            }
        }
        dbRegistry.clear();
    }
    
    private Path getDefaultPath() {
        //get the default filepath of the permanet db file
        String outputFolder = this.dbConf.getOutputFolder();
        
        Path filepath;
        if(outputFolder == null || outputFolder.isEmpty()) {
            filepath= FileSystems.getDefault().getPath(database); //write them to the default accessible path
        }
        else {
            filepath= Paths.get(outputFolder + File.separator + database);
        }
        
        return filepath;
    }
}
