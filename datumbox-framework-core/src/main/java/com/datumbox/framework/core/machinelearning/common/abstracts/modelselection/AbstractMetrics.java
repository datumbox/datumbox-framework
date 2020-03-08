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
package com.datumbox.framework.core.machinelearning.common.abstracts.modelselection;

import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.machinelearning.common.interfaces.ValidationMetrics;

import java.util.List;

/**
 * The AbstractMetrics class stores and estimates information about the performance of the algorithm.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public abstract class AbstractMetrics implements ValidationMetrics {

    /**
     * Estimates the validation metrics on the predicted data.
     *
     * @param predictedData
     */
    protected AbstractMetrics(Dataframe predictedData) {

    }
    
    /**
     * Calculates the average validation metrics by combining the results of the
     * provided list.
     * 
     * @param validationMetricsList
     */
    protected AbstractMetrics(List<? extends AbstractMetrics> validationMetricsList) {

    }

}
