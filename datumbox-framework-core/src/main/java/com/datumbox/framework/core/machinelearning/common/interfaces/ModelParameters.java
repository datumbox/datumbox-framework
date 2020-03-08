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
package com.datumbox.framework.core.machinelearning.common.interfaces;

import com.datumbox.framework.core.common.interfaces.Learnable;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * The ModelParameter objects stores the coefficients that were learned during
 * the training of the algorithm.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public interface ModelParameters extends Learnable {

    /**
     * Generates a new instance of the Model Parameters by using the provided class.
     *
     * @param <MP>
     * @param mpClass
     * @param storageEngine
     * @return
     */
    public static <MP extends ModelParameters> MP newInstance(Class<MP> mpClass, StorageEngine storageEngine) {
        try {
            Constructor<MP> c = mpClass.getDeclaredConstructor(StorageEngine.class);
            c.setAccessible(true);
            return c.newInstance(storageEngine);
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

}
