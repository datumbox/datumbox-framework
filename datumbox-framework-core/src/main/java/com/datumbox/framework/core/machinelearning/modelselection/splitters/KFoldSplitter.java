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

import java.util.*;

/**
 * The KFoldSplitter partitions the data in K folds.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class KFoldSplitter extends AbstractSplitter {

    private final int k;

    /**
     * The default constructor which receives the number of folds.
     *
     * @param k
     */
    public KFoldSplitter(int k) {
        super();
        this.k = k;
    }

    /**
     * The auxiliary constructor which receives the number of folds and a random generator.
     *
     * @param k
     * @param random
     */
    public KFoldSplitter(int k, Random random) {
        super(random);
        this.k = k;
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<KFoldSplitter.Split> split(final Dataframe dataset) {
        final int n = dataset.size();
        if(k<=0 || n<=k) {
            throw new IllegalArgumentException("Invalid number of folds.");
        }
        else if(k == 1) {
            //by convention we the train and test datasets are the same. we need to copy both of them to ensure the original data won't be modified.
            return Arrays.asList(new Split(dataset.copy(), dataset.copy())).iterator();
        }

        //shuffle the ids of the records
        final Integer[] ids = new Integer[n];
        int j = 0;
        for(Integer rId : dataset.index()) {
            ids[j++]=rId;
        }
        PHPMethods.shuffle(ids, random);

        //estimate the size of fold. we floor the number here
        final int foldSize= n/k;

        return new Iterator<Split>() {

            private int counter = 0;

            /** {@inheritDoc} */
            @Override
            public boolean hasNext() {
                return counter < k;
            }

            /** {@inheritDoc} */
            @Override
            public Split next() {
                logger.info("Kfold {}", counter);

                //We consider as fold window the part of the ids which are used for test
                FlatDataList trainIds = new FlatDataList(new ArrayList<>(n-foldSize));
                FlatDataList testIds = new FlatDataList(new ArrayList<>(foldSize));

                for(int i=0;i<n;i++) {
                    if(counter*foldSize<=i && i<(counter+1)*foldSize) {
                        testIds.add(ids[i]);
                    }
                    else {
                        trainIds.add(ids[i]);
                    }
                }

                counter++;

                return new Split(dataset.getSubset(trainIds), dataset.getSubset(testIds));
            }
        };
    }
}
