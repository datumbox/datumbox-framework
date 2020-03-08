/**
 * Copyright (C) 2013-2020 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.common.storage.abstracts;

import com.datumbox.framework.common.storage.interfaces.BigMap;
import com.datumbox.framework.common.storage.interfaces.StorageConfiguration;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.common.utilities.ReflectionMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The AbstractStorageEngine is the base class for all concrete storage engines.
 * Those classes can be used in a try-with-resources statement block. Moreover this class
 * setups a shutdown hook which ensures that the storage engine will automatically call close()
 * before the JVM is terminated. Finally it contains methods to store complex objects
 * with fields that point to other serialized objects.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public abstract class AbstractStorageEngine<SC extends StorageConfiguration> implements StorageEngine {

    protected String storageName;
    protected final SC storageConfiguration;

    /**
     * Logger for all Storage Engines.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    
    private Thread hook;

    /**
     * Protected Constructor which is responsible for adding the Shutdown hook and storing the storage name and configuration.
     *
     * @param storageName
     * @param storageConfiguration
     */
    protected AbstractStorageEngine(String storageName, SC storageConfiguration) {
        this.storageName = storageName;
        this.storageConfiguration = storageConfiguration;

        hook = new Thread(() -> {
            AbstractStorageEngine.this.hook = null;
            if(AbstractStorageEngine.this.isClosed()) {
                return;
            }
            AbstractStorageEngine.this.close();
        });
        Runtime.getRuntime().addShutdownHook(hook);

        logger.trace("Opened storage {}", storageName);
    }

    /** {@inheritDoc} */
    @Override
    public String getStorageName() {
        return storageName;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isClosed() {
        return isClosed.get();
    }
    
    /** {@inheritDoc} */
    @Override
    public void close() {
        if(isClosed() == false && hook != null) {
            //remove hook to save memory
            Runtime.getRuntime().removeShutdownHook(hook);
            hook = null;
        }
        isClosed.set(true);
    }
    
    /**
     * Ensures the connection is not closed.
     */
    protected void assertConnectionOpen() {
        if(isClosed()) {
            throw new RuntimeException("The connection is already closed.");
        }
    }

    /**
     * This method is called before serializing the objects. It extracts all the not-serializable BigMap references
     * of the provided object and stores them in a Map. Then it replaces all the references of the provided object
     * with nulls to avoid their serialization. The main idea is that we temporarily remove from the object any reference
     * that will cause problems during the serialization phase.
     *
     * @param serializableObject
     * @param <T>
     * @return
     */
    protected <T extends Serializable> Map<String, Object> preSerializer(T serializableObject) {
        Map<String, Object> objReferences = new HashMap<>();
        for(Field field : ReflectionMethods.getAllFields(new LinkedList<>(), serializableObject.getClass())) {

            if (field.isAnnotationPresent(BigMap.class)) {
                field.setAccessible(true);

                try {
                    Object value = field.get(serializableObject);
                    if(!isSerializableBigMap(value)) { //if the field is annotated with BigMap AND the value is not serializable
                        //extract the reference to the object and put it in the map.
                        objReferences.put(field.getName(), value);
                        //then replace the reference with null to avoid serialization.
                        field.set(serializableObject, null);
                    }
                }
                catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        return objReferences;
    }

    /**
     * Returns a list of non-serializable bigmap classes which are dependent to the StorageEngine.
     *
     * @return
     */
    protected abstract Set<Class> nonSerializableBigMaps();

    private boolean isSerializableBigMap(Object value) {
        Class valueClass = value.getClass();
        //Check if the class is not serializable directly
        if(!Serializable.class.isAssignableFrom(valueClass)) {
            return false;
        }

        //Check the black listed bigmap types of the storage engine
        if(nonSerializableBigMaps().contains(valueClass)) {
            return false;
        }

        //Also check that we are not dealing with a SynchronizedMap that has a non-serializable map inside.
        if(valueClass.getCanonicalName().equals("java.util.Collections.SynchronizedMap")) {
            try {
                Field field = valueClass.getDeclaredField("m");
                field.setAccessible(true);
                if(!Serializable.class.isAssignableFrom(field.get(value).getClass())) {
                    return false;
                }
            }
            catch (IllegalAccessException | NoSuchFieldException ex) {
                throw new RuntimeException(ex);
            }

        }

        return true;
    }

    /**
     * This method is called after the object serialization. It moves all the not-serializable BigMap references from
     * the Map back to the provided object. The main idea is that once the serialization is completed, we are allowed to
     * restore back all the references which were removed by the preSerializer.
     *
     * @param serializableObject
     * @param objReferences
     * @param <T>
     */
    protected <T extends Serializable> void postSerializer(T serializableObject, Map<String, Object> objReferences) {
        for(Field field : ReflectionMethods.getAllFields(new LinkedList<>(), serializableObject.getClass())) {
            String fieldName = field.getName();

            Object ref = objReferences.remove(fieldName);
            if(ref != null) { //if a reference is found in the map
                field.setAccessible(true);

                try {
                    //restore the reference in the object
                    field.set(serializableObject, ref);
                }
                catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    /**
     * This method is called after the object deserialization. It initializes all BigMaps of the serializable object which
     * have a null value. The main idea is that once an object is deserialized it will contain nulls in all the BigMap fields
     * which were not serialized. For all of those fields we call their initialization methods.
     *
     * @param serializableObject
     * @param <T>
     */
    protected <T extends Serializable> void postDeserializer(T serializableObject) {
        Method method = null;
        for(Field field : ReflectionMethods.getAllFields(new LinkedList<>(), serializableObject.getClass())) {
            if (field.isAnnotationPresent(BigMap.class)) { //look only for BigMaps
                field.setAccessible(true);
                try {
                    if(field.get(serializableObject) == null) { //initialize it only if null. this makes it safe it the BigMap was serialized in the file.

                        //lazy initialize the correct method once
                        if(method == null) {
                            method = ReflectionMethods.findMethod(serializableObject, "initializeBigMapField", this, field);
                        }

                        ReflectionMethods.invokeMethod(serializableObject, method, this, field);
                    }
                }
                catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

    }

}
