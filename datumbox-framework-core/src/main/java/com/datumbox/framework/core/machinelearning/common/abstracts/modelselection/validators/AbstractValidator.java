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
package com.datumbox.framework.core.machinelearning.common.abstracts.modelselection.validators;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.core.machinelearning.common.interfaces.TrainingParameters;
import com.datumbox.framework.core.machinelearning.common.interfaces.ValidationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all Validators.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <VM>
 */
public abstract class AbstractValidator<VM extends ValidationMetrics> {

    /**
     * The Logger of all Validators.
     * We want this to be non-static in order to print the names of the inherited classes.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final Class<VM> vmClass;
    protected final Configuration conf;

    /**
     * Default constructor.
     *
     * @param vmClass
     * @param conf
     */
    public AbstractValidator(Class<VM> vmClass, Configuration conf) {
        this.vmClass = vmClass;
        this.conf = conf;
    }

    /**
     * Performs a split on the data, trains models and performs validation.
     *
     * @param dataset
     * @param trainingParameters
     * @return
     */
    public abstract VM validate(Dataframe dataset, TrainingParameters trainingParameters);

}
