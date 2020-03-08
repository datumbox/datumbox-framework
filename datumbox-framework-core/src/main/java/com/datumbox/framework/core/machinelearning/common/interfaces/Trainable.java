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
import com.datumbox.framework.core.common.interfaces.Learnable;
import com.datumbox.framework.core.common.interfaces.Parameterizable;
import com.datumbox.framework.core.common.interfaces.Savable;

/**
 * This interface is used to mark classes that can be trained. This interface 
 * used for classes that perform training/analysis and learn parameters. 
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public interface Trainable<MP extends Learnable, TP extends Parameterizable> extends Savable {

    /**
     * Returns the model parameters that were estimated after training.
     * 
     * @return
     */
    public MP getModelParameters();

    /**
     * It returns the training parameters that configure the algorithm.
     * 
     * @return 
     */
    public TP getTrainingParameters();

    /**
     * Trains a model using the provided training parameters and data.
     * 
     * @param trainingData
     */
    public void fit(Dataframe trainingData);
    
}
