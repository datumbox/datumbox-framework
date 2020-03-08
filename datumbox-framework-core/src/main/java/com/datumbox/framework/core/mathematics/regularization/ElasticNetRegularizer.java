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
 * Utility class for ElasticNet regularization.
 *
 * https://web.stanford.edu/~hastie/Papers/B67.2%20(2005)%20301-320%20Zou%20&%20Hastie.pdf
 * http://web.stanford.edu/~hastie/TALKS/enet_talk.pdf
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ElasticNetRegularizer {

    /**
     * Updates the weights by applying the ElasticNet regularization.
     *
     * @param l1
     * @param l2
     * @param learningRate
     * @param weights
     * @param newWeights
     * @param <K>
     */
    public static <K> void updateWeights(double l1, double l2, double learningRate, Map<K, Double> weights, Map<K, Double> newWeights) {
        L2Regularizer.updateWeights(l2, learningRate, weights, newWeights);
        L1Regularizer.updateWeights(l1, learningRate, weights, newWeights);
    }

    /**
     * Estimates the penalty by adding the ElasticNet regularization.
     *
     * @param l1
     * @param l2
     * @param weights
     * @param <K>
     * @return
     */
    public static <K> double estimatePenalty(double l1, double l2, Map<K, Double> weights) {
        double penalty = 0.0;
        penalty += L2Regularizer.estimatePenalty(l2, weights);
        penalty += L1Regularizer.estimatePenalty(l1, weights);
        return penalty;
    }

}
