/* 
 * Copyright (C) 2014 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
package com.datumbox.common.dataobjects;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class DatasetTest {
    
    public DatasetTest() {
    }
    
    /**
     * Test of copyCollection2Array method, of class Dataset.
     */
    @Test
    public void testCopyCollection2Array() {
        System.out.println("copyCollection2Array");
        FlatDataCollection flatDataCollection = new FlatDataCollection(Arrays.asList(new Object[]{1,2,3,4,5}));
        Object[] expResult = new Object[]{1,2,3,4,5};
        Object[] result = Dataset.<Object>copyCollection2Array(Object.class, flatDataCollection);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of getRanksFromValues method, of class Dataset.
     */
    @Test
    public void testGetRanksFromValues() {
        System.out.println("getRanksFromValues");
        FlatDataList flatDataCollection =  new FlatDataList(Arrays.asList(new Object[]{50,10,10,30,40}));
        FlatDataList expResult = new FlatDataList(Arrays.asList(new Object[]{5.0,1.5,1.5,3.0,4.0}));
        AssociativeArray expResult2 = new AssociativeArray(new ConcurrentSkipListMap<>());
        expResult2.put(10, 2);
        AssociativeArray tiesCounter = Dataset.getRanksFromValues(flatDataCollection);
        assertEquals(expResult, flatDataCollection);
        assertEquals(expResult2, tiesCounter);
    }

    /**
     * Test of getColumns method, of class Dataset.
     */
    @Test
    public void testGetColumns() {
        System.out.println("getColumns");
        Dataset instance = new Dataset();
        
        
        Record rec1 = new Record();
        rec1.getX().put("1", true);
        instance.add(rec1);
        
        Record rec2 = new Record();
        rec2.getX().put("2", 1.0);
        instance.add(rec2);
        
        Record rec3 = new Record();
        rec3.getX().put("3", (short)1);
        instance.add(rec3);
        
        Record rec4 = new Record();
        rec4.getX().put("4", "s");
        instance.add(rec4);
        
        Map<Object, Dataset.ColumnType> expResult = new HashMap<>();
        expResult.put("1", Dataset.ColumnType.DUMMYVAR);
        expResult.put("2", Dataset.ColumnType.NUMERICAL);
        expResult.put("3", Dataset.ColumnType.ORDINAL);
        expResult.put("4", Dataset.ColumnType.CATEGORICAL);
        Map<Object, Dataset.ColumnType> result = instance.getColumns();
        assertEquals(expResult, result);
    }

    /**
     * Test of convert2Sparse method, of class Dataset.
     */
    @Test
    public void testConvert2Sparse() {
        System.out.println("convert2Sparse");
        Dataset instance = new Dataset();
        
        
        Record rec1 = new Record();
        rec1.getX().put("1", true);
        instance.add(rec1);
        
        Record rec2 = new Record();
        rec2.getX().put("2", 1.0);
        instance.add(rec2);
        
        Record rec3 = new Record();
        rec3.getX().put("3", 1);
        instance.add(rec3);
        
        instance.convert2Sparse();
        
        boolean allRecordsHaveAllColumns = true;
        int numberOfColumns = instance.getColumns().size();
        
        for(Record r : instance) {
            if(r.getX().size()!=numberOfColumns) {
                allRecordsHaveAllColumns=false;
                break;
            }
        }
        
        assertTrue(allRecordsHaveAllColumns);
    }

    /**
     * Test of extractColumnValues method, of class Dataset.
     */
    @Test
    public void testExtractColumnValues() {
        System.out.println("extractColumnValues");
        Object column = "height";
        Dataset instance = new Dataset();
        
        
        Record rec1 = new Record();
        rec1.getX().put("height", 188.0);
        rec1.getX().put("weight", 88.0);
        instance.add(rec1);
        
        Record rec2 = new Record();
        rec2.getX().put("height", 189.0);
        rec2.getX().put("weight", 89.0);
        instance.add(rec2);
        
        Record rec3 = new Record();
        rec3.getX().put("height", 190.0);
        rec3.getX().put("weight", null);
        instance.add(rec3);
        
        
        FlatDataList expResult = new FlatDataList(Arrays.asList(new Object[]{188.0,189.0,190.0}));
        FlatDataList result = instance.extractColumnValues(column);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractColumnValuesByY method, of class Dataset.
     */
    @Test
    public void testExtractColumnValuesByY() {
        System.out.println("extractColumnValuesByY");
        Object column = "height";
        Dataset instance = new Dataset();
        
        Record rec1 = new Record();
        rec1.getX().put("height", 188.0);
        rec1.getX().put("weight", 88.0);
        rec1.setY("Class1");
        instance.add(rec1);
        
        Record rec2 = new Record();
        rec2.getX().put("height", 189.0);
        rec2.getX().put("weight", 89.0);
        rec2.setY("Class1");
        instance.add(rec2);
        
        Record rec3 = new Record();
        rec3.getX().put("height", 190.0);
        rec3.getX().put("weight", null);
        rec3.setY("Class2");
        instance.add(rec3);
        
        TransposeDataList expResult = new TransposeDataList();
        expResult.put("Class1", new FlatDataList(Arrays.asList(new Object[]{188.0,189.0})));
        expResult.put("Class2", new FlatDataList(Arrays.asList(new Object[]{190.0})));
        TransposeDataList result = instance.extractColumnValuesByY(column);
        assertEquals(expResult, result);
    }

}
