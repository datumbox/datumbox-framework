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
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.featureselectors.AbstractCountBasedFeatureSelector;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the Mutual Information Feature Selection algorithm which can be used
 * for evaluating boolean variables.
 * 
 * References: 
 * http://nlp.stanford.edu/IR-book/html/htmledition/mutual-information-1.html
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MutualInformation extends AbstractCountBasedFeatureSelector<MutualInformation.ModelParameters, MutualInformation.TrainingParameters> {
    
    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractCountBasedFeatureSelector.AbstractModelParameters {
        private static final long serialVersionUID = 1L;

        /** 
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected ModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
        }
        
    }

    /** {@inheritDoc} */
    public static class TrainingParameters extends AbstractCountBasedFeatureSelector.AbstractTrainingParameters {
        private static final long serialVersionUID = 1L;

    }

    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainer.AbstractTrainingParameters, Configuration)
     */
    protected MutualInformation(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected MutualInformation(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }

    /** {@inheritDoc} */
    @Override
    protected void estimateFeatureScores(Map<Object, Double> featureScores, int N, Map<Object, Integer> classCounts, Map<List<Object>, Integer> featureClassCounts, Map<Object, Double> featureCounts) {
        logger.debug("estimateFeatureScores()");
        final double log2 = Math.log(2.0);
        
        streamExecutor.forEach(StreamMethods.stream(featureCounts.entrySet().stream(), isParallelized()), featureCount -> {
            Object feature = featureCount.getKey();
            double N1_ = featureCount.getValue(); //calculate the N1. (number of records that has the feature)
            double N0_ = N - N1_; //also the N0. (number of records that DONT have the feature)
            
            double bestScore = Double.NEGATIVE_INFINITY;
            
            for(Map.Entry<Object, Integer> classCount : classCounts.entrySet()) {
                Object theClass = classCount.getKey();
                
                double N_1 = classCount.getValue();
                double N_0 = N - N_1;
                Integer featureClassC = featureClassCounts.get(Arrays.asList(feature, theClass));
                double N11 = (featureClassC!=null)?featureClassC.doubleValue():0.0; //N11 is the number of records that have the feature and belong on the specific class
                
                double N01 = N_1 - N11; //N01 is the total number of records that do not have the particular feature BUT they belong to the specific class
                
                double N00 = N0_ - N01;
                double N10 = N1_ - N11;
                
                //calculate Mutual Information
                //Note we calculate it partially because if one of the N.. is zero the log will not be defined and it will return NAN.
                double scorevalue=0.0;
                if(N11>0.0) {
                    scorevalue+=(N11/N)*Math.log((N/N1_)*(N11/N_1))/log2; 
                }
                if(N01>0.0) {
                    scorevalue+=(N01/N)*Math.log((N/N0_)*(N01/N_1))/log2;
                }
                if(N10>0.0) {
                    scorevalue+=(N10/N)*Math.log((N/N1_)*(N10/N_0))/log2;
                }
                if(N00>0.0) {
                    scorevalue+=(N00/N)*Math.log((N/N0_)*(N00/N_0))/log2;
                }

                if(scorevalue>bestScore) {
                    bestScore = scorevalue;
                }
            }
            
            featureScores.put(feature, bestScore); //This Map is concurrent and there are no overlaping keys between threads
        });
    }
    
}
