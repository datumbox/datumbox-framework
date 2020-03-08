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
import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.common.dataobjects.DataTable2D;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.featureselectors.AbstractCountBasedFeatureSelector;
import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;
import com.datumbox.framework.core.statistics.nonparametrics.independentsamples.Chisquare;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the Chisquare Feature Selection algorithm which can be used
 * for evaluating boolean variables.
 * 
 * References: 
 * http://nlp.stanford.edu/IR-book/html/htmledition/feature-selectionchi2-feature-selection-1.html
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ChisquareSelect extends AbstractCountBasedFeatureSelector<ChisquareSelect.ModelParameters, ChisquareSelect.TrainingParameters> {
    
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
        
        private double aLevel = 0.05; 
        
        /**
         * Getter for the threshold of the maximum p-value; a feature must
         * have a p-value less or equal than the threshold to be retained in the 
         * feature list.
         * 
         * @return 
         */
        public double getALevel() {
            return aLevel;
        }
        
        /**
         * Setter for the threshold of the maximum p-value; a feature must
         * have a p-value less or equal than the threshold to be retained in the 
         * feature list.
         * 
         * @param aLevel 
         */
        public void setALevel(double aLevel) {
            if(aLevel>1.0 || aLevel<0.0) {
                throw new IllegalArgumentException("Invalid value for the level of statistical significance.");
            }
            this.aLevel = aLevel;
        }
    }

    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainer.AbstractTrainingParameters, Configuration)
     */
    protected ChisquareSelect(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected ChisquareSelect(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }

    /** {@inheritDoc} */
    @Override
    protected void estimateFeatureScores(Map<Object, Double> featureScores, int N, Map<Object, Integer> classCounts, Map<List<Object>, Integer> featureClassCounts, Map<Object, Double> featureCounts) {
        logger.debug("estimateFeatureScores()");

        double criticalValue = ContinuousDistributions.chisquareInverseCdf(knowledgeBase.getTrainingParameters().getALevel(), 1); //one degree of freedom because the tables below are 2x2

        streamExecutor.forEach(StreamMethods.stream(featureCounts.entrySet().stream(), isParallelized()), featureCount -> {
            Object feature = featureCount.getKey();
            double N1_ = featureCount.getValue(); //calculate the N1. (number of records that has the feature)
            double N0_ = N - N1_; //also the N0. (number of records that DONT have the feature)
            
            double bestScore = Double.NEGATIVE_INFINITY;
            
            DataTable2D contingencyTable = new DataTable2D();
            contingencyTable.put(0, new AssociativeArray());
            contingencyTable.put(1, new AssociativeArray());

            for (Map.Entry<Object, Integer> classCount : classCounts.entrySet()) {
                Object theClass = classCount.getKey();
                
                Integer featureClassC = featureClassCounts.get(Arrays.asList(feature, theClass));
                double N11 = (featureClassC!=null)?featureClassC.doubleValue():0.0; //N11 is the number of records that have the feature and belong on the specific class
                double N01 = classCount.getValue() - N11; //N01 is the total number of records that do not have the particular feature BUT they belong to the specific class
                
                double N00 = N0_ - N01;
                double N10 = N1_ - N11;
                
                contingencyTable.put2d(0, 0, N00);
                contingencyTable.put2d(0, 1, N01);
                contingencyTable.put2d(1, 0, N10);
                contingencyTable.put2d(1, 1, N11);
                
                double scorevalue = Chisquare.getScoreValue(contingencyTable);
                //contingencyTable = null;
                
                if(scorevalue>bestScore) {
                    bestScore = scorevalue;
                }
            }
            
            if (bestScore>=criticalValue) { //if the score is larger than the critical value, then select the feature
                featureScores.put(feature, bestScore); //This Map is concurrent and there are no overlaping keys between threads
            }
        });
    }
    
}
