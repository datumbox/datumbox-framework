/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class TypeConversions {

    /**
     * Converts safely any Number to Double.
     * @param o
     * @return
     */
    public static Double toDouble(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Boolean) {
            return ((Boolean) o) ? 1.0 : 0.0;
        }
        return ((Number) o).doubleValue();
    }

    /**
     * Converts safely any Number to Integer.
     * @param o
     * @return
     */
    public static Integer toInteger(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Boolean) {
            return ((Boolean) o) ? 1 : 0;
        }
        return ((Number) o).intValue();
    }
    
}
