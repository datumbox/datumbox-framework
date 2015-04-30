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
package com.datumbox.framework.machinelearning.recommendersystem;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.dataobjects.TransposeDataList;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.MapFunctions;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLrecommender;
import com.datumbox.framework.mathematics.distances.Distance;
import com.datumbox.framework.statistics.parametrics.relatedsamples.PearsonCorrelation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * Implementation of Collaborative Filtering algorithm.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class CollaborativeFiltering extends BaseMLrecommender<CollaborativeFiltering.ModelParameters, CollaborativeFiltering.TrainingParameters> {

    /**
     * The ModelParameters class stores the coefficients that were learned during
     * the training of the algorithm.
     */
    public static class ModelParameters extends BaseMLrecommender.ModelParameters {
        
        @BigMap
        private Map<List<Object>, Double> similarities; //the similarity map among observations

        /**
         * Protected constructor which accepts as argument the DatabaseConnector.
         * 
         * @param dbc 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
        //Getters / Setters
        
        /**
         * Getter for the similarities map.
         * 
         * @return 
         */
        public Map<List<Object>, Double> getSimilarities() {
            return similarities;
        }
        
        /**
         * Setter for the similarities map.
         * 
         * @param similarities 
         */
        protected void setSimilarities(Map<List<Object>, Double> similarities) {
            this.similarities = similarities;
        }
   
    }
    
    /**
     * The TrainingParameters class stores the parameters that can be changed
     * before training the algorithm.
     */
    public static class TrainingParameters extends BaseMLrecommender.TrainingParameters {
        /**
         * Enum with Similarity Measures.
         */
        public enum SimilarityMeasure {
            /**
             * Euclidian distance.
             */
            EUCLIDIAN,
            
            /**
             * Manhattan distance.
             */
            MANHATTAN,
            
            /**
             * Pearson's Correlation.
             */
            PEARSONS_CORRELATION;
        }
        
        private SimilarityMeasure similarityMethod = SimilarityMeasure.EUCLIDIAN;
        
        /**
         * Getter for the similarity method.
         * 
         * @return 
         */
        public SimilarityMeasure getSimilarityMethod() {
            return similarityMethod;
        }
        
        /**
         * Setter for the similarity method.
         * 
         * @param similarityMethod 
         */
        public void setSimilarityMethod(SimilarityMeasure similarityMethod) {
            this.similarityMethod = similarityMethod;
        }

    }
    
    /**
     * Public constructor of the algorithm.
     * 
     * @param dbName
     * @param dbConf 
     */
    public CollaborativeFiltering(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, CollaborativeFiltering.ModelParameters.class, CollaborativeFiltering.TrainingParameters.class);
    } 
    
    @Override
    protected void _fit(Dataset trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        //calculate similarity matrix
        Map<List<Object>, Double> similarities = modelParameters.getSimilarities();
        for(Integer rId1 : trainingData) {
            Record r1 = trainingData.get(rId1);
            for(Integer rId2: trainingData) {
                Record r2 = trainingData.get(rId2);
                Object y1 = r1.getY();
                Object y2 = r2.getY();
                
                if(Objects.equals(y1, y2)) {
                    continue;
                }
                
                List<Object> tkp = Arrays.asList(y1, y2);
                if(similarities.containsKey(tkp)) {
                    continue;
                }
                
                double similarity = calculateSimilarity(r1, r2);
                if(similarity>0) {
                    similarities.put(tkp, similarity);
                    similarities.put(Arrays.asList(y2, y1), similarity); //add also for the reverse key because similarity is symmetric
                }
            }
        }
    }

    @Override
    protected void predictDataset(Dataset newData) {
        Map<List<Object>, Double> similarities = knowledgeBase.getModelParameters().getSimilarities();
        
        //generate recommendation for each record in the list
        for(Integer rId : newData) {
            Record r = newData.get(rId);
            Map<Object, Object> recommendations = new HashMap<>();
            
            Map<Object, Double> simSums = new HashMap<>();
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object row = entry.getKey();
                Double score = TypeInference.toDouble(entry.getValue());
                
                //Since we can't use 2D Maps due to mongo, we are forced to loop
                //the whole similarities map which is very inefficient.
                for(Map.Entry<List<Object>, Double> entry2 : similarities.entrySet()) {
                    List<Object> tpk = entry2.getKey();
                    if(!tpk.get(0).equals(row)) {
                        continue; //filter the irrelevant two pair key combinations that do not include the row
                    }
                    
                    Object column = tpk.get(1);
                    if(r.getX().containsKey(column)) {
                        continue; // they already rated this
                    }
                    
                    Double previousRecValue = TypeInference.toDouble(recommendations.get(column));
                    Double previousSimsumValue = simSums.get(column);
                    if(previousRecValue==null) {
                        previousRecValue=0.0;
                        previousSimsumValue=0.0;
                    }
                    
                    Double similarity = entry2.getValue();
                    
                    recommendations.put(column, previousRecValue+similarity*score);
                    simSums.put(column, previousSimsumValue+similarity);
                }
            }
            
            for(Map.Entry<Object, Object> entry : recommendations.entrySet()) {
                Object column = entry.getKey();
                Double score = TypeInference.toDouble(entry.getValue());
                
                recommendations.put(column, score/simSums.get(column));
            }
            simSums = null;
            
            if(!recommendations.isEmpty()) {
                //sort recommendation by popularity
                recommendations = MapFunctions.sortNumberMapByValueDescending(recommendations);
                newData.set(rId, new Record(r.getX(), r.getY(), recommendations.keySet().iterator().next(), new AssociativeArray(recommendations)));
            }
        }
    }

    private double calculateSimilarity(Record r1, Record r2) {        
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        double similarity = 0.0;
        TrainingParameters.SimilarityMeasure similarityMethod = trainingParameters.getSimilarityMethod();
        if(similarityMethod==TrainingParameters.SimilarityMeasure.EUCLIDIAN) {
            similarity = Distance.euclidean(r1.getX(), r2.getX());
            
            similarity = 1.0/(1.0+similarity); //convert distance into a similarity measure
        }
        else if(similarityMethod==TrainingParameters.SimilarityMeasure.MANHATTAN) {
            similarity = Distance.manhattan(r1.getX(), r2.getX());
            
            similarity = 1.0/(1.0+similarity); //convert distance into a similarity measure
        }
        else if(similarityMethod==TrainingParameters.SimilarityMeasure.PEARSONS_CORRELATION) {
            
            
            //extract the commonColumns to ensure that the order of the data will be the same for both records
            Set<Object> commonColumns = new HashSet<>(r1.getX().keySet());
            commonColumns.addAll(r2.getX().keySet());
            
            //create the FlatDataLists with the data of each record
            FlatDataList flatDataList1 = new FlatDataList();
            FlatDataList flatDataList2 = new FlatDataList();
            for(Object column : commonColumns) {
                flatDataList1.add(TypeInference.toDouble(r1.getX().get(column)));
                flatDataList2.add(TypeInference.toDouble(r2.getX().get(column)));
            }
            
            TransposeDataList transposeDataList = new TransposeDataList();
            transposeDataList.put(1, flatDataList1);
            transposeDataList.put(2, flatDataList2);
            
            //estimate the pearson's correlation
            similarity = PearsonCorrelation.calculateCorrelation(transposeDataList);
        }
        else { 
            throw new RuntimeException("Unsupported Distance method");
        }
        
        return similarity;
    } 
    
}
