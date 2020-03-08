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

import com.datumbox.framework.core.common.dataobjects.Dataframe;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Interface for every Metrics class in the framework.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */

public interface ValidationMetrics extends Serializable {

    /**
     * Creates a new empty Validation Metrics object.
     *
     * @return
     */
    public static <VM extends ValidationMetrics> VM newInstance(Class<VM> vmClass) {
        try {
            return vmClass.getConstructor().newInstance();
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Estimates the Validation Metrics object from predictions.
     *
     * @param vmClass
     * @param predictedData
     * @param <VM>
     * @return
     */
    public static <VM extends ValidationMetrics> VM newInstance(Class<VM> vmClass, Dataframe predictedData) {
        try {
            return vmClass.getConstructor(Dataframe.class).newInstance(predictedData);
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Estimates the average Validation Metrics object from a list of metrics.
     *
     * @param vmClass
     * @param validationMetricsList
     * @param <VM>
     * @return
     */
    public static <VM extends ValidationMetrics> VM newInstance(Class<VM> vmClass, List<VM> validationMetricsList) {
        try {
            return vmClass.getConstructor(List.class).newInstance(validationMetricsList);
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

}
