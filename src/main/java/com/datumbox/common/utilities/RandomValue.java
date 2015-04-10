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
public class RandomValue {
    private static Random randomGenerator = new Random();
    
    public static Random getRandomGenerator() {
        return randomGenerator;
    }

    public static void setRandomGenerator(Random randomGenerator) {
        RandomValue.randomGenerator = randomGenerator;
    }
    
    public static double doubleRand(double min, double max) {
        return min + (randomGenerator.nextDouble() * (max - min));
    }
    
    public static long longRand(long min, long max) {
        return min + (long)(randomGenerator.nextDouble() * ((max - min) + 1L));
    }
    
    public static int intRand(int min, int max) {
        return min + (int)(randomGenerator.nextDouble() * ((max - min) + 1));
    }
}
