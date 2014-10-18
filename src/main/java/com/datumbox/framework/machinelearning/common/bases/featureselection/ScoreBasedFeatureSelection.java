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
package com.datumbox.framework.machinelearning.common.bases.featureselection;

import com.google.common.collect.Ordering;
import java.util.Iterator;
import java.util.Map;

/**
 * Abstract class which is the base of every Categorical Feature Selection algorithm.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class ScoreBasedFeatureSelection<MP extends ScoreBasedFeatureSelection.ModelParameters, TP extends ScoreBasedFeatureSelection.TrainingParameters> extends FeatureSelection<MP, TP> {

    public static abstract class ModelParameters extends FeatureSelection.ModelParameters {
        
    }
    
    
    public static abstract class TrainingParameters extends FeatureSelection.TrainingParameters {
        
    }
    

    protected ScoreBasedFeatureSelection(String dbName, Class<MP> mpClass, Class<TP> tpClass) {
        super(dbName, mpClass, tpClass);
    }
    
    
    public static void selectHighScoreFeatures(Map<Object, Double> featureScores, Integer maxFeatures) {
        
        Double minPermittedScore=Ordering.<Double>natural().greatestOf(featureScores.values().iterator(), maxFeatures).get(maxFeatures-1);

        boolean mongoDBhackRequired = featureScores.getClass().getName().contains("mongo"); //the MongoDB does not support iterator remove. We use this nasty hack to detect it and use remove instead

        //remove any entry with score less than the minimum permitted one
        Iterator<Map.Entry<Object, Double>> it = featureScores.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Object, Double> entry = it.next();
            if(entry.getValue()<minPermittedScore) { 
                if(!mongoDBhackRequired) {
                    it.remove(); 
                }
                else {
                    featureScores.remove(entry.getKey()); //hack for mongo
                }
            }
        }

        //if some extra features still exist (due to ties on the scores) remove some of those extra features
        int numOfExtraFeatures = featureScores.size()-maxFeatures;
        if(numOfExtraFeatures>0) {
            it = featureScores.entrySet().iterator();
            while(it.hasNext() && numOfExtraFeatures>0) {
                Map.Entry<Object, Double> entry = it.next();
                if(entry.getValue()-minPermittedScore<=0.0) { //DO NOT COMPARE THEM DIRECTLY USE SUBTRACTION!
                    if(!mongoDBhackRequired) {
                        it.remove(); 
                    }
                    else {
                        featureScores.remove(entry.getKey()); //hack for mongo
                    }
                    --numOfExtraFeatures;
                }
            }
        }
    }
    
}
