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
package com.datumbox.framework.machinelearning.classification;

import com.datumbox.framework.machinelearning.common.bases.basemodels.BaseNaiveBayes;
import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.MapType;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.StorageHint;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * The BernoulliNaiveBayes class implements the Bernoulli Naive Bayes model. 
 * 
 * References: 
 * http://blog.datumbox.com/machine-learning-tutorial-the-naive-bayes-text-classifier/
 * http://nlp.stanford.edu/IR-book/html/htmledition/the-bernoulli-model-1.html
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class BernoulliNaiveBayes extends BaseNaiveBayes<BernoulliNaiveBayes.ModelParameters, BernoulliNaiveBayes.TrainingParameters, BernoulliNaiveBayes.ValidationMetrics> {
    
    /** {@inheritDoc} */
    public static class ModelParameters extends BaseNaiveBayes.ModelParameters {
        private static final long serialVersionUID = 1L;
        
        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY)
        private Map<Object, Double> sumOfLog1minusProb; //the Sum Of Log(1-prob) for each class. This is used to optimize the speed of validation. Instead of looping through all the keywords by having this Sum we are able to loop only through the features of the observation
        
        /** 
         * @param dbc
         * @see com.datumbox.framework.machinelearning.common.bases.baseobjects.BaseModelParameters#BaseModelParameters(com.datumbox.common.persistentstorage.interfaces.DatabaseConnector) 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
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
    public static class TrainingParameters extends BaseNaiveBayes.TrainingParameters {   
        private static final long serialVersionUID = 1L;

    } 
    
    /** {@inheritDoc} */
    public static class ValidationMetrics extends BaseNaiveBayes.ValidationMetrics {
        private static final long serialVersionUID = 1L;

    }

    /**
     * Public constructor of the algorithm.
     * 
     * @param dbName
     * @param dbConf 
     */
    public BernoulliNaiveBayes(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, BernoulliNaiveBayes.ModelParameters.class, BernoulliNaiveBayes.TrainingParameters.class, BernoulliNaiveBayes.ValidationMetrics.class);
        isBinarized = true;
    }
    
    @Override
    protected void predictDataset(Dataframe newData) { 
        if(newData.isEmpty()) {
            return;
        }
        
        ModelParameters modelParameters = kb().getModelParameters();
        
        Map<List<Object>, Double> logLikelihoods = modelParameters.getLogLikelihoods();
        Map<Object, Double> logPriors = modelParameters.getLogPriors();
        Set<Object> classesSet = modelParameters.getClasses();
        Map<Object, Double> sumOfLog1minusProb = modelParameters.getSumOfLog1minusProb();
        
        Object someClass = classesSet.iterator().next();
        
        
        Map<Object, Double> cachedLogPriors = new HashMap<>(logPriors); //this is small. Size equal to class numbers. We cache it because we don't want to load it again and again from the DB
        
        for(Map.Entry<Integer, Record> e : newData.entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            //Build new map here! clear the prediction scores with the scores of the classes
            AssociativeArray predictionScores = new AssociativeArray(new HashMap<>(cachedLogPriors)); 
            
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
                //classLogScoresForThisFeature=null;
            }
            
            Object theClass=getSelectedClassFromClassScores(predictionScores);
            
            Descriptives.normalizeExp(predictionScores);
            
            newData._unsafe_set(rId, new Record(r.getX(), r.getY(), theClass, predictionScores));
        }
        
    }
    
    @Override
    protected void _fit(Dataframe trainingData) {
        ModelParameters modelParameters = kb().getModelParameters();
        int n = modelParameters.getN();
        int d = modelParameters.getD();
        
        kb().getTrainingParameters().setMultiProbabilityWeighted(false);
        
        
        Map<List<Object>, Double> likelihoods = modelParameters.getLogLikelihoods();
        Map<Object, Double> logPriors = modelParameters.getLogPriors();
        Set<Object> classesSet = modelParameters.getClasses();
        Map<Object, Double> sumOfLog1minusProb = modelParameters.getSumOfLog1minusProb();
        
        
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
                
                sumOfLog1minusProb.put(theClass, 0.0);
            }
        }
        
        //now calculate the statistics of features
        for(Record r : trainingData) {
            //store the occurrances of the features
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                Double occurrences=TypeInference.toDouble(entry.getValue());
                
                if(occurrences==0.0) {
                    continue;
                }
                else {
                    occurrences=1.0; //clip occurrences to 1
                }
                
                //loop through all the classes to ensure that the feature-class combination is initialized for ALL the classes
                //in a previous implementation I did not loop through all the classes and used only the one of the record.
                //THIS IS WRONG. By not assigning 0 scores to the rest of the classes for this feature, we don't penalties for the non occurrance. 
                //The math REQUIRE us to have scores for all classes to make the probabilities comparable.
                for(Object theClass : classesSet) {
                    List<Object> featureClassTuple = Arrays.<Object>asList(feature, theClass);
                    Double previousValue = likelihoods.get(featureClassTuple);
                    if(previousValue==null) {
                        previousValue=0.0;
                        likelihoods.put(featureClassTuple, 0.0);
                    }
                    
                    //find the class of this particular example
                    if(theClass.equals(r.getY())) {
                        //update the statistics of the feature
                        likelihoods.put(featureClassTuple, previousValue+occurrences);
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
        for(Map.Entry<List<Object>, Double> featureClassCounts : likelihoods.entrySet()) {
            List<Object> tp = featureClassCounts.getKey();
            //Object feature = tp.get(0);
            Object theClass = tp.get(1);
            Double occurrences = featureClassCounts.getValue();
            if(occurrences==null) {
                occurrences=0.0;
            }

            //We perform laplace smoothing (also known as add-1)
            Double smoothedProbability = (occurrences+1.0)/(totalFeatureOccurrencesForEachClass.getDouble(theClass)+d); // the d is also known in NLP problems as the Vocabulary size. 
            
            likelihoods.put(featureClassCounts.getKey(), smoothedProbability);
            
            //WARNING! We store real probabilities NOT logProbs here. This is because we will need to estimate the log(1-prob) during validation
            sumOfLog1minusProb.put(theClass, sumOfLog1minusProb.get(theClass) + Math.log( 1.0-smoothedProbability )); 
        }
        
        //totalFeatureOccurrencesForEachClass=null;
    }
}
