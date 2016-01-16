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
package com.datumbox.tests.abstracts;

import com.datumbox.common.utilities.RandomGenerator;
import com.datumbox.tests.Constants;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The abstract class for all the Tests of the framework.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public abstract class AbstractTest {
    /**
     * The Logger of all Tests.
     * We want this to be non-static in order to print the names of the inherited classes.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    /**
     * Public constructor of the Tests. It sets up the global seed in the RandomGenerator.
     */
    public AbstractTest() {
        //Set the global seed (Optional)
        RandomGenerator.setGlobalSeed(Constants.RANDOM_SEED);
    }
    
    /**
     * This method is executed before every test. It resets the seed of the
     * local Random to the original seed.
     */
    @Before
    public void setUp() {
        //Reset the seed of the local Random before every method execution
        RandomGenerator.getThreadLocalRandom().setSeed(Constants.RANDOM_SEED);
    }
}
