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
package com.datumbox.framework.core.machinelearning.common.interfaces;

import com.datumbox.framework.common.concurrency.ConcurrencyConfiguration;
import com.datumbox.framework.common.concurrency.ForkJoinStream;
import com.datumbox.framework.common.concurrency.StreamMethods;
import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;

import java.io.Serializable;
import java.util.Map;

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
     * Estimates the predictions for a new Dataframe in a parallel way. We provide
     * the Dataframe and an empty map which acts as a temporary buffer for storing
     * the results before they are loaded in the dataframe.
     * 
     * @param newData 
     * @param resultsBuffer 
     * @param concurrencyConfiguration
     */
    default public void _predictDatasetParallel(Dataframe newData, final Map<Integer, Prediction> resultsBuffer, ConcurrencyConfiguration concurrencyConfiguration) {
        ForkJoinStream streamExecutor = new ForkJoinStream(concurrencyConfiguration);
        
        streamExecutor.forEach(StreamMethods.stream(newData.entries(), isParallelized()), e -> {
            resultsBuffer.put(e.getKey(), _predictRecord(e.getValue())); //the key is unique across threads and the map is concurrent
        });

        //Use the map to update the records
        streamExecutor.forEach(StreamMethods.stream(newData.entries(), isParallelized()), e -> {
            Integer rId = e.getKey();
            Record r = e.getValue();
            Prediction p = resultsBuffer.get(rId);
            
            
            Record newR = new Record(r.getX(), r.getY(), p.getYPredicted(), p.getYPredictedProbabilities());

            newData._unsafe_set(rId, newR);
        });
        
    }

    /**
     * Estimates the predictions for a new Dataframe in a parallel way.
     *
     * @param newData
     * @param storageEngine
     * @param concurrencyConfiguration
     */
    default public void _predictDatasetParallel(Dataframe newData, StorageEngine storageEngine, ConcurrencyConfiguration concurrencyConfiguration) {
        Map<Integer, Prediction> resultsBuffer = storageEngine.getBigMap("tmp_resultsBuffer", Integer.class, Prediction.class, StorageEngine.MapType.HASHMAP, StorageEngine.StorageHint.IN_DISK, true, true);
        _predictDatasetParallel(newData, resultsBuffer, concurrencyConfiguration);
        storageEngine.dropBigMap("tmp_resultsBuffer", resultsBuffer);
    }
}
