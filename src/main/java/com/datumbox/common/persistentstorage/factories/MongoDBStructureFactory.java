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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.LRUMap;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;

import com.datumbox.common.loggers.MorphiaNullLoggerFactory;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureContainer;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureContainerHolder;
import com.datumbox.configuration.GeneralConfiguration;
import com.datumbox.configuration.StorageConfiguration;
import com.github.mongoutils.collections.CachingMap;
import com.github.mongoutils.collections.DBObjectSerializer;
import com.github.mongoutils.collections.MongoConcurrentMap;
import com.github.mongoutils.collections.SimpleFieldDBObjectSerializer;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class MongoDBStructureFactory implements BigDataStructureFactory {
    //In Memory MongoDB: http://edgystuff.tumblr.com/post/49304254688/how-to-use-mongodb-as-a-pure-in-memory-db-redis-style
     
    static {
        if(!GeneralConfiguration.DEBUG) {
            MorphiaLoggerFactory.registerLogger( MorphiaNullLoggerFactory.class );
        }
    }
    
    private static MongoClient connection;
    
    private final String dbName;
    private final DB db;
    
    public MongoDBStructureFactory(String database) {       
        dbName = database;
        
        if(connection==null) {
            connection = new MongoClient(StorageConfiguration.MongoDB.SERVER_LIST, StorageConfiguration.MongoDB.CREDENTIAL_LIST);
        }
        db = connection.getDB(database);
    } 

    /**
     * Opens a Datastore on MongoDBDB with Morphia.
     * 
     * @return 
     */
    private Datastore getMorphiaDS(Class<? extends BigDataStructureContainerHolder> klass) {
        Morphia morphia = new Morphia();
        //Mapper mapper = morphia.getMapper();
        //mapper.getOptions().objectFactory = new MongoDBStructureFactory.CustomMorphiaObjectFactory();
        morphia.map(klass);
        return morphia.createDatastore(connection, dbName);
    }   
    
    @Override
    public <H extends BigDataStructureContainerHolder> void save(H holderObject) {
        getMorphiaDS(holderObject.getClass()).save(holderObject);
    }
    
    @Override
    public <H extends BigDataStructureContainerHolder> H load(Class<H> klass) {
        H holderObject = (H)getMorphiaDS(klass).find(klass).get();
        
        for(Field field : getAllFields(new LinkedList<>(), holderObject.getClass())){

            Class<?> fieldClass = field.getType();
            field.setAccessible(true);
            
            try {
            	Object value = field.get(holderObject);
	            if(value!=null && BigDataStructureContainer.class.isAssignableFrom(value.getClass())) {
	                ((BigDataStructureContainer) value).bigDataStructureInitializer(this); 
	            }
            } 
            catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        return holderObject;
    }


    
    @Override
    public boolean existsDatabase() {
        return connection.getDatabaseNames().contains(db.getName());
    }
    
    @Override
    public void dropDatabase() {
        if(!existsDatabase()) {
            return;
        }
        db.dropDatabase();
    }
    
    @Override
    public <T extends Map> void dropMap(String collectionName, T map) {
        db.getCollection(collectionName).drop();
        map.clear();
    }    
    
    
    
    
    @Override
    public <K,V> Map<K,V> getMap(String collectionName) {
        return getCachingMap(collectionName);
    }
    
    private <K,V> Map<K,V> getCachingMap(String collectionName) {
        Map<K, V> backstore = getMongoConcurrentMap(collectionName);
        // max. in memory
        Map<K, V> cache = new LRUMap<>(StorageConfiguration.MongoDB.LRUsize);
        Map<K, V> mongoMap = new CachingMap<>(cache, (MongoConcurrentMap<K, V>) backstore);
        
        return mongoMap;
    }
    /*
    private <K,V> Map<K,V> getCachingConcurrentMap(String collectionName) {
        Map<K, V> backstore = getMongoConcurrentMap(collectionName);
        // max. in memory
        Map<K, V> cache = new LRUMap<>(StorageConfiguration.MongoDB.LRUsize);
        Map<K, V> mongoMap = new CachingConcurrentMap<>( new ConcurrentHashMap<>(cache), (MongoConcurrentMap<K, V>) backstore);
        
        return mongoMap;
    }
    */
    private <K,V> Map<K,V> getMongoConcurrentMap(String collectionName) {
        DBCollection collection = db.getCollection(collectionName);
        // the serializers for mapping DBObjects to String and vice versa
        DBObjectSerializer<K> keySerializer = new SimpleFieldDBObjectSerializer<>("k");
        DBObjectSerializer<V> valueSerializer = new SimpleFieldDBObjectSerializer<>("v");
        // will produce documents like "{'k':...,'v':...,'_id':ObjectID(...)}"
        Map<K, V> mongoMap = new MongoConcurrentMap<>(collection, keySerializer, valueSerializer);
        
        //collection.createIndex(new BasicDBObject("k", 1)); //create an index on k
        if(StorageConfiguration.MongoDB.USE_HASH_INDEXES_IN_MAPS) {
            collection.createIndex(new BasicDBObject("k", "hashed")); //hash index
        }
        else {
            //uniques are not supported for lists
            //collection.createIndex(new BasicDBObject("k", 1), new BasicDBObject("unique", true)); //unique btree index
            collection.createIndex(new BasicDBObject("k", 1));
        }
                
        return mongoMap;
    }
    
    
    
    
    
    private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }
    

}
