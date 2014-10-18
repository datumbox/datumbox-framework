/* 
 * Copyright (C) 2014 Vasilis Vryniotis <bbriniotis at datumbox.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.datumbox.framework.machinelearning.featureselection.categorical;

import com.datumbox.framework.machinelearning.common.bases.featureselection.CategoricalFeatureSelection;
import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.DataTable2D;
import com.datumbox.framework.machinelearning.common.bases.featureselection.ScoreBasedFeatureSelection;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;
import com.datumbox.framework.statistics.nonparametrics.independentsamples.Chisquare;
import java.util.Arrays;
import java.util.Map;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class ChisquareSelect extends CategoricalFeatureSelection<ChisquareSelect.ModelParameters, ChisquareSelect.TrainingParameters>{
    //References: http://nlp.stanford.edu/IR-book/html/htmledition/feature-selectionchi2-feature-selection-1.html
    
    public static final String SHORT_METHOD_NAME = "ChiFS";
       
    public static class ModelParameters extends CategoricalFeatureSelection.ModelParameters {
        
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
    
    public ChisquareSelect(String dbName) {
        super(dbName, ChisquareSelect.ModelParameters.class, ChisquareSelect.TrainingParameters.class);
    }
    
    @Override
    public String shortMethodName() {
        return SHORT_METHOD_NAME;
    }
    
    @Override
    protected void estimateFeatureScores() {
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        Map<Object, Double> featureScores = modelParameters.getFeatureScores();
        
        DataTable2D contingencyTable = new DataTable2D();
        contingencyTable.put(0, new AssociativeArray());
        contingencyTable.put(1, new AssociativeArray());
        
        double criticalValue = ContinuousDistributions.ChisquareInverseCdf(trainingParameters.getALevel(), 1); //one degree of freedom because the tables below are 2x2
        
        
        double N = modelParameters.getN();
        for(Map.Entry<Object, Double> featureCount : modelParameters.getFeatureCounts().entrySet()) {
            Object feature = featureCount.getKey();
            double N1_ = featureCount.getValue(); //calculate the N1. (number of records that has the feature)
            double N0_ = N - N1_; //also the N0. (number of records that DONT have the feature)
            
            for(Map.Entry<Object, Integer> classCount : modelParameters.getClassCounts().entrySet()) {
                Object theClass = classCount.getKey();
                
                Integer featureClassC = modelParameters.getFeatureClassCounts().get(Arrays.<Object>asList(feature, theClass));                
                double N11 = (featureClassC!=null)?featureClassC:0.0; //N11 is the number of records that have the feature and belong on the specific class
                double N01 = classCount.getValue() - N11; //N01 is the total number of records that do not have the particular feature BUT they belong to the specific class
                
                double N00 = N0_ - N01;
                double N10 = N1_ - N11;
                
                contingencyTable.get(0).put(0, N00);
                contingencyTable.get(0).put(1, N01);
                contingencyTable.get(1).put(0, N10);
                contingencyTable.get(1).put(1, N11);
                
                /*
                //REMEMBER! smaller pvalue means more important keyword. We reject the H0 of being not important.
                double pvalue = Chisquare.getPvalue(contingencyTable); 
                if(pvalue<=parameters.getALevel()) { //if the pvalue is smaller than the significance that we requested, then select the feature
                    Double previousPvalue = featureScores.get(feature);
                    if(previousPvalue==null || previousPvalue>pvalue) { //add or update score
                        featureScores.put(feature, pvalue);
                    }
                }
                */
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
