/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
import com.datumbox.common.utilities.RandomValue;
import com.datumbox.common.utilities.TypeConversions;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.tests.utilities.TestUtils;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class PCATest {
    
    public PCATest() {
    }

    /**
     * Test of _fit123123123 method, of class PCA.
     */
    @Test
    public void testCalculateParameters() {
        TestUtils.log(this.getClass(), "calculateParameters");
        RandomValue.setRandomGenerator(new Random(TestConfiguration.RANDOM_SEED));
        DatabaseConfiguration dbConfig = TestUtils.getDBConfig();
        
        Dataset originaldata = new Dataset(dbConfig);
        originaldata.add(Record.<Double>newDataVector(new Double[]{1.0, 2.0, 3.0}, null));
        originaldata.add(Record.<Double>newDataVector(new Double[]{0.0, 5.0, 6.0}, null));
        originaldata.add(Record.<Double>newDataVector(new Double[]{7.0, 8.0, 0.0}, null));
        originaldata.add(Record.<Double>newDataVector(new Double[]{10.0, 0.0, 12.0}, null));
        originaldata.add(Record.<Double>newDataVector(new Double[]{13.0, 14.0, 15.0}, null));
        
        
        
        String dbName = "JUnitPCAdimred";
        
        PCA instance = new PCA(dbName, dbConfig);
        
        PCA.TrainingParameters param = new PCA.TrainingParameters();
        param.setMaxDimensions(null);
        
        instance.fit(originaldata, param);
        instance=null;
        
        Dataset newdata = originaldata;
        
        instance = new PCA(dbName, dbConfig);
        
        
        Dataset expResult = new Dataset(dbConfig);
        expResult.add(Record.<Double>newDataVector(new Double[]{-3.4438, 0.0799, -1.4607}, null));
        expResult.add(Record.<Double>newDataVector(new Double[]{-6.0641, 1.0143, -4.8165}, null));
        expResult.add(Record.<Double>newDataVector(new Double[]{-7.7270, 6.7253, 2.8399}, null));
        expResult.add(Record.<Double>newDataVector(new Double[]{-14.1401, -6.4677, 1.4920}, null));
        expResult.add(Record.<Double>newDataVector(new Double[]{-23.8837, 3.7408, -2.3614}, null));
        
        instance.transform(newdata);
        
        assertEquals(newdata.size(), expResult.size());
        
        Iterator<Integer> itResult = newdata.iterator();
        Iterator<Integer> itExpectedResult = expResult.iterator();
        
        
        while(itResult.hasNext()) {
            Record r= newdata.get(itResult.next());
            Record r2 = expResult.get(itExpectedResult.next());
            
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                Double value = TypeConversions.toDouble(entry.getValue());
                
                assertEquals(TypeConversions.toDouble(r2.getX().get(feature)), value, TestConfiguration.DOUBLE_ACCURACY_MEDIUM);
            }
        }
        
        instance.erase();
    }
    
}
