/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.machinelearning.featureselection.continuous;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.RandomSingleton;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.tests.utilities.Datasets;
import com.datumbox.tests.utilities.TestUtils;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class PCATest {
    
    public PCATest() {
    }

    /**
     * Test of selectFeatures method, of class PCA.
     */
    @Test
    public void testSelectFeatures() {
        TestUtils.log(this.getClass(), "selectFeatures");
        RandomSingleton.getInstance().setSeed(TestConfiguration.RANDOM_SEED);
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        Dataset[] data = Datasets.featureTransformationCPA(dbConf);
        
        Dataset originalData = data[0];
        Dataset validationdata = data[0].copy();
        Dataset expResult = data[1];
        
        String dbName = "JUnitFeatureSelection";
        PCA instance = new PCA(dbName, dbConf);
        
        PCA.TrainingParameters param = new PCA.TrainingParameters();
        param.setMaxDimensions(null);
        
        instance.fit(originalData, param);
        instance=null;
        
        instance = new PCA(dbName, dbConf);
        
        instance.transform(validationdata);
        
        assertEquals(validationdata.getRecordNumber(), expResult.getRecordNumber());
        
        Iterator<Integer> itResult = validationdata.iterator();
        Iterator<Integer> itExpectedResult = expResult.iterator();
        
        
        while(itResult.hasNext()) {
            Record r= validationdata.get(itResult.next());
            Record r2 = expResult.get(itExpectedResult.next());
            
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                Double value = TypeInference.toDouble(entry.getValue());
                
                assertEquals(TypeInference.toDouble(r2.getX().get(feature)), value, TestConfiguration.DOUBLE_ACCURACY_MEDIUM);
            }
        }
        
        instance.erase();
        
        originalData.erase();
        validationdata.erase();
        expResult.erase();
    }
    
}
