/* 
 * Copyright (C) 2014 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
package com.datumbox.common.persistentstorage.factories;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.datumbox.common.persistentstorage.interfaces.BigDataStructureContainerHolder;
import com.datumbox.common.utilities.DeepCopy;
import com.datumbox.configuration.StorageConfiguration;


/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class InMemoryStructureFactory implements BigDataStructureFactory {
        
    private final Path filepath;
    
    public InMemoryStructureFactory(String database) {       
        if(StorageConfiguration.InMemory.DB_ROOT_FOLDER.isEmpty()) {
            filepath= FileSystems.getDefault().getPath(database); //write them to the default accessible path
        }
        else {
            filepath= Paths.get(StorageConfiguration.InMemory.DB_ROOT_FOLDER + File.separator + database);
        }
    }

    @Override
    public <H extends BigDataStructureContainerHolder> void save(H holderObject) {
        try { 
            Files.write(filepath, DeepCopy.serialize(holderObject));
        } 
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <H extends BigDataStructureContainerHolder> H load(Class<H> klass) {
        try { 
            //read the stored serialized object
            H holderObject = (H)DeepCopy.deserialize(Files.readAllBytes(filepath));
            return holderObject;
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
            Files.delete(filepath);
        } 
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public <T extends Map> void dropMap(String collectionName, T map) {
        map.clear();
    }
    
    @Override
    public <K,V> Map<K,V> getMap(String collectionName) {
        return new HashMap<>();
    }   

}
