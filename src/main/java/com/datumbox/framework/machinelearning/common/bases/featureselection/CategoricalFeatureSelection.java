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

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureMarker;
import com.datumbox.configuration.GeneralConfiguration;
import com.datumbox.configuration.MemoryConfiguration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.mongodb.morphia.annotations.Transient;

/**
 * Abstract class which is the base of every Categorical Feature Selection algorithm.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class CategoricalFeatureSelection<MP extends CategoricalFeatureSelection.ModelParameters, TP extends CategoricalFeatureSelection.TrainingParameters> extends FeatureSelection<MP, TP> {
    
    
    public static abstract class ModelParameters extends FeatureSelection.ModelParameters {

        private int N;
        
        @BigDataStructureMarker
        @Transient
        private Map<List<Object>, Integer> featureClassCounts; //set which stores the counts of feature-class combinations.
        
        
        @BigDataStructureMarker
        @Transient
        private Map<Object, Integer> classCounts; //map which stores the counts of the classes
        
        
        @BigDataStructureMarker
        @Transient
        private Map<Object, Double> featureCounts; //map which stores the counts of the features

        @BigDataStructureMarker
        @Transient
        private Map<Object, Double> featureScores; //map which stores the scores of the features
        
        //Getters and Setters
        
        public int getN() {
            return N;
        }

        public void setN(int N) {
            this.N = N;
        }

        public Map<List<Object>, Integer> getFeatureClassCounts() {
            return featureClassCounts;
        }

        public void setFeatureClassCounts(Map<List<Object>, Integer> featureClassCounts) {
            this.featureClassCounts = featureClassCounts;
        }

        public Map<Object, Integer> getClassCounts() {
            return classCounts;
        }

        public void setClassCounts(Map<Object, Integer> classCounts) {
            this.classCounts = classCounts;
        }

        public Map<Object, Double> getFeatureCounts() {
            return featureCounts;
        }

        public void setFeatureCounts(Map<Object, Double> featureCounts) {
            this.featureCounts = featureCounts;
        }

        public Map<Object, Double> getFeatureScores() {
            return featureScores;
        }

        public void setFeatureScores(Map<Object, Double> featureScores) {
            this.featureScores = featureScores;
        }
        
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            super.bigDataStructureInitializer(bdsf, memoryConfiguration);
            
            BigDataStructureFactory.MapType mapType = memoryConfiguration.getMapType();
            int LRUsize = memoryConfiguration.getLRUsize();
            
            //String tmpPrefix=StorageConfiguration.getTmpPrefix(); //ensure that these are temporary and that will be dropped automatically
            classCounts = bdsf.getMap("classCounts", mapType, LRUsize);
            featureClassCounts = bdsf.getMap("featureClassCounts", mapType, LRUsize);
            featureCounts = bdsf.getMap("featureCounts", mapType, LRUsize);
            featureScores = bdsf.getMap("featureScores", mapType, LRUsize);
        }
        
        @Override
        public void bigDataStructureCleaner(BigDataStructureFactory bdsf) {
            super.bigDataStructureCleaner(bdsf);
            
            //drop the unnecessary stastistics tables
            bdsf.dropTable("classCounts", classCounts);
            bdsf.dropTable("featureClassCounts", featureClassCounts);
            bdsf.dropTable("featureCounts", featureCounts);
        }
    }
    
    
    public static abstract class TrainingParameters extends FeatureSelection.TrainingParameters {
        
        private Integer rareFeatureThreshold = null;
        private Integer maxFeatures=null;
        private boolean ignoringNumericalFeatures = true;

        public Integer getRareFeatureThreshold() {
            return rareFeatureThreshold;
        }

        public void setRareFeatureThreshold(Integer rareFeatureThreshold) {
            this.rareFeatureThreshold = rareFeatureThreshold;
        }

        public Integer getMaxFeatures() {
            return maxFeatures;
        }

        public void setMaxFeatures(Integer maxFeatures) {
            this.maxFeatures = maxFeatures;
        }

        public boolean isIgnoringNumericalFeatures() {
            return ignoringNumericalFeatures;
        }

        public void setIgnoringNumericalFeatures(boolean ignoringNumericalFeatures) {
            this.ignoringNumericalFeatures = ignoringNumericalFeatures;
        }
        
    }
    

    protected CategoricalFeatureSelection(String dbName, Class<MP> mpClass, Class<TP> tpClass) {
        super(dbName, mpClass, tpClass);
    }
    
    
    @Override
    protected void estimateModelParameters(Dataset data) {
        
        if(GeneralConfiguration.DEBUG) {
            System.out.println("estimateModelParameters()");
        }
        
        //set the number of observations
        MP modelParameters = knowledgeBase.getModelParameters();
        
        modelParameters.setN(data.size());
        
        
        //build the maps with teh feature statistics and counts
        buildFeatureStatistics(data);
        
        
        
        
        //call the overriden method to get the scores of the features.
        //WARNING: do not use feature scores for any weighting. Sometimes the features are selected based on a minimum and others on a maximum criterion.
        estimateFeatureScores();
    }
    
    @Override
    protected void filterFeatures(Dataset newdata) {
        MP modelParameters = knowledgeBase.getModelParameters();
        
        //now filter the data by removing all the features that are not selected
        filterData(newdata, modelParameters.getFeatureScores(), knowledgeBase.getTrainingParameters().isIgnoringNumericalFeatures());
    }
    
    private static void filterData(Dataset data, Map<Object, Double> featureScores, boolean ignoringNumericalFeatures) {
        for(Record r : data) {
            Iterator<Map.Entry<Object, Object>> it = r.getX().entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<Object, Object> entry = it.next();
                Object feature = entry.getKey();
                if(ignoringNumericalFeatures) { //if we ignore the numerical features, investigate further if we must skip the feature
                    if(data.getColumns().get(feature)==Dataset.ColumnType.NUMERICAL) { //is it numerical? 
                        continue; //skip any further analysis
                    }
                }
                
                Double value = Dataset.toDouble(entry.getValue());
                
                
                if(!featureScores.containsKey(feature)) { //unselected feature
                    //remove it both from the columns and from the record
                    data.getColumns().remove(feature);
                    it.remove();
                }
                else if(value==null || value==0.0) { //inactive feature
                    //remove it only from this record
                    it.remove();
                }
            }
        }
        
    }
    
    public static void removeRareFeatures(Dataset data, Integer rareFeatureThreshold, Map<Object, Double> featureCounts, boolean ignoringNumericalFeatures) {
        //This method contains part of the statistics collection of the object
        //but can also be called statically in order to aggressively remove rare
        //features especially in NLP applications. It was developed in such a way
        //so that duplicate code is reduced. To do so, "call by reference" is 
        //required. In Java what we can do is initialize an empty featureCounts
        //map externally and pass it here to be constructred. When called by the
        //object itself this map is part of the statistics that it collects during
        //feature selection. If called statically, the map should be instatiated
        //just before the call to this method and dropped immediately after 
        //since it has no use.
        
        if(!featureCounts.isEmpty()) {
            throw new RuntimeException("The featureCounts map should be empty.");
        }
        
        //find the featureCounts
        for(Record r : data) {
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                
                if(ignoringNumericalFeatures) { //if we ignore the numerical features, investigate further if we must skip the feature
                    if(data.getColumns().get(feature)==Dataset.ColumnType.NUMERICAL) { //is it numerical? 
                        continue; //skip any further analysis
                    }
                }
                
                Double value = Dataset.toDouble(entry.getValue());
                if(value==null || value==0.0) {
                    continue;
                }


                
                //feature counts
                Double featureCounter = featureCounts.get(feature);
                if(featureCounter==null) {
                    featureCounter=0.0;
                }
                featureCounts.put(feature, ++featureCounter);
                
            }
        }

        //remove rare features
        if(rareFeatureThreshold != null && rareFeatureThreshold>0) {
            boolean mongoDBhackRequired = featureCounts.getClass().getName().contains("mongo"); //the MongoDB does not support iterator remove. We use this nasty hack to detect it and use remove instead
            
            //remove features from the featureCounts list
            Iterator<Map.Entry<Object, Double>> it = featureCounts.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<Object, Double> entry = it.next();
                if(entry.getValue()<=rareFeatureThreshold) {
                    if(!mongoDBhackRequired) {
                        it.remove(); 
                    }
                    else {
                        featureCounts.remove(entry.getKey()); //hack for mongo
                    }
                }
            }
            
            //then remove the features in dataset that does not appear in the list
            filterData(data, featureCounts, ignoringNumericalFeatures);
        }
    }
    
    private void buildFeatureStatistics(Dataset data) {        
        TP trainingParameters = knowledgeBase.getTrainingParameters();
        Integer rareFeatureThreshold = trainingParameters.getRareFeatureThreshold();
        boolean ignoringNumericalFeatures = trainingParameters.isIgnoringNumericalFeatures();
        
        MP modelParameters = knowledgeBase.getModelParameters();
        
        Map<Object, Double> featureCounts = modelParameters.getFeatureCounts();
        Map<Object, Integer> classCounts = modelParameters.getClassCounts();
        Map<List<Object>,Integer> featureClassCounts = modelParameters.getFeatureClassCounts();
        
        //the method below does not only removes the rare features but also
        //first and formost calculates the contents of featureCounts map. 
        //The map must be empty or else you get a RuntimeException
        removeRareFeatures(data, rareFeatureThreshold, featureCounts, ignoringNumericalFeatures);
        
        //now find the classCounts and the featureClassCounts
        for(Record r : data) {
            Object theClass = r.getY();

            //class counts
            Integer classCounter = classCounts.get(theClass);
            if(classCounter==null) {
                classCounter=0;
            }
            classCounts.put(theClass, ++classCounter);


            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                
                if(ignoringNumericalFeatures) { //if we ignore the numerical features, investigate further if we must skip the feature
                    if(data.getColumns().get(feature)==Dataset.ColumnType.NUMERICAL) { //is it numerical? 
                        continue; //skip any further analysis
                    }
                }
                
                Double value = Dataset.toDouble(entry.getValue());
                if(value==null || value==0.0) {
                    continue;
                }



                //featureClass counts
                List<Object> featureClassTuple = Arrays.<Object>asList(feature, theClass);
                Integer featureClassCounter = featureClassCounts.get(featureClassTuple);
                if(featureClassCounter==null) {
                    featureClassCounter=0;
                }
                featureClassCounts.put(featureClassTuple, ++featureClassCounter);
            }


        }
        
    }
    
    protected abstract void estimateFeatureScores();
}
