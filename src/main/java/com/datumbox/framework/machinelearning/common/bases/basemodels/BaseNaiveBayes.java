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
package com.datumbox.framework.machinelearning.common.bases.basemodels;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclassifier;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureMarker;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mongodb.morphia.annotations.Transient;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public abstract class BaseNaiveBayes<MP extends BaseNaiveBayes.ModelParameters, TP extends BaseNaiveBayes.TrainingParameters, VM extends BaseNaiveBayes.ValidationMetrics> extends BaseMLclassifier<MP, TP, VM> {
    
    
    public static abstract class ModelParameters extends BaseMLclassifier.ModelParameters {
        /**
         * log priors for log( P(c) )
         */
        @BigDataStructureMarker
        @Transient
        private Map<Object, Double> logPriors; //prior log probabilities of the classes

        /**
         * log likelihood for log( P(x|c) ) 
         */
        @BigDataStructureMarker
        @Transient
        private Map<List<Object>, Double> logLikelihoods; //posterior log probabilities of features-classes combination

        
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            super.bigDataStructureInitializer(bdsf, memoryConfiguration);
            
            BigDataStructureFactory.MapType mapType = memoryConfiguration.getMapType();
            int LRUsize = memoryConfiguration.getLRUsize();
            
            logPriors = bdsf.getMap("logPriors", mapType, LRUsize);
            logLikelihoods = bdsf.getMap("logLikelihoods", mapType, LRUsize);
            
        }
        
        public Map<Object, Double> getLogPriors() {
            return logPriors;
        }

        public void setLogPriors(Map<Object, Double> logPriors) {
            this.logPriors = logPriors;
        }

        public Map<List<Object>, Double> getLogLikelihoods() {
            return logLikelihoods;
        }

        public void setLogLikelihoods(Map<List<Object>, Double> logLikelihoods) {
            this.logLikelihoods = logLikelihoods;
        }
    } 

    
    public static abstract class TrainingParameters extends BaseMLclassifier.TrainingParameters {         
        private boolean multiProbabilityWeighted=false; //whether the classifier weights the probabilities based on the number of occurences. (multiple occurences are taken into account when we estimate the classification scores) 
                
        public boolean isMultiProbabilityWeighted() {
            return multiProbabilityWeighted;
        }

        public void setMultiProbabilityWeighted(boolean multiProbabilityWeighted) {
            this.multiProbabilityWeighted = multiProbabilityWeighted;
        }
    } 
    
    
    public static abstract class ValidationMetrics extends BaseMLclassifier.ValidationMetrics {

    }
        
        
    protected static final boolean IS_BINARIZED = false;

    
    protected BaseNaiveBayes(String dbName, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass) {
        super(dbName, mpClass, tpClass, vmClass);
    } 
    
    @Override
    protected void predictDataset(Dataset newData) { 
        if(newData.isEmpty()) {
            return;
        }
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        Map<List<Object>, Double> logLikelihoods = modelParameters.getLogLikelihoods();
        Map<Object, Double> logPriors = modelParameters.getLogPriors();
        Set<Object> classesSet = modelParameters.getClasses();
        
        Object someClass = classesSet.iterator().next();
        
        
        Map<Object, Object> cachedLogPriors = new HashMap<>(logPriors); //this is small. Size equal to class numbers. We cache it because we don't want to load it again and again from the DB
        
        for(Record r : newData) {
            //Build new map here! reinitialize the prediction scores with the scores of the classes
            AssociativeArray predictionScores = new AssociativeArray(new HashMap<>(cachedLogPriors)); 
            
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                
                
                //EVERY feature within our dictionary has a value for EVERY class
                //So if the feature has no value for one random class (someClass has 
                //no particular significance), then it will not have for any class
                //and thus the feature is not in the dictionary and can be ignored.
                if(!logLikelihoods.containsKey(Arrays.<Object>asList(feature, someClass))) {
                    continue;
                }
                
                //extract the feature scores for each class for the particular feature
                AssociativeArray classLogScoresForThisFeature = new AssociativeArray();
                
                for(Object theClass : classesSet) {
                    Double logScore = logLikelihoods.get(Arrays.<Object>asList(feature, theClass));
                    classLogScoresForThisFeature.put(theClass, logScore);
                }
                
                
                Double occurrences=Dataset.toDouble(entry.getValue());
                if((!knowledgeBase.getTrainingParameters().isMultiProbabilityWeighted() || IS_BINARIZED) && occurrences>0) {
                    occurrences=1.0;
                }
                
                for(Map.Entry<Object, Object> entry2 : classLogScoresForThisFeature.entrySet()) {
                    Object theClass = entry2.getKey();
                    Double logScore = Dataset.toDouble(entry2.getValue());
                    Double previousValue = predictionScores.getDouble(theClass);
                    predictionScores.put(theClass, previousValue+occurrences*logScore);
                }
                classLogScoresForThisFeature=null;
            }
            
            Object theClass=getSelectedClassFromClassScores(predictionScores);
            Descriptives.normalizeExp(predictionScores);
            
            r.setYPredicted(theClass);
            r.setYPredictedProbabilities(predictionScores);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected void estimateModelParameters(Dataset trainingData) {
        int n = trainingData.size();
        int d = trainingData.getColumnSize();
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        //initialization
        modelParameters.setN(n);
        modelParameters.setD(d);
        
        
        Map<List<Object>, Double> logLikelihoods = modelParameters.getLogLikelihoods();
        Map<Object, Double> logPriors = modelParameters.getLogPriors();
        Set<Object> classesSet = modelParameters.getClasses();
        
        //calculate first statistics about the classes
        AssociativeArray totalFeatureOccurrencesForEachClass = new AssociativeArray();
        for(Record r : trainingData) {
            Object theClass=r.getY();
            
            Double classCount = logPriors.get(theClass);
            if(classCount!=null) { //already exists? increase counter
                logPriors.put(theClass,classCount+1.0);  
            }
            else { //is it new class? add it
                classesSet.add(theClass);
                logPriors.put(theClass, 1.0);  
                totalFeatureOccurrencesForEachClass.put(theClass, 0.0);
            }
        }
        
        
        //now calculate the statistics of features
        for(Record r : trainingData) {
            
            //store the occurrances of the features
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                Double occurrences=Dataset.toDouble(entry.getValue());
                
                if(IS_BINARIZED && occurrences>0) {
                    occurrences=1.0;
                }
                
                //loop through all the classes to ensure that the feature-class combination is initialized for ALL the classes
                //in a previous implementation I did not loop through all the classes and used only the one of the record.
                //THIS IS WRONG. By not assigning 0 scores to the rest of the classes for this feature, we don't penalties for the non occurrance. 
                //The math REQUIRE us to have scores for all classes to make the probabilities comparable.
                for(Object theClass : classesSet) {
                    List<Object> featureClassTuple = Arrays.<Object>asList(feature, theClass);
                    Double previousValue = logLikelihoods.get(featureClassTuple);
                    if(previousValue==null) {
                        previousValue=0.0;
                        logLikelihoods.put(featureClassTuple, 0.0);
                    }
                    
                    //find the class of this particular example
                    if(theClass.equals(r.getY())) {
                        //update the statistics of the feature
                        logLikelihoods.put(featureClassTuple, previousValue+occurrences);
                        totalFeatureOccurrencesForEachClass.put(theClass,totalFeatureOccurrencesForEachClass.getDouble(theClass)+occurrences);
                    }
                }                
            }
            
        }
        
        int c = classesSet.size();
        modelParameters.setC(c);
        
        //calculate prior log probabilities
        for(Map.Entry<Object, Double> entry : logPriors.entrySet()) {
            Object theClass = entry.getKey();
            Double count = entry.getValue();
            
            //updated log priors
            logPriors.put(theClass, Math.log(count/n));
        }
        
        
        //update log likelihood
        for(Map.Entry<List<Object>, Double> featureClassCounts : logLikelihoods.entrySet()) {
            List<Object> tp = featureClassCounts.getKey();
            //Object feature = tp.get(0);
            Object theClass = tp.get(1);
            Double occurrences = featureClassCounts.getValue();
            if(occurrences==null) {
                occurrences=0.0;
            }

            //We perform laplace smoothing (also known as add-1)
            Double smoothedProbability = (occurrences+1.0)/(totalFeatureOccurrencesForEachClass.getDouble(theClass)+d); // the d is also known in NLP problems as the Vocabulary size. 
            
            logLikelihoods.put(featureClassCounts.getKey(), Math.log( smoothedProbability )); //calculate the logScore
        }
        
        totalFeatureOccurrencesForEachClass=null;
    }
    
}