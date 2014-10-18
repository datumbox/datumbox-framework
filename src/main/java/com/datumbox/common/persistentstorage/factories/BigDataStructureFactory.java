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

import com.datumbox.common.persistentstorage.interfaces.BigDataStructureContainer;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureContainerHolder;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.configuration.StorageConfiguration;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * DB Drivers which can be used to permanently store/handle the parameters of the
 * models should implement this interface. This interface defines the methods which
 * are used to initialize db-backed collections and objects used for storing the
 * data. It is also responsible for connecting, clearing and managing the dbs.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public interface BigDataStructureFactory {
    
    
    public static BigDataStructureFactory newInstance(String dbName) {
        //get from General StorageConfiguration the class that handles permanent storage
        try {
            Class<? extends BigDataStructureFactory> selectedBDSFClass = StorageConfiguration.getSelectedBDSFClass();
            return (BigDataStructureFactory) selectedBDSFClass.getConstructor(String.class).newInstance(dbName);
        } 
        catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }
        
    /**
     * Basic supported DataStructureType interface
     */
    public interface DataStructureType {
        public boolean isInMemory();
        public boolean isConcurrent();
    }
    
    //Supported maps
    public interface MapType extends DataStructureType {
        
    }
    
    //Supported collections
    public interface CollectionType extends DataStructureType {
        
    }
    
    //Supported set
    public interface SetType extends DataStructureType {
        
    }
    
    //Supported queue
    public interface QueueType extends DataStructureType {
        
    }

    public void preSave(BigDataStructureContainer learnedParameters, MemoryConfiguration memoryConfiguration);
    
    public <H extends BigDataStructureContainerHolder> void save(H holderObject);
    
    public <H extends BigDataStructureContainerHolder> H load(Class<H> klass);
    
    public void postLoad(BigDataStructureContainer learnedParameters, MemoryConfiguration memoryConfiguration);
    
    public boolean existsDatabase();
    
    public void dropDatabase();
    
    public void clearDatabase();
    
    public void cleanUp();
        
    public <T extends Map> void dropTable(String collectionName, T map);
    
    public <T extends Collection> void dropTable(String collectionName, T anyCollection);
    
    public <K,V,T extends BigDataStructureFactory.MapType> Map<K,V> getMap(String collectionName, T mapType, int LRUsize);
    
    public <E,T extends BigDataStructureFactory.CollectionType> Collection<E> getCollection(String collectionName, T collectionType);
    
    public <E,T extends BigDataStructureFactory.SetType> Set<E> getSet(String collectionName, T setType);
    
    public <E,T extends BigDataStructureFactory.QueueType> Queue<E> getQueue(String collectionName, T queueType);
    
    /*
    //can't be defined in the interface because they are static but they must be included in all the classes that implement the interface
    public static BigDataStructureFactory.MapType getDefaultMapType();
    
    public static BigDataStructureFactory.CollectionType getDefaultCollectionType();
    
    public static BigDataStructureFactory.SetType getDefaultSetType();
    
    public static BigDataStructureFactory.QueueType getDefaultQueueType();
    
    public static int getDefaultLRUsize();
    */
}
