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
package com.datumbox.framework.core.machinelearning.featureselection;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.machinelearning.MLBuilder;
import com.datumbox.framework.tests.Constants;
import com.datumbox.framework.core.Datasets;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.junit.Test;

import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;

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
        
        Configuration configuration = getConfiguration();
        
        Dataframe[] data = Datasets.featureTransformationPCA(configuration);
        
        Dataframe originalData = data[0];
        Dataframe validationData = data[0].copy();
        Dataframe expResult = data[1];
        
        String storageName = this.getClass().getSimpleName();
        
        PCA.TrainingParameters param = new PCA.TrainingParameters();
        param.setMaxDimensions(null);

        PCA instance = MLBuilder.create(param, configuration);
        instance.fit_transform(originalData);
        instance.save(storageName);

        originalData.close();
        instance.close();

        
        instance = MLBuilder.load(PCA.class, storageName, configuration);
        
        instance.transform(validationData);
        
        assertEquals(validationData.size(), expResult.size());
        
        Iterator<Record> itResult = validationData.iterator();
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

        validationData.close();
        expResult.close();
    }
    
}
