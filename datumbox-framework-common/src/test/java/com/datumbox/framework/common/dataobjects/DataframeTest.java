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
package com.datumbox.framework.common.dataobjects;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Test cases for Dataframe.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class DataframeTest extends AbstractTest {
    
    /**
     * Test of parseCSVFile method, of class Dataframe.Builder.
     */
    @Test
    public void testParseCSVFile() {
        logger.info("parseCSVFile");
        
        Configuration conf = Configuration.getConfiguration();
        
        
        LinkedHashMap<String, TypeInference.DataType> headerDataTypes = new LinkedHashMap<>(); 
        headerDataTypes.put("city", TypeInference.DataType.CATEGORICAL);
        headerDataTypes.put("temperature", TypeInference.DataType.NUMERICAL);
        headerDataTypes.put("is_sunny", TypeInference.DataType.BOOLEAN);
        headerDataTypes.put("traffic_rank", TypeInference.DataType.ORDINAL);
        headerDataTypes.put("is_capital", TypeInference.DataType.BOOLEAN);
        headerDataTypes.put("name_of_port", TypeInference.DataType.CATEGORICAL);
        headerDataTypes.put("metro_population", TypeInference.DataType.NUMERICAL);
        
        Dataframe dataset;
        try (Reader fileReader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("datasets/cities.csv"), "UTF-8")) {
            dataset = Dataframe.Builder.parseCSVFile(fileReader, "metro_population", headerDataTypes, ',', '"', "\r\n", null, null, conf);
        }
        catch(UncheckedIOException | IOException ex) {
            logger.warn("Unable to download datasets, skipping test.");
            throw new RuntimeException(ex);
        }
        
        Dataframe expResult = new Dataframe(conf);
        
        AssociativeArray xData1 = new AssociativeArray();
        xData1.put("city", "Athens");
        xData1.put("temperature", 30.0);
        xData1.put("is_sunny", true);
        xData1.put("traffic_rank", (short)3);
        xData1.put("is_capital", true);
        xData1.put("name_of_port", "Piraeus");
        expResult.add(new Record(xData1, 3753783.0));
        
        AssociativeArray xData2 = new AssociativeArray();
        xData2.put("city", "London");
        xData2.put("temperature", 14.0);
        xData2.put("is_sunny", false);
        xData2.put("traffic_rank", (short)2);
        xData2.put("is_capital", true);
        xData2.put("name_of_port", "Port of London");
        expResult.add(new Record(xData2, 13614409.0));
        
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
        expResult.add(new Record(xData4, null));
        
        Iterator<Record> it1 = expResult.iterator();
        Iterator<Record> it2 = dataset.iterator();
        
        while(it1.hasNext() && it2.hasNext()) {
            assertEquals(it1.next().equals(it2.next()),true);
        }
        
        assertEquals(it1.hasNext(),it2.hasNext()); //check that both finished
        
        assertEquals(expResult.getYDataType(),dataset.getYDataType());
        
        assertEquals(expResult.getXDataTypes().equals(dataset.getXDataTypes()),true);
        
        expResult.delete();
        dataset.delete();
    }
    
    /**
     * Test of copyCollection2Array method, of class Dataframe.
     */
    @Test
    public void testCopyCollection2Array() {
        logger.info("copyCollection2Array");
        FlatDataCollection flatDataCollection = new FlatDataCollection(Arrays.asList(new Object[]{1,2,3,4,5}));
        Object[] expResult = new Object[]{1,2,3,4,5};
        Object[] result = flatDataCollection.<Object>copyCollection2Array(Object.class);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of getColumns method, of class Dataframe.
     */
    @Test
    public void testGetColumns() {
        logger.info("getColumns");
        
        Configuration conf = Configuration.getConfiguration();
        
        Dataframe dataset = new Dataframe(conf);
        
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
        
        dataset.delete();
    }

    /**
     * Test of extractColumnValues method, of class Dataframe.
     */
    @Test
    public void testExtractColumnValues() {
        logger.info("extractColumnValues");
        
        Configuration conf = Configuration.getConfiguration();
        
        Object column = "height";
        Dataframe dataset = new Dataframe(conf);
        
        
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
        FlatDataList result = dataset.getXColumn(column);
        assertEquals(expResult, result);
        
        dataset.delete();
    }

    /**
     * Test of extractColumnValuesByY method, of class Dataframe.
     */
    @Test
    public void testExtractColumnValuesByY() {
        logger.info("extractColumnValuesByY");
        
        Configuration conf = Configuration.getConfiguration();
        
        Object column = "height";
        Dataframe dataset = new Dataframe(conf);
        
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
        
        dataset.delete();
    }

    /**
     * Test of remove method, of class Dataframe.
     */
    @Test
    public void testRemove() {
        logger.info("remove");
        
        Configuration conf = Configuration.getConfiguration();
        
        Dataframe dataset = new Dataframe(conf);
        
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
        
        dataset.remove(2);
        
        assertEquals(3, dataset.size());
        
        dataset.delete();
    }

}
