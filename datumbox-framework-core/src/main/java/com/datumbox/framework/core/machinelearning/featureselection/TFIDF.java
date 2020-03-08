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
package com.datumbox.framework.core.machinelearning.featureselection;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.concurrency.StreamMethods;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.common.storage.interfaces.StorageEngine.MapType;
import com.datumbox.framework.common.storage.interfaces.StorageEngine.StorageHint;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.featureselectors.AbstractScoreBasedFeatureSelector;

import java.util.*;
import java.util.function.BiFunction;


/**
 * Implementation of the TF-IDF Feature Selection algorithm. *
 *
 * References:
 * http://en.wikipedia.org/wiki/Tf%E2%80%93idf
 * https://gist.github.com/AloneRoad/1605037
 * http://www.tfidf.com/
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class TFIDF extends AbstractScoreBasedFeatureSelector<TFIDF.ModelParameters, TFIDF.TrainingParameters> {

    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractScoreBasedFeatureSelector.AbstractModelParameters {
        private static final long serialVersionUID = 2L;

        /**
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected ModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
        }

    }

    /** {@inheritDoc} */
    public static class TrainingParameters extends AbstractScoreBasedFeatureSelector.AbstractTrainingParameters {
        private static final long serialVersionUID = 1L;
        
        private boolean binarized = false;
        
        /**
         * Getter for the binarized flag; when it is set on the frequencies of the
         * activated keywords are clipped to 1.
         * 
         * @return 
         */
        public boolean isBinarized() {
            return binarized;
        }
        
        /**
         * Setter for the binarized flag; when it is set on the frequencies of the
         * activated keywords are clipped to 1.
         * 
         * @param binarized 
         */
        public void setBinarized(boolean binarized) {
            this.binarized = binarized;
        }

    }

    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainer.AbstractTrainingParameters, Configuration)
     */
    protected TFIDF(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected TFIDF(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }

    /** {@inheritDoc} */
    @Override
    public void fit(Dataframe trainingData) {
        Set<TypeInference.DataType> supportedXDataTypes = getSupportedXDataTypes();
        for(TypeInference.DataType d : trainingData.getXDataTypes().values()) {
            if(!supportedXDataTypes.contains(d)) {
                throw new IllegalArgumentException("A DataType that is not supported by this method was detected in the Dataframe.");
            }
        }
        super.fit(trainingData);
    }

    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        boolean binarized = trainingParameters.isBinarized();
        
        
        int n = trainingData.size();
        
        StorageEngine storageEngine = knowledgeBase.getStorageEngine();
        Map<Object, Double> tmp_idfMap = storageEngine.getBigMap("tmp_idf", Object.class, Double.class, MapType.HASHMAP, StorageHint.IN_MEMORY, true, true);

        //initially estimate the counts of the terms in the dataset and store this temporarily
        //in idf map. this help us avoid using twice much memory comparing to using two different maps
        for(Record r : trainingData) { 
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object keyword = entry.getKey();
                Double counts = TypeInference.toDouble(entry.getValue());
                
                if(counts > 0.0) {
                    tmp_idfMap.put(keyword, tmp_idfMap.getOrDefault(keyword, 0.0)+1.0);
                }
            }
        }

        //remove rare features
        Integer rareFeatureThreshold = trainingParameters.getRareFeatureThreshold();
        if(rareFeatureThreshold != null && rareFeatureThreshold>0) {
            removeRareFeatures(tmp_idfMap, rareFeatureThreshold);
        }
        
        //convert counts to idf scores
        streamExecutor.forEach(StreamMethods.stream(tmp_idfMap.entrySet().stream(), isParallelized()), entry -> {
            Object keyword = entry.getKey();
            Double countsInDocument = entry.getValue();
            
            tmp_idfMap.put(keyword, Math.log10(n/countsInDocument));
        });
        
        
        final Map<Object, Double> featureScores = modelParameters.getFeatureScores();
        
        //this lambda checks if the new score is larger than the current max score of the feature
        BiFunction<Object, Double, Boolean> isGreaterThanMax = (feature, newScore) -> {
            Double maxScore = featureScores.get(feature);
            return maxScore==null || maxScore<newScore;
        };
        
        //calculate the maximum tfidf scores
        streamExecutor.forEach(StreamMethods.stream(trainingData.stream(), isParallelized()), r -> {
            //calculate the tfidf scores
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Double counts = TypeInference.toDouble(entry.getValue());
                
                if(counts > 0.0) {
                    Object keyword = entry.getKey();

                    double tf = binarized?1.0:counts;
                    double idf = tmp_idfMap.getOrDefault(keyword, 0.0);

                    double tfidf = tf*idf;
                    
                    if(tfidf > 0.0) { //ignore 0 scored features
                        
                        //Threads will help here under the assumption that only 
                        //a small number of records will have features that exceed 
                        //the maximum score. Thus they will stop in this if statement
                        //and they will not go into the synced block.
                        if(isGreaterThanMax.apply(keyword, tfidf)) {
                            synchronized(featureScores) {
                                if(isGreaterThanMax.apply(keyword, tfidf)) {
                                    featureScores.put(keyword, tfidf);
                                }
                            }
                        }
                        
                    }
                }
            }
        });
        
        //Drop the temporary Collection
        storageEngine.dropBigMap("tmp_idf", tmp_idfMap);


        //keep only the top features
        Integer maxFeatures = trainingParameters.getMaxFeatures();
        if(maxFeatures!=null && maxFeatures<featureScores.size()) {
            keepTopFeatures(featureScores, maxFeatures);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected Set<TypeInference.DataType> getSupportedXDataTypes() {
        return new HashSet<>(Arrays.asList(TypeInference.DataType.BOOLEAN, TypeInference.DataType.NUMERICAL));
    }

    /** {@inheritDoc} */
    @Override
    protected Set<TypeInference.DataType> getSupportedYDataTypes() {
        return null;
    }

}
