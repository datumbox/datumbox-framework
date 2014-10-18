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
package com.datumbox.framework.machinelearning.classification;

import com.datumbox.framework.machinelearning.common.bases.basemodels.BaseNaiveBayes;
import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureMarker;
import com.datumbox.configuration.MemoryConfiguration;
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
 */
public class BernoulliNaiveBayes extends BaseNaiveBayes<BernoulliNaiveBayes.ModelParameters, BernoulliNaiveBayes.TrainingParameters, BernoulliNaiveBayes.ValidationMetrics> {
    //References: http://nlp.stanford.edu/IR-book/html/htmledition/the-bernoulli-model-1.html
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    public static final String SHORT_METHOD_NAME = "BerNB";
    
    public static class ModelParameters extends BaseNaiveBayes.ModelParameters {
        @BigDataStructureMarker
        @Transient
        private Map<Object, Double> sumOfLog1minusProb; //the Sum Of Log(1-prob) for each class. This is used to optimize the speed of validation. Instead of looping through all the keywords by having this Sum we are able to loop only through the features of the observation
        
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            super.bigDataStructureInitializer(bdsf, memoryConfiguration);
            
            BigDataStructureFactory.MapType mapType = memoryConfiguration.getMapType();
            int LRUsize = memoryConfiguration.getLRUsize();
            
            sumOfLog1minusProb = bdsf.getMap("sumOfLog1minusProb", mapType, LRUsize);
        }
        
        public Map<Object, Double> getSumOfLog1minusProb() {
            return sumOfLog1minusProb;
        }

        public void setSumOfLog1minusProb(Map<Object, Double> sumOfLog1minusProb) {
            this.sumOfLog1minusProb = sumOfLog1minusProb;
        }
    } 
    
    
    public static class TrainingParameters extends BaseNaiveBayes.TrainingParameters {    

    } 

    
    public static class ValidationMetrics extends BaseNaiveBayes.ValidationMetrics {

    }

    protected static final boolean IS_BINARIZED = true;
    
    public BernoulliNaiveBayes(String dbName) {
        super(dbName, BernoulliNaiveBayes.ModelParameters.class, BernoulliNaiveBayes.TrainingParameters.class, BernoulliNaiveBayes.ValidationMetrics.class);
    }
    
    @Override
    public final String shortMethodName() {
        return SHORT_METHOD_NAME;
    }
    
    @Override
    public void train(Dataset trainingData, Dataset validationData) {
        knowledgeBase.getTrainingParameters().setMultiProbabilityWeighted(false);
        super.train(trainingData, validationData);
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
        Map<Object, Double> sumOfLog1minusProb = modelParameters.getSumOfLog1minusProb();
        
        Object someClass = classesSet.iterator().next();
        
        
        Map<Object, Double> cachedLogPriors = new HashMap<>(logPriors); //this is small. Size equal to class numbers. We cache it because we don't want to load it again and again from the DB
        
        for(Record r : newData) {
            //Build new map here! reinitialize the prediction scores with the scores of the classes
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
                
                
                Double occurrences=Dataset.toDouble(entry.getValue());
                if(occurrences==null || occurrences==0.0) { 
                    continue;
                }
                //no need to specifically binarize the occurrences. we will not multiply the score by it
                
                
                
                for(Map.Entry<Object, Object> entry2 : classLogScoresForThisFeature.entrySet()) {
                    Object theClass = entry2.getKey();
                    Double probability = Dataset.toDouble(entry2.getValue());
                    Double previousValue = predictionScores.getDouble(theClass);
                    predictionScores.put(theClass, previousValue + Math.log(probability)-Math.log(1.0-probability));
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
    protected void estimateModelParameters(Dataset trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        Map<List<Object>, Double> likelihoods = modelParameters.getLogLikelihoods();
        Map<Object, Double> logPriors = modelParameters.getLogPriors();
        Set<Object> classesSet = modelParameters.getClasses();
        Map<Object, Double> sumOfLog1minusProb = modelParameters.getSumOfLog1minusProb();
        
        int n = trainingData.size();
        int d = trainingData.getColumnSize();
        
        //initialization
        modelParameters.setN(n);
        modelParameters.setD(d);
        
        
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
                Double occurrences=Dataset.toDouble(entry.getValue());
                
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
        
        totalFeatureOccurrencesForEachClass=null;
    }
}
