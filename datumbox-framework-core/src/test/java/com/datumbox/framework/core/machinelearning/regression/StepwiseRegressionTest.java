/**
 * Copyright (C) 2013-2017 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.core.machinelearning.regression;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.Record;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.machinelearning.MLBuilder;
import com.datumbox.framework.core.machinelearning.preprocessing.CornerConstraintsEncoder;
import com.datumbox.framework.core.machinelearning.preprocessing.MinMaxScaler;
import com.datumbox.framework.tests.Constants;
import com.datumbox.framework.tests.Datasets;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for StepwiseRegression.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class StepwiseRegressionTest extends AbstractTest {

    /**
     * Test of predict method, of class StepwiseRegression.
     */
    @Test
    public void testPredict() {
        logger.info("testPredict");
        
        Configuration configuration = Configuration.getConfiguration();
        
        Dataframe[] data = Datasets.regressionNumeric(configuration);
        
        Dataframe trainingData = data[0];
        Dataframe validationData = data[1];
        
        String storageName = this.getClass().getSimpleName();
        MinMaxScaler.TrainingParameters nsParams = new MinMaxScaler.TrainingParameters();
        MinMaxScaler numericalScaler = MLBuilder.create(nsParams, configuration);

        numericalScaler.fit_transform(trainingData);
        numericalScaler.save(storageName);

        CornerConstraintsEncoder.TrainingParameters ceParams = new CornerConstraintsEncoder.TrainingParameters();
        CornerConstraintsEncoder categoricalEncoder = MLBuilder.create(ceParams, configuration);

        categoricalEncoder.fit_transform(trainingData);
        categoricalEncoder.save(storageName);
        
        StepwiseRegression.TrainingParameters param = new StepwiseRegression.TrainingParameters();
        param.setAout(0.05);
        
        MatrixLinearRegression.TrainingParameters trainingParams = new MatrixLinearRegression.TrainingParameters();
        param.setRegressionTrainingParameters(trainingParams);


        StepwiseRegression instance = MLBuilder.create(param, configuration);
        instance.fit(trainingData);
        instance.save(storageName);

        trainingData.close();
        
        
        instance.close();
        numericalScaler.close();
        categoricalEncoder.close();



        numericalScaler = MLBuilder.load(MinMaxScaler.class, storageName, configuration);
        categoricalEncoder = MLBuilder.load(CornerConstraintsEncoder.class, storageName, configuration);
        instance = MLBuilder.load(StepwiseRegression.class, storageName, configuration);

        numericalScaler.transform(validationData);
        categoricalEncoder.transform(validationData);
        instance.predict(validationData);
        
        for(Record r : validationData) {
            Assert.assertEquals(TypeInference.toDouble(r.getY()), TypeInference.toDouble(r.getYPredicted()), Constants.DOUBLE_ACCURACY_HIGH);
        }

        numericalScaler.delete();
        categoricalEncoder.delete();
        instance.delete();

        validationData.close();
    }


}
