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
package com.datumbox.framework.core.machinelearning;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.core.common.interfaces.Parameterizable;
import com.datumbox.framework.core.machinelearning.common.interfaces.Trainable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Creates or Loads a Machine Learning algorithm.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MLBuilder {

    /**
     * Creates a new algorithm based on the provided training parameters.
     *
     * @param <T>
     * @param configuration
     * @return
     */
    public static <T extends Trainable, TP extends Parameterizable> T create(TP trainingParameters, Configuration configuration) {
        try {
            Class<T> aClass = (Class<T>) trainingParameters.getClass().getEnclosingClass();
            Constructor<T> constructor = aClass.getDeclaredConstructor(trainingParameters.getClass(), Configuration.class);
            constructor.setAccessible(true);
            return constructor.newInstance(trainingParameters, configuration);
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Loads an algorithm from the storage.
     *
     * @param <T>
     * @param aClass
     * @param storageName
     * @param configuration
     * @return
     */
    public static <T extends Trainable> T load(Class<T> aClass, String storageName, Configuration configuration) {
        try {
            Constructor<T> constructor = aClass.getDeclaredConstructor(String.class, Configuration.class);
            constructor.setAccessible(true);
            return constructor.newInstance(storageName, configuration);
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }
}
