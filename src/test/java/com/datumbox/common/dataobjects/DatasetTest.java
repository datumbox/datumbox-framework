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
import java.io.FileReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
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
     * Test of parseCSVFile method, of class Dataset.Builder.
     */
    @Test
    public void testParseCSVFile() {
        TestUtils.log(this.getClass(), "parseCSVFile");
        RandomSingleton.getInstance().setSeed(TestConfiguration.RANDOM_SEED);
        DatabaseConfiguration dbConf = TestUtils.getDBConfig();
        
        Reader fileReader;
        try {
            fileReader = new FileReader(Paths.get(TestUtils.getRemoteFile(new URL("http://www.datumbox.com/files/datasets/cities.csv"))).toFile());
        }
        catch(Exception ex) {
            TestUtils.log(this.getClass(), "Unable to download datasets, skipping test.");
            return;
        }
        
        Map<String, TypeInference.DataType> headerDataTypes = new HashMap<>(); 
        headerDataTypes.put("city", TypeInference.DataType.CATEGORICAL);
        headerDataTypes.put("temperature", TypeInference.DataType.NUMERICAL);
        headerDataTypes.put("is_sunny", TypeInference.DataType.BOOLEAN);
        headerDataTypes.put("traffic_rank", TypeInference.DataType.ORDINAL);
        headerDataTypes.put("is_capital", TypeInference.DataType.BOOLEAN);
        headerDataTypes.put("name_of_port", TypeInference.DataType.CATEGORICAL);
        headerDataTypes.put(Dataset.yColumnName, TypeInference.DataType.NUMERICAL);
        
        Dataset dataset = Dataset.Builder.parseCSVFile(fileReader, headerDataTypes, ',', '"', "\r\n", dbConf);
        Dataset expResult = new Dataset(dbConf);
        
        AssociativeArray xData1 = new AssociativeArray();
        xData1.put("city", "Athens");
        xData1.put("temperature", 30.0);
        xData1.put("is_sunny", true);
        xData1.put("traffic_rank", (short)3);
        xData1.put("is_capital", true);
        xData1.put("name_of_port", "Piraeus");
        expResult.add(new Record(xData1, 1.0));
        
        AssociativeArray xData2 = new AssociativeArray();
        xData2.put("city", "London");
        xData2.put("temperature", 14.0);
        xData2.put("is_sunny", false);
        xData2.put("traffic_rank", (short)2);
        xData2.put("is_capital", true);
        xData2.put("name_of_port", "Port of Lond");
        expResult.add(new Record(xData2, null));
        
        AssociativeArray xData3 = new AssociativeArray();
        xData3.put("city", "New York");
        xData3.put("temperature", -12.0);
        xData3.put("is_sunny", true);
        xData3.put("traffic_rank", (short)1);
        xData3.put("is_capital", false);
        xData3.put("name_of_port", "New York's port");
        expResult.add(new Record(xData3, null));
        
        AssociativeArray xData4 = new AssociativeArray();
        xData4.put("city", "Atlantis,	\"the lost city\"");
        xData4.put("temperature", null);
        xData4.put("is_sunny", null);
        xData4.put("traffic_rank", (short)4);
        xData4.put("is_capital", null);
        xData4.put("name_of_port", null);
        expResult.add(new Record(xData4, 4.0));
        
        for(Integer rId : expResult) {
            Record r1 = expResult.get(rId);
            Record r2 = dataset.get(rId);
            
            assertEquals(r1.equals(r2),true);
        }
        
        assertEquals(expResult.getYDataType(),dataset.getYDataType());
        
        assertEquals(expResult.getXDataTypes().equals(dataset.getXDataTypes()),true);
        
        expResult.erase();
        dataset.erase();
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
        
        Dataset dataset = new Dataset(dbConf);
        
        AssociativeArray xData1 = new AssociativeArray();
        xData1.put("1", true);
        dataset.add(new Record(xData1, null));
        
        AssociativeArray xData2 = new AssociativeArray();
        xData2.put("2", 1.0);
        dataset.add(new Record(xData2, null));
        
        AssociativeArray xData3 = new AssociativeArray();
        xData3.put("3", (short)1);
        dataset.add(new Record(xData3, null));
        
        AssociativeArray xData4 = new AssociativeArray();
        xData4.put("4", "s");
        dataset.add(new Record(xData4, null));
        
        Map<Object, TypeInference.DataType> expResult = new LinkedHashMap<>();
        expResult.put("1", TypeInference.DataType.BOOLEAN);
        expResult.put("2", TypeInference.DataType.NUMERICAL);
        expResult.put("3", TypeInference.DataType.ORDINAL);
        expResult.put("4", TypeInference.DataType.CATEGORICAL);
        Map<Object, TypeInference.DataType> result = dataset.getXDataTypes();
        assertEquals(expResult, result);
        
        assertEquals(dataset.getYDataType(), null);
        
        dataset.erase();
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
        Dataset dataset = new Dataset(dbConf);
        
        
        AssociativeArray xData1 = new AssociativeArray();
        xData1.put("height", 188.0);
        xData1.put("weight", 88.0);
        dataset.add(new Record(xData1, null));
        
        AssociativeArray xData2 = new AssociativeArray();
        xData2.put("height", 189.0);
        xData2.put("weight", 89.0);
        dataset.add(new Record(xData2, null));
        
        AssociativeArray xData3 = new AssociativeArray();
        xData3.put("height", 190.0);
        xData3.put("weight", null);
        dataset.add(new Record(xData3, null));
        
        
        FlatDataList expResult = new FlatDataList(Arrays.asList(new Object[]{188.0,189.0,190.0}));
        FlatDataList result = dataset.extractXColumnValues(column);
        assertEquals(expResult, result);
        
        dataset.erase();
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
        Dataset dataset = new Dataset(dbConf);
        
        AssociativeArray xData1 = new AssociativeArray();
        xData1.put("height", 188.0);
        xData1.put("weight", 88.0);
        dataset.add(new Record(xData1, "Class1"));
        
        AssociativeArray xData2 = new AssociativeArray();
        xData2.put("height", 189.0);
        xData2.put("weight", 89.0);
        dataset.add(new Record(xData2, "Class1"));
        
        AssociativeArray xData3 = new AssociativeArray();
        xData3.put("height", 190.0);
        xData3.put("weight", null);
        dataset.add(new Record(xData3, "Class2"));
        
        
        TransposeDataList expResult = new TransposeDataList();
        expResult.put("Class1", new FlatDataList(Arrays.asList(new Object[]{188.0,189.0})));
        expResult.put("Class2", new FlatDataList(Arrays.asList(new Object[]{190.0})));
        TransposeDataList result = dataset.extractXColumnValuesByY(column);
        assertEquals(expResult, result);
        
        dataset.erase();
    }

}
