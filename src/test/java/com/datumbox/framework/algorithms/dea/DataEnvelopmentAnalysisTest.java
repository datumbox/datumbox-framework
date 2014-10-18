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
package com.datumbox.framework.algorithms.dea;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.tests.utilities.TestUtils;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class DataEnvelopmentAnalysisTest {
    
    public DataEnvelopmentAnalysisTest() {
    }

    /**
     * Test of estimateEfficiency method, of class DataEnvelopmentAnalysis.
     */
    @Test
    public void testEstimateEfficiency() throws Exception {
        System.out.println("estimateEfficiency");
        Map<Object, DataEnvelopmentAnalysis.DeaRecord> id2DeaRecordMapDatabase = new LinkedHashMap<>();
        id2DeaRecordMapDatabase.put("Depot1", new DataEnvelopmentAnalysis.DeaRecord(new FlatDataList(Arrays.asList(40.0,55.0,30.0)), new FlatDataList(Arrays.asList(3.0,5.0))));
        id2DeaRecordMapDatabase.put("Depot2", new DataEnvelopmentAnalysis.DeaRecord(new FlatDataList(Arrays.asList(45.0,50.0,40.0)), new FlatDataList(Arrays.asList(2.5,4.5))));
        id2DeaRecordMapDatabase.put("Depot3", new DataEnvelopmentAnalysis.DeaRecord(new FlatDataList(Arrays.asList(55.0,45.0,30.0)), new FlatDataList(Arrays.asList(4.0,6.0))));
        id2DeaRecordMapDatabase.put("Depot4", new DataEnvelopmentAnalysis.DeaRecord(new FlatDataList(Arrays.asList(48.0,20.0,60.0)), new FlatDataList(Arrays.asList(6.0,7.0))));
        id2DeaRecordMapDatabase.put("Depot5", new DataEnvelopmentAnalysis.DeaRecord(new FlatDataList(Arrays.asList(28.0,50.0,25.0)), new FlatDataList(Arrays.asList(2.3,3.5))));
        id2DeaRecordMapDatabase.put("Depot6", new DataEnvelopmentAnalysis.DeaRecord(new FlatDataList(Arrays.asList(48.0,20.0,65.0)), new FlatDataList(Arrays.asList(4.0,6.5))));
        id2DeaRecordMapDatabase.put("Depot7", new DataEnvelopmentAnalysis.DeaRecord(new FlatDataList(Arrays.asList(80.0,65.0,57.0)), new FlatDataList(Arrays.asList(7.0,10.0))));
        id2DeaRecordMapDatabase.put("Depot8", new DataEnvelopmentAnalysis.DeaRecord(new FlatDataList(Arrays.asList(25.0,48.0,30.0)), new FlatDataList(Arrays.asList(4.4,6.4))));
        id2DeaRecordMapDatabase.put("Depot9", new DataEnvelopmentAnalysis.DeaRecord(new FlatDataList(Arrays.asList(45.0,64.0,42.0)), new FlatDataList(Arrays.asList(3.0,5.0))));
        id2DeaRecordMapDatabase.put("Depot10", new DataEnvelopmentAnalysis.DeaRecord(new FlatDataList(Arrays.asList(70.0,65.0,48.0)), new FlatDataList(Arrays.asList(5.0,7.0))));
        id2DeaRecordMapDatabase.put("Depot11", new DataEnvelopmentAnalysis.DeaRecord(new FlatDataList(Arrays.asList(45.0,65.0,40.0)), new FlatDataList(Arrays.asList(5.0,7.0))));
        id2DeaRecordMapDatabase.put("Depot12", new DataEnvelopmentAnalysis.DeaRecord(new FlatDataList(Arrays.asList(45.0,40.0,44.0)), new FlatDataList(Arrays.asList(2.0,4.0))));
        id2DeaRecordMapDatabase.put("Depot13", new DataEnvelopmentAnalysis.DeaRecord(new FlatDataList(Arrays.asList(65.0,25.0,35.0)), new FlatDataList(Arrays.asList(5.0,7.0))));
        id2DeaRecordMapDatabase.put("Depot14", new DataEnvelopmentAnalysis.DeaRecord(new FlatDataList(Arrays.asList(38.0,18.0,64.0)), new FlatDataList(Arrays.asList(4.0,4.0))));
        id2DeaRecordMapDatabase.put("Depot15", new DataEnvelopmentAnalysis.DeaRecord(new FlatDataList(Arrays.asList(20.0,50.0,15.0)), new FlatDataList(Arrays.asList(2.0,3.0))));
        id2DeaRecordMapDatabase.put("Depot16", new DataEnvelopmentAnalysis.DeaRecord(new FlatDataList(Arrays.asList(38.0,20.0,60.0)), new FlatDataList(Arrays.asList(3.0,6.0))));
        id2DeaRecordMapDatabase.put("Depot17", new DataEnvelopmentAnalysis.DeaRecord(new FlatDataList(Arrays.asList(68.0,64.0,54.0)), new FlatDataList(Arrays.asList(7.0,11.0))));
        id2DeaRecordMapDatabase.put("Depot18", new DataEnvelopmentAnalysis.DeaRecord(new FlatDataList(Arrays.asList(25.0,38.0,20.0)), new FlatDataList(Arrays.asList(4.0,6.0))));
        id2DeaRecordMapDatabase.put("Depot19", new DataEnvelopmentAnalysis.DeaRecord(new FlatDataList(Arrays.asList(45.0,67.0,32.0)), new FlatDataList(Arrays.asList(3.0,4.0))));
        id2DeaRecordMapDatabase.put("Depot20", new DataEnvelopmentAnalysis.DeaRecord(new FlatDataList(Arrays.asList(57.0,60.0,40.0)), new FlatDataList(Arrays.asList(5.0,6.0))));
        
        //the list of observations that we want to evaluate
        Map<Object, DataEnvelopmentAnalysis.DeaRecord> id2DeaRecordMapEvaluation = new LinkedHashMap<>(id2DeaRecordMapDatabase);
        
        DataEnvelopmentAnalysis instance = new DataEnvelopmentAnalysis();
        AssociativeArray expResult = new AssociativeArray(new LinkedHashMap<>());
        
        expResult.put("Depot1", 0.82038345105954);
        expResult.put("Depot2", 0.94174174174174);
        expResult.put("Depot3", 0.81481481481481);
        expResult.put("Depot4", 0.65279091769158);
        expResult.put("Depot5", 0.94655825212588);
        expResult.put("Depot6", 0.82278481012658);
        expResult.put("Depot7", 0.71111111111111);
        expResult.put("Depot8", 0.51685181698362);
        expResult.put("Depot9", 0.96344285053216);
        expResult.put("Depot10", 0.88888888888889);
        expResult.put("Depot11", 0.63128611170554);
        expResult.put("Depot12", 1);
        expResult.put("Depot13", 0.82539682539683);
        expResult.put("Depot14", 1);
        expResult.put("Depot15", 1);
        expResult.put("Depot16", 0.90909090909091);
        expResult.put("Depot17", 0.54949494949495);
        expResult.put("Depot18", 0.42007168458781);
        expResult.put("Depot19", 1);
        expResult.put("Depot20", 0.84444444444444);
        
        
        AssociativeArray result = instance.estimateEfficiency(id2DeaRecordMapDatabase, id2DeaRecordMapEvaluation);
        TestUtils.assetDoubleAssociativeArray(expResult, result);
    }
    
}
