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

import com.datumbox.common.persistentstorage.AutoCloseConnector;
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
public class MapDBConnector extends AutoCloseConnector {
    
    /**
     * Enum class which stores the Database Type used for every collection.
     */
    private enum DatabaseType {
        DEFAULT_DB,
        TEMP_DB;
    }
    
    private final MapDBConfiguration dbConf;
    private final String database;
    
    /**
     * This list stores all the DB objects which are used to persist the data. This
     * library uses one default and one temporary db.
     */
    private final Map<DatabaseType, DB> dbRegistry = new HashMap<>(); 
    
    /**
     * Non-public constructor used by MapDBConfiguration class to generate
     * new connections.
     * 
     * @param database
     * @param dbConf 
     */
    protected MapDBConnector(String database, MapDBConfiguration dbConf) {  
        super();
        this.dbConf = dbConf;
        this.database = database;
    }

    /**
     * This method is responsible for storing serializable objects in the
     * database.
     * 
     * @param <T>
     * @param name
     * @param serializableObject 
     */
    @Override
    public <T extends Serializable> void save(String name, T serializableObject) {
        ensureNotClosed();
        openDB(DatabaseType.DEFAULT_DB);
        DB db = dbRegistry.get(DatabaseType.DEFAULT_DB);
        Atomic.Var<T> knowledgeBaseVar = db.getAtomicVar(name);
        knowledgeBaseVar.set(serializableObject);
        db.commit();
        db.compact();
    }

    /**
     * Loads serializable objects from the database.
     * 
     * @param <T>
     * @param name
     * @param klass
     * @return 
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Serializable> T load(String name, Class<T> klass) {
        ensureNotClosed();
        openDB(DatabaseType.DEFAULT_DB);
        DB db = dbRegistry.get(DatabaseType.DEFAULT_DB);
        Atomic.Var<T> atomicVar = db.getAtomicVar(name);
        return atomicVar.get();
    }
    
    /**
     * Closes the connection and clean ups the resources.
     */
    @Override
    public void close() {
        if(isClosed()){
            return; 
        }
        super.close();
        closeAllDBs();
    }
    
    /**
     * Checks if a particular database exists.
     * 
     * @return 
     */
    @Override
    public boolean existsDatabase() {
        ensureNotClosed();
        if(Files.exists(getDefaultPath())) {
            return true;
        }
        
        for(DB db: dbRegistry.values()) {
            if(db != null) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Drops the particular database.
     */
    @Override
    public void dropDatabase() {
        ensureNotClosed();
        if(!existsDatabase()) {
            return;
        }
        
        closeAllDBs();
            
        try {
            dbRegistry.clear();
            Files.deleteIfExists(getDefaultPath());
            Files.deleteIfExists(Paths.get(getDefaultPath().toString()+".p"));
            Files.deleteIfExists(Paths.get(getDefaultPath().toString()+".t"));
        } 
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    
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
    @Override
    public <K,V> Map<K,V> getBigMap(String name, MapType type, StorageHint storageHint, boolean isTemporary) {
        ensureNotClosed();
        validateName(name, isTemporary);
        
        DatabaseType dbType = isTemporary?DatabaseType.TEMP_DB:DatabaseType.DEFAULT_DB;

        openDB(dbType);
        
        boolean permitInMemory = StorageHint.IN_MEMORY.equals(storageHint) && dbConf.isHybridized();
        
        if(MapType.HASHMAP.equals(type)) {
            if(permitInMemory) {
                return new HashMap<>();
            }
            else {
                return dbRegistry.get(dbType).createHashMap(name)
                .counterEnable()
                .makeOrGet();
            }
        }
        else if(MapType.TREEMAP.equals(type)) {
            if(permitInMemory) {
                return new TreeMap<>();
            }
            else {
                return dbRegistry.get(dbType).createTreeMap(name)
                .counterEnable()
                .makeOrGet();
            }
        }
        else {
            throw new IllegalArgumentException("Unsupported MapType.");
        }
    }   
    
    /**
     * Drops a particular Big Map.
     * 
     * @param <T>
     * @param name
     * @param map 
     */
    @Override
    public <T extends Map> void dropBigMap(String name, T map) {
        ensureNotClosed();
        boolean isTemporary = existsInDB(dbRegistry.get(DatabaseType.TEMP_DB), name); 
        
        DatabaseType dbType = isTemporary?DatabaseType.TEMP_DB:DatabaseType.DEFAULT_DB;
        
        DB db = dbRegistry.get(dbType);
        if(isOpenDB(db)) {
            db.delete(name);
        }
    }
    
    //private methods of connector class
    
    private void closeAllDBs() {
        //close all dbs stored in dbRegistry
        for(DB db : dbRegistry.values()) {
            if(isOpenDB(db)) {
                db.close();
            }
        }
    }
    
    private boolean isOpenDB(DB db) {
        return !(db == null || db.isClosed());
    }
    
    private DB openDB(DatabaseType dbType) {
        DB db = dbRegistry.get(dbType);
        if(!isOpenDB(db)) {
            boolean isTemporary = dbType==DatabaseType.TEMP_DB;
            DBMaker m = (isTemporary==true)?DBMaker.newTempFileDB().deleteFilesAfterClose():DBMaker.newFileDB(getDefaultPath().toFile());
            
            if(dbConf.isTransacted()==false) {
                m = m.transactionDisable();
            }
            
            if(dbConf.isCompressed()) {
                m = m.compressionEnable();
            }
            
            if(dbConf.getCacheSize()>0) {
                m = m.cacheLRUEnable().cacheSize(dbConf.getCacheSize()) ;
            }
            else {
                m = m.cacheDisable();
            }
            
            m = m.asyncWriteEnable();
            m = m.closeOnJvmShutdown();
            
            db = m.make();
            dbRegistry.put(dbType, db);
        }
        return db;
    }
    
    private boolean existsInDB(DB db, String name) {
        return isOpenDB(db) && db.exists(name);
    }
    
    private void validateName(String name, boolean isTemporary) {
        DB db = dbRegistry.get(DatabaseType.TEMP_DB);
        if (existsInDB(db, name)) {
            //try to find a map in temporary db with the same name
            throw new IllegalArgumentException("A temporary map already exists with the same name.");
        }
        
        db = dbRegistry.get(DatabaseType.DEFAULT_DB);
        if (isTemporary && existsInDB(db, name)) {
            //try to find if a permanent map exists and we want to declare a new temporary with the same name
            throw new IllegalArgumentException("A BigMap already exists with the same name.");
        }
    }
    
    private Path getDefaultPath() {
        //get the default filepath of the permanet db file
        String outputFolder = this.dbConf.getOutputFolder();
        
        Path filepath = null;
        if(outputFolder == null || outputFolder.isEmpty()) {
            filepath= FileSystems.getDefault().getPath(database); //write them to the default accessible path
        }
        else {
            filepath= Paths.get(outputFolder + File.separator + database);
        }
        
        return filepath;
    }
}
