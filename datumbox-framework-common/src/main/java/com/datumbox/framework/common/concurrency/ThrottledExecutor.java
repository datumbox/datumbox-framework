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

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

/**
 * The ThrottledExecutor enables us to throttle the input of the executor. This
 * can be useful when we don't wish to submit all the tasks at once in order to
 * preserve memory.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ThrottledExecutor implements Executor {

    private final Executor wrappedExecutor;
    
    private final Semaphore semaphore;
    
    /**
     * This Executor will block the main thread (when execute() is called) if the 
     * number of submitted and unfinished tasks reaches the provided limit. This
     * enabled us to pace the input and reduce memory consumption.
     * 
     * @param executor
     * @param maxConcurrentTasks 
     */
    public ThrottledExecutor(Executor executor, int maxConcurrentTasks) {
        this.wrappedExecutor = executor;
        this.semaphore = new Semaphore(maxConcurrentTasks);
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute(final Runnable command) {
        try {
            semaphore.acquire();
        } 
        catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        
        try {
            wrappedExecutor.execute(() -> {
                try {
                    command.run();
                } 
                finally {
                    semaphore.release();
                }
            });
        } 
        catch (RejectedExecutionException ex) {
            semaphore.release();
            throw new RuntimeException(ex);
        }
    }
}