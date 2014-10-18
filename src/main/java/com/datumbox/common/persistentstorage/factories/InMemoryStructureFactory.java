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

import com.datumbox.common.utilities.DeepCopy;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.configuration.StorageConfiguration;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureContainer;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureContainerHolder;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class InMemoryStructureFactory implements BigDataStructureFactory {
    
    public enum MapType implements BigDataStructureFactory.MapType {
        CONCURRENT_HASH_MAP(true,true),
        HASH_MAP(true,false);

        private final boolean inMemory;
        private final boolean concurrent;
        
        private MapType(boolean inMemory, boolean concurrent) {
            this.inMemory = inMemory;
            this.concurrent = concurrent;
        }

        @Override
        public boolean isInMemory() {
            return inMemory;
        }

        @Override
        public boolean isConcurrent() {
            return concurrent;
        }
        
    }
    
    public enum CollectionType implements BigDataStructureFactory.CollectionType {
        CONCURRENT_LINKED_QUEUE(true,true),
        LINKED_LIST(true,false),
        SYNCHRONIZED_ARRAY_LIST(true,true),
        ARRAY_LIST(true,false);

        private final boolean inMemory;
        private final boolean concurrent;
        
        private CollectionType(boolean inMemory, boolean concurrent) {
            this.inMemory = inMemory;
            this.concurrent = concurrent;
        }

        @Override
        public boolean isInMemory() {
            return inMemory;
        }

        @Override
        public boolean isConcurrent() {
            return concurrent;
        }
    }
    
    public enum SetType implements BigDataStructureFactory.SetType {
        HASH_SET(true,false),
        CONCURRENT_HASH_SET(true,true);

        private final boolean inMemory;
        private final boolean concurrent;
        
        private SetType(boolean inMemory, boolean concurrent) {
            this.inMemory = inMemory;
            this.concurrent = concurrent;
        }

        @Override
        public boolean isInMemory() {
            return inMemory;
        }

        @Override
        public boolean isConcurrent() {
            return concurrent;
        }
    }
    
    public enum QueueType implements BigDataStructureFactory.QueueType {
        LINKED_LIST(true,false),
        CONCURRENT_LINKED_QUEUE(true,true);

        private final boolean inMemory;
        private final boolean concurrent;
        
        private QueueType(boolean inMemory, boolean concurrent) {
            this.inMemory = inMemory;
            this.concurrent = concurrent;
        }

        @Override
        public boolean isInMemory() {
            return inMemory;
        }

        @Override
        public boolean isConcurrent() {
            return concurrent;
        }
    }

    
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
        /*
        catch (NoSuchFileException | FileNotFoundException | AccessDeniedException ex) {
            //ignore if the file does not exist or can't be accesses
        }
        */
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public void clearDatabase() {
        if(!existsDatabase()) {
            return;
        }
        //we can't do anything better than delete the file
        dropDatabase();
    }
    
    @Override
    public <T extends Map> void dropTable(String collectionName, T map) {
        map.clear();
    }
    
    @Override
    public <T extends Collection> void dropTable(String collectionName, T anyCollection) {
        anyCollection.clear();
    }
    
    
    @Override
    public <K,V,T extends BigDataStructureFactory.MapType> Map<K,V> getMap(String collectionName, T mapType, int LRUsize) {
        Map<K,V> map;    
        
        if(mapType == MapType.CONCURRENT_HASH_MAP) {
            map = getConcurrentHashMap();
        }
        else if(mapType == MapType.HASH_MAP) {
            map = getHashMap();
        }
        else {
            //map = getHashMap();
            throw new IllegalArgumentException("Unsupported map");
        }    
        
        return map;
    }
    
    private <K,V> Map<K,V> getHashMap() { 
        return new HashMap<>();
    }
    
    private <K,V> Map<K,V> getConcurrentHashMap() { 
        return new ConcurrentHashMap<>();
    }
    
    @Override
    public <E,T extends BigDataStructureFactory.CollectionType> Collection<E> getCollection(String collectionName, T collectionType) {
        Collection<E> collection;
        if(collectionType == CollectionType.CONCURRENT_LINKED_QUEUE) {
            collection = getConcurrentLinkedQueueCollection();
        }
        else if(collectionType == CollectionType.LINKED_LIST) {
            collection = getLinkedListCollection();
        }
        else if(collectionType == CollectionType.SYNCHRONIZED_ARRAY_LIST) {
            collection = getSynchronizedArrayListCollection();
        }
        else if(collectionType == CollectionType.ARRAY_LIST) {
            collection = getArrayListCollection();
        }
        else {
            //collection = getArrayListCollection();
            throw new IllegalArgumentException("Unsupported collection");
        } 
        
        return collection;
    }
        
    private <E> Collection<E> getConcurrentLinkedQueueCollection() {
        return new ConcurrentLinkedQueue<>();
    }
       
    private <E> Collection<E> getLinkedListCollection() {
        return new LinkedList<>();
    }
       
    private <E> Collection<E> getSynchronizedArrayListCollection() {
        return Collections.<E>synchronizedList(new ArrayList<>());
    }
       
    private <E> Collection<E> getArrayListCollection() {
        return new ArrayList<>();
    }
    
    @Override
    public <E,T extends BigDataStructureFactory.SetType> Set<E> getSet(String collectionName, T setType) {
        Set<E> set;
        if(setType == SetType.CONCURRENT_HASH_SET) {
            set = getConcurrentHashSet();
        }
        else if(setType == SetType.HASH_SET) {
            set = getHashSet();
        }
        else {
            //set = getHashSet();
            throw new IllegalArgumentException("Unsupported set");
        } 
        
        return set;
    }
    
    private <E> Set<E> getConcurrentHashSet() {
        return Collections.<E>newSetFromMap(new ConcurrentHashMap<E, Boolean>());
    }
    
    private <E> Set<E> getHashSet() {
        return new HashSet<>();
    }
    
    @Override
    public <E,T extends BigDataStructureFactory.QueueType> Queue<E> getQueue(String collectionName, T queueType) {
        Queue<E> queue;
        if(queueType == QueueType.CONCURRENT_LINKED_QUEUE) {
            queue = getConcurrentLinkedQueue();
        }
        else if(queueType == QueueType.LINKED_LIST) {
            queue = getLinkedListQueue();
        }
        else {
            //queue = getLinkedListQueue();
            throw new IllegalArgumentException("Unsupported Queue");
        } 
        
        return queue;
    }
    
    private <E> Queue<E> getConcurrentLinkedQueue() {
        return new ConcurrentLinkedQueue<>();
    }
    
    private <E> Queue<E> getLinkedListQueue() {
        return new LinkedList<>();
    }

    @Override
    public void preSave(BigDataStructureContainer learnedParameters, MemoryConfiguration memoryConfiguration) {
        //no necessary pre save actions
    }

    @Override
    public void postLoad(BigDataStructureContainer learnedParameters, MemoryConfiguration memoryConfiguration) {
        //no necessary post load actions
    }
    

    @Override
    public void cleanUp() {
        //no necessary clean up actions
    }

    public static BigDataStructureFactory.MapType getDefaultMapType() {
        return MapType.HASH_MAP;
    }

    public static BigDataStructureFactory.CollectionType getDefaultCollectionType() {
        return CollectionType.LINKED_LIST;
    }

    public static BigDataStructureFactory.SetType getDefaultSetType() {
        return SetType.HASH_SET;
    }

    public static BigDataStructureFactory.QueueType getDefaultQueueType() {
        return QueueType.LINKED_LIST;
    }
    
    public static int getDefaultLRUsize() {
        return 10000;
    }

}
