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
package com.datumbox.framework.core.machinelearning.common.abstracts.validators;

import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.core.machinelearning.common.interfaces.ValidationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The AbstractValidator class is an abstract class responsible for the K-fold Cross
 Validation and for the estimation of the average validation metrics. Given that
 * different models use different validation metrics, each model family implements
 * its own validator.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <VM>
 */
public abstract class AbstractValidator<VM extends AbstractValidator.AbstractValidationMetrics> {
    
    /**
     * The Logger of all Validators.
     * We want this to be non-static in order to print the names of the inherited classes.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The AbstractValidationMetrics class stores information about the performance of the algorithm.
     */
    public static abstract class AbstractValidationMetrics implements ValidationMetrics {

    }

    /**
     * Estimates the validation metrics on the predicted data.
     *
     * @param predictedData
     * @return
     */
    public abstract VM validate(Dataframe predictedData);
    
    /**
     * Calculates the average validation metrics by combining the results of the
     * provided list.
     * 
     * @param validationMetricsList
     * @return 
     */
    public abstract VM average(List<VM> validationMetricsList);

}
