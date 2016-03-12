/**
 * Copyright (C) 2013-2016 Vasilis Vryniotis <bbriniotis@datumbox.com>
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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * Interface for every ValidationMetrics class in the framework. 
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */

public interface ValidationMetrics extends Serializable {

    /**
     * This method allows us to create a new empty Validation Metrics object 
     * from an existing object. Casting to the appropriate type is required.
     * 
     * @return 
     */
    default public ValidationMetrics getEmptyObject() {
        try {
            return this.getClass().getConstructor().newInstance();
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }      
}
