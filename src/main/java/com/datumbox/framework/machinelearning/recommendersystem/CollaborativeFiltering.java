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
package com.datumbox.framework.machinelearning.recommendersystem;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.dataobjects.TransposeDataList;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.MapFunctions;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.MapType;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.StorageHint;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLrecommender;
import com.datumbox.framework.machinelearning.common.validation.CollaborativeFilteringValidation;
import com.datumbox.framework.mathematics.distances.Distance;
import com.datumbox.framework.statistics.parametrics.relatedsamples.PearsonCorrelation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Implementation of Collaborative Filtering algorithm.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class CollaborativeFiltering extends BaseMLrecommender<CollaborativeFiltering.ModelParameters, CollaborativeFiltering.TrainingParameters, CollaborativeFiltering.ValidationMetrics> {

    /** {@inheritDoc} */
    public static class ModelParameters extends BaseMLrecommender.ModelParameters {
        private static final long serialVersionUID = 1L;
        
        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_CACHE)
        private Map<List<Object>, Double> similarities; //the similarity map among observations
        
        /** 
         * @param dbc
         * @see com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseModelParameters#BaseModelParameters(com.datumbox.common.persistentstorage.interfaces.DatabaseConnector) 
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
    
    /** {@inheritDoc} */
    public static class TrainingParameters extends BaseMLrecommender.TrainingParameters {
        private static final long serialVersionUID = 1L;
        
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
    
    /** {@inheritDoc} */
    public static class ValidationMetrics extends BaseMLrecommender.ValidationMetrics {
        private static final long serialVersionUID = 1L;
        
        private double RMSE = 0.0; 
        
        /**
         * Getter for Root-mean-square deviation.
         * 
         * @return 
         */
        public double getRMSE() {
            return RMSE;
        }

        /**
         * Setter for Root-mean-square deviation.
         * 
         * @param RMSE
         */
        public void setRMSE(double RMSE) {
            this.RMSE = RMSE;
        }
    }
    
    /**
     * Public constructor of the algorithm.
     * 
     * @param dbName
     * @param dbConf 
     */
    public CollaborativeFiltering(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, CollaborativeFiltering.ModelParameters.class, CollaborativeFiltering.TrainingParameters.class, CollaborativeFiltering.ValidationMetrics.class, new CollaborativeFilteringValidation<>());
    } 
    
    @Override
    protected void _fit(Dataframe trainingData) {
        ModelParameters modelParameters = kb().getModelParameters();
        
        //calculate similarity matrix
        Map<List<Object>, Double> similarities = modelParameters.getSimilarities();
        for(Record r1 : trainingData) {
            Object y1 = r1.getY();
            for(Record r2: trainingData) {
                Object y2 = r2.getY();
                
                List<Object> tkp = Arrays.asList(y1, y2);
                if(similarities.containsKey(tkp)) {
                    continue;
                }
                
                double similarity = calculateSimilarity(r1, r2);
                
                similarities.put(tkp, similarity);
                similarities.put(Arrays.asList(y2, y1), similarity); //add also for the reverse key because similarity is symmetric
            }
        }
    }

    @Override
    protected void predictDataset(Dataframe newData) {
        _predictDataset(newData, false);
    }
    
    private void _predictDataset(Dataframe newData, boolean includeRated) {
        Map<List<Object>, Double> similarities = kb().getModelParameters().getSimilarities();
        
        //generate recommendation for each record in the list
        for(Map.Entry<Integer, Record> e : newData.entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            Map<Object, Object> recommendations = new HashMap<>();
            
            Map<Object, Double> simSums = new HashMap<>();
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object row = entry.getKey();
                Double score = TypeInference.toDouble(entry.getValue());
                
                for(Map.Entry<List<Object>, Double> entry2 : similarities.entrySet()) {
                    List<Object> tpk = entry2.getKey();
                    if(!tpk.get(0).equals(row)) {
                        continue; //filter the irrelevant two pair key combinations that do not include the row
                    }
                    
                    Object column = tpk.get(1);
                    
                    Double previousRecValue = TypeInference.toDouble(recommendations.get(column));
                    Double previousSimsumValue = simSums.get(column);
                    if(previousRecValue==null) {
                        previousRecValue=0.0;
                        previousSimsumValue=0.0;
                    }
                    
                    Double similarity = entry2.getValue();
                    
                    if(includeRated == true || !r.getX().containsKey(column)) {
                        recommendations.put(column, previousRecValue+similarity*score);
                    }
                    simSums.put(column, previousSimsumValue+similarity);
                }
            }
            
            for(Map.Entry<Object, Object> entry : recommendations.entrySet()) {
                Object column = entry.getKey();
                Double score = TypeInference.toDouble(entry.getValue());
                
                recommendations.put(column, score/simSums.get(column));
            }
            //simSums = null;
            
            recommendations = MapFunctions.sortNumberMapByValueDescending(recommendations);
            newData._unsafe_set(rId, new Record(r.getX(), r.getY(), recommendations.keySet().iterator().next(), new AssociativeArray(recommendations)));
        }
    }

    private double calculateSimilarity(Record r1, Record r2) {        
        TrainingParameters trainingParameters = kb().getTrainingParameters();
        
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
            
            //Pearson's correlation goes from -1 to 1. This will mess up the 
            //scaling of the rates. As a result we need to rescale it rescale it to 0-1 range.
            similarity = (similarity+1.0)/2.0;
        }
        else { 
            throw new IllegalArgumentException("Unsupported Distance method.");
        }
        
        return similarity;
    } 
    
    @Override
    protected ValidationMetrics validateModel(Dataframe validationData) {
        _predictDataset(validationData, true);
        
        //create new validation metrics object
        ValidationMetrics validationMetrics = kb().getEmptyValidationMetricsObject();
        
        double RMSE = 0.0;
        int i = 0;
        for(Record r : validationData) {
            AssociativeArray predictions = r.getYPredictedProbabilities();
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object column = entry.getKey();
                Object value = entry.getValue();
                RMSE += Math.pow(TypeInference.toDouble(value)-TypeInference.toDouble(predictions.get(column)), 2.0);
                ++i;
            }           
        }
        
        RMSE = Math.sqrt(RMSE/i);
        validationMetrics.setRMSE(RMSE);
        
        return validationMetrics;
    }
}
