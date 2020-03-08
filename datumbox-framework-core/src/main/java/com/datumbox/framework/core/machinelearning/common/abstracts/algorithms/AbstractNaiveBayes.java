/**
 * Copyright (C) 2013-2020 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
package com.datumbox.framework.core.machinelearning.common.abstracts.algorithms;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.concurrency.ForkJoinStream;
import com.datumbox.framework.common.concurrency.StreamMethods;
import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.common.storage.interfaces.BigMap;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.common.storage.interfaces.StorageEngine.MapType;
import com.datumbox.framework.common.storage.interfaces.StorageEngine.StorageHint;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelers.AbstractClassifier;
import com.datumbox.framework.core.machinelearning.common.interfaces.PredictParallelizable;
import com.datumbox.framework.core.machinelearning.common.interfaces.TrainParallelizable;
import com.datumbox.framework.core.statistics.descriptivestatistics.Descriptives;

import java.util.*;


/**
 * Base class for Naive Bayes Models.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class AbstractNaiveBayes<MP extends AbstractNaiveBayes.AbstractModelParameters, TP extends AbstractNaiveBayes.AbstractTrainingParameters> extends AbstractClassifier<MP, TP> implements PredictParallelizable, TrainParallelizable {

    /** {@inheritDoc} */
    public static abstract class AbstractModelParameters extends AbstractClassifier.AbstractModelParameters {

        private Map<Object, Double> logPriors = new HashMap<>(); //prior log probabilities of the classes

        @BigMap(keyClass=List.class, valueClass=Double.class, mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=true)
        private Map<List<Object>, Double> logLikelihoods; //posterior log probabilities of features-classes combination
        
        /** 
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected AbstractModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
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
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainer.AbstractTrainingParameters, Configuration)
     */
    protected AbstractNaiveBayes(TP trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
        streamExecutor = new ForkJoinStream(knowledgeBase.getConfiguration().getConcurrencyConfiguration());
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected AbstractNaiveBayes(String storageName, Configuration configuration) {
        super(storageName, configuration);
        streamExecutor = new ForkJoinStream(knowledgeBase.getConfiguration().getConcurrencyConfiguration());
    }

    /**
     * Returns a flag that indicates whether the algorithm binarizes the provided activated
     * features.
     *
     * @return
     */
    protected abstract boolean isBinarized();
    
    private boolean parallelized = true;
    
    /**
     * This executor is used for the parallel processing of streams with custom 
     * Thread pool.
     */
    protected final ForkJoinStream streamExecutor;
    
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
    protected void _predict(Dataframe newData) {
        _predictDatasetParallel(newData, knowledgeBase.getStorageEngine(), knowledgeBase.getConfiguration().getConcurrencyConfiguration());
    }
    
    /** {@inheritDoc} */
    @Override
    public Prediction _predictRecord(Record r) {
        AbstractModelParameters modelParameters = knowledgeBase.getModelParameters();
        Map<List<Object>, Double> logLikelihoods = modelParameters.getLogLikelihoods();
        Map<Object, Double> logPriors = modelParameters.getLogPriors();
        Set<Object> classesSet = modelParameters.getClasses();
        
        Object someClass = classesSet.iterator().next();
        boolean isBinarized = isBinarized();
        
        //initialize scores with the scores of the priors
        AssociativeArray predictionScores = new AssociativeArray(new HashMap<>(logPriors)); 

        for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
            Object feature = entry.getKey();

            //EVERY feature within our dictionary has a value for EVERY class
            //So if the feature has no value for one random class (someClass has 
            //no particular significance), then it will not have for any class
            //and thus the feature is not in the dictionary and can be ignored.
            if(!logLikelihoods.containsKey(Arrays.asList(feature, someClass))) {
                continue;
            }

            //extract the feature scores for each class for the particular feature
            AssociativeArray classLogScoresForThisFeature = new AssociativeArray();

            for(Object theClass : classesSet) {
                Double logScore = logLikelihoods.get(Arrays.asList(feature, theClass));
                classLogScoresForThisFeature.put(theClass, logScore);
            }


            Double occurrences=TypeInference.toDouble(entry.getValue());
            if((!knowledgeBase.getTrainingParameters().isMultiProbabilityWeighted() || isBinarized) && occurrences>0) {
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
        AbstractModelParameters modelParameters = knowledgeBase.getModelParameters();
        int n = trainingData.size();
        int d = trainingData.xColumnSize();
        
        Map<List<Object>, Double> logLikelihoods = modelParameters.getLogLikelihoods();
        Map<Object, Double> logPriors = modelParameters.getLogPriors();
        Set<Object> classesSet = modelParameters.getClasses();
        boolean isBinarized = isBinarized();
        
        //calculate first statistics about the classes
        Map<Object, Double> totalFeatureOccurrencesForEachClass = new HashMap<>();
        for(Record r : trainingData) { 
            Object theClass=r.getY();
            
            if(classesSet.add(theClass)) { //is it new class? add it
                logPriors.put(theClass, 1.0);  
                totalFeatureOccurrencesForEachClass.put(theClass, 0.0);
            }
            else { //already exists? increase counter
                logPriors.put(theClass,logPriors.get(theClass)+1.0);  
            }
        }
        
        //Loop through all the classes to ensure that the feature-class combination is initialized for ALL the classes
        //The math REQUIRE us to have scores for all classes to make the probabilities comparable.
        /*
            Implementation note:
            The code below uses the metadata from the Dataframe to avoid looping through all the data. 
            This means that if the metadata are stale (contain more columns than the actual data due to 
            updates/removes) we will initialize more parameters here. Nevertheless this should not have 
            any effects on the results of the algorithm since the scores will be the same in all classes
            and it will be taken care by the normalization.
        */
        streamExecutor.forEach(StreamMethods.stream(trainingData.getXDataTypes().keySet().stream(), isParallelized()), feature -> {
            for(Object theClass : classesSet) {
                List<Object> featureClassTuple = Arrays.asList(feature, theClass);
                logLikelihoods.put(featureClassTuple, 0.0); //the key is unique across threads and the map is concurrent
            }
        });
        
        
        //now calculate the statistics of features
        streamExecutor.forEach(StreamMethods.stream(trainingData.stream(), isParallelized()), r -> {
            Object theClass = r.getY();
            //store the occurrances of the features
            double sumOfOccurrences = 0.0;
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                Double occurrences=TypeInference.toDouble(entry.getValue());
                
                if(occurrences!= null && occurrences>0.0) {
                    if(isBinarized) {
                        occurrences=1.0;
                    }
                    
                    List<Object> featureClassTuple = Arrays.asList(feature, theClass);
                    logLikelihoods.put(featureClassTuple, logLikelihoods.get(featureClassTuple)+occurrences); //each thread updates a unique key and the map is cuncurrent
                    
                    sumOfOccurrences+=occurrences;
                }
            }
            synchronized(totalFeatureOccurrencesForEachClass) {
                totalFeatureOccurrencesForEachClass.put(theClass,totalFeatureOccurrencesForEachClass.get(theClass)+sumOfOccurrences);
            }
        });
        
        //calculate prior log probabilities
        for(Map.Entry<Object, Double> entry : logPriors.entrySet()) {
            Object theClass = entry.getKey();
            Double count = entry.getValue();
            
            //updated log priors
            logPriors.put(theClass, Math.log(count/n));
        }
        
        
        //update log likelihood
        streamExecutor.forEach(StreamMethods.stream(logLikelihoods.entrySet().stream(), isParallelized()), featureClassCounts -> {
            List<Object> featureClassTuple = featureClassCounts.getKey();
            Object theClass = featureClassTuple.get(1);
            Double occurrences = featureClassCounts.getValue();
            if(occurrences==null) {
                occurrences=0.0;
            }

            //We perform laplace smoothing (also known as add-1)
            Double smoothedProbability = (occurrences+1.0)/(totalFeatureOccurrencesForEachClass.get(theClass)+d); // the d is also known in NLP problems as the Vocabulary size. 
            
            logLikelihoods.put(featureClassTuple, Math.log( smoothedProbability )); //calculate the logScore
        }); 
    }
    
}