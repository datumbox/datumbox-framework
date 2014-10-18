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

import com.datumbox.configuration.StorageConfiguration;
import com.datumbox.common.loggers.MorphiaNullLoggerFactory;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureMarker;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureContainer;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureContainerHolder;
import com.datumbox.configuration.GeneralConfiguration;
import com.github.mongoutils.collections.CachingConcurrentMap;
import com.github.mongoutils.collections.CachingMap;
import com.github.mongoutils.collections.DBObjectSerializer;
import com.github.mongoutils.collections.MongoCollection;
import com.github.mongoutils.collections.MongoConcurrentMap;
import com.github.mongoutils.collections.MongoQueue;
import com.github.mongoutils.collections.MongoSet;
import com.github.mongoutils.collections.SimpleFieldDBObjectSerializer;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.collections4.map.LRUMap;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class MongoDBStructureFactory implements BigDataStructureFactory {
    //In Memory MongoDB: http://edgystuff.tumblr.com/post/49304254688/how-to-use-mongodb-as-a-pure-in-memory-db-redis-style
    
    private static final String SYSTEM_COLLECTION_PREFIX = "system.";
    
    public enum MapType implements BigDataStructureFactory.MapType {
        MONGODB_CACHING_MAP(false,false),
        MONGODB_CACHING_CONCURRENT_MAP(false,true), 
        MONGODB_UNCACHED_CONCURRENT_MAP(false,true);

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
        MONGODB_COLLECTION(false,true);

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
        MONGODB_SET(false,true);

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
        MONGODB_QUEUE(false,true);

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
    public void clearDatabase() {
        if(!existsDatabase()) {
            return;
        }
        //remove all collections
        for(String collectionName : db.getCollectionNames()) {
            if(!collectionName.startsWith(SYSTEM_COLLECTION_PREFIX)) {
                db.getCollection(collectionName).drop();
            }
        }
    }
    
    @Override
    public <T extends Map> void dropTable(String collectionName, T map) {
        db.getCollection(collectionName).drop();
        map.clear();
    }
    
    @Override
    public <T extends Collection> void dropTable(String collectionName, T anyCollection) {
        db.getCollection(collectionName).drop();
        anyCollection.clear();
    }
    
    
    
    
    
    @Override
    public <K,V,T extends BigDataStructureFactory.MapType> Map<K,V> getMap(String collectionName, T mapType, int LRUsize) {
        Map<K,V> map;    
        
        if(mapType == MapType.MONGODB_CACHING_CONCURRENT_MAP) {
            map = getCachingConcurrentMap(collectionName, LRUsize);
        }
        else if(mapType == MapType.MONGODB_CACHING_MAP) {
            map = getCachingMap(collectionName, LRUsize);
        }
        else if(mapType == MapType.MONGODB_UNCACHED_CONCURRENT_MAP) {
            map = getMongoConcurrentMap(collectionName);
        }
        else {
            //if you can't spot it on mongodb types try the InMemory ones
            return new InMemoryStructureFactory(dbName).getMap(collectionName, mapType, LRUsize);
        }    
        
        return map;
    }
    
    private <K,V> Map<K,V> getCachingMap(String collectionName, int LRUMaxSize) {
        Map<K, V> backstore = getMongoConcurrentMap(collectionName);
        // max. in memory
        Map<K, V> cache = new LRUMap<>(LRUMaxSize);
        Map<K, V> mongoMap = new CachingMap<>(cache, (MongoConcurrentMap<K, V>) backstore);
        
        return mongoMap;
    }
    
    private <K,V> Map<K,V> getCachingConcurrentMap(String collectionName, int LRUMaxSize) {
        Map<K, V> backstore = getMongoConcurrentMap(collectionName);
        // max. in memory
        Map<K, V> cache = new LRUMap<>(LRUMaxSize);
        Map<K, V> mongoMap = new CachingConcurrentMap<>( new ConcurrentHashMap<>(cache), (MongoConcurrentMap<K, V>) backstore);
        
        return mongoMap;
    }
    
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
    
    @Override
    public <E,T extends BigDataStructureFactory.CollectionType> Collection<E> getCollection(String collectionName, T collectionType) {
        Collection<E> collection;
        if(collectionType == CollectionType.MONGODB_COLLECTION) {
            collection = getMongoDBCollection(collectionName);
        }
        else {
            //if you can't spot it on mongodb types try the InMemory ones
            return new InMemoryStructureFactory(dbName).getCollection(collectionName, collectionType);
        } 
        
        return collection;
    }
    
    private <E> Collection<E> getMongoDBCollection(String collectionName) {
        DBCollection collection = db.getCollection(collectionName);
        DBObjectSerializer<E> serializer = new SimpleFieldDBObjectSerializer<>("v");

        Collection<E> mongoCollection = new MongoCollection<>(collection, serializer);
        
        return mongoCollection;
    }
    
    @Override
    public <E,T extends BigDataStructureFactory.SetType> Set<E> getSet(String collectionName, T setType) {
        Set<E> set;
        if(setType == SetType.MONGODB_SET) {
            set = getMongoDBSet(collectionName);
        }
        else {
            //if you can't spot it on mongodb types try the InMemory ones
            return new InMemoryStructureFactory(dbName).getSet(collectionName, setType);
        } 
        
        return set;
    }
    
    private <E> Set<E> getMongoDBSet(String collectionName) {
        DBCollection collection = db.getCollection(collectionName);
        DBObjectSerializer<E> serializer = new SimpleFieldDBObjectSerializer<>("v");

        Set<E> mongoSet = new MongoSet<>(collection, serializer);
        
        return mongoSet;
    }
    
    @Override
    public <E,T extends BigDataStructureFactory.QueueType> Queue<E> getQueue(String collectionName, T queueType) {
        Queue<E> queue;
        if(queueType == QueueType.MONGODB_QUEUE) {
            queue = getMongoDBQueue(collectionName);
        }
        else {
            //if you can't spot it on mongodb types try the InMemory ones
            return new InMemoryStructureFactory(dbName).getQueue(collectionName, queueType);
        } 
        
        return queue;
    }
    
    private <E> Queue<E> getMongoDBQueue(String collectionName) {
        DBCollection collection = db.getCollection(collectionName);
        DBObjectSerializer<E> serializer = new SimpleFieldDBObjectSerializer<>("v");

        Queue<E> mongoQueue = new MongoQueue<>(collection, serializer);
        
        return mongoQueue;
    }

    @Override
    public void preSave(BigDataStructureContainer learnedParameters, MemoryConfiguration memoryConfiguration) {
       
        boolean usesInMemoryStructures=
                memoryConfiguration.getMapType().isInMemory() ||
                memoryConfiguration.getSetType().isInMemory() ||
                memoryConfiguration.getQueueType().isInMemory() ||
                memoryConfiguration.getCollectionType().isInMemory();
        
        
        //If in-memory structures are used to speed up the execution of the algorithm, then
        //those fields are marked as Transient and thus they will not be stored
        //by Morphia. To avoid losing this information, we check to find the fields
        //of ModelParameter object and we try to spote the fields that are marked
        //as Transient and BigDataStructureMarker (custom annotation). If such a field is found
        //we add its contents in the database in a collection named as the name
        //of the field.
        if(usesInMemoryStructures) {
            Queue<BigDataStructureContainer> learnableObjects = new LinkedList<>();
            Set<BigDataStructureContainer> alreadyChecked = new HashSet<>(); //This set uses the default equals() which means that it compares memory addresses. This behavior is desired
            
            learnableObjects.add(learnedParameters);
            
            
            while(learnableObjects.size()>0) {
                //get the next object from the queue
                BigDataStructureContainer obj = learnableObjects.poll();
                
                //mark it as examined
                alreadyChecked.add(obj);
                

                //get all the fields from all the inherited classes
                for(Field field : getAllFields(new LinkedList<>(), obj.getClass())){
                    handleBigDataStructureField(field, obj, memoryConfiguration);

                    Class<?> fieldClass = field.getType();
                    //if this object can be learned and is not already checked add it in the Queue
                    if(BigDataStructureContainer.class.isAssignableFrom(fieldClass)) {
                        field.setAccessible(true);
                        BigDataStructureContainer fieldValue;
                        try {
                            fieldValue = (BigDataStructureContainer) field.get(obj);
                        } 
                        catch (IllegalArgumentException | IllegalAccessException ex) {
                            throw new RuntimeException(ex);
                        }
                        
                        if(!alreadyChecked.contains(fieldValue)) {
                            learnableObjects.add(fieldValue);
                        }
                    }
                }
            }
            
        }
    }
    
    private void handleBigDataStructureField(Field field, BigDataStructureContainer learnableClass, MemoryConfiguration memoryConfiguration) {
        //check if the field is annotated as BigDataStructureMarker and if it is marked as Transient
        if(field.getAnnotationsByType(BigDataStructureMarker.class).length>0 && field.getAnnotationsByType(Transient.class).length>0) {                    
            field.setAccessible(true);

            //WARNING! DO NOT CHANGE THE ORDER OF IFs
            if(memoryConfiguration.getMapType().isInMemory() && Map.class.isAssignableFrom(field.getType())) {
                try {
                    //try
                         this //select our database
                         .getMap(field.getName(), getDefaultMapType(), getDefaultLRUsize()) //open a Map on DB with name equal to the field
                         .putAll(
                                 (Map<? extends Object, ? extends Object>) field.get(learnableClass) //put inside all the contents of the field. We read the contents with reflection
                         );
                } 
                catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new IllegalArgumentException("Could not store the collection in database. Try chaning the configuration to DB-backed collections.");
                }
            }
            else if( memoryConfiguration.getSetType().isInMemory() && Set.class.isAssignableFrom(field.getType())) {
                try {
                    //try
                         this //select our database
                         .getSet(field.getName(), getDefaultSetType()) //open a Set on DB with name equal to the field
                         .addAll(
                                 (Set<? extends Object>) field.get(learnableClass) //put inside all the contents of the field. We read the contents with reflection
                         );
                } 
                catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new IllegalArgumentException("Could not store the collection in database. Try chaning the configuration to DB-backed collections.");
                }
            }
            else if(memoryConfiguration.getQueueType().isInMemory() && Queue.class.isAssignableFrom(field.getType())) {
                try {
                    //try
                         this //select our database
                         .getQueue(field.getName(), getDefaultQueueType()) //open a Queue on DB with name equal to the field
                         .addAll(
                                 (Queue<? extends Object>) field.get(learnableClass) //put inside all the contents of the field. We read the contents with reflection
                         );
                } 
                catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new IllegalArgumentException("Could not store the collection in database. Try chaning the configuration to DB-backed collections.");
                }
            }
            else if(memoryConfiguration.getCollectionType().isInMemory() && Collection.class.isAssignableFrom(field.getType())) {
                try {
                    //try
                         this //select our database
                         .getCollection(field.getName(), getDefaultCollectionType()) //open a Collection on DB with name equal to the field
                         .addAll(
                                 (Collection<? extends Object>) field.get(learnableClass) //put inside all the contents of the field. We read the contents with reflection
                         );
                } 
                catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new IllegalArgumentException("Could not store the collection in database. Try chaning the configuration to DB-backed collections.");
                }
            }
            else {
                //no need to save it on the DB because it is not in memory or a type that can be handled
            }
        }
    }
    

    @Override
    public void postLoad(BigDataStructureContainer learnedParameters, MemoryConfiguration memoryConfiguration) {
        //InMemory DataStructureTypes can be used to speed up training. This point of load() 
        //method is reached when the classifier calls a method that requires data and the 
        //data are not already loaded. 
        //
        //Loading the data from the DB to a map during methods like test() or predict() is 
        //slow and a waste of resources. Usually LRU caching is more appropriate.
        //Thus using hashmaps during test() or predict() is disallowed if the
        //data are not already in there.
        //
        //Below if the MapType is set to HashMap we switch it to the default map.
        //We do the same with the other DataStructures
        if(memoryConfiguration.getMapType().isInMemory()) {
            memoryConfiguration.setMapType(getDefaultMapType());
            memoryConfiguration.setLRUsize(getDefaultLRUsize());
        }
        if(memoryConfiguration.getCollectionType().isInMemory()) {
            memoryConfiguration.setCollectionType(getDefaultCollectionType());
        }
        if(memoryConfiguration.getSetType().isInMemory()) {
            memoryConfiguration.setSetType(getDefaultSetType());
        }
        if(memoryConfiguration.getQueueType().isInMemory()) {
            memoryConfiguration.setQueueType(getDefaultQueueType());
        }

        
        Queue<BigDataStructureContainer> learnableObjects = new LinkedList<>();
        Set<BigDataStructureContainer> alreadyChecked = new HashSet<>(); //This set uses the default equals() which means that it compares memory addresses. This behavior is desired

        learnableObjects.add(learnedParameters);


        while(learnableObjects.size()>0) {
            //get the next object from the queue
            BigDataStructureContainer obj = learnableObjects.poll();

            //mark it as examined
            alreadyChecked.add(obj);

            //reinitialize the big data structures to load the data from the mongodb collections
            obj.bigDataStructureInitializer(this, memoryConfiguration); 

            //get all the fields from all the inherited classes
            for(Field field : getAllFields(new LinkedList<>(), obj.getClass())){

                Class<?> fieldClass = field.getType();
                //if this object can be learned and is not already checked add it in the Queue
                if(BigDataStructureContainer.class.isAssignableFrom(fieldClass)) {
                    field.setAccessible(true);
                    BigDataStructureContainer fieldValue;
                    try {
                        fieldValue = (BigDataStructureContainer) field.get(obj);
                    } 
                    catch (IllegalArgumentException | IllegalAccessException ex) {
                        throw new RuntimeException(ex);
                    }

                    if(fieldValue!=null && !alreadyChecked.contains(fieldValue)) {
                        learnableObjects.add(fieldValue);
                    }
                }
            }
        }
        
        
    }
    

    @Override
    public void cleanUp() {
        String tmpPrefix=StorageConfiguration.getTmpPrefix();
        //remove all collections starting with TMP_ if we forgot to do it already
        int remainingCollections = 0;
        for(String collectionName : db.getCollectionNames()) {
            if(collectionName.startsWith(tmpPrefix)) {
                db.getCollection(collectionName).drop();
            }
            else if(!collectionName.startsWith(SYSTEM_COLLECTION_PREFIX)) {
                ++remainingCollections;
            }
        }
        
        if(remainingCollections==0) { //if the db is empty drop it
            dropDatabase();
        }
    }

    public static BigDataStructureFactory.MapType getDefaultMapType() {
        return MapType.MONGODB_CACHING_CONCURRENT_MAP;
    }

    public static BigDataStructureFactory.CollectionType getDefaultCollectionType() {
        return CollectionType.MONGODB_COLLECTION;
    }

    public static BigDataStructureFactory.SetType getDefaultSetType() {
        return SetType.MONGODB_SET;
    }

    public static BigDataStructureFactory.QueueType getDefaultQueueType() {
        return QueueType.MONGODB_QUEUE;
    }
    
    public static int getDefaultLRUsize() {
        return 100000;
    }

    
    
    
    
    
    public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }
    
    /**
     * Creates an object even if it has no default constructor.
     * Reference: http://www.jayway.com/2012/02/28/configure-morphia-to-work-without-a-default-constructor/
     * 
     * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
     */
    /*
    public static class CustomMorphiaObjectFactory extends DefaultCreator {
        @Override
        public Object createInstance(Class clazz) {
            try {
                final Constructor constructor = getNoArgsConstructor(clazz);
                if(constructor != null) {
                    return constructor.newInstance();
                }
                try {
                    return ReflectionFactory.getReflectionFactory().newConstructorForSerialization(clazz, Object.class.getDeclaredConstructor(null)).newInstance(null);
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new MappingException("Failed to instantiate " + clazz.getName(), e);
                }
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | MappingException e) {
                throw new RuntimeException(e);
            }
        }

        private Constructor getNoArgsConstructor(final Class ctorType) {
            try {
                Constructor ctor = ctorType.getDeclaredConstructor();
                ctor.setAccessible(true);
                return ctor;
            } catch (NoSuchMethodException e) {
                return null;
            }
        }
    }
    */
}
