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
package com.datumbox.framework.core.machinelearning.modelselection.metrics;

import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelselection.AbstractMetrics;

import java.util.*;

/**
 * Estimates RMSE for Recommendation models.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class RecommendationMetrics extends AbstractMetrics {
    private static final long serialVersionUID = 1L;

    private double RMSE = 0.0;

    /**
     * Getter for RMSE.
     *
     * @return
     */
    public double getRMSE() {
        return RMSE;
    }

    /**
     * @param predictedData
     * @see AbstractMetrics#AbstractMetrics(Dataframe)
     */
    public RecommendationMetrics(Dataframe predictedData) {
        super(predictedData);

        int i = 0;
        for(Record r : predictedData) {
            AssociativeArray predictions = r.getYPredictedProbabilities();
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object column = entry.getKey();
                Object value = entry.getValue();
                RMSE += Math.pow(TypeInference.toDouble(value)-TypeInference.toDouble(predictions.get(column)), 2.0);
                ++i;
            }
        }

        RMSE = Math.sqrt(RMSE/i);
    }

    /**
     * @param validationMetricsList
     * @see AbstractMetrics#AbstractMetrics(List)
     */
    public RecommendationMetrics(List<RecommendationMetrics> validationMetricsList) {
        super(validationMetricsList);

        if(!validationMetricsList.isEmpty()) {
            int k = validationMetricsList.size(); //number of samples
            for(RecommendationMetrics vmSample : validationMetricsList) {
                RMSE += vmSample.getRMSE()/k;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        String sep = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(":").append(sep);
        sb.append("RMSE=").append(RMSE).append(sep);
        return sb.toString();
    }
}
