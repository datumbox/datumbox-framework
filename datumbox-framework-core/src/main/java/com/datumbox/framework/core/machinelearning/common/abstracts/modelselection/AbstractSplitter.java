/**
 * Copyright (C) 2013-2020 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.core.machinelearning.common.abstracts.modelselection;

import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.common.utilities.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Random;

/**
 * The AbstractSplitter class is the base class of all splitters in the framework.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public abstract class AbstractSplitter {

    /**
     * The Split class serves as a tuple class that contains the train and test data of the split.
     */
    public static class Split {
        private final Dataframe train;
        private final Dataframe test;

        /**
         * Protected constructor.
         *
         * @param train
         * @param test
         */
        public Split(Dataframe train, Dataframe test) {
            this.train = train;
            this.test = test;
        }

        /**
         * Getter for the training data.
         *
         * @return
         */
        public Dataframe getTrain() {
            return train;
        }

        /**
         * Getter for the test data.
         *
         * @return
         */
        public Dataframe getTest() {
            return test;
        }
    }

    protected final Random random;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Default constructor for the Splitter.
     */
    public AbstractSplitter() {
        this(RandomGenerator.getThreadLocalRandom());
    }

    /**
     * Constructor that received a random generator.
     *
     * @param random
     */
    public AbstractSplitter(Random random) {
        this.random = random;
    }

    /**
     * Produces a series of train/test splits on the provided dataframe.
     *
     * @param dataset
     * @return
     */
    public abstract Iterator<Split> split(Dataframe dataset);

}
