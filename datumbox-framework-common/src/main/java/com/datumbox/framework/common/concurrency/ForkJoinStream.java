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
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * This class can be used to process a Stream in parallel using a custom ForkJoinPool.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ForkJoinStream {
    
    private final ConcurrencyConfiguration concurrencyConfiguration;
    
    /**
     * Default constructor which receives the Concurrency Configuration object.
     * 
     * @param concurrencyConfiguration
     */
    public ForkJoinStream(ConcurrencyConfiguration concurrencyConfiguration) {
        this.concurrencyConfiguration = concurrencyConfiguration;
    } 
    
    /**
     * Executes forEach on the provided stream. If the Stream is parallel, it is
     * executed using the custom pool, else it is executed directly from the
     * main thread.
     * 
     * @param <T>
     * @param stream
     * @param action 
     */
    public <T> void forEach(Stream<T> stream, Consumer<? super T> action) {
        Runnable runnable = () -> stream.forEach(action);
        ThreadMethods.forkJoinExecution(runnable, concurrencyConfiguration, stream.isParallel());
    }
    
    /**
     * Executes map on the provided stream. If the Stream is parallel, it is
     * executed using the custom pool, else it is executed directly from the
     * main thread.
     * 
     * @param <T>
     * @param <R>
     * @param stream
     * @param mapper
     * @return 
     */
    public <T, R> Stream<R> map(Stream<T> stream, Function<? super T, ? extends R> mapper) {
        Callable<Stream<R>> callable = () -> stream.map(mapper);
        return ThreadMethods.forkJoinExecution(callable, concurrencyConfiguration, stream.isParallel());
    }
    
    
    /**
     * Executes collect on the provided stream using the provided collector. 
     * If the Stream is parallel, it is executed using the custom pool, else it 
     * is executed directly from the main thread.
     * 
     * @param <T>
     * @param <R>
     * @param <A>
     * @param stream
     * @param collector
     * @return 
     */
    public <T, R, A> R collect(Stream<T> stream, Collector<? super T, A, R> collector) {
        Callable<R> callable = () -> stream.collect(collector);
        return ThreadMethods.forkJoinExecution(callable, concurrencyConfiguration, stream.isParallel());
    }
    
    /**
     * Executes min on the provided stream using the provided collector. 
     * If the Stream is parallel, it is executed using the custom pool, else it 
     * is executed directly from the main thread.
     * 
     * @param <T>
     * @param stream
     * @param comparator
     * @return 
     */
    public <T> Optional<T> min(Stream<T> stream, Comparator<? super T> comparator) {
        Callable<Optional<T>> callable = () -> stream.min(comparator);
        return ThreadMethods.forkJoinExecution(callable, concurrencyConfiguration, stream.isParallel());
    }
    
    /**
     * Executes max on the provided stream using the provided collector. 
     * If the Stream is parallel, it is executed using the custom pool, else it 
     * is executed directly from the main thread.
     * 
     * @param <T>
     * @param stream
     * @param comparator
     * @return 
     */
    public <T> Optional<T> max(Stream<T> stream, Comparator<? super T> comparator) {
        Callable<Optional<T>> callable = () -> stream.max(comparator);
        return ThreadMethods.forkJoinExecution(callable, concurrencyConfiguration, stream.isParallel());
    }
    
    /**
     * Executes sum on the provided DoubleStream using the provided collector. 
     * If the Stream is parallel, it is executed using the custom pool, else it 
     * is executed directly from the main thread.
     * 
     * @param stream
     * @return 
     */
    public double sum(DoubleStream stream) {
        Callable<Double> callable = () -> stream.sum();
        return ThreadMethods.forkJoinExecution(callable, concurrencyConfiguration, stream.isParallel());
    }
}