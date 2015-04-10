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
package com.datumbox.common.persistentstorage.inmemory;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.datumbox.common.utilities.DeepCopy;
import com.datumbox.framework.machinelearning.common.dataobjects.KnowledgeBase;
import java.util.HashMap;


/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class InMemoryConnector implements DatabaseConnector {
        
    private final Path filepath;
    private final InMemoryConfiguration dbConf;
    
    public InMemoryConnector(String database, InMemoryConfiguration dbConf) {  
        this.dbConf = dbConf;
        String outputFolder = this.dbConf.getOutputFolder();
        if(outputFolder.isEmpty()) {
            filepath= FileSystems.getDefault().getPath(database); //write them to the default accessible path
        }
        else {
            filepath= Paths.get(outputFolder + File.separator + database);
        }
    }

    @Override
    public <KB extends KnowledgeBase> void save(KB knowledgeBaseObject) {
        try { 
            Files.write(filepath, DeepCopy.serialize(knowledgeBaseObject));
        } 
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <KB extends KnowledgeBase> KB load(Class<KB> klass) {
        try { 
            //read the stored serialized object
            KB knowledgeBaseObject = (KB)DeepCopy.deserialize(Files.readAllBytes(filepath));
            return knowledgeBaseObject;
        } 
        catch (NoSuchFileException ex) {
            return null;
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public boolean existsDatabase() {
        return Files.exists(filepath);
    }
    
    @Override
    public void dropDatabase() {
        if(!existsDatabase()) {
            return;
        }
        
        try {
            Files.deleteIfExists(filepath);
        } 
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public <T extends Map> void dropBigMap(String name, T map) {
        map.clear();
    }
    
    @Override
    public <K,V> Map<K,V> getBigMap(String name, boolean isTemporary) {
        return new HashMap<>();
    }   

}
