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
package com.datumbox.framework.core.machinelearning.modelselection;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.machinelearning.MLBuilder;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelers.AbstractModeler;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelselection.AbstractSplitter.Split;
import com.datumbox.framework.core.machinelearning.common.interfaces.TrainingParameters;
import com.datumbox.framework.core.machinelearning.common.interfaces.ValidationMetrics;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Estimates the validation metrics of a specific model.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <VM>
 */
public class Validator<VM extends ValidationMetrics> {

    private final Class<VM> vmClass;
    private final Configuration configuration;

    /**
     * The constructor of the K-Fold cross validator.
     *
     * @param vmClass
     */
    public Validator(Class<VM> vmClass, Configuration configuration) {
        this.vmClass = vmClass;
        this.configuration = configuration;
    }

    /**
     * Estimates the average validation metrics on the provided data splits.
     *
     * @param dataSplits
     * @param trainingParameters
     * @return
     */
    public VM validate(Iterator<Split> dataSplits, TrainingParameters trainingParameters) {
        AbstractModeler modeler = MLBuilder.create(trainingParameters, configuration);

        List<VM> validationMetricsList = new LinkedList<>();
        while (dataSplits.hasNext()) {
            Split s = dataSplits.next();
            Dataframe trainData = s.getTrain();
            Dataframe testData = s.getTest();

            modeler.fit(trainData);
            trainData.close();

            modeler.predict(testData);
            VM entrySample = ValidationMetrics.newInstance(vmClass, testData);
            testData.close();

            validationMetricsList.add(entrySample);
        }
        modeler.close();

        VM avgValidationMetrics = ValidationMetrics.newInstance(vmClass, validationMetricsList);

        return avgValidationMetrics;
    }
}
