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
package com.datumbox.framework.core.machinelearning.regression;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.Record;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.machinelearning.MLBuilder;
import com.datumbox.framework.core.machinelearning.featureselection.continuous.PCA;
import com.datumbox.framework.core.machinelearning.modelselection.metrics.LinearRegressionMetrics;
import com.datumbox.framework.core.machinelearning.modelselection.Validator;
import com.datumbox.framework.core.machinelearning.modelselection.splitters.KFoldSplitter;
import com.datumbox.framework.core.machinelearning.preprocessing.CornerConstraintsEncoder;
import com.datumbox.framework.core.machinelearning.preprocessing.StandardScaler;
import com.datumbox.framework.tests.Constants;
import com.datumbox.framework.tests.Datasets;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for NLMS.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class NLMSTest extends AbstractTest {

    /**
     * Test of predict method, of class NLMS.
     */
    @Test
    public void testPredict() {
        logger.info("testPredict");
        
        Configuration configuration = Configuration.getConfiguration();
        
        Dataframe[] data = Datasets.regressionNumeric(configuration);
        
        Dataframe trainingData = data[0];
        Dataframe validationData = data[1];
        
        String storageName = this.getClass().getSimpleName();


        StandardScaler.TrainingParameters nsParams = new StandardScaler.TrainingParameters();
        StandardScaler numericalScaler = MLBuilder.create(nsParams, configuration);

        numericalScaler.fit_transform(trainingData);
        numericalScaler.save(storageName);

        CornerConstraintsEncoder.TrainingParameters ceParams = new CornerConstraintsEncoder.TrainingParameters();
        CornerConstraintsEncoder categoricalEncoder = MLBuilder.create(ceParams, configuration);

        categoricalEncoder.fit_transform(trainingData);
        categoricalEncoder.save(storageName);

        
        NLMS.TrainingParameters param = new NLMS.TrainingParameters();
        param.setTotalIterations(1600);
        param.setL1(0.00000001);



        NLMS instance = MLBuilder.create(param, configuration);
        instance.fit(trainingData);
        instance.save(storageName);

        trainingData.close();
        
        instance.close();
        numericalScaler.close();
        categoricalEncoder.close();



        numericalScaler = MLBuilder.load(StandardScaler.class, storageName, configuration);
        categoricalEncoder = MLBuilder.load(CornerConstraintsEncoder.class, storageName, configuration);
        instance = MLBuilder.load(NLMS.class, storageName, configuration);

        numericalScaler.transform(validationData);
        categoricalEncoder.transform(validationData);
        
        instance.predict(validationData);

        for(Record r : validationData) {
            assertEquals(TypeInference.toDouble(r.getY()), TypeInference.toDouble(r.getYPredicted()), Constants.DOUBLE_ACCURACY_HIGH);
        }

        numericalScaler.delete();
        categoricalEncoder.delete();
        instance.delete();

        validationData.close();
    }


    /**
     * Test of validate method, of class NLMS.
     */
    @Test
    public void testKFoldCrossValidation() {
        logger.info("testKFoldCrossValidation");
        
        Configuration configuration = Configuration.getConfiguration();
        
        int k = 5;
        
        Dataframe[] data = Datasets.housingNumerical(configuration);
        Dataframe trainingData = data[0];
        data[1].close();

        StandardScaler.TrainingParameters nsParams = new StandardScaler.TrainingParameters();
        nsParams.setScaleResponse(true);
        StandardScaler numericalScaler = MLBuilder.create(nsParams, configuration);

        numericalScaler.fit_transform(trainingData);

        CornerConstraintsEncoder.TrainingParameters ceParams = new CornerConstraintsEncoder.TrainingParameters();
        CornerConstraintsEncoder categoricalEncoder = MLBuilder.create(ceParams, configuration);

        categoricalEncoder.fit_transform(trainingData);

        PCA.TrainingParameters featureSelectorParameters = new PCA.TrainingParameters();
        featureSelectorParameters.setMaxDimensions(trainingData.xColumnSize()-1);
        featureSelectorParameters.setWhitened(true);
        featureSelectorParameters.setVariancePercentageThreshold(0.99999995);

        PCA featureSelector = MLBuilder.create(featureSelectorParameters, configuration);
        featureSelector.fit_transform(trainingData);
        featureSelector.close();



        NLMS.TrainingParameters param = new NLMS.TrainingParameters();
        param.setTotalIterations(500);
        param.setL1(0.001);
        param.setL2(0.001);
        
        LinearRegressionMetrics vm = new Validator<>(LinearRegressionMetrics.class, configuration)
                .validate(new KFoldSplitter(k).split(trainingData), param);


        double expResult = 0.7780829672538567;
        double result = vm.getRSquare();
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);

        numericalScaler.close();
        categoricalEncoder.close();
        
        trainingData.close();
    }

}
