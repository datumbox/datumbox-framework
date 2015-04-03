/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
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
            if (isTemporary) {
                //create a temporary DB
                db = DBMaker.newTempFileDB()
                            .deleteFilesAfterClose()
                            .transactionDisable()
                            .compressionEnable()
                            //.cacheDisable()
                            .cacheLRUEnable()
                            .cacheSize(this.dbConf.getCacheSize()) 
                            .asyncWriteEnable()
                            .closeOnJvmShutdown()
                            .make();
            }
            else {
                //create or open a permanent DB
                db = DBMaker.newFileDB(getDefaultPath().toFile())
                            .transactionDisable()
                            .compressionEnable()
                            //.cacheDisable()
                            .cacheLRUEnable()
                            .cacheSize(this.dbConf.getCacheSize()) 
                            .asyncWriteEnable()
                            .closeOnJvmShutdown()
                            .make();
            }
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
        String rootDbFolder = this.dbConf.getDbRootFolder();
        
        Path filepath = null;
        if(rootDbFolder.isEmpty()) {
            filepath= FileSystems.getDefault().getPath(database); //write them to the default accessible path
        }
        else {
            filepath= Paths.get(rootDbFolder + File.separator + database);
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
            //Files.deleteIfExists(Paths.get(getDefaultPath().toString()+".t"));
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
