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
import com.datumbox.common.utilities.PHPfunctions;
import com.datumbox.framework.machinelearning.common.bases.featureselection.ScoreBasedFeatureSelection;
import java.util.Arrays;
import java.util.Map;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class MutualInformation extends CategoricalFeatureSelection<MutualInformation.ModelParameters, MutualInformation.TrainingParameters>{
    //References: http://nlp.stanford.edu/IR-book/html/htmledition/mutual-information-1.html
    
    public static final String SHORT_METHOD_NAME = "MIFS";
       
    public static class ModelParameters extends CategoricalFeatureSelection.ModelParameters {
        
    }

    public static class TrainingParameters extends CategoricalFeatureSelection.TrainingParameters {

    }
    
    public MutualInformation(String dbName) {
        super(dbName, MutualInformation.ModelParameters.class, MutualInformation.TrainingParameters.class);
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
        
        double N = modelParameters.getN();
        for(Map.Entry<Object, Double> featureCount : modelParameters.getFeatureCounts().entrySet()) {
            Object feature = featureCount.getKey();
            double N1_ = featureCount.getValue(); //calculate the N1. (number of records that has the feature)
            double N0_ = N - N1_; //also the N0. (number of records that DONT have the feature)
            
            for(Map.Entry<Object, Integer> classCount : modelParameters.getClassCounts().entrySet()) {
                Object theClass = classCount.getKey();
                
                double N_1 = classCount.getValue();
                double N_0 = N - N_1;
                Integer featureClassC = modelParameters.getFeatureClassCounts().get(Arrays.<Object>asList(feature, theClass));                
                double N11 = (featureClassC!=null)?featureClassC:0.0; //N11 is the number of records that have the feature and belong on the specific class
                
                double N01 = N_1 - N11; //N01 is the total number of records that do not have the particular feature BUT they belong to the specific class
                
                double N00 = N0_ - N01;
                double N10 = N1_ - N11;
                
                //calculate Mutual Information
                //Note we calculate it partially because if one of the N.. is zero the log will not be defined and it will return NAN.
                double MI=0.0;
                if(N11>0.0) {
                    MI+=(N11/N)*PHPfunctions.log((N/N1_)*(N11/N_1),2.0);
                }
                if(N01>0.0) {
                    MI+=(N01/N)*PHPfunctions.log((N/N0_)*(N01/N_1),2.0);
                }
                if(N10>0.0) {
                    MI+=(N10/N)*PHPfunctions.log((N/N1_)*(N10/N_0),2.0);
                }
                if(N00>0.0) {
                    MI+=(N00/N)*PHPfunctions.log((N/N0_)*(N00/N_0),2.0);
                }

                
                //REMEMBER! larger scores means more important keywords.
                Double previousMI = featureScores.get(feature);
                if(previousMI==null || previousMI<MI) { //add or update score
                    featureScores.put(feature, MI);
                }
                
            }
        }
        
        Integer maxFeatures = trainingParameters.getMaxFeatures();
        if(maxFeatures!=null && maxFeatures<featureScores.size()) {
            ScoreBasedFeatureSelection.selectHighScoreFeatures(featureScores, maxFeatures);
        }
        

    }
    

}
