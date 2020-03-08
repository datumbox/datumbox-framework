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

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class contains a number of helper methods for Java streams.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class StreamMethods {
    
    /**
     * Converts an spliterator to a stream.
     * 
     * @param <T>
     * @param spliterator
     * @param parallel
     * @return 
     */
    public static <T> Stream<T> stream(Spliterator<T> spliterator, boolean parallel) {
        return StreamSupport.<T>stream(spliterator, parallel);
    }
    
    /**
     * Converts an iterable to a stream.
     * 
     * @param <T>
     * @param iterable
     * @param parallel
     * @return 
     */
    public static <T> Stream<T> stream(Iterable<T> iterable, boolean parallel) {
        return StreamSupport.<T>stream(iterable.spliterator(), parallel);
    }
    
    /**
     * Converts an Stream to parallel or sequential .
     * 
     * @param <T>
     * @param stream
     * @param parallel
     * @return 
     */
    public static <T> Stream<T> stream(Stream<T> stream, boolean parallel) {
        if(parallel) {
            return stream.parallel();
        }
        else {
            return stream.sequential();
        }
    }
    
    /**
     * Converts an iterator to a stream.
     * 
     * @param <T>
     * @param iterator
     * @param parallel
     * @return 
     */
    public static <T> Stream<T> stream(Iterator<T> iterator, boolean parallel) {
        Iterable<T> iterable = () -> iterator;
        return StreamSupport.<T>stream(iterable.spliterator(), parallel);
    }
    
    /**
     * Takes a stream and enumerates it (similar to Python's enumerate()) to return
     * the item along with its position.
     * 
     * @param <T>
     * @param stream
     * @return 
     */
    public static <T> Stream<Map.Entry<Integer, T>> enumerate(Stream<T> stream) {
        Iterator<Map.Entry<Integer, T>> iterator = new Iterator<Map.Entry<Integer, T>>() {
            private int counter = 0;
            
            private final Iterator<T> internalIterator = stream.iterator();
            
            /** {@inheritDoc} */
            @Override
            public boolean hasNext() {
                return internalIterator.hasNext();
            }
            
            /** {@inheritDoc} */
            @Override
            public Map.Entry<Integer, T> next() {
                return new AbstractMap.SimpleImmutableEntry<>(counter++, internalIterator.next());
            }

            /** {@inheritDoc} */
            @Override
            public void remove() {
                throw new UnsupportedOperationException("This is a read-only iterator, remove operation is not supported.");
            }
        };
        return stream(iterator, stream.isParallel());
    }
    
}
