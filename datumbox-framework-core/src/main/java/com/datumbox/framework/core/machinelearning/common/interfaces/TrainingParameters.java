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

import com.datumbox.framework.core.common.interfaces.Parameterizable;

/**
 * The TrainingParameters objects store all the initial parameters provided
 * to the algorithms during training. 
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */

public interface TrainingParameters extends Parameterizable {

    /**
     * Retrieves the ModelParameters class.
     *
     * @param <MP>
     * @return
     */
    @SuppressWarnings("unchecked")
    default public <MP extends ModelParameters> Class<MP> getMPClass() {
        try {
            //By convention the training and model parameters are one level below the algorithm class.;
            return (Class<MP>) Class.forName(getTClass().getCanonicalName() + "$ModelParameters");
        }
        catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Retrives the Trainable class.
     *
     * @param <T>
     * @return
     */
    default public <T extends Trainable> Class<T> getTClass() {
        return (Class<T>) this.getClass().getEnclosingClass();
    }

}
