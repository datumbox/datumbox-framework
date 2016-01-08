/**
 * Copyright (C) 2013-2016 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.machinelearning.common.interfaces;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.utilities.StreamMethods;
import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * All Machine Learning models capable of predicting records in parallel implement
 * this interface.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public interface PredictParallelizable extends Parallelizable {
    
    /**
     * Tuple that stores the results of the prediction namely the yPredicted and 
     * the yPredictedProbabilities objects.
     */
    public static class Prediction implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final Object yPredicted;
        
        private final AssociativeArray yPredictedProbabilities;
        
        /**
         * Constructor of the Prediction object.
         * 
         * @param yPredicted
         * @param yPredictedProbabilities 
         */
        public Prediction(Object yPredicted, AssociativeArray yPredictedProbabilities) {
            this.yPredicted = yPredicted;
            this.yPredictedProbabilities = yPredictedProbabilities;
        }
        
        /**
         * Getter for the yPredicted field.
         * 
         * @return 
         */
        public Object getYPredicted() {
            return yPredicted;
        }
        
        /**
         * Getter for the yPredictedProbabilities field.
         * 
         * @return 
         */
        public AssociativeArray getYPredictedProbabilities() {
            return yPredictedProbabilities;
        }
        
    }
    
    /**
     * Takes a single record, makes a prediction using the model and returns
     * the result wrapped in the Prediction tuple. The method assumes that the
     * KnowledgeBase is be loaded.
     * 
     * @param r
     * @return 
     */
    public Prediction _predictRecord(Record r);
    
    /**
     * Estimates the predictions for a new Dataframe in a parallel way.
     * 
     * @param newData 
     */
    default public void _predictDatasetParallel(Dataframe newData) {
        
        Map<Integer, Prediction> results = StreamMethods.<Map.Entry<Integer, Record>>stream(newData.entries(), isParallelized())
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey, 
                                e -> _predictRecord(e.getValue())
                        )
                );

        //Use the map to update the records
        for(Map.Entry<Integer, Record> entry : newData.entries()) {
            Integer rId = entry.getKey();
            Record r = entry.getValue();
            Prediction p = results.get(rId);
            newData._unsafe_set(rId, new Record(r.getX(), r.getY(), p.getYPredicted(), p.getYPredictedProbabilities()));
        }
        
    }
}
