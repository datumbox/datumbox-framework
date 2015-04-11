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
package com.datumbox.framework.machinelearning.common.bases.featureselection;

import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.google.common.collect.Ordering;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class which is the base of every Categorical Feature Selection algorithm.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class ScoreBasedFeatureSelection<MP extends ScoreBasedFeatureSelection.ModelParameters, TP extends ScoreBasedFeatureSelection.TrainingParameters> extends FeatureSelection<MP, TP> {

    public static abstract class ModelParameters extends FeatureSelection.ModelParameters {

        public ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
    }
    
    
    public static abstract class TrainingParameters extends FeatureSelection.TrainingParameters {
        
    }
    

    protected ScoreBasedFeatureSelection(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass) {
        super(dbName, dbConf, mpClass, tpClass);
    }
    
    
    public static void selectHighScoreFeatures(Map<Object, Double> featureScores, Integer maxFeatures) {
        Logger logger = LoggerFactory.getLogger(ScoreBasedFeatureSelection.class);
        logger.debug("selectHighScoreFeatures()");
        
        logger.debug("Estimating the minPermittedScore");
        Double minPermittedScore=Ordering.<Double>natural().greatestOf(featureScores.values().iterator(), maxFeatures).get(maxFeatures-1);

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
    
}
