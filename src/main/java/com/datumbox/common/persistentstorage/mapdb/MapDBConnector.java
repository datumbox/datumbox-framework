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

import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.datumbox.framework.machinelearning.common.dataobjects.KnowledgeBase;
import java.util.HashMap;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;


/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MapDBConnector implements DatabaseConnector {
    
    private static final String DEFAULT_DB = "DEFAULT";
    private static final String TEMP_DB = "TEMP";
    
    private final MapDBConfiguration dbConf;
    private final String database;
    
    /**
     * This list stores all the DB objects which are used to persist the data. This
     * library uses one default and one temporary db.
     */
    private final Map<String, DB> dbRegistry = new HashMap<>(); 
    
    public MapDBConnector(String database, MapDBConfiguration dbConf) {  
        this.dbConf = dbConf;
        this.database = database;
    }
    
    private boolean isOpenDB(DB db) {
        return !(db == null || db.isClosed());
    }
    
    private DB openDB(String dbName) {
        DB db = dbRegistry.get(dbName);
        if(!isOpenDB(db)) {
            boolean isTemporary = dbName.equals(TEMP_DB);
            DBMaker m = (isTemporary==true)?DBMaker.newTempFileDB().deleteFilesAfterClose():DBMaker.newFileDB(getDefaultPath().toFile());
            
            if(dbConf.getTransactions()==false) {
                m = m.transactionDisable();
            }
            
            if(dbConf.getCompression()) {
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
            dbRegistry.put(dbName, db);
        }
        return db;
    }
    
    private boolean existsInDB(DB db, String name) {
        return isOpenDB(db) && db.exists(name);
    }
    
    private void validateName(String name, boolean isTemporary) {
        DB db = dbRegistry.get(TEMP_DB);
        if (existsInDB(db, name)) {
            //try to find a map in temporary db with the same name
            throw new RuntimeException("A temporary map already exists with the same name.");
        }
        
        db = dbRegistry.get(DEFAULT_DB);
        if (isTemporary && existsInDB(db, name)) {
            //try to find if a permanent map exists and we want to declare a new temporary with the same name
            throw new RuntimeException("A BigMap already exists with the same name.");
        }
    }
    
    private Path getDefaultPath() {
        //get the default filepath of the permanet db file
        String outputFolder = this.dbConf.getOutputFolder();
        
        Path filepath = null;
        if(outputFolder.isEmpty()) {
            filepath= FileSystems.getDefault().getPath(database); //write them to the default accessible path
        }
        else {
            filepath= Paths.get(outputFolder + File.separator + database);
        }
        
        return filepath;
    }

    @Override
    public <KB extends KnowledgeBase> void save(KB knowledgeBaseObject) {
        openDB(DEFAULT_DB);
        DB db = dbRegistry.get(DEFAULT_DB);
        Atomic.Var<KB> knowledgeBaseVar = db.getAtomicVar("KnowledgeBase");
        knowledgeBaseVar.set(knowledgeBaseObject);
        db.commit();
        db.compact();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <KB extends KnowledgeBase> KB load(Class<KB> klass) {
        openDB(DEFAULT_DB);
        DB db = dbRegistry.get(DEFAULT_DB);
        Atomic.Var<KB> knowledgeBaseVar = db.getAtomicVar("KnowledgeBase");
        return knowledgeBaseVar.get();
    }
    
    @Override
    public boolean existsDatabase() {
        if(Files.exists(getDefaultPath())) {
            return true;
        }
        
        for(DB db: dbRegistry.values()) {
            if(db != null) {
                if(db.isClosed()) {
                    return true; //assume that the db existed before closing it. This is done in order to delete any remaining files
                }

                if(db.getCatalog().size()>0) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public void dropDatabase() {
        if(!existsDatabase()) {
            return;
        }
        
        try {
            //close all dbs stored in dbRegistry
            for(DB db : dbRegistry.values()) {
                if(isOpenDB(db)) {
                    db.close();
                }
            }
            dbRegistry.clear();
            Files.deleteIfExists(getDefaultPath());
            Files.deleteIfExists(Paths.get(getDefaultPath().toString()+".p"));
            Files.deleteIfExists(Paths.get(getDefaultPath().toString()+".t"));
        } 
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public <T extends Map> void dropBigMap(String name, T map) {
        boolean isTemporary = existsInDB(dbRegistry.get(TEMP_DB), name); 
        
        String dbName = isTemporary?TEMP_DB:DEFAULT_DB;
        
        DB db = dbRegistry.get(dbName);
        if(isOpenDB(db)) {
            db.delete(name);
        }
    }
    
    @Override
    public <K,V> Map<K,V> getBigMap(String name, boolean isTemporary) {
        validateName(name, isTemporary);
        
        String dbName = isTemporary?TEMP_DB:DEFAULT_DB;

        openDB(dbName);
        return dbRegistry.get(dbName).createHashMap(name)
            .counterEnable()
            .makeOrGet();
    }   

}
