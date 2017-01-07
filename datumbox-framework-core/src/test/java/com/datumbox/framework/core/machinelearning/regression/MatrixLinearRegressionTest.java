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
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.machinelearning.MLBuilder;
import com.datumbox.framework.core.machinelearning.modelselection.metrics.LinearRegressionMetrics;
import com.datumbox.framework.core.machinelearning.modelselection.Validator;
import com.datumbox.framework.core.machinelearning.modelselection.splitters.KFoldSplitter;
import com.datumbox.framework.core.machinelearning.preprocessing.CornerConstraintsEncoder;
import com.datumbox.framework.core.machinelearning.preprocessing.MaxAbsScaler;
import com.datumbox.framework.tests.Constants;
import com.datumbox.framework.core.Datasets;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for MatrixLinearRegression.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MatrixLinearRegressionTest extends AbstractTest {

    /**
     * Test of predict method, of class MatrixLinearRegression.
     */
    @Test
    public void testPredict() {
        logger.info("testPredict");
        
        Configuration configuration = getConfiguration();
        
        Dataframe[] data = Datasets.regressionNumeric(configuration);
        
        Dataframe trainingData = data[0];
        Dataframe validationData = data[1];
        
        String storageName = this.getClass().getSimpleName();

        MaxAbsScaler.TrainingParameters nsParams = new MaxAbsScaler.TrainingParameters();
        nsParams.setScaleResponse(true);

        MaxAbsScaler scaler = MLBuilder.create(nsParams, configuration);
        scaler.fit_transform(trainingData);
        scaler.save(storageName);


        MatrixLinearRegression instance = MLBuilder.create(new MatrixLinearRegression.TrainingParameters(), configuration);
        instance.fit(trainingData);
        instance.save(storageName);

        trainingData.close();
        
        instance.close();
        scaler.close();



        scaler = MLBuilder.load(MaxAbsScaler.class, storageName, configuration);
        instance = MLBuilder.load(MatrixLinearRegression.class, storageName, configuration);


        scaler.transform(validationData);
        instance.predict(validationData);


        for(Record r : validationData) {
            assertEquals(TypeInference.toDouble(r.getY()), TypeInference.toDouble(r.getYPredicted()), Constants.DOUBLE_ACCURACY_HIGH);
        }

        scaler.delete();
        instance.delete();

        validationData.close();
    }


    /**
     * Test of validate method, of class MatrixLinearRegression.
     */
    @Test
    public void testKFoldCrossValidation() {
        logger.info("testKFoldCrossValidation");
        
        Configuration configuration = getConfiguration();
        
        int k = 5;
        
        Dataframe[] data = Datasets.regressionMixed(configuration);
        Dataframe trainingData = data[0];
        data[1].close();


        MaxAbsScaler.TrainingParameters nsParams = new MaxAbsScaler.TrainingParameters();
        MaxAbsScaler numericalScaler = MLBuilder.create(nsParams, configuration);

        numericalScaler.fit_transform(trainingData);

        CornerConstraintsEncoder.TrainingParameters ceParams = new CornerConstraintsEncoder.TrainingParameters();
        CornerConstraintsEncoder categoricalEncoder = MLBuilder.create(ceParams, configuration);

        categoricalEncoder.fit_transform(trainingData);

        
        MatrixLinearRegression.TrainingParameters param = new MatrixLinearRegression.TrainingParameters();
        
        LinearRegressionMetrics vm = new Validator<>(LinearRegressionMetrics.class, configuration)
                .validate(new KFoldSplitter(k).split(trainingData), param);
        System.out.println(vm);

        double expResult = 1;
        double result = vm.getRSquare();
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);

        numericalScaler.close();
        categoricalEncoder.close();
        
        trainingData.close();
    }


}
