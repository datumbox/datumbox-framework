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
package com.datumbox.framework.core.mathematics.regularization;

import java.util.Map;

/**
 * Utility class for L2 regularization.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class L2Regularizer {

    /**
     * Updates the weights by applying the L2 regularization.
     *
     * @param l2
     * @param learningRate
     * @param weights
     * @param newWeights
     * @param <K>
     */
    public static <K> void updateWeights(double l2, double learningRate, Map<K, Double> weights, Map<K, Double> newWeights) {
        if(l2 > 0.0) {
            for(Map.Entry<K, Double> e : weights.entrySet()) {
                K column = e.getKey();
                newWeights.put(column, newWeights.get(column) + l2*e.getValue()*(-learningRate));
            }
        }
    }

    /**
     * Estimates the penalty by adding the L2 regularization.
     *
     * @param l2
     * @param weights
     * @param <K>
     * @return
     */
    public static <K> double estimatePenalty(double l2, Map<K, Double> weights) {
        double penalty = 0.0;
        if(l2 > 0.0) {
            double sumWeightsSquared = 0.0;
            for(double w : weights.values()) {
                sumWeightsSquared += w*w;
            }
            penalty = l2*sumWeightsSquared/2.0;
        }
        return penalty;
    }

}
