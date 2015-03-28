/* 
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
package com.datumbox.framework.machinelearning.featureselection.continuous;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.configuration.TestConfiguration;
import java.util.Iterator;
import java.util.Map;
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
        System.out.println("calculateParameters");
        Dataset originaldata = new Dataset();
        originaldata.add(Record.<Double>newDataVector(new Double[]{1.0, 2.0, 3.0}, null));
        originaldata.add(Record.<Double>newDataVector(new Double[]{0.0, 5.0, 6.0}, null));
        originaldata.add(Record.<Double>newDataVector(new Double[]{7.0, 8.0, 0.0}, null));
        originaldata.add(Record.<Double>newDataVector(new Double[]{10.0, 0.0, 12.0}, null));
        originaldata.add(Record.<Double>newDataVector(new Double[]{13.0, 14.0, 15.0}, null));
        
        
        
        String dbName = "JUnitPCAdimred";
        
        PCA instance = new PCA(dbName, TestConfiguration.getDBConfig());
        
        PCA.TrainingParameters param = new PCA.TrainingParameters();
        param.setMaxDimensions(null);
        
        instance.fit(originaldata, param);
        instance=null;
        
        Dataset newdata = originaldata;
        
        instance = new PCA(dbName, TestConfiguration.getDBConfig());
        
        
        Dataset expResult = new Dataset();
        expResult.add(Record.<Double>newDataVector(new Double[]{-3.4438, 0.0799, -1.4607}, null));
        expResult.add(Record.<Double>newDataVector(new Double[]{-6.0641, 1.0143, -4.8165}, null));
        expResult.add(Record.<Double>newDataVector(new Double[]{-7.7270, 6.7253, 2.8399}, null));
        expResult.add(Record.<Double>newDataVector(new Double[]{-14.1401, -6.4677, 1.4920}, null));
        expResult.add(Record.<Double>newDataVector(new Double[]{-23.8837, 3.7408, -2.3614}, null));
        
        instance.transform(newdata);
        
        assertEquals(newdata.size(), expResult.size());
        
        Iterator<Record> itResult = newdata.iterator();
        Iterator<Record> itExpectedResult = expResult.iterator();
        
        
        while(itResult.hasNext()) {
            Record r=itResult.next();
            Record r2 = itExpectedResult.next();
            
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                Double value = Dataset.toDouble(entry.getValue());
                
                assertEquals(Dataset.toDouble(r2.getX().get(feature)), value, TestConfiguration.DOUBLE_ACCURACY_MEDIUM);
            }
        }
        
        instance.erase();
    }
    
}
