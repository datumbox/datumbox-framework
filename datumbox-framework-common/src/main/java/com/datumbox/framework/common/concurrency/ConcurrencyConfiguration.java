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

import com.datumbox.framework.common.interfaces.Configurable;

import java.util.Properties;

/**
 * The ConcurrencyConfiguration class is used to store the concurrency settings
 * of the framework.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ConcurrencyConfiguration implements Configurable {
    
    /**
     * The total number of processors.
     */
    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    
    private boolean parallelized = true;
    
    private Integer maxNumberOfThreadsPerTask = AVAILABLE_PROCESSORS;
    
    /**
     * Protected constructor.
     */
    public ConcurrencyConfiguration() {
        
    }
    
    /**
     * Getter for the parallelized flag.
     * 
     * @return 
     */
    public boolean isParallelized() {
        return parallelized;
    }
    
    /**
     * Setter for the parallelized flag. This option controls whether parallel
     * execution of threads is allowed in the framework.
     * 
     * @param parallelized 
     */
    public void setParallelized(boolean parallelized) {
        this.parallelized = parallelized;
    }
    
    /**
     * Returns the maximum number of threads which can be used for a specific
     * task by the framework.
     * 
     * @return 
     */
    public Integer getMaxNumberOfThreadsPerTask() {
        return maxNumberOfThreadsPerTask;
    }
    
    /**
     * Setter for the maximum number of threads which can be used for a specific
     * task by the framework. By convention if the value is 0, the max number
     * of threads is set equal to the available processors.
     * 
     * @param maxNumberOfThreadsPerTask 
     */
    public void setMaxNumberOfThreadsPerTask(Integer maxNumberOfThreadsPerTask) {
        if(maxNumberOfThreadsPerTask<0) {
            throw new IllegalArgumentException("The max number of threads can not be negative.");
        }
        else if(maxNumberOfThreadsPerTask==0) {
            this.maxNumberOfThreadsPerTask = AVAILABLE_PROCESSORS;
        }
        else {
            this.maxNumberOfThreadsPerTask = Math.min(maxNumberOfThreadsPerTask, 4*AVAILABLE_PROCESSORS);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void load(Properties properties) {
        setParallelized("true".equalsIgnoreCase(properties.getProperty("concurrencyConfiguration.parallelized")));
        setMaxNumberOfThreadsPerTask(Integer.parseInt(properties.getProperty("concurrencyConfiguration.maxNumberOfThreadsPerTask")));
        if(isParallelized()==false) {
            setMaxNumberOfThreadsPerTask(1);
        }
        else if(getMaxNumberOfThreadsPerTask()==1) {
            setParallelized(false);
        }
    }
    
}
