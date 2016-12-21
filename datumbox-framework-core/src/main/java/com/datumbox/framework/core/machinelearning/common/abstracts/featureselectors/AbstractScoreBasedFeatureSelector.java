/**
 * Copyright (C) 2013-2016 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
import com.datumbox.framework.common.utilities.SelectKth;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

/**
 * Abstract class which is the base of every Scored Based Feature Selection algorithm.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class AbstractScoreBasedFeatureSelector<MP extends AbstractScoreBasedFeatureSelector.AbstractModelParameters, TP extends AbstractScoreBasedFeatureSelector.AbstractTrainingParameters> extends AbstractFeatureSelector<MP, TP> {

    /**
     * @param trainingParameters
     * @param conf
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainingParameters, Configuration)
     */
    protected AbstractScoreBasedFeatureSelector(TP trainingParameters, Configuration conf) {
        super(trainingParameters, conf);
    }

    /**
     * @param dbName
     * @param conf
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected AbstractScoreBasedFeatureSelector(String dbName, Configuration conf) {
        super(dbName, conf);
    }
    
    /**
     * This method keeps the highest scoring features of the provided feature map
     * and removes all the others.
     * 
     * @param featureScores
     * @param maxFeatures 
     */
    public static void selectHighScoreFeatures(Map<Object, Double> featureScores, Integer maxFeatures) {
        Logger logger = LoggerFactory.getLogger(AbstractScoreBasedFeatureSelector.class);
        logger.debug("selectHighScoreFeatures()");
        
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
    
}
