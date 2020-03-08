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
package com.datumbox.framework.core.machinelearning.classification;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.concurrency.StreamMethods;
import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.algorithms.AbstractNaiveBayes;
import com.datumbox.framework.core.machinelearning.common.interfaces.PredictParallelizable;
import com.datumbox.framework.core.statistics.descriptivestatistics.Descriptives;

import java.util.*;


/**
 * The BernoulliNaiveBayes class implements the Bernoulli Naive Bayes model. 
 * 
 * References: 
 * http://blog.datumbox.com/machine-learning-tutorial-the-naive-bayes-text-classifier/
 * http://nlp.stanford.edu/IR-book/html/htmledition/the-bernoulli-model-1.html
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class BernoulliNaiveBayes extends AbstractNaiveBayes<BernoulliNaiveBayes.ModelParameters, BernoulliNaiveBayes.TrainingParameters> {
    
    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractNaiveBayes.AbstractModelParameters {
        private static final long serialVersionUID = 1L;
        
        private Map<Object, Double> sumOfLog1minusProb = new HashMap<>(); //the Sum Of Log(1-prob) for each class. This is used to optimize the speed of validation. Instead of looping through all the keywords by having this Sum we are able to loop only through the features of the observation
        
        /** 
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected ModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
        }
        
        /**
         * Getter for the sumOfLog1minusProb coefficients.
         * 
         * @return 
         */
        public Map<Object, Double> getSumOfLog1minusProb() {
            return sumOfLog1minusProb;
        }
        
        /**
         * Setter for the sumOfLog1minusProb coefficients.
         * 
         * @param sumOfLog1minusProb 
         */
        protected void setSumOfLog1minusProb(Map<Object, Double> sumOfLog1minusProb) {
            this.sumOfLog1minusProb = sumOfLog1minusProb;
        }
    } 
    
    /** {@inheritDoc} */
    public static class TrainingParameters extends AbstractNaiveBayes.AbstractTrainingParameters {   
        private static final long serialVersionUID = 1L;

    }

    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainer.AbstractTrainingParameters, Configuration)
     */
    protected BernoulliNaiveBayes(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected BernoulliNaiveBayes(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }

    /** {@inheritDoc} */
    protected boolean isBinarized() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public PredictParallelizable.Prediction _predictRecord(Record r) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        Map<List<Object>, Double> logLikelihoods = modelParameters.getLogLikelihoods();
        Map<Object, Double> logPriors = modelParameters.getLogPriors();
        Set<Object> classesSet = modelParameters.getClasses();
        Map<Object, Double> sumOfLog1minusProb = modelParameters.getSumOfLog1minusProb();
        
        Object someClass = classesSet.iterator().next();
        
        
        //Build new map here! clear the prediction scores with the scores of the classes
        AssociativeArray predictionScores = new AssociativeArray(new HashMap<>(logPriors));

        //in order to avoid looping throug all available features for each record, we have already calculated the Sum of log(1-prob). So we know the score of a record that has no feature activated. We add this score on the initial score below:
        for(Map.Entry<Object, Double> entry : sumOfLog1minusProb.entrySet()) {
            Object theClass = entry.getKey();
            Double value = entry.getValue();
            Double previousValue = predictionScores.getDouble(theClass);
            predictionScores.put(theClass, previousValue+value);
        }


        //Then we loop through all the active features of the record, we add the log(prob) and we subtract the log(1-prob)
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


            Double occurrences= TypeInference.toDouble(entry.getValue());
            if(occurrences==null || occurrences==0.0) { 
                continue;
            }
            //no need to specifically binarize the occurrences. we will not multiply the score by it

            for(Map.Entry<Object, Object> entry2 : classLogScoresForThisFeature.entrySet()) {
                Object theClass = entry2.getKey();
                Double probability = TypeInference.toDouble(entry2.getValue());
                Double previousValue = predictionScores.getDouble(theClass);
                predictionScores.put(theClass, previousValue + Math.log(probability)-Math.log(1.0-probability));
            }
        }

        Object predictedClass=getSelectedClassFromClassScores(predictionScores);

        Descriptives.normalizeExp(predictionScores);
        
        return new PredictParallelizable.Prediction(predictedClass, predictionScores);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        int n = trainingData.size();
        int d = trainingData.xColumnSize();
        
        knowledgeBase.getTrainingParameters().setMultiProbabilityWeighted(false);
        
        
        Map<List<Object>, Double> likelihoods = modelParameters.getLogLikelihoods();
        Map<Object, Double> logPriors = modelParameters.getLogPriors();
        Set<Object> classesSet = modelParameters.getClasses();
        Map<Object, Double> sumOfLog1minusProb = modelParameters.getSumOfLog1minusProb();
        
        
        //calculate first statistics about the classes
        Map<Object, Integer> totalFeatureOccurrencesForEachClass = new HashMap<>();
        for(Record r : trainingData) {
            Object theClass=r.getY();
            
            if(classesSet.add(theClass)) { //is it new class? add it
                logPriors.put(theClass, 1.0);  
                totalFeatureOccurrencesForEachClass.put(theClass, 0);
                sumOfLog1minusProb.put(theClass, 0.0);
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
                likelihoods.put(featureClassTuple, 0.0); //the key unique across threads and the map is concurrent
            }
        });
        
        
        //now calculate the statistics of features
        streamExecutor.forEach(StreamMethods.stream(trainingData.stream(), isParallelized()), r -> {
            Object theClass = r.getY();
            
            //store the occurrances of the features
            int sumOfOccurrences = 0;
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                Double occurrences=TypeInference.toDouble(entry.getValue());
                
                if(occurrences!= null && occurrences>0.0) {
                    //The below block of code clips occurrences to 1
                    List<Object> featureClassTuple = Arrays.asList(feature, theClass);
                    likelihoods.put(featureClassTuple, likelihoods.get(featureClassTuple)+1.0); //each thread updates a unique key and the map is cuncurrent
                    
                    sumOfOccurrences++;
                }
            }
            
            synchronized(totalFeatureOccurrencesForEachClass) {
                totalFeatureOccurrencesForEachClass.put(theClass, totalFeatureOccurrencesForEachClass.get(theClass)+sumOfOccurrences);
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
        for(Object theClass : classesSet) {
            double sumLog1minusP = streamExecutor.sum(StreamMethods.stream(trainingData.getXDataTypes().keySet().stream(), isParallelized()).mapToDouble(feature -> {
                List<Object> featureClassTuple = Arrays.asList(feature, theClass);
                Double occurrences = likelihoods.get(featureClassTuple);

                //We perform laplace smoothing (also known as add-1)
                Double smoothedProbability = (occurrences+1.0)/(totalFeatureOccurrencesForEachClass.get(theClass)+d); // the d is also known in NLP problems as the Vocabulary size. 

                likelihoods.put(featureClassTuple, smoothedProbability);

                double log1minusP = Math.log( 1.0-smoothedProbability );
                
                return log1minusP;
            }));
            
            sumOfLog1minusProb.put(theClass, sumOfLog1minusProb.get(theClass) + sumLog1minusP); 
        }
        
        //Drop the temporary Collection
    }
}
