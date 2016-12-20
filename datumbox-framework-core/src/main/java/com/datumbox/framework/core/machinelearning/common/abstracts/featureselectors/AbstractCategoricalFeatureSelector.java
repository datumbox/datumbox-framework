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
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.Record;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.common.dataobjects.TypeInference.DataType;
import com.datumbox.framework.common.persistentstorage.interfaces.BigMap;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector.MapType;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector.StorageHint;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Abstract class which is the base of every Categorical Feature Selection algorithm.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class AbstractCategoricalFeatureSelector<MP extends AbstractCategoricalFeatureSelector.AbstractModelParameters, TP extends AbstractCategoricalFeatureSelector.AbstractTrainingParameters> extends AbstractFeatureSelector<MP, TP> {

    /** {@inheritDoc} */
    public static abstract class AbstractModelParameters extends AbstractFeatureSelector.AbstractModelParameters {

        @BigMap(keyClass=Object.class, valueClass=Double.class, mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=true)
        private Map<Object, Double> featureScores; //map which stores the scores of the features

        /** 
         * @param dbc
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(DatabaseConnector)
         */
        protected AbstractModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
        /**
         * Getter of the Feature Scores.
         * 
         * @return 
         */
        public Map<Object, Double> getFeatureScores() {
            return featureScores;
        }
        
        /**
         * Setter of the Feature Scores.
         * 
         * @param featureScores 
         */
        protected void setFeatureScores(Map<Object, Double> featureScores) {
            this.featureScores = featureScores;
        }
        
    }
    
    /** {@inheritDoc} */
    public static abstract class AbstractTrainingParameters extends AbstractFeatureSelector.AbstractTrainingParameters {
        
        private Integer rareFeatureThreshold = null;
        private Integer maxFeatures=null;
        
        /**
         * Getter for the rare feature threshold. Any feature that exists
         * in the training dataset less times than this number will be removed
         * directly. 
         * 
         * @return 
         */
        public Integer getRareFeatureThreshold() {
            return rareFeatureThreshold;
        }
        
        /**
         * Setter for the rare feature threshold. Set to null to deactivate this 
         * feature. Any feature that exists in the training dataset less times 
         * than this number will be removed directly. 
         * 
         * @param rareFeatureThreshold 
         */
        public void setRareFeatureThreshold(Integer rareFeatureThreshold) {
            this.rareFeatureThreshold = rareFeatureThreshold;
        }
        
        /**
         * Getter for the maximum number of features that should be kept in the
         * dataset.
         * 
         * @return 
         */
        public Integer getMaxFeatures() {
            return maxFeatures;
        }
        
        /**
         * Setter for the maximum number of features that should be kept in the
         * dataset. Set to null for unlimited.
         * 
         * @param maxFeatures 
         */
        public void setMaxFeatures(Integer maxFeatures) {
            this.maxFeatures = maxFeatures;
        }
        
    }

    /**
     * @param dbName
     * @param conf
     * @param trainingParameters
     * @see AbstractTrainer#AbstractTrainer(String, Configuration, AbstractTrainer.AbstractTrainingParameters)
     */
    protected AbstractCategoricalFeatureSelector(String dbName, Configuration conf, TP trainingParameters) {
        super(dbName, conf, trainingParameters);
    }

    /**
     * @param dbName
     * @param conf
     * @see AbstractTrainer#AbstractTrainer(java.lang.String, Configuration)
     */
    protected AbstractCategoricalFeatureSelector(String dbName, Configuration conf) {
        super(dbName, conf);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        
        DatabaseConnector dbc = knowledgeBase.getDbc();
        
        Map<Object, Integer> tmp_classCounts = new HashMap<>(); //map which stores the counts of the classes
        Map<List<Object>, Integer> tmp_featureClassCounts = dbc.getBigMap("tmp_featureClassCounts", (Class<List<Object>>)(Class<?>)List.class, Integer.class, MapType.HASHMAP, StorageHint.IN_MEMORY, false, true); //map which stores the counts of feature-class combinations.
        Map<Object, Double> tmp_featureCounts = dbc.getBigMap("tmp_featureCounts", Object.class, Double.class, MapType.HASHMAP, StorageHint.IN_MEMORY, false, true); //map which stores the counts of the features

        
        //build the maps with the feature statistics and counts
        buildFeatureStatistics(trainingData, tmp_classCounts, tmp_featureClassCounts, tmp_featureCounts);
        
        
        
        
        //call the overriden method to get the scores of the features.
        //WARNING: do not use feature scores for any weighting. Sometimes the features are selected based on a minimum and others on a maximum criterion.
        estimateFeatureScores(trainingData.size(), tmp_classCounts, tmp_featureClassCounts, tmp_featureCounts);
        

        //drop the unnecessary stastistics tables
        dbc.dropBigMap("tmp_featureClassCounts", tmp_featureClassCounts);
        dbc.dropBigMap("tmp_featureCounts", tmp_featureCounts);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _transform(Dataframe newdata) {
        //now filter the data by removing all the features that are not selected
        filterData(newdata, knowledgeBase.getDbc(), knowledgeBase.getModelParameters().getFeatureScores());
    }
    
    private static void filterData(Dataframe data, DatabaseConnector dbc, Map<Object, Double> featureScores) {
        Logger logger = LoggerFactory.getLogger(AbstractCategoricalFeatureSelector.class);
        logger.debug("filterData()");
        
        Map<Object, Boolean> tmp_removedColumns = dbc.getBigMap("tmp_removedColumns", Object.class, Boolean.class, MapType.HASHMAP, StorageHint.IN_MEMORY, false, true);
        
        for(Map.Entry<Object, DataType> entry: data.getXDataTypes().entrySet()) {
            Object feature = entry.getKey();

            if(!featureScores.containsKey(feature)) {
                tmp_removedColumns.put(feature, true);
            }
        }
        
        logger.debug("Removing Columns");
        data.dropXColumns(tmp_removedColumns.keySet());
        
        //Drop the temporary Collection
        dbc.dropBigMap("tmp_removedColumns", tmp_removedColumns);
    }
    
    private void removeRareFeatures(Dataframe data, Map<Object, Double> featureCounts) {
        logger.debug("removeRareFeatures()");
        DatabaseConnector dbc = knowledgeBase.getDbc();
        TP trainingParameters = knowledgeBase.getTrainingParameters();
        Integer rareFeatureThreshold = trainingParameters.getRareFeatureThreshold();
        
        Map<Object, TypeInference.DataType> columnTypes = data.getXDataTypes();
        
        //find the featureCounts
        
        logger.debug("Estimating featureCounts");
        for(Record r : data) {
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();

                Double value = TypeInference.toDouble(entry.getValue());
                if(value==null || value==0.0) {
                    continue;
                }

                //feature counts
                double featureCounter = featureCounts.getOrDefault(feature, 0.0);
                featureCounts.put(feature, ++featureCounter);
                
            }
        }

        //remove rare features
        if(rareFeatureThreshold != null && rareFeatureThreshold>0) {
            logger.debug("Removing rare features");
            //remove features from the featureCounts list
            Iterator<Map.Entry<Object, Double>> it = featureCounts.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<Object, Double> entry = it.next();
                if(entry.getValue()<=rareFeatureThreshold) {
                    it.remove(); 
                }
            }
            
            //then remove the features in dataset that do not appear in the list
            filterData(data, dbc, featureCounts);
        }
    }
    
    private void buildFeatureStatistics(Dataframe data, Map<Object, Integer> classCounts, Map<List<Object>, Integer> featureClassCounts, Map<Object, Double> featureCounts) {        
        logger.debug("buildFeatureStatistics()");
        TP trainingParameters = knowledgeBase.getTrainingParameters();
        
        //the method below does not only removes the rare features but also
        //first and formost calculates the contents of featureCounts map. 
        removeRareFeatures(data, featureCounts);
        
        Map<Object, TypeInference.DataType> columnTypes = data.getXDataTypes();
        //now find the classCounts and the featureClassCounts
        logger.debug("Estimating classCounts and featureClassCounts");
        for(Record r : data) {
            Object theClass = r.getY();

            //class counts
            int classCounter = classCounts.getOrDefault(theClass, 0);
            classCounts.put(theClass, ++classCounter);


            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                
                Double value = TypeInference.toDouble(entry.getValue());
                if(value==null || value==0.0) {
                    continue;
                }



                //featureClass counts
                List<Object> featureClassTuple = Arrays.asList(feature, theClass);
                Integer featureClassCounter = featureClassCounts.get(featureClassTuple);
                if(featureClassCounter==null) {
                    featureClassCounter=0;
                }
                featureClassCounts.put(featureClassTuple, ++featureClassCounter);
            }


        }
        
    }
    
    /**
     * Abstract method which is responsible for estimating the score of each
     * Feature.
     *
     * @param N
     * @param classCounts
     * @param featureClassCounts
     * @param featureCounts 
     */
    protected abstract void estimateFeatureScores(int N, Map<Object, Integer> classCounts, Map<List<Object>, Integer> featureClassCounts, Map<Object, Double> featureCounts);
}
