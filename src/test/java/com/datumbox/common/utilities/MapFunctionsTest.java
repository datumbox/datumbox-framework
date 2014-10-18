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
package com.datumbox.common.utilities;

import com.datumbox.common.dataobjects.AssociativeArray;
import java.util.AbstractMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class MapFunctionsTest {
    
    public MapFunctionsTest() {
    }
    
    /**
     * Test of selectMaxKeyValue method, of class MapFunctions.
     */
    @Test
    public void testSelectMaxKeyValue() {
        System.out.println("selectMaxKeyValue");
        AssociativeArray keyValueMap = new AssociativeArray();
        keyValueMap.put("1", 1.0);
        keyValueMap.put("2", 2.0);
        keyValueMap.put("5", 5.0);
        keyValueMap.put("3", 3.0);
        keyValueMap.put("4", 4.0);
        
        Map.Entry<Object, Object> expResult = new AbstractMap.SimpleEntry<>("5", 5.0);
        Map.Entry<Object, Object> result = MapFunctions.selectMaxKeyValue(keyValueMap);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of selectMinKeyValue method, of class MapFunctions.
     */
    @Test
    public void testSelectMinKeyValue() {
        System.out.println("selectMaxKeyValue");
        AssociativeArray keyValueMap = new AssociativeArray();
        keyValueMap.put("1", 1);
        keyValueMap.put("2", 2);
        keyValueMap.put("5", 5);
        keyValueMap.put("3", 3);
        keyValueMap.put("4", 4);
        
        Map.Entry<Object, Object> expResult = new AbstractMap.SimpleEntry<>("1", 1);
        Map.Entry<Object, Object> result = MapFunctions.selectMinKeyValue(keyValueMap);
        assertEquals(expResult, result);
    }
    
}
