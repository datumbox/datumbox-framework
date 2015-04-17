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
public class RandomGenerator {
    private static final ThreadLocal<Random> randomGenerator = new ThreadLocal<Random>() {
        @Override
        protected Random initialValue() {
            return new Random();
        }
    };
 
    public static double nextDouble() {
        return randomGenerator.get().nextDouble();
    }
    
    public static int nextInt() {
        return randomGenerator.get().nextInt();
    }
    
    public static int nextInt(int bound) {
        return randomGenerator.get().nextInt(bound);
    }
    
    public static long nextLong() {
        return randomGenerator.get().nextLong();
    }
    
    public static double nextGaussian() {
        return randomGenerator.get().nextGaussian();
    }
    
    public static void setSeed(long seed) {
        randomGenerator.get().setSeed(seed);
    } 
}
