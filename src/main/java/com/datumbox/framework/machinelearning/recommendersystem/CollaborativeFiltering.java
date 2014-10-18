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
package com.datumbox.framework.machinelearning.recommendersystem;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.dataobjects.TransposeDataList;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureMarker;
import com.datumbox.common.utilities.MapFunctions;
import com.datumbox.configuration.MemoryConfiguration;
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
import org.mongodb.morphia.annotations.Transient;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class CollaborativeFiltering extends BaseMLrecommender<CollaborativeFiltering.ModelParameters, CollaborativeFiltering.TrainingParameters> {

    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    public static final String SHORT_METHOD_NAME = "CoFlt";

   
    public static class ModelParameters extends BaseMLrecommender.ModelParameters {
        
        //number of observations used for training
        private Integer n =0 ;
        
        //number of features in data. IN DATA not in the algorithm.
        private Integer d =0 ;

        
        @BigDataStructureMarker
        @Transient
        private Map<List<Object>, Double> similarities; //the similarity map among observations

        
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            super.bigDataStructureInitializer(bdsf, memoryConfiguration);
            
            BigDataStructureFactory.MapType mapType = memoryConfiguration.getMapType();
            int LRUsize = memoryConfiguration.getLRUsize();
            
            similarities = bdsf.getMap("similarities", mapType, LRUsize);
        }
        
        
        //Getters / Setters
        
        public Integer getN() {
            return n;
        }

        public void setN(Integer n) {
            this.n = n;
        }

        public Integer getD() {
            return d;
        }

        public void setD(Integer d) {
            this.d = d;
        }

        public Map<List<Object>, Double> getSimilarities() {
            return similarities;
        }

        public void setSimilarities(Map<List<Object>, Double> similarities) {
            this.similarities = similarities;
        }

        
    }
    
    public static class TrainingParameters extends BaseMLrecommender.TrainingParameters {
 
        public enum SimilarityMeasure {
            EUCLIDIAN,
            MANHATTAN,
            PEARSONS_CORRELATION;
        }
        
        private SimilarityMeasure similarityMethod = SimilarityMeasure.EUCLIDIAN;

        public SimilarityMeasure getSimilarityMethod() {
            return similarityMethod;
        }

        public void setSimilarityMethod(SimilarityMeasure similarityMethod) {
            this.similarityMethod = similarityMethod;
        }

    }
    

    public CollaborativeFiltering(String dbName) {
        super(dbName, CollaborativeFiltering.ModelParameters.class, CollaborativeFiltering.TrainingParameters.class);
    } 

    @Override
    public final String shortMethodName() {
        return SHORT_METHOD_NAME;
    } 
    

    @Override
    protected void estimateModelParameters(Dataset trainingData) {
        int n = trainingData.size();
        int d = trainingData.getColumnSize();
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        //initialization
        modelParameters.setN(n);
        modelParameters.setD(d);
        
        //calculate similarity matrix
        Map<List<Object>, Double> similarities = modelParameters.getSimilarities();
        for(Record r1 : trainingData) {
            for(Record r2: trainingData) {
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
        for(Record r : newData) {
            Map<Object, Object> recommendations = new HashMap<>();
            
            Map<Object, Double> simSums = new HashMap<>();
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object row = entry.getKey();
                Double score = Dataset.toDouble(entry.getValue());
                
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
                    
                    Double previousRecValue = Dataset.toDouble(recommendations.get(column));
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
                Double score = Dataset.toDouble(entry.getValue());
                
                recommendations.put(column, score/simSums.get(column));
            }
            simSums = null;
            
            if(!recommendations.isEmpty()) {
                //sort recommendation by popularity
                recommendations = MapFunctions.sortNumberMapByValueDescending(recommendations);
                r.setY(recommendations.keySet().iterator().next());
                r.setYPredictedProbabilities(new AssociativeArray(recommendations));
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
                flatDataList1.add(Dataset.toDouble(r1.getX().get(column)));
                flatDataList2.add(Dataset.toDouble(r2.getX().get(column)));
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
