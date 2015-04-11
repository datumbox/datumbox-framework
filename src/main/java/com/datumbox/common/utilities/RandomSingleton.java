/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
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

import java.util.Random;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class RandomSingleton {
    private static final RandomSingleton instance = new RandomSingleton();
    private final Random randomGenerator = new Random();
 
    private RandomSingleton() {
        
    }
 
    public static RandomSingleton getInstance() {
        return instance;
    }
 
    public double nextDouble() {
        return randomGenerator.nextDouble();
    }
    
    public int nextInt() {
        return randomGenerator.nextInt();
    }
    
    public int nextInt(int bound) {
        return randomGenerator.nextInt(bound);
    }
    
    public long nextLong() {
        return randomGenerator.nextLong();
    }
    
    public synchronized double nextGaussian() {
        return randomGenerator.nextGaussian();
    }
    
    public synchronized void setSeed(long seed) {
        randomGenerator.setSeed(seed);
    } 
}
