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
package com.datumbox.framework.utilities.text.analysis;

import com.datumbox.framework.utilities.text.analysis.PHPSimilarText;
import com.datumbox.configuration.TestConfiguration;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class PHPSimilarTextTest {
    
    public PHPSimilarTextTest() {
    }

    /**
     * Test of similarityChars method, of class PHPSimilarText.
     */
    @Test
    public void testSimilarityChars() {
        System.out.println("similarityChars");
        String txt1 = "this is a fine text";
        String txt2 = "this is a great document";
        int expResult = 12;
        int result = PHPSimilarText.similarityChars(txt1, txt2);
        assertEquals(expResult, result);
    }

    /**
     * Test of similarityPercentage method, of class PHPSimilarText.
     */
    @Test
    public void testSimilarityPercentage() {
        System.out.println("similarityPercentage");
        String txt1 = "this is a fine text";
        String txt2 = "this is a great document";
        double expResult = 55.813953488372;
        double result = PHPSimilarText.similarityPercentage(txt1, txt2);
        assertEquals(expResult, result, TestConfiguration.DOUBLE_ACCURACY_HIGH);
    }
    
}
