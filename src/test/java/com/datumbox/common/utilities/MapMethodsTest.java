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
package com.datumbox.common.utilities;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.tests.abstracts.AbstractTest;
import java.util.AbstractMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for MapMethods.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MapMethodsTest extends AbstractTest {
    
    /**
     * Test of selectMaxKeyValue method, of class MapMethods.
     */
    @Test
    public void testSelectMaxKeyValue() {
        logger.info("selectMaxKeyValue");
        AssociativeArray keyValueMap = new AssociativeArray();
        keyValueMap.put("1", 1.0);
        keyValueMap.put("2", 2.0);
        keyValueMap.put("5", 5.0);
        keyValueMap.put("3", 3.0);
        keyValueMap.put("4", 4.0);
        
        Map.Entry<Object, Object> expResult = new AbstractMap.SimpleEntry<>("5", 5.0);
        Map.Entry<Object, Object> result = MapMethods.selectMaxKeyValue(keyValueMap);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of selectMinKeyValue method, of class MapMethods.
     */
    @Test
    public void testSelectMinKeyValue() {
        logger.info("selectMaxKeyValue");
        AssociativeArray keyValueMap = new AssociativeArray();
        keyValueMap.put("1", 1);
        keyValueMap.put("2", 2);
        keyValueMap.put("5", 5);
        keyValueMap.put("3", 3);
        keyValueMap.put("4", 4);
        
        Map.Entry<Object, Object> expResult = new AbstractMap.SimpleEntry<>("1", 1);
        Map.Entry<Object, Object> result = MapMethods.selectMinKeyValue(keyValueMap);
        assertEquals(expResult, result);
    }
    
}
