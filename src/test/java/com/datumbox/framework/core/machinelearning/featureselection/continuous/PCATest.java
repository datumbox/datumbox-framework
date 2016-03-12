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
package com.datumbox.framework.core.machinelearning.featureselection.continuous;

import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.Record;
import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.tests.Constants;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import com.datumbox.framework.tests.Datasets;
import com.datumbox.framework.tests.utilities.TestUtils;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for PCA.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class PCATest extends AbstractTest {

    /**
     * Test of selectFeatures method, of class PCA.
     */
    @Test
    public void testSelectFeatures() {
        logger.info("selectFeatures");
        
        Configuration conf = TestUtils.getConfig();
        
        Dataframe[] data = Datasets.featureTransformationPCA(conf);
        
        Dataframe originalData = data[0];
        Dataframe validationdata = data[0].copy();
        Dataframe expResult = data[1];
        
        String dbName = this.getClass().getSimpleName();
        PCA instance = new PCA(dbName, conf);
        
        PCA.TrainingParameters param = new PCA.TrainingParameters();
        param.setMaxDimensions(null);
        
        instance.fit_transform(originalData, param);
        instance.close();
        //instance = null;
        
        instance = new PCA(dbName, conf);
        
        instance.transform(validationdata);
        
        assertEquals(validationdata.size(), expResult.size());
        
        Iterator<Record> itResult = validationdata.iterator();
        Iterator<Record> itExpectedResult = expResult.iterator();
        
        
        while(itResult.hasNext()) {
            Record r1 = itResult.next();
            Record r2 = itExpectedResult.next();
            
            for(Map.Entry<Object, Object> entry : r1.getX().entrySet()) {
                Object feature = entry.getKey();
                Double value = TypeInference.toDouble(entry.getValue());
                
                assertEquals(TypeInference.toDouble(r2.getX().get(feature)), value, Constants.DOUBLE_ACCURACY_MEDIUM);
            }
        }
        
        instance.delete();
        
        originalData.delete();
        validationdata.delete();
        expResult.delete();
    }
    
}
