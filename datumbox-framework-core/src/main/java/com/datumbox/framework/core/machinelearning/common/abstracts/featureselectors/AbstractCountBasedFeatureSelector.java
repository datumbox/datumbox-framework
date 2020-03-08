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
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;

import java.util.*;

/**
 * Abstract class which is the base of every Count Based Feature Selection algorithm.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class AbstractCountBasedFeatureSelector<MP extends AbstractCountBasedFeatureSelector.AbstractModelParameters, TP extends AbstractCountBasedFeatureSelector.AbstractTrainingParameters> extends AbstractScoreBasedFeatureSelector<MP, TP> {

    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainer.AbstractTrainingParameters, Configuration)
     */
    protected AbstractCountBasedFeatureSelector(TP trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected AbstractCountBasedFeatureSelector(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }

    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        StorageEngine storageEngine = knowledgeBase.getStorageEngine();
        TP trainingParameters = knowledgeBase.getTrainingParameters();
        MP modelParameters = knowledgeBase.getModelParameters();

        Set<TypeInference.DataType> supportedXDataTypes = getSupportedXDataTypes();
        Map<Object, TypeInference.DataType> xDataTypes = trainingData.getXDataTypes();

        Map<Object, Integer> tmp_classCounts = new HashMap<>(); //map which stores the counts of the classes
        Map<List<Object>, Integer> tmp_featureClassCounts = storageEngine.getBigMap("tmp_featureClassCounts", (Class<List<Object>>)(Class<?>)List.class, Integer.class, StorageEngine.MapType.HASHMAP, StorageEngine.StorageHint.IN_MEMORY, false, true); //map which stores the counts of feature-class combinations.
        Map<Object, Double> tmp_featureCounts = storageEngine.getBigMap("tmp_featureCounts", Object.class, Double.class, StorageEngine.MapType.HASHMAP, StorageEngine.StorageHint.IN_MEMORY, false, true); //map which stores the counts of the features

        //find the featureCounts
        logger.debug("Estimating featureCounts");
        for(Record r : trainingData) {
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object column = entry.getKey();
                if(!supportedXDataTypes.contains(xDataTypes.get(column))) {
                    continue;
                }

                Double value = TypeInference.toDouble(entry.getValue());
                if(value>0.0) {
                    double featureCounter = tmp_featureCounts.getOrDefault(column, 0.0);
                    tmp_featureCounts.put(column, ++featureCounter);
                }
            }
        }

        //remove rare features
        Integer rareFeatureThreshold = trainingParameters.getRareFeatureThreshold();
        if(rareFeatureThreshold != null && rareFeatureThreshold>0) {
            removeRareFeatures(tmp_featureCounts, rareFeatureThreshold);
        }

        //now find the classCounts and the featureClassCounts
        logger.debug("Estimating classCounts and featureClassCounts");
        for(Record r : trainingData) {
            Object theClass = r.getY();

            Integer classCounter = tmp_classCounts.getOrDefault(theClass, 0);
            tmp_classCounts.put(theClass, ++classCounter);


            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object column = entry.getKey();
                if(!supportedXDataTypes.contains(xDataTypes.get(column))) {
                    continue;
                }

                Double value = TypeInference.toDouble(entry.getValue());
                if(value>0.0) {
                    List<Object> featureClassTuple = Arrays.asList(column, theClass);
                    Integer featureClassCounter = tmp_featureClassCounts.getOrDefault(featureClassTuple, 0);
                    tmp_featureClassCounts.put(featureClassTuple, ++featureClassCounter);
                }
            }
        }


        //call the overriden method to get the scores of the features.
        final Map<Object, Double> featureScores = modelParameters.getFeatureScores();
        estimateFeatureScores(featureScores, trainingData.size(), tmp_classCounts, tmp_featureClassCounts, tmp_featureCounts);


        //drop the unnecessary stastistics tables
        tmp_classCounts.clear();
        storageEngine.dropBigMap("tmp_featureClassCounts", tmp_featureClassCounts);
        storageEngine.dropBigMap("tmp_featureCounts", tmp_featureCounts);


        //keep only the top features
        Integer maxFeatures = trainingParameters.getMaxFeatures();
        if(maxFeatures != null && maxFeatures<featureScores.size()) {
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
        return new HashSet<>(Arrays.asList(TypeInference.DataType.BOOLEAN, TypeInference.DataType.CATEGORICAL, TypeInference.DataType.ORDINAL));
    }

    /**
     * Abstract method which is responsible for estimating the score of each Feature.
     *
     * @param featureScores
     * @param N
     * @param classCounts
     * @param featureClassCounts
     * @param featureCounts
     */
    protected abstract void estimateFeatureScores(Map<Object, Double> featureScores, int N, Map<Object, Integer> classCounts, Map<List<Object>, Integer> featureClassCounts, Map<Object, Double> featureCounts);

}
