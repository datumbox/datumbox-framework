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
package com.datumbox.framework.machinelearning.featureselection.scorebased;

import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.Configuration;
import com.datumbox.tests.abstracts.AbstractTest;
import com.datumbox.tests.Datasets;
import com.datumbox.tests.utilities.TestUtils;
import java.util.Arrays;
import java.util.HashSet;

import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for TFIDF.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class TFIDFTest extends AbstractTest {

    /**
     * Test of shortMethodName method, of class TFIDF.
     */
    @Test
    public void testSelectFeatures() {
        logger.info("selectFeatures");
        
        Configuration conf = TestUtils.getConfig();
        
        Dataframe[] data = Datasets.featureSelectorTFIDF(conf);
        
        Dataframe trainingData = data[0];
        Dataframe validationData = data[1];
        
        String dbName = this.getClass().getSimpleName();
        TFIDF.TrainingParameters param = new TFIDF.TrainingParameters();
        param.setBinarized(false);
        param.setMaxFeatures(3);
        
        TFIDF instance = new TFIDF(dbName, conf);
        
        instance.fit_transform(trainingData, param);
        instance.close();
        //instance = null;
        
        
        instance = new TFIDF(dbName, conf);
        
        instance.transform(validationData);
        
        Set<Object> expResult = new HashSet<>(Arrays.asList("important1", "important2", "important3"));
        Set<Object> result = validationData.getXDataTypes().keySet();
        assertEquals(expResult, result);
        instance.delete();
        
        trainingData.delete();
        validationData.delete();
    }
    
}
