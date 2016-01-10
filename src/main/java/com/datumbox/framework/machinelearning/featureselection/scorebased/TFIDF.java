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
package com.datumbox.framework.machinelearning.featureselection.scorebased;

import com.datumbox.common.concurrency.StreamMethods;
import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.MapType;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.StorageHint;

import com.datumbox.framework.machinelearning.common.abstracts.featureselectors.AbstractScoreBasedFeatureSelector;
import com.datumbox.framework.machinelearning.common.interfaces.Parallelizable;
import java.util.Map;
import java.util.function.BiFunction;


/**
 * Implementation of the TF-IDF Feature Selection algorithm. * 
 * 
 * References: 
 * http://en.wikipedia.org/wiki/Tf%E2%80%93idf
 * https://gist.github.com/AloneRoad/1605037
 * http://www.tfidf.com/
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class TFIDF extends AbstractScoreBasedFeatureSelector<TFIDF.ModelParameters, TFIDF.TrainingParameters> implements Parallelizable {

    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractScoreBasedFeatureSelector.AbstractModelParameters {
        private static final long serialVersionUID = 1L;
        
        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=false)
        private Map<Object, Double> maxTFIDFfeatureScores; //map which stores the max tfidf of the features
        
        /** 
         * @param dbc
         * @see com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer.AbstractModelParameters#AbstractModelParameters(com.datumbox.common.persistentstorage.interfaces.DatabaseConnector) 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
        /**
         * Getter for the maximum TFIDF scores of each keyword in the vocabulary.
         * 
         * @return 
         */
        public Map<Object, Double> getMaxTFIDFfeatureScores() {
            return maxTFIDFfeatureScores;
        }
        
        /**
         * Setter for the maximum TFIDF scores of each keyword in the vocabulary.
         * 
         * @param maxTFIDFfeatureScores 
         */
        protected void setMaxTFIDFfeatureScores(Map<Object, Double> maxTFIDFfeatureScores) {
            this.maxTFIDFfeatureScores = maxTFIDFfeatureScores;
        }

    }
    
    /** {@inheritDoc} */
    public static class TrainingParameters extends AbstractScoreBasedFeatureSelector.AbstractTrainingParameters {
        private static final long serialVersionUID = 1L;
        
        private boolean binarized = false;
        private Integer maxFeatures=null;
        
        /**
         * Getter for the binarized flag; when it is set on the frequencies of the
         * activated keywords are clipped to 1.
         * 
         * @return 
         */
        public boolean isBinarized() {
            return binarized;
        }
        
        /**
         * Setter for the binarized flag; when it is set on the frequencies of the
         * activated keywords are clipped to 1.
         * 
         * @param binarized 
         */
        public void setBinarized(boolean binarized) {
            this.binarized = binarized;
        }
        
        /**
         * Getter for the threshold of maximum selected features.
         * 
         * @return 
         */
        public Integer getMaxFeatures() {
            return maxFeatures;
        }
        
        /**
         * Setter for the threshold of maximum selected features.
         * 
         * @param maxFeatures 
         */
        public void setMaxFeatures(Integer maxFeatures) {
            this.maxFeatures = maxFeatures;
        }
        
    }    
    
    /**
     * Public constructor of the algorithm.
     * 
     * @param dbName
     * @param dbConf 
     */
    public TFIDF(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, TFIDF.ModelParameters.class, TFIDF.TrainingParameters.class);
    }
    
    private boolean parallelized = true;
    
    /** {@inheritDoc} */
    @Override
    public boolean isParallelized() {
        return parallelized;
    }

    /** {@inheritDoc} */
    @Override
    public void setParallelized(boolean parallelized) {
        this.parallelized = parallelized;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        ModelParameters modelParameters = kb().getModelParameters();
        TrainingParameters trainingParameters = kb().getTrainingParameters();
        
        boolean binarized = trainingParameters.isBinarized();
        
        
        int n = modelParameters.getN();
        
        DatabaseConnector dbc = kb().getDbc();
        Map<Object, Double> tmp_idfMap = dbc.getBigMap("tmp_idf", MapType.HASHMAP, StorageHint.IN_MEMORY, false, true);

        //initially estimate the counts of the terms in the dataset and store this temporarily
        //in idf map. this help us avoid using twice much memory comparing to
        //using two different maps
        for(Record r : trainingData) { 
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object keyword = entry.getKey();
                Double counts = TypeInference.toDouble(entry.getValue());
                
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
        
        
        final Map<Object, Double> maxFeatureScores = modelParameters.getMaxTFIDFfeatureScores();
        
        //this lambda checks if the new score is larger than the current max score of the feature
        BiFunction<Object, Double, Boolean> isGreaterThanMax = (feature, newScore) -> {
            Double maxScore = maxFeatureScores.get(feature);
            return maxScore==null || maxScore<newScore;
        };
        
        //calculate the maximum tfidf scores
        StreamMethods.stream(trainingData, isParallelized()).forEach(r -> {
            //calculate the tfidf scores
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object keyword = entry.getKey();
                Double counts = TypeInference.toDouble(entry.getValue());
                
                if(counts != null && counts > 0.0) {

                    if(binarized) {
                        counts = 1.0;
                    }

                    //double tf = counts/documentLength;
                    double tf = counts;
                    double idf = tmp_idfMap.get(keyword);

                    double tfidf = tf*idf;
                    
                    if(tfidf > 0.0) { //ignore 0 scored features
                        
                        //Threads will help here under the assumption that only 
                        //a small number of records will have features that exceed 
                        //the maximum score. Thus they will stop in this if statement
                        //and they will not go into the synced block.
                        if(isGreaterThanMax.apply(keyword, tfidf)) {
                            synchronized(maxFeatureScores) {
                                if(isGreaterThanMax.apply(keyword, tfidf)) {
                                    maxFeatureScores.put(keyword, tfidf);
                                }
                            }
                        }
                        
                    }
                }
            }
        });
        
        //Drop the temporary Collection
        dbc.dropBigMap("tmp_idf", tmp_idfMap);
        
        Integer maxFeatures = trainingParameters.getMaxFeatures();
        if(maxFeatures!=null && maxFeatures<maxFeatureScores.size()) {
            AbstractScoreBasedFeatureSelector.selectHighScoreFeatures(maxFeatureScores, maxFeatures);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void filterFeatures(Dataframe newData) {
        DatabaseConnector dbc = kb().getDbc();
        Map<Object, Double> maxTFIDFfeatureScores = kb().getModelParameters().getMaxTFIDFfeatureScores();
        
        Map<Object, Boolean> tmp_removedColumns = dbc.getBigMap("tmp_removedColumns", MapType.HASHMAP, StorageHint.IN_MEMORY, false, true);
        
        for(Object feature: newData.getXDataTypes().keySet()) {
            if(!maxTFIDFfeatureScores.containsKey(feature)) {
                tmp_removedColumns.put(feature, true);
            }
        }
        
        newData.dropXColumns(tmp_removedColumns.keySet());
        
        //Drop the temporary Collection
        dbc.dropBigMap("tmp_removedColumns", tmp_removedColumns);
        
    }
    
}
