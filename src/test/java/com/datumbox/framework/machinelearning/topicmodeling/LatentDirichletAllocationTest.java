/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.datumbox.framework.machinelearning.topicmodeling;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.utilities.RandomValue;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.framework.machinelearning.classification.SoftMaxRegression;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclassifier;
import com.datumbox.framework.utilities.dataset.DatasetBuilder;
import com.datumbox.framework.utilities.text.extractors.UniqueWordSequenceExtractor;
import com.datumbox.tests.utilities.TestUtils;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class LatentDirichletAllocationTest {
    
    public LatentDirichletAllocationTest() {
    }


    /**
     * Test of predict method, of class NLMS.
     * @throws java.net.URISyntaxException
     * @throws java.net.MalformedURLException
     */
    @Test
    public void testValidate() throws URISyntaxException, MalformedURLException {
        TestUtils.log(this.getClass(), "validate");
        
        RandomValue.randomGenerator = new Random(42);
        
        
        String dbName = "JUnitTopicSelection";

        
        Map<Object, URI> dataset = new HashMap<>();
        dataset.put("negative", TestUtils.getRemoteFile(new URL("http://www.datumbox.com/files/datasets/example.neg")));
        dataset.put("positive", TestUtils.getRemoteFile(new URL("http://www.datumbox.com/files/datasets/example.pos")));
        
        UniqueWordSequenceExtractor wsExtractor = new UniqueWordSequenceExtractor();
        wsExtractor.setParameters(new UniqueWordSequenceExtractor.Parameters());
        Dataset trainingData =DatasetBuilder.parseFromTextFiles(dataset, wsExtractor);
        
        
        LatentDirichletAllocation lda = new LatentDirichletAllocation(dbName, TestUtils.getDBConfig());
        
        LatentDirichletAllocation.TrainingParameters trainingParameters = new LatentDirichletAllocation.TrainingParameters();
        trainingParameters.setMaxIterations(15);
        trainingParameters.setAlpha(0.01);
        trainingParameters.setBeta(0.01);
        trainingParameters.setK(25);        
        
        lda.fit(trainingData, trainingParameters);
        
        lda.validate(trainingData);
        
        Dataset reducedTrainingData = new Dataset();
        for(Integer rId : trainingData) {
            Record r = trainingData.get(rId);
            //take the topic assignments and convert them into a new Record
            reducedTrainingData.add(new Record(r.getYPredictedProbabilities(), r.getY()));
        }
        
        SoftMaxRegression smr = new SoftMaxRegression(dbName, TestUtils.getDBConfig());
        SoftMaxRegression.TrainingParameters tp = new SoftMaxRegression.TrainingParameters();
        tp.setLearningRate(1.0);
        tp.setTotalIterations(50);
        
        BaseMLclassifier.ValidationMetrics vm = smr.kFoldCrossValidation(reducedTrainingData, tp, 1);
        
        double expResult = 0.6859007513066202;
        double result = vm.getMacroF1();
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);

        smr.erase();
        lda.erase();
        
    }

    
}
