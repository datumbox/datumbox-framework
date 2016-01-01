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
package com.datumbox.framework.statistics.nonparametrics.independentsamples;

import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.dataobjects.TransposeDataCollection;
import com.datumbox.tests.bases.BaseTest;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class KruskalWallisTest extends BaseTest {

    /**
     * Test of test method, of class KruskalWallis.
     */
    @Test
    public void testTest() {
        logger.info("test");
        TransposeDataCollection transposeDataCollection = new TransposeDataCollection();
        transposeDataCollection.put(0, new FlatDataCollection(Arrays.asList(new Object[]{82,93,86,87,99,95,93,89,96})));
        transposeDataCollection.put(1, new FlatDataCollection(Arrays.asList(new Object[]{81,85,93,91,84,88,84,92,81,92})));
        transposeDataCollection.put(2, new FlatDataCollection(Arrays.asList(new Object[]{97,85,83,93,88,86,90,94,87,93})));
        transposeDataCollection.put(3, new FlatDataCollection(Arrays.asList(new Object[]{93,89,94,96,81,84,80,84,92})));        
        double aLevel = 0.05;
        boolean expResult = false;
        boolean result = KruskalWallis.test(transposeDataCollection, aLevel);
        assertEquals(expResult, result);
    }
    
}
