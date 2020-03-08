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
 * Utility class for L1 regularization.
 *
 * https://www.cs.ubc.ca/~murphyk/Teaching/CS540-Fall08/L13.pdf
 * http://www.aclweb.org/anthology/P09-1054
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class L1Regularizer {

    /**
     * Updates the weights by applying the L1 regularization.
     *
     * @param l1
     * @param learningRate
     * @param weights
     * @param newWeights
     * @param <K>
     */
    public static <K> void updateWeights(double l1, double learningRate, Map<K, Double> weights, Map<K, Double> newWeights) {
        if(l1 > 0.0) {
            /*
            //SGD-L1 (Naive)
            for(Map.Entry<K, Double> e : weights.entrySet()) {
                K column = e.getKey();
                newWeights.put(column, newWeights.get(column) + l1*Math.signum(e.getValue())*(-learningRate));
            }
            */
            //SGDL1 (Clipping)
            for(Map.Entry<K, Double> e : newWeights.entrySet()) {
                K column = e.getKey();
                double wi_k_intermediate = e.getValue(); //the weight wi_k+1/2 as seen on the paper
                if(wi_k_intermediate > 0.0) {
                    newWeights.put(column, Math.max(0.0, wi_k_intermediate - l1*wi_k_intermediate));

                }
                else if(wi_k_intermediate < 0.0) {
                    newWeights.put(column, Math.min(0.0, wi_k_intermediate + l1*wi_k_intermediate));
                }
            }
        }
    }

    /**
     * Estimates the penalty by adding the L1 regularization.
     *
     * @param l1
     * @param weights
     * @param <K>
     * @return
     */
    public static <K> double estimatePenalty(double l1, Map<K, Double> weights) {
        double penalty = 0.0;
        if(l1 > 0.0) {
            double sumAbsWeights = 0.0;
            for(double w : weights.values()) {
                sumAbsWeights += Math.abs(w);
            }
            penalty = l1*sumAbsWeights;
        }
        return penalty;
    }

}
