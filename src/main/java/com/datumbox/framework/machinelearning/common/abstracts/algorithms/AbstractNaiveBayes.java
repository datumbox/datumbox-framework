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
package com.datumbox.framework.machinelearning.common.abstracts.algorithms;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.framework.machinelearning.common.abstracts.modelers.AbstractClassifier;
import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.MapType;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.StorageHint;
import com.datumbox.framework.machinelearning.common.interfaces.PredictParallelizable;
import com.datumbox.framework.machinelearning.common.validators.ClassifierValidator;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Base class for Naive Bayes Models.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public abstract class AbstractNaiveBayes<MP extends AbstractNaiveBayes.AbstractModelParameters, TP extends AbstractNaiveBayes.AbstractTrainingParameters, VM extends AbstractNaiveBayes.AbstractValidationMetrics> extends AbstractClassifier<MP, TP, VM> implements PredictParallelizable {
    /**
     * Flag that indicates whether the algorithm binarizes the provided activated 
     * features.
     */
    private final boolean isBinarized;
    
    /** {@inheritDoc} */
    public static abstract class AbstractModelParameters extends AbstractClassifier.AbstractModelParameters {

        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=false)
        private Map<Object, Double> logPriors; //prior log probabilities of the classes

        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=false)
        private Map<List<Object>, Double> logLikelihoods; //posterior log probabilities of features-classes combination
        
        /** 
         * @param dbc
         * @see com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer.AbstractModelParameters#AbstractModelParameters(com.datumbox.common.persistentstorage.interfaces.DatabaseConnector) 
         */
        protected AbstractModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }

        /**
         * Getter for the log of priors of the classes.
         * 
         * @return 
         */
        public Map<Object, Double> getLogPriors() {
            return logPriors;
        }
        
        /**
         * Setter for the log of priors of the classes.
         * 
         * @param logPriors 
         */
        protected void setLogPriors(Map<Object, Double> logPriors) {
            this.logPriors = logPriors;
        }
        
        /**
         * Getter for the log likelihood of P(x|c).
         * 
         * @return 
         */
        public Map<List<Object>, Double> getLogLikelihoods() {
            return logLikelihoods;
        }
        
        /**
         * Setter for the log likelihood of P(x|c).
         * 
         * @param logLikelihoods 
         */
        protected void setLogLikelihoods(Map<List<Object>, Double> logLikelihoods) {
            this.logLikelihoods = logLikelihoods;
        }
    } 

    /** {@inheritDoc} */
    public static abstract class AbstractTrainingParameters extends AbstractClassifier.AbstractTrainingParameters {         
        private boolean multiProbabilityWeighted=false; //whether the classifier weights the probabilities based on the number of occurences. (multiple occurences are taken into account when we estimate the classification scores) 
        
        /**
         * Getter for whether the algorithm weights the probabilities based on the
         * multiple occurrences of a feature.
         * 
         * @return 
         */
        public boolean isMultiProbabilityWeighted() {
            return multiProbabilityWeighted;
        }
        
        /**
         * Setter for whether the algorithm weights the probabilities based on the
         * multiple occurrences of a feature.
         * 
         * @param multiProbabilityWeighted 
         */
        public void setMultiProbabilityWeighted(boolean multiProbabilityWeighted) {
            this.multiProbabilityWeighted = multiProbabilityWeighted;
        }
    } 

    /** 
     * @param dbName
     * @param dbConf
     * @param mpClass
     * @param tpClass
     * @param vmClass
     * @param isBinarized
     * @see com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer#AbstractTrainer(java.lang.String, com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration, java.lang.Class, java.lang.Class...)  
     */
    protected AbstractNaiveBayes(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass, boolean isBinarized) {
        super(dbName, dbConf, mpClass, tpClass, vmClass, new ClassifierValidator<>());
        this.isBinarized = isBinarized;
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
    protected void _predictDataset(Dataframe newData) {
        _predictDatasetParallel(newData);
    }
    
    /** {@inheritDoc} */
    @Override
    public Prediction _predictRecord(Record r) {
        AbstractModelParameters modelParameters = kb().getModelParameters();
        Map<List<Object>, Double> logLikelihoods = modelParameters.getLogLikelihoods();
        Map<Object, Double> logPriors = modelParameters.getLogPriors();
        Set<Object> classesSet = modelParameters.getClasses();
        
        Object someClass = classesSet.iterator().next();
        
        //initialize scores with the scores of the priors
        AssociativeArray predictionScores = new AssociativeArray(new HashMap<>(logPriors)); 

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


            Double occurrences=TypeInference.toDouble(entry.getValue());
            if((!kb().getTrainingParameters().isMultiProbabilityWeighted() || isBinarized) && occurrences>0) {
                occurrences=1.0;
            }

            for(Map.Entry<Object, Object> entry2 : classLogScoresForThisFeature.entrySet()) {
                Object theClass = entry2.getKey();
                Double logScore = TypeInference.toDouble(entry2.getValue());
                Double previousValue = predictionScores.getDouble(theClass);
                predictionScores.put(theClass, previousValue+occurrences*logScore);
            }
            //classLogScoresForThisFeature=null;
        }

        Object predictedClass=getSelectedClassFromClassScores(predictionScores);
        Descriptives.normalizeExp(predictionScores);    
        
        return new Prediction(predictedClass, predictionScores);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        AbstractModelParameters modelParameters = kb().getModelParameters();
        int n = modelParameters.getN();
        int d = modelParameters.getD();
        
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
                Double occurrences=TypeInference.toDouble(entry.getValue());
                
                if(isBinarized && occurrences>0) {
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
        
        //totalFeatureOccurrencesForEachClass=null;
    }
    
}