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
package com.datumbox.framework.core.machinelearning.common.abstracts.featureselectors;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.concurrency.StreamMethods;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.common.storage.interfaces.BigMap;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.common.storage.interfaces.StorageEngine.MapType;
import com.datumbox.framework.common.storage.interfaces.StorageEngine.StorageHint;
import com.datumbox.framework.core.common.utilities.SelectKth;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;

import java.util.*;
import java.util.stream.Stream;

/**
 * Abstract class which is the base of every Score Based Feature Selection algorithm.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class AbstractScoreBasedFeatureSelector<MP extends AbstractScoreBasedFeatureSelector.AbstractModelParameters, TP extends AbstractScoreBasedFeatureSelector.AbstractTrainingParameters> extends AbstractFeatureSelector<MP, TP> {

    /** {@inheritDoc} */
    public static abstract class AbstractModelParameters extends AbstractFeatureSelector.AbstractModelParameters {

        @BigMap(keyClass=Object.class, valueClass=Double.class, mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=true)
        private Map<Object, Double> featureScores; //map which stores the scores of the features

        /** 
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected AbstractModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
        }
        
        /**
         * Getter of the Feature Scores.
         * 
         * @return 
         */
        public Map<Object, Double> getFeatureScores() {
            return featureScores;
        }
        
        /**
         * Setter of the Feature Scores.
         * 
         * @param featureScores 
         */
        protected void setFeatureScores(Map<Object, Double> featureScores) {
            this.featureScores = featureScores;
        }
        
    }
    
    /** {@inheritDoc} */
    public static abstract class AbstractTrainingParameters extends AbstractFeatureSelector.AbstractTrainingParameters {
        
        private Integer rareFeatureThreshold = null;
        private Integer maxFeatures = null;

        /**
         * Getter for the rare feature threshold. Any feature that exists
         * in the training dataset less times than this number will be removed
         * directly. 
         * 
         * @return 
         */
        public Integer getRareFeatureThreshold() {
            return rareFeatureThreshold;
        }
        
        /**
         * Setter for the rare feature threshold. Set to null to deactivate this 
         * feature. Any feature that exists in the training dataset less times 
         * than this number will be removed directly. 
         * 
         * @param rareFeatureThreshold 
         */
        public void setRareFeatureThreshold(Integer rareFeatureThreshold) {
            this.rareFeatureThreshold = rareFeatureThreshold;
        }
        
        /**
         * Getter for the maximum number of features that should be kept in the
         * dataset.
         * 
         * @return 
         */
        public Integer getMaxFeatures() {
            return maxFeatures;
        }
        
        /**
         * Setter for the maximum number of features that should be kept in the
         * dataset. Set to null for unlimited.
         * 
         * @param maxFeatures 
         */
        public void setMaxFeatures(Integer maxFeatures) {
            this.maxFeatures = maxFeatures;
        }
        
    }

    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainer.AbstractTrainingParameters, Configuration)
     */
    protected AbstractScoreBasedFeatureSelector(TP trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected AbstractScoreBasedFeatureSelector(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }

    /** {@inheritDoc} */
    @Override
    protected void _transform(Dataframe newData) {
        Set<Object> selectedFeatures = knowledgeBase.getModelParameters().getFeatureScores().keySet();
        StorageEngine storageEngine = knowledgeBase.getStorageEngine();
        Set<TypeInference.DataType> supportedXDataTypes = getSupportedXDataTypes();

        Map<Object, Boolean> tmp_removedColumns = storageEngine.getBigMap("tmp_removedColumns", Object.class, Boolean.class, StorageEngine.MapType.HASHMAP, StorageEngine.StorageHint.IN_MEMORY, true, true);

        //keep only the columns which are compatible with the algorithm
        Stream<Object> compatibleColumns = newData.getXDataTypes().entrySet().stream()
            .filter(e -> supportedXDataTypes.contains(e.getValue()))
            .map(e -> e.getKey());

        streamExecutor.forEach(StreamMethods.stream(compatibleColumns, isParallelized()), column -> {
            if(!selectedFeatures.contains(column)) {
                tmp_removedColumns.put(column, true);
            }
        });

        logger.debug("Removing Columns");
        newData.dropXColumns(tmp_removedColumns.keySet());

        storageEngine.dropBigMap("tmp_removedColumns", tmp_removedColumns);
    }

    /**
     * This method keeps the highest scoring features of the provided feature map
     * and removes all the others.
     *
     * @param featureScores
     * @param maxFeatures
     */
    protected void keepTopFeatures(Map<Object, Double> featureScores, int maxFeatures) {
        logger.debug("keepTopFeatures()");

        logger.debug("Estimating the minPermittedScore");
        Double minPermittedScore = SelectKth.largest(featureScores.values().iterator(), maxFeatures);

        //remove any entry with score less than the minimum permitted one
        logger.debug("Removing features with scores less than threshold");
        Iterator<Map.Entry<Object, Double>> it = featureScores.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Object, Double> entry = it.next();
            if(entry.getValue()<minPermittedScore) {
                it.remove();
            }
        }

        //if some extra features still exist (due to ties on the scores) remove some of those extra features
        int numOfExtraFeatures = featureScores.size()-maxFeatures;
        if(numOfExtraFeatures>0) {
            logger.debug("Removing extra features caused by ties");
            it = featureScores.entrySet().iterator();
            while(it.hasNext() && numOfExtraFeatures>0) {
                Map.Entry<Object, Double> entry = it.next();
                if(entry.getValue()-minPermittedScore<=0.0) { //DO NOT COMPARE THEM DIRECTLY USE SUBTRACTION!
                    it.remove();
                    --numOfExtraFeatures;
                }
            }
        }
    }

    /**
     * Removes any feature with less occurrences than the threshold.
     *
     * @param featureCounts
     * @param rareFeatureThreshold
     */
    protected void removeRareFeatures(Map<Object, Double> featureCounts, int rareFeatureThreshold) {
        logger.debug("removeRareFeatures()");

        Iterator<Map.Entry<Object, Double>> it = featureCounts.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Object, Double> entry = it.next();
            if(entry.getValue()<rareFeatureThreshold) {
                it.remove();
            }
        }
    }

}
