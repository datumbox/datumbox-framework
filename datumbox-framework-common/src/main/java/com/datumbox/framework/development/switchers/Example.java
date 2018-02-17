/**
 * Copyright (C) 2013-2018 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.development.switchers;

import com.datumbox.framework.development.FeatureContext;
import com.datumbox.framework.development.interfaces.Feature;

/**
 * Example of a Feature Switch.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public enum Example implements Feature {
    
    /**
     * 1st Option.
     */
    OPTION1,
    
    /**
     * 2nd Option.
     */
    OPTION2;
    
    /** {@inheritDoc} */
    @Override
    public boolean isActivated() {
        return FeatureContext.isActive(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return name();
    }
}
