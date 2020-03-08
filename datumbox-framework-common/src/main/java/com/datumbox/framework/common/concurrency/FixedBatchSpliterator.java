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
package com.datumbox.framework.common.concurrency;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.util.Spliterators.spliterator;

/**
 * This class can be used to process an Iterator in a Batched manner.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class FixedBatchSpliterator<T> extends FixedBatchSpliteratorBase<T> {

    /**
     * The internal iterator.
     */
    private final Iterator<T> it;

    /**
     * Public constructor.
     *
     * @param it
     * @param batchSize
     */
    public FixedBatchSpliterator(Iterator<T> it, int batchSize) {
        super(IMMUTABLE | ORDERED | NONNULL, batchSize);
        this.it = it;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (action == null) {
            throw new NullPointerException();
        }
        if(it.hasNext() == false) {
            return false;
        }
        action.accept(it.next());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        if (action == null) {
            throw new NullPointerException();
        }
        while(it.hasNext()) {
            action.accept(it.next());
        }
    }

}

/**
 * The Fixed Batch Spliterator Base class.
 *
 * Modified code from: https://www.airpair.com/java/posts/parallel-processing-of-io-based-data-with-java-streams
 *
 * @param <T>
 */
abstract class FixedBatchSpliteratorBase<T> implements Spliterator<T> {
    /**
     * The size of batches.
     */
    private final int batchSize;

    /**
     * The characteristics of the Spliterator.
     */
    private final int characteristics;

    /**
     * The estimated number of remaining elements.
     */
    private long est;

    /**
     * Public three-parameter constructor.
     *
     * @param characteristics
     * @param batchSize
     * @param est
     */
    public FixedBatchSpliteratorBase(int characteristics, int batchSize, long est) {
        this.characteristics = characteristics | SUBSIZED;
        this.batchSize = batchSize;
        this.est = est;
    }

    /**
     * Public two-parameter constructor.
     *
     * @param characteristics
     * @param batchSize
     */
    public FixedBatchSpliteratorBase(int characteristics, int batchSize) {
        this(characteristics, batchSize, Long.MAX_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Spliterator<T> trySplit() {
        final HoldingConsumer<T> holder = new HoldingConsumer<>();
        if(!tryAdvance(holder)) {
            return null;
        }

        final Object[] a = new Object[batchSize];
        int j = 0;
        do {
            a[j] = holder.value;

        }
        while (++j < batchSize && tryAdvance(holder));

        if(est != Long.MAX_VALUE) {
            est -= j;
        }

        return spliterator(a, 0, j, characteristics() | SIZED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparator<? super T> getComparator() {
        if(hasCharacteristics(SORTED)) {
            return null;
        }
        throw new IllegalStateException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long estimateSize() {
        return est;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int characteristics() {
        return characteristics;
    }

    /**
     * Holding consumer class.
     *
     * @param <T>
     */
    static final class HoldingConsumer<T> implements Consumer<T> {
        Object value;

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(T value) {
            this.value = value;
        }
    }
}