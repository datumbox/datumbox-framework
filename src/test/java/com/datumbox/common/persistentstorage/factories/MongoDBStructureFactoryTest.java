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
        
        Map<String, Double[]> cachingMap = bdsf.getMap("string2DoubleArrayMap");
        
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

}
