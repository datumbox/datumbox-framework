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
import com.datumbox.framework.core.machinelearning.datatransformation.DummyXYMinMaxNormalizer;
import com.datumbox.framework.core.machinelearning.datatransformation.XYMinMaxNormalizer;
import com.datumbox.framework.core.machinelearning.modelselection.metrics.LinearRegressionMetrics;
import com.datumbox.framework.core.machinelearning.modelselection.validators.KFoldValidator;
import com.datumbox.framework.tests.Constants;
import com.datumbox.framework.tests.Datasets;
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
        
        Configuration conf = Configuration.getConfiguration();
        
        Dataframe[] data = Datasets.regressionNumeric(conf);
        
        Dataframe trainingData = data[0];
        Dataframe validationData = data[1];
        
        String dbName = this.getClass().getSimpleName();
        XYMinMaxNormalizer df = MLBuilder.create(new XYMinMaxNormalizer.TrainingParameters(), dbName, conf);
        df.fit_transform(trainingData);
        
        df.transform(validationData);


        MatrixLinearRegression instance = MLBuilder.create(new MatrixLinearRegression.TrainingParameters(), dbName, conf);
        instance.fit(trainingData);
        
        
        instance.close();
        df.close();
        //instance = null;
        //df = null;
        
        df = MLBuilder.load(XYMinMaxNormalizer.class, dbName, conf);
        instance = MLBuilder.load(MatrixLinearRegression.class, dbName, conf);
        
        instance.predict(validationData);
        
        
	        
        df.denormalize(trainingData);
        df.denormalize(validationData);


        for(Record r : validationData) {
            assertEquals(TypeInference.toDouble(r.getY()), TypeInference.toDouble(r.getYPredicted()), Constants.DOUBLE_ACCURACY_HIGH);
        }
        
        df.delete();
        instance.delete();
        
        trainingData.delete();
        validationData.delete();
    }


    /**
     * Test of validate method, of class MatrixLinearRegression.
     */
    @Test
    public void testKFoldCrossValidation() {
        logger.info("testKFoldCrossValidation");
        
        Configuration conf = Configuration.getConfiguration();
        
        int k = 5;
        
        Dataframe[] data = Datasets.regressionMixed(conf);
        Dataframe trainingData = data[0];
        data[1].delete();
                
        String dbName = this.getClass().getSimpleName();

        DummyXYMinMaxNormalizer df = MLBuilder.create(new DummyXYMinMaxNormalizer.TrainingParameters(), dbName, conf);
        df.fit_transform(trainingData);

        
        MatrixLinearRegression.TrainingParameters param = new MatrixLinearRegression.TrainingParameters();
        
        LinearRegressionMetrics vm = new KFoldValidator<>(LinearRegressionMetrics.class, conf, k).validate(trainingData, param);
        
        df.denormalize(trainingData);

        double expResult = 1;
        double result = vm.getRSquare();
        assertEquals(expResult, result, Constants.DOUBLE_ACCURACY_HIGH);
        
        df.delete();
        
        trainingData.delete();
    }


}
