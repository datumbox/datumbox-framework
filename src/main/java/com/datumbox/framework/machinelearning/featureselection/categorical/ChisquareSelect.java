/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.machinelearning.featureselection.categorical;

import com.datumbox.framework.machinelearning.common.bases.featureselection.CategoricalFeatureSelection;
import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.DataTable2D;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.bases.featureselection.ScoreBasedFeatureSelection;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;
import com.datumbox.framework.statistics.nonparametrics.independentsamples.Chisquare;
import java.util.Arrays;
import java.util.Map;
import java.util.List;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class ChisquareSelect extends CategoricalFeatureSelection<ChisquareSelect.ModelParameters, ChisquareSelect.TrainingParameters>{
    //References: http://nlp.stanford.edu/IR-book/html/htmledition/feature-selectionchi2-feature-selection-1.html
    
    public static class ModelParameters extends CategoricalFeatureSelection.ModelParameters {

        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
    }

    public static class TrainingParameters extends CategoricalFeatureSelection.TrainingParameters {
        private double aLevel = 0.05; 

        public double getALevel() {
            return aLevel;
        }

        public void setALevel(double aLevel) {
            if(aLevel>1 || aLevel<0) {
                throw new RuntimeException("Wrong statistical significance aLevel");
            }
            this.aLevel = aLevel;
        }
    }
    
    public ChisquareSelect(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, ChisquareSelect.ModelParameters.class, ChisquareSelect.TrainingParameters.class);
    }
    
    @Override
    protected void estimateFeatureScores(Map<Object, Integer> classCounts, Map<List<Object>, Integer> featureClassCounts, Map<Object, Double> featureCounts) {
        logger.debug("estimateFeatureScores()");
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        Map<Object, Double> featureScores = modelParameters.getFeatureScores();
        
        DataTable2D contingencyTable = new DataTable2D();
        contingencyTable.put(0, new AssociativeArray());
        contingencyTable.put(1, new AssociativeArray());
        
        double criticalValue = ContinuousDistributions.ChisquareInverseCdf(trainingParameters.getALevel(), 1); //one degree of freedom because the tables below are 2x2
        
        
        double N = modelParameters.getN();
        for(Map.Entry<Object, Double> featureCount : featureCounts.entrySet()) {
            Object feature = featureCount.getKey();
            double N1_ = featureCount.getValue(); //calculate the N1. (number of records that has the feature)
            double N0_ = N - N1_; //also the N0. (number of records that DONT have the feature)
            
            for(Map.Entry<Object, Integer> classCount : classCounts.entrySet()) {
                Object theClass = classCount.getKey();
                
                Integer featureClassC = featureClassCounts.get(Arrays.<Object>asList(feature, theClass));                
                double N11 = (featureClassC!=null)?featureClassC:0.0; //N11 is the number of records that have the feature and belong on the specific class
                double N01 = classCount.getValue() - N11; //N01 is the total number of records that do not have the particular feature BUT they belong to the specific class
                
                double N00 = N0_ - N01;
                double N10 = N1_ - N11;
                
                contingencyTable.get(0).put(0, N00);
                contingencyTable.get(0).put(1, N01);
                contingencyTable.get(1).put(0, N10);
                contingencyTable.get(1).put(1, N11);
                
                double scorevalue = Chisquare.getScoreValue(contingencyTable); 
                if(scorevalue>=criticalValue) { //if the score is larger than the critical value, then select the feature
                    Double previousCriticalValue = featureScores.get(feature);
                    if(previousCriticalValue==null || previousCriticalValue<scorevalue) { //add or update score
                        featureScores.put(feature, scorevalue);
                    }
                }
            }
        }
        contingencyTable = null;
        
        Integer maxFeatures = trainingParameters.getMaxFeatures();
        if(maxFeatures!=null && maxFeatures<featureScores.size()) {
            ScoreBasedFeatureSelection.selectHighScoreFeatures(featureScores, maxFeatures);
        }
    }
    
}
