/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
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

package com.datumbox.tests.bases;

import com.datumbox.common.utilities.RandomGenerator;
import com.datumbox.configuration.TestConfiguration;
import org.junit.Before;

/**
 * The base abstract class for all the Tests of the framework.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public abstract class BaseTest {
    
    public BaseTest() {
        //Set the global seed (Optional)
        RandomGenerator.setGlobalSeed(TestConfiguration.RANDOM_SEED);
    }
    
    @Before
    public void setUp() {
        //Reset the seed of the local Random before every method execution
        RandomGenerator.getThreadLocalRandom().setSeed(TestConfiguration.RANDOM_SEED);
    }
}
