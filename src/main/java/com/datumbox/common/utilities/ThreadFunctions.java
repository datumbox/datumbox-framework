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
package com.datumbox.common.utilities;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class contains a number of helper methods for Java 8 streams and
 * Threads.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ThreadFunctions {
    /**
     * Converts an iterable to a stream.
     * 
     * @param <T>
     * @param in
     * @return 
     */
    public static <T> Stream<T> stream(Iterable<T> in) {
        return StreamSupport.stream(in.spliterator(), false);
    }
    
    /**
     * Converts an iterable to a parallel stream.
     * 
     * @param <T>
     * @param in
     * @return 
     */
    public static <T> Stream<T> parallelStream(Iterable<T> in) {
        return StreamSupport.stream(in.spliterator(), true);
    }
    
    /**
     * Converts an iterator to a stream.
     * 
     * @param <T>
     * @param in
     * @return 
     */
    public static <T> Stream<T> stream(Iterator<T> in) {
        Iterable<T> iterable = () -> in;
        return StreamSupport.stream(iterable.spliterator(), false);
    }
    
    /**
     * Converts an iterator to a parallel stream.
     * 
     * @param <T>
     * @param in
     * @return 
     */
    public static <T> Stream<T> parallelStream(Iterator<T> in) {
        Iterable<T> iterable = () -> in;
        return StreamSupport.stream(iterable.spliterator(), true);
    }
}
