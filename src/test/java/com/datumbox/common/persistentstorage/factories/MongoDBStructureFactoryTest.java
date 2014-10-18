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

import com.datumbox.common.persistentstorage.factories.MongoDBStructureFactory;
import com.datumbox.common.utilities.RandomValue;
import com.datumbox.configuration.StorageConfiguration;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class MongoDBStructureFactoryTest {
    
    public MongoDBStructureFactoryTest() {
    }

    /**
     * Test of getCachingMap method, of class MongoDBStructureFactory.
     */
    @Test
    public void testGetCachingMap() {
        System.out.println("getCachingMap");
        if(StorageConfiguration.PERMANENT_STORAGE==StorageConfiguration.InMemory.class) {
            return;
        }
        RandomValue.randomGenerator = new Random(42);
        
        //caching map
        String dbName = "JUnitTestingDB";
        int maxIterations=5;
        MongoDBStructureFactory bdsf = new MongoDBStructureFactory(dbName);
        
        Map<String, Double[]> cachingMap = bdsf.getMap("string2DoubleArrayMap", MongoDBStructureFactory.MapType.MONGODB_CACHING_MAP, 100);
        
        for(int i =0;i<maxIterations;++i) {
            Double[] x = new Double[100];
            for(int j=0;j<x.length;++j) {
                x[j] = RandomValue.randomGenerator.nextDouble();
            }
            
            cachingMap.put("key"+i, x);
        }
        
        assertEquals(maxIterations, cachingMap.size());
        
        bdsf.dropDatabase();
    }

    /**
     * Test of getCachingConcurrentMap method, of class MongoDBStructureFactory.
     */
    @Test
    public void testGetCachingConcurrentMap() {
        System.out.println("getCachingConcurrentMap");
        if(StorageConfiguration.PERMANENT_STORAGE==StorageConfiguration.InMemory.class) {
            return;
        }
        RandomValue.randomGenerator = new Random(42);
        
        //caching concurrent map
        String dbName = "JUnitTestingDB";
        int maxIterations=5;
        MongoDBStructureFactory bdsf = new MongoDBStructureFactory(dbName);
        
        Map<String, Double[]> getCachingConcurrentMap = bdsf.getMap("String2DoubleArrayMap", MongoDBStructureFactory.MapType.MONGODB_CACHING_CONCURRENT_MAP, 100);
        
        for(int i =0;i<maxIterations;++i) {
            Double[] x = new Double[100];
            for(int j=0;j<x.length;++j) {
                x[j] = RandomValue.randomGenerator.nextDouble();
            }
            
            getCachingConcurrentMap.put("value"+i,x);
        }
        assertEquals(maxIterations, getCachingConcurrentMap.size());
        
        bdsf.dropDatabase();
    }

    /**
     * Test of getMongoConcurrentMap method, of class MongoDBStructureFactory.
     */
    @Test
    public void testGetMongoConcurrentMap() {
        System.out.println("getMongoConcurrentMap");
        if(StorageConfiguration.PERMANENT_STORAGE==StorageConfiguration.InMemory.class) {
            return;
        }
        
        //mongo concurrent map
        String dbName = "JUnitTestingDB";
        int maxIterations=5;
        MongoDBStructureFactory bdsf = new MongoDBStructureFactory(dbName);
        
        Map<String, String> getMongoConcurrentMap = bdsf.getMap("String2StringMap", MongoDBStructureFactory.MapType.MONGODB_UNCACHED_CONCURRENT_MAP, 0);
        
        for(int i =0;i<maxIterations;++i) {
            getMongoConcurrentMap.put("key"+i, "value"+i);
        }
        
        assertEquals(maxIterations, getMongoConcurrentMap.size());
        
        bdsf.dropDatabase();
    }

    /**
     * Test of getMongoDBCollection method, of class MongoDBStructureFactory.
     */
    @Test
    public void testGetCollection() {
        System.out.println("getCollection");
        if(StorageConfiguration.PERMANENT_STORAGE==StorageConfiguration.InMemory.class) {
            return;
        }
        //mongo collection
        String dbName = "JUnitTestingDB";
        int maxIterations=5;
        MongoDBStructureFactory bdsf = new MongoDBStructureFactory(dbName);
        
        Collection<String> getMongoCollection = bdsf.getCollection("StringCollection", MongoDBStructureFactory.CollectionType.MONGODB_COLLECTION);
        
        for(int i =0;i<maxIterations;++i) {
            getMongoCollection.add("value"+i);
        }
        
        assertEquals(maxIterations, getMongoCollection.size());
        
        bdsf.dropDatabase();
    }

    /**
     * Test of getMongoDBSet method, of class MongoDBStructureFactory.
     */
    @Test
    public void testGetSet() {
        System.out.println("getSet");
        if(StorageConfiguration.PERMANENT_STORAGE==StorageConfiguration.InMemory.class) {
            return;
        }
        //mongo set
        String dbName = "JUnitTestingDB";
        int maxIterations=5;
        MongoDBStructureFactory bdsf = new MongoDBStructureFactory(dbName);
        
        Set<String> getMongoSet = bdsf.getSet("StringSet", MongoDBStructureFactory.SetType.MONGODB_SET);
        
        for(int i =0;i<maxIterations;++i) {
            getMongoSet.add("value"+i);
        }
        
        assertEquals(maxIterations, getMongoSet.size());
        
        bdsf.dropDatabase();
    }

    /**
     * Test of getMongoDBQueue method, of class MongoDBStructureFactory.
     */
    @Test
    public void testGetQueue() {
        System.out.println("getQueue");
        if(StorageConfiguration.PERMANENT_STORAGE==StorageConfiguration.InMemory.class) {
            return;
        }
        //mongo queue
        String dbName = "JUnitTestingDB";
        int maxIterations=5;
        MongoDBStructureFactory bdsf = new MongoDBStructureFactory(dbName);
        
        Queue<String> getMongoQueue = bdsf.getQueue("StringQueue", MongoDBStructureFactory.QueueType.MONGODB_QUEUE);
        
        for(int i =0;i<maxIterations;++i) {
            getMongoQueue.add("value"+i);
        }
        
        assertEquals(maxIterations, getMongoQueue.size());
        
        bdsf.dropDatabase();
    }

}
