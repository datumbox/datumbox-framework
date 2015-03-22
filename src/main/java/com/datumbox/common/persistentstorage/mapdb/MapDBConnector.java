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
import java.util.concurrent.ConcurrentNavigableMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;


/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class MapDBConnector implements DatabaseConnector {
    
    private final Path filepath;
    private final DB db;
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
        
        //TODO: put LRU cache, see if we can add no-lock and no transactions
        db = DBMaker.newFileDB(filepath.toFile())
                //.transactionDisable()
                .compressionEnable()
                .cacheSize(this.dbConf.getCacheSize()) 
                .closeOnJvmShutdown()
                .make();
    }

    @Override
    public <KB extends KnowledgeBase> void save(KB knowledgeBaseObject) {
        //TODO: do not store the @BigMaps here. Find a way to exclude them. Should we make the fields transient? This affects the InMemory too.
        db.commit();
        db.compact();
        ConcurrentNavigableMap<Integer, KB> map = db.getTreeMap("KnowledgeBase");
        map.put(0, knowledgeBaseObject);
        db.commit();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <KB extends KnowledgeBase> KB load(Class<KB> klass) {
        ConcurrentNavigableMap<Integer, KB> map = db.getTreeMap("KnowledgeBase");
        KB knowledgeBaseObject = map.get(0);
        
        return knowledgeBaseObject;
    }
    
    @Override
    public boolean existsDatabase() {
        //TODO: this does not work correctly, see if an OR can help us detect if the db is there
        return Files.exists(filepath) && db.exists("KnowledgeBase");
    }
    
    @Override
    public void dropDatabase() {
        if(!existsDatabase()) {
            return;
        }
        
        try {
            db.close();
            //TODO: see how we can delete all the files
            Files.delete(filepath);
            Files.delete(Paths.get(filepath.toString()+".p"));
            Files.delete(Paths.get(filepath.toString()+".t"));
        } 
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public <T extends Map> void dropBigMap(String name, T map) {
        map.clear();
        db.delete(name);
    }
    
    @Override
    public <K,V> Map<K,V> getBigMap(String name) {
        return db.getHashMap(name);
    }   

}
