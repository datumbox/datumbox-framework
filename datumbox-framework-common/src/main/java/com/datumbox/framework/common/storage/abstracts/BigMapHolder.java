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
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.common.utilities.ReflectionMethods;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * Base class for every object that stored BigMaps internally.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public abstract class BigMapHolder implements Serializable {

    /**
     * Protected constructor which accepts as argument the StorageEngine.
     *
     * @param storageEngine
     */
    protected BigMapHolder(StorageEngine storageEngine) {
        //Initialize all the BigMap fields
        bigMapInitializer(storageEngine);
    }

    /**
     * Initializes all the fields of the class which are marked with the BigMap
     * annotation automatically.
     *
     * @param storageEngine
     */
    private void bigMapInitializer(StorageEngine storageEngine) {
        //get all the fields from all the inherited classes
        for(Field field : ReflectionMethods.getAllFields(new LinkedList<>(), this.getClass())){
            //if the field is annotated with BigMap
            if (field.isAnnotationPresent(BigMap.class)) {
                initializeBigMapField(storageEngine, field);
            }
        }
    }

    /**
     * Initializes a field which is marked as BigMap.
     *
     * @param storageEngine
     * @param field
     */
    private void initializeBigMapField(StorageEngine storageEngine, Field field) {
        field.setAccessible(true);

        try {
            BigMap a = field.getAnnotation(BigMap.class);
            field.set(this, storageEngine.getBigMap(field.getName(), a.keyClass(), a.valueClass(), a.mapType(), a.storageHint(), a.concurrent(), false));
        }
        catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}
