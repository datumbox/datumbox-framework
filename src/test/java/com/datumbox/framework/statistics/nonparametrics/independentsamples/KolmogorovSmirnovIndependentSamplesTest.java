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
package com.datumbox.framework.statistics.nonparametrics.independentsamples;

import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.TransposeDataList;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bbriniotis
 */
public class KolmogorovSmirnovIndependentSamplesTest {
    
    public KolmogorovSmirnovIndependentSamplesTest() {
    }
    
    /**
     * Test of test method, of class KolmogorovSmirnovIndependentSamples.
     */
    @Test
    public void testTest() {
        System.out.println("test");
        TransposeDataList transposeDataList = new TransposeDataList();
        
        //Synthetic internalData coming the first from normal and the second from logistic. It should reject the null hypothesis
        //transposeDataList.internalData.put("normal", new FlatDataList(Arrays.asList(new Object[]{-0.07,-0.59,-0.04,-0.17,0.39,0.66,-0.31,0.27,0.19,0.24,0.99,0.61,1.25,-0.71,-0.94,-0.07,-0.86,-1.44,-0.76,1.14,-1.07,0.12,-0.75,-0.05,-1.13,2.21,0.74,-0.9,-0.45,-0.82,-0.03,0.7,-1.62,0.01,0.24,-0.83,0.41,1.22,0.16,-0.21,0.94,0.21,-1.14,-0.5,0.7,-1.65,0.59,0.12,0.71,1.63,-0.74,0.45,1.25,0,-0.2,-0.62,-0.16,-0.63,-0.4,0.56,-0.76,0.77,0.14,0.65,-0.05,-1.53,-0.17,1.77,0.79,-0.53,0.09,-1.87,0.14,0.05,-1.52,0.03,2.02,-1.22,-0.45,1.32,1.49,0.04,0.55,0.32,0.87,-0.23,-0.08,-0.42,-0.86,0.78,-1.1,-0.45,-2,1,0.08,0.62,-0.05,-0.49,-0.87,0.19,-1.37,2.64,0.31,0.75,-0.65,0.3,-0.29,1.73,0.08,1.03,-0.12,-0.13,-1.1,0,0.38,0,1.33,0.68,0.42,0.1})));
        //transposeDataList.internalData.put("logistic", new FlatDataList(Arrays.asList(new Object[]{-4.45,0.17,2.37,-1.68,-4.07,3.53,-0.91,0.35,-7.68,-5.49,-4.42,-8.31,2.49,-4.12,3.76,8.68,1.95,-2.11,3.91,-8.77,-5.57,4.02,-2.7,-2.77,2.34,10.09,-2.51,-1.01,-7.66,4.11,-4.98,-7.31,-3.39,-5.61,3.2,3.44,6.62,-0.84,0.96,3.57,-3.05,4.68,-6.9,1.33,3.01,9.04,-1.32,1.27,4.56,-4.78,1.72,13.1,-1.19,1.43,1.09,-1.8,-0.89,8.37,-0.11,-5.74,-1.07,2.48,2.74,4.33,-3.07,-4.37,0.35,-0.61,0.82,-5.62,3.92,-8.76,-3.66,-1.71,3.11,4.48,5.22,3.58,-7.97,-0.29,-2.65,3.93,7.05,-3.5,-1.84,3.48,7.68,-4.9,1.09,-5.06,8.53,1.21,-1.96,2.79,2.78,6.27,-6.99,-0.13,1.81,6.05,-3.45,-2.42,-3.4,15.27,-1.94,3.07,3.12,2.23,8.77,5.89,9.66,1.46,6.77,3.65,-0.41,0.81,-11.63,-3.35,1.99,12.15})));
        //boolean expResult = true;
        
        //It should NOT reject the null hypothesis and return false.
        transposeDataList.put(0, new FlatDataList(Arrays.asList(new Object[]{15.9,15.9,16.0,16.1,16.1,16.2,16.3,16.5,15.5,15.7,15.8})));
        transposeDataList.put(1, new FlatDataList(Arrays.asList(new Object[]{15.1,15.2,15.4,15.6,16.6,16.8,16.9,15.7,16.0,16.3,16.4})));
        boolean expResult = false;
        
        boolean is_twoTailed = true;
        double aLevel = 0.05;
        boolean result = KolmogorovSmirnovIndependentSamples.test(transposeDataList, is_twoTailed, aLevel);
        assertEquals(expResult, result);
    }
    
}
