/* 
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
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;


/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class MapDBConnector implements DatabaseConnector {
    
    private final Path filepath;
    private DB db;
    private final MapDBConfiguration dbConf;
    
    public MapDBConnector(String database, MapDBConfiguration dbConf) {  
        this.dbConf = dbConf;
        String rootDbFolder = this.dbConf.getDbRootFolder();
        
        if(rootDbFolder.isEmpty()) {
            filepath= FileSystems.getDefault().getPath(database); //write them to the default accessible path
        }
        else {
            filepath= Paths.get(rootDbFolder + File.separator + database);
        }
        
        openDB();
    }
    
    private boolean isDBopen() {
        return !(db == null || db.isClosed());
    }
    
    private void openDB() {
        if(!isDBopen()) {
            db = DBMaker.newFileDB(filepath.toFile())
                    .transactionDisable()
                    .compressionEnable()
                    .cacheLRUEnable()
                    .cacheSize(this.dbConf.getCacheSize()) 
                    .asyncWriteEnable()
                    .closeOnJvmShutdown()
                    .make();
        }
    }

    @Override
    public <KB extends KnowledgeBase> void save(KB knowledgeBaseObject) {
        //TODO: do not store the @BigMaps here. Find a way to exclude them. Should we make the fields transient? This affects the InMemory too.
        openDB();
        Atomic.Var<KB> knowledgeBaseVar = db.getAtomicVar("KnowledgeBase");
        knowledgeBaseVar.set(knowledgeBaseObject);
        db.commit();
        db.compact();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <KB extends KnowledgeBase> KB load(Class<KB> klass) {
        openDB();
        Atomic.Var<KB> knowledgeBaseVar = db.getAtomicVar("KnowledgeBase");
        return knowledgeBaseVar.get();
    }
    
    @Override
    public boolean existsDatabase() {
        if(Files.exists(filepath) || db == null) {
            return true;
        }
        
        if(db.isClosed()) {
            return true; //assume that the db existed before closing it. This is done in order to delete any remaining files
        }
        
        if(db.getCatalog().size()>0) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public void dropDatabase() {
        if(!existsDatabase()) {
            return;
        }
        
        try {
            if(isDBopen()) {
                db.close();
            }
            Files.deleteIfExists(filepath);
            Files.deleteIfExists(Paths.get(filepath.toString()+".p"));
            //Files.deleteIfExists(Paths.get(filepath.toString()+".t"));
        } 
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public <T extends Map> void dropBigMap(String name, T map) {
        map.clear();
        if(isDBopen()) {
            db.delete(name);
        }
    }
    
    @Override
    public <K,V> Map<K,V> getBigMap(String name) {
        openDB();
        return db.createHashMap(name)
            .counterEnable()
            .makeOrGet();
    }   

}
