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

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * This class contains a number of helper methods for Java Threads.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ThreadMethods {
    
    /**
     * Takes the items of the stream in a throttled way and provides them to the 
     * consumer. It uses as many threads as the available processors and it does
     * not start more tasks than 2 times the previous number.
     * 
     * @param <T>
     * @param stream
     * @param consumer 
     * @param concurrencyConfiguration
     */
    public static <T> void throttledExecution(Stream<T> stream, Consumer<T> consumer, ConcurrencyConfiguration concurrencyConfiguration) {
        if(concurrencyConfiguration.isParallelized()) {
            int maxThreads = concurrencyConfiguration.getMaxNumberOfThreadsPerTask();
            int maxTasks = 2*maxThreads; 

            ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);
            ThrottledExecutor executor = new ThrottledExecutor(executorService, maxTasks);

            stream.sequential().forEach(i -> {
                executor.execute(() -> {
                    consumer.accept(i);
                });
            });

            executorService.shutdown();
            try {
                executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
            } 
            catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        else {
            Runnable runnable = () -> stream.forEach(consumer);
            runnable.run();
        }
    }
    
    /**
     * Alternative to parallelStreams() which executes a callable in a separate
     * pool.
     * 
     * @param <T>
     * @param callable 
     * @param concurrencyConfiguration
     * @param parallelStream 
     * @return  
     */
    public static <T> T forkJoinExecution(Callable<T> callable, ConcurrencyConfiguration concurrencyConfiguration, boolean parallelStream) {
        if(parallelStream && concurrencyConfiguration.isParallelized()) {
            try {
                ForkJoinPool pool = new ForkJoinPool(concurrencyConfiguration.getMaxNumberOfThreadsPerTask());
                
                T results = pool.submit(callable).get();
                pool.shutdown();
                return results;
            } 
            catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }
        else {
            try {
                return callable.call();
            } 
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    /**
     * Alternative to parallelStreams() which executes a runnable in a separate
     * pool.
     * 
     * @param runnable 
     * @param concurrencyConfiguration
     * @param parallelStream 
     */
    public static void forkJoinExecution(Runnable runnable, ConcurrencyConfiguration concurrencyConfiguration, boolean parallelStream) {
        if(parallelStream && concurrencyConfiguration.isParallelized()) {
            try {
                ForkJoinPool pool = new ForkJoinPool(concurrencyConfiguration.getMaxNumberOfThreadsPerTask());
                pool.submit(runnable).get();
                pool.shutdown();
            } 
            catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }
        else {
            runnable.run();
        }
    }
}
