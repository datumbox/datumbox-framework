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
package com.datumbox.common.dataobjects;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.RandomSingleton;
import com.datumbox.configuration.TestConfiguration;
import com.datumbox.tests.utilities.TestUtils;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class DatasetTest {
    
    public DatasetTest() {
    }
    
    /**
     * Test of copyCollection2Array method, of class Dataset.
     */
    @Test
    public void testCopyCollection2Array() {
        TestUtils.log(this.getClass(), "copyCollection2Array");
        FlatDataCollection flatDataCollection = new FlatDataCollection(Arrays.asList(new Object[]{1,2,3,4,5}));
        Object[] expResult = new Object[]{1,2,3,4,5};
        Object[] result = flatDataCollection.<Object>copyCollection2Array(Object.class);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of getColumns method, of class Dataset.
     */
    @Test
    public void testGetColumns() {
        TestUtils.log(this.getClass(), "getColumns");
        RandomSingleton.getInstance().setSeed(TestConfiguration.RANDOM_SEED);
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        Dataset instance = new Dataset(dbConf);
        
        AssociativeArray xData1 = new AssociativeArray();
        xData1.put("1", true);
        instance.add(new Record(xData1, null));
        
        AssociativeArray xData2 = new AssociativeArray();
        xData2.put("2", 1.0);
        instance.add(new Record(xData2, null));
        
        AssociativeArray xData3 = new AssociativeArray();
        xData3.put("3", (short)1);
        instance.add(new Record(xData3, null));
        
        AssociativeArray xData4 = new AssociativeArray();
        xData4.put("4", "s");
        instance.add(new Record(xData4, null));
        
        Map<Object, Dataset.ColumnType> expResult = new LinkedHashMap<>();
        expResult.put("1", Dataset.ColumnType.DUMMYVAR);
        expResult.put("2", Dataset.ColumnType.NUMERICAL);
        expResult.put("3", Dataset.ColumnType.ORDINAL);
        expResult.put("4", Dataset.ColumnType.CATEGORICAL);
        Map<Object, Dataset.ColumnType> result = instance.getColumns();
        assertEquals(expResult, result);
    }

    /**
     * Test of extractColumnValues method, of class Dataset.
     */
    @Test
    public void testExtractColumnValues() {
        TestUtils.log(this.getClass(), "extractColumnValues");
        RandomSingleton.getInstance().setSeed(TestConfiguration.RANDOM_SEED);
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        Object column = "height";
        Dataset instance = new Dataset(dbConf);
        
        
        AssociativeArray xData1 = new AssociativeArray();
        xData1.put("height", 188.0);
        xData1.put("weight", 88.0);
        instance.add(new Record(xData1, null));
        
        AssociativeArray xData2 = new AssociativeArray();
        xData2.put("height", 189.0);
        xData2.put("weight", 89.0);
        instance.add(new Record(xData2, null));
        
        AssociativeArray xData3 = new AssociativeArray();
        xData3.put("height", 190.0);
        xData3.put("weight", null);
        instance.add(new Record(xData3, null));
        
        
        FlatDataList expResult = new FlatDataList(Arrays.asList(new Object[]{188.0,189.0,190.0}));
        FlatDataList result = instance.extractColumnValues(column);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractColumnValuesByY method, of class Dataset.
     */
    @Test
    public void testExtractColumnValuesByY() {
        TestUtils.log(this.getClass(), "extractColumnValuesByY");
        RandomSingleton.getInstance().setSeed(TestConfiguration.RANDOM_SEED);
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        Object column = "height";
        Dataset instance = new Dataset(dbConf);
        
        AssociativeArray xData1 = new AssociativeArray();
        xData1.put("height", 188.0);
        xData1.put("weight", 88.0);
        instance.add(new Record(xData1, "Class1"));
        
        AssociativeArray xData2 = new AssociativeArray();
        xData2.put("height", 189.0);
        xData2.put("weight", 89.0);
        instance.add(new Record(xData2, "Class1"));
        
        AssociativeArray xData3 = new AssociativeArray();
        xData3.put("height", 190.0);
        xData3.put("weight", null);
        instance.add(new Record(xData3, "Class2"));
        
        
        TransposeDataList expResult = new TransposeDataList();
        expResult.put("Class1", new FlatDataList(Arrays.asList(new Object[]{188.0,189.0})));
        expResult.put("Class2", new FlatDataList(Arrays.asList(new Object[]{190.0})));
        TransposeDataList result = instance.extractColumnValuesByY(column);
        assertEquals(expResult, result);
    }

}
