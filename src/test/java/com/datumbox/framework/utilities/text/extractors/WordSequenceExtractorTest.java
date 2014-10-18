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
package com.datumbox.framework.utilities.text.extractors;

import com.datumbox.framework.utilities.text.extractors.WordSequenceExtractor;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class WordSequenceExtractorTest {
    
    public WordSequenceExtractorTest() {
    }

    /**
     * Test of extract method, of class WordSequenceExtractor.
     */
    @Test
    public void testExtract() {
        System.out.println("extract");
        String text = "this is a text sequence that is amazing text sequence";
        WordSequenceExtractor instance = new WordSequenceExtractor();
        instance.setParameters(new WordSequenceExtractor.Parameters());
        Map<Integer, String> expResult = new LinkedHashMap<>();
        expResult.put(0, "this");
        expResult.put(1, "is");
        expResult.put(2, "a");
        expResult.put(3, "text");
        expResult.put(4, "sequence");
        expResult.put(5, "that");
        expResult.put(6, "is");
        expResult.put(7, "amazing");
        expResult.put(8, "text");
        expResult.put(9, "sequence");
        
        Map<Integer, String> result = instance.extract(text);
        assertEquals(expResult, result);
    }
    
}
