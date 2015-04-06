/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
package com.datumbox.framework.machinelearning.featureselection.scorebased;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.TypeConversions;

import com.datumbox.framework.machinelearning.common.bases.featureselection.ScoreBasedFeatureSelection;
import java.util.Iterator;
import java.util.Map;


/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class TFIDF extends ScoreBasedFeatureSelection<TFIDF.ModelParameters, TFIDF.TrainingParameters> {
    /*
        References: 
            http://en.wikipedia.org/wiki/Tf%E2%80%93idf
            https://gist.github.com/AloneRoad/1605037
            http://www.tfidf.com/
    */

    public static class TrainingParameters extends ScoreBasedFeatureSelection.TrainingParameters {
        private boolean binarized = false;
        private Integer maxFeatures=null;

        public boolean isBinarized() {
            return binarized;
        }

        public void setBinarized(boolean binarized) {
            this.binarized = binarized;
        }

        public Integer getMaxFeatures() {
            return maxFeatures;
        }

        public void setMaxFeatures(Integer maxFeatures) {
            this.maxFeatures = maxFeatures;
        }
        
    }
    
    public static class ModelParameters extends ScoreBasedFeatureSelection.ModelParameters {
        private int N;
        
        @BigMap
        private Map<Object, Double> maxTFIDFfeatureScores; //map which stores the max tfidf of the features

        

        public ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
        public int getN() {
            return N;
        }

        public void setN(int N) {
            this.N = N;
        }

        public Map<Object, Double> getMaxTFIDFfeatureScores() {
            return maxTFIDFfeatureScores;
        }

        public void setMaxTFIDFfeatureScores(Map<Object, Double> maxTFIDFfeatureScores) {
            this.maxTFIDFfeatureScores = maxTFIDFfeatureScores;
        }

    }
    
    
    public TFIDF(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, TFIDF.ModelParameters.class, TFIDF.TrainingParameters.class);
    }
    
    @Override
    protected void _fit(Dataset trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        boolean binarized = trainingParameters.isBinarized();
        
        
        int n = trainingData.size();
        modelParameters.setN(n);
        
        DatabaseConnector dbc = knowledgeBase.getDbc();
        Map<Object, Double> tmp_idfMap = dbc.getBigMap("tmp_idf", true);

        //initially estimate the counts of the terms in the dataset and store this temporarily
        //in idf map. this help us avoid using twice much memory comparing to
        //using two different maps
        for(Integer rId : trainingData) { 
            Record r = trainingData.get(rId);
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object keyword = entry.getKey();
                Double counts = TypeConversions.toDouble(entry.getValue());
                
                if(counts==null || counts == 0.0) {
                    continue;
                }
                
                Double previousIDFvalue = tmp_idfMap.get(keyword);
                if(previousIDFvalue==null) {
                    previousIDFvalue = 0.0;
                }
                
                tmp_idfMap.put(keyword, ++previousIDFvalue);
            }
        }
        
        //convert counts to idf scores
        for(Map.Entry<Object, Double> entry : tmp_idfMap.entrySet()) {
            Object keyword = entry.getKey();
            Double countsInDocument = entry.getValue();
            
            tmp_idfMap.put(keyword, Math.log10(n/countsInDocument));
        }
        
        
        Map<Object, Double> maxTFIDFfeatureScores = modelParameters.getMaxTFIDFfeatureScores();
        //calculate the maximum tfidf scores
        for(Integer rId : trainingData) { 
            Record r = trainingData.get(rId);
            
            //calculate the tfidf scores
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object keyword = entry.getKey();
                Double counts = TypeConversions.toDouble(entry.getValue());
                
                if(counts==null || counts == 0.0) {
                    continue;
                }
                
                if(binarized) {
                    counts = 1.0;
                }
                
                //double tf = counts/documentLength;
                double tf = counts;
                double idf = tmp_idfMap.get(keyword);
                
                double tfidf = tf*idf;
                
                if(tfidf==0.0) {
                    continue; //ignore 0 scored features
                }
                
                //store the maximum value of the tfidf
                Double maxTfidf = maxTFIDFfeatureScores.get(keyword);
                if(maxTfidf==null || maxTfidf<tfidf) {
                    maxTFIDFfeatureScores.put(keyword, tfidf);
                }
            }
        }
        
        //Drop the temporary Collection
        dbc.dropBigMap("tmp_idf", tmp_idfMap);
        
        Integer maxFeatures = trainingParameters.getMaxFeatures();
        if(maxFeatures!=null && maxFeatures<maxTFIDFfeatureScores.size()) {
            ScoreBasedFeatureSelection.selectHighScoreFeatures(maxTFIDFfeatureScores, maxFeatures);
        }
    }

    @Override
    protected void filterFeatures(Dataset newData) {
        DatabaseConnector dbc = knowledgeBase.getDbc();
        Map<Object, Double> maxTFIDFfeatureScores = knowledgeBase.getModelParameters().getMaxTFIDFfeatureScores();
        
        Map<Object, Boolean> tmp_removedColumns = dbc.getBigMap("tmp_removedColumns", true);
        
        for(Object feature: newData.getColumns().keySet()) {
            if(!maxTFIDFfeatureScores.containsKey(feature)) {
                tmp_removedColumns.put(feature, true);
            }
        }
        
        for(Object feature: tmp_removedColumns.keySet()) {
            newData.removeColumn(feature);
        }
        
        //Drop the temporary Collection
        dbc.dropBigMap("tmp_removedColumns", tmp_removedColumns);
        
    }
    
}
