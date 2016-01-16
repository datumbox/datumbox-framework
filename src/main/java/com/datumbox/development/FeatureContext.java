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
package com.datumbox.development;

import com.datumbox.development.switchers.SynchronizedBlocks;
import com.datumbox.development.interfaces.Feature;
import java.util.HashMap;
import java.util.Map;

/**
 * This class stores all the active feature switches of the framework which are 
 * used during development.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class FeatureContext {
    
    /**
     * Map that stores all the active feature switches.
     */
    private static final Map<Class<? extends Feature>, Enum> ACTIVE_SWITCHES = new HashMap<>();
    
    /**
     * This static block initializes the ACTIVE_SWITCHES map by adding all the 
     * active features switches along with activated options.
     */
    static {
        ACTIVE_SWITCHES.put(SynchronizedBlocks.class, SynchronizedBlocks.WITH_SYNCHRONIZED);
    }
    
    /**
     * Validates whether the feature is active.
     * 
     * @param obj
     * @return 
     */
    public static boolean isActive(Enum obj) {
        Enum value = ACTIVE_SWITCHES.get((Class)obj.getClass());
        return value != null && value == obj;
    }
    
}
