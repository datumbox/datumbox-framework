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
package com.datumbox.framework.core.machinelearning.modelselection.splitters;

import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.FlatDataList;
import com.datumbox.framework.core.common.utilities.PHPMethods;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelselection.AbstractSplitter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * The ShuffleSplitter partitions the data in train/test splits.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ShuffleSplitter extends AbstractSplitter {

    private final double proportion;
    private final int splits;

    /**
     * The default constructor which receives the train size and the number of splits.
     *
     * @param proportion: the proportion of the dataset in the train split
     * @param splits: number of reshuffling and splitting iterations
     */
    public ShuffleSplitter(double proportion, int splits) {
        super();
        this.proportion = proportion;
        this.splits = splits;
    }

    /**
     * The auxiliary constructor which receives the train size, the number of splits and a random generator.
     *
     * @param proportion: the proportion of the dataset in the train split
     * @param splits: number of reshuffling and splitting iterations
     */
    public ShuffleSplitter(double proportion, int splits, Random random) {
        super(random);
        this.proportion = proportion;
        this.splits = splits;
    }

    @Override
    public Iterator<ShuffleSplitter.Split> split(Dataframe dataset) {
        final int n = dataset.size();
        if(proportion <=0.0 || proportion >=1.0) {
            throw new IllegalArgumentException("The train size should be between 0.0 and 1.0.");
        }

        final int trainSize = (int) (n*proportion);

        return new Iterator<Split>() {

            private int counter = 0;

            /** {@inheritDoc} */
            @Override
            public boolean hasNext() {
                return counter < splits;
            }

            /** {@inheritDoc} */
            @Override
            public Split next() {
                logger.info("Split {}", counter);

                //shuffle the ids of the records
                final Integer[] ids = new Integer[n];
                int j = 0;
                for(Integer rId : dataset.index()) {
                    ids[j++]=rId;
                }
                PHPMethods.shuffle(ids, random);

                FlatDataList trainIds = new FlatDataList(new ArrayList<>(trainSize));
                for(int i=0;i<trainSize;i++) {
                    trainIds.add(ids[i]);
                }

                FlatDataList testIds = new FlatDataList(new ArrayList<>(n-trainSize));
                for(int i=trainSize;i<n;i++) {
                    testIds.add(ids[i]);
                }

                counter++;

                return new Split(dataset.getSubset(trainIds), dataset.getSubset(testIds));
            }
        };
    }
}
