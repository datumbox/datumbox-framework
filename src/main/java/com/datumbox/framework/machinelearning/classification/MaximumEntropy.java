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

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclassifier;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.framework.machinelearning.common.validation.ClassifierValidation;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * The MaximumEntropy implements the Max Ent classifier which is an alternative
 * implementation of Multinomial Logistic Regression model.
 * 
 * References: 
 * http://blog.datumbox.com/machine-learning-tutorial-the-max-entropy-text-classifier/
 * http://www.cs.cmu.edu/afs/cs/user/aberger/www/html/tutorial/node3.html http://acl.ldc.upenn.edu/P/P02/P02-1002.pdf
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MaximumEntropy extends BaseMLclassifier<MaximumEntropy.ModelParameters, MaximumEntropy.TrainingParameters, MaximumEntropy.ValidationMetrics> {
    
    /**
     * The ModelParameters class stores the coefficients that were learned during
     * the training of the algorithm.
     */
    public static class ModelParameters extends BaseMLclassifier.ModelParameters {
        private static final long serialVersionUID = 1L;
        
        @BigMap
        private Map<List<Object>, Double> lambdas; //the lambda parameters of the model

        /**
         * Protected constructor which accepts as argument the DatabaseConnector.
         * 
         * @param dbc 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
        /**
         * Getter for the Lambda coefficients.
         * 
         * @return 
         */
        public Map<List<Object>, Double> getLambdas() {
            return lambdas;
        }
        
        /**
         * Setter for the Lambda coefficients.
         * 
         * @param lambdas 
         */
        protected void setLambdas(Map<List<Object>, Double> lambdas) {
            this.lambdas = lambdas;
        }
        
    } 
    
    /**
     * The TrainingParameters class stores the parameters that can be changed
     * before training the algorithm.
     */
    public static class TrainingParameters extends BaseMLclassifier.TrainingParameters { 
        private static final long serialVersionUID = 1L;
        
        private int totalIterations=100; 
        
        /**
         * Getter for the total iterations of the training process.
         * 
         * @return 
         */
        public int getTotalIterations() {
            return totalIterations;
        }
        
        /**
         * Setter for the total iterations of the training process.
         * 
         * @param totalIterations 
         */
        public void setTotalIterations(int totalIterations) {
            this.totalIterations = totalIterations;
        }

    } 
    
    /**
     * The ValidationMetrics class stores information about the performance of the
     * algorithm.
     */
    public static class ValidationMetrics extends BaseMLclassifier.ValidationMetrics {
        private static final long serialVersionUID = 1L;

    }
        
    /**
     * Public constructor of the algorithm.
     * 
     * @param dbName
     * @param dbConf 
     */
    public MaximumEntropy(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, MaximumEntropy.ModelParameters.class, MaximumEntropy.TrainingParameters.class, MaximumEntropy.ValidationMetrics.class, new ClassifierValidation<>());
    }
    
    @Override
    protected void predictDataset(Dataframe newData) { 
        Set<Object> classesSet = knowledgeBase.getModelParameters().getClasses();
                
        for(Map.Entry<Integer, Record> e : newData.entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            AssociativeArray predictionScores = new AssociativeArray();
            for(Object theClass : classesSet) {
                predictionScores.put(theClass, calculateClassScore(r.getX(),theClass));
            }
            
            Object theClass=getSelectedClassFromClassScores(predictionScores);
            
            
            Descriptives.normalizeExp(predictionScores);
            
            newData.set(rId, new Record(r.getX(), r.getY(), theClass, predictionScores));
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected void _fit(Dataframe trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        int n = modelParameters.getN();
        
        
        Map<List<Object>, Double> lambdas = modelParameters.getLambdas();
        Set<Object> classesSet = modelParameters.getClasses();
        
        //first we need to find all the classes
        for(Record r : trainingData) { 
            Object theClass=r.getY();
            
            classesSet.add(theClass); 
        }
        
        //create a temporary map for the observed probabilities in training set
        DatabaseConnector dbc = knowledgeBase.getDbc();
        Map<List<Object>, Double> tmp_EpFj_observed = dbc.getBigMap("tmp_EpFj_observed", true);
        
        double Cmax = 0.0; //max number of activated features in the dataset. Required from the IIS algorithm
        double increment = 1.0/n; //this is done for speed reasons. We don't want to repeat the same division over and over
        
        //then we calculate the observed probabilities in training set
        for(Record r : trainingData) { 
            int activeFeatures=0; //counts the number of non-zero (active) features of the record
            
            //store the occurrances of the features
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Double occurrences=TypeInference.toDouble(entry.getValue());
                
                if(occurrences==null || occurrences==0.0) {
                    continue;
                }
                Object feature = entry.getKey();
                
                //loop through all the classes to ensure that the feature-class combination is initialized for ALL the classes
                //in a previous implementation I did not loop through all the classes and used only the one of the record.
                //THIS IS WRONG. By not assigning 0 scores to the rest of the classes for this feature, we don't penalties for the non occurrance. 
                //The math REQUIRE us to have scores for all classes to make the probabilities comparable.
                for(Object theClass : classesSet) {
                    List<Object> featureClassTuple = Arrays.<Object>asList(feature, theClass);
                    Double previousValue = tmp_EpFj_observed.get(featureClassTuple);
                    if(previousValue==null) {
                        previousValue=0.0;
                        tmp_EpFj_observed.put(featureClassTuple, 0.0);
                        lambdas.put(featureClassTuple, 0.0); //initialize lambda
                    }
                    
                    //find the class of this particular example
                    if(theClass.equals(r.getY())) {
                        //update the statistics of the feature
                        tmp_EpFj_observed.put(featureClassTuple, previousValue + increment);
                    }
                } 
                
                ++activeFeatures;
            }
            

            //NOTE: we try to find the Cmax the maximum number of active featured in the training dataset. The total number of features in original IIS were required to be constant. NEVERTHELESS as it is mentioned here http://acl.ldc.upenn.edu/P/P02/P02-1002.pdf the Cmax only needs to constrain the number of features and not necessarily to be equal to them.
            //NOTE2: In this implementation the Cmax is equal to the maximum number of features that were found in the training dataset. We don't need to go through all the classes to find the Cmax. This is because of the way that the features are selected.
            if(activeFeatures>Cmax) {
                Cmax=activeFeatures;
            }
            
        }
        
        
        //IIS algorithm
        IIS(trainingData, tmp_EpFj_observed, Cmax);
        
        
        //Drop the temporary Collection
        dbc.dropBigMap("tmp_EpFj_observed", tmp_EpFj_observed);
    }
    
    private void IIS(Dataframe trainingData, Map<List<Object>, Double> EpFj_observed, double Cmax) {
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();

        int totalIterations = knowledgeBase.getTrainingParameters().getTotalIterations();
        Set<Object> classesSet = modelParameters.getClasses();
        Map<List<Object>, Double> lambdas = modelParameters.getLambdas();
        
        int n = modelParameters.getN();
        
        DatabaseConnector dbc = knowledgeBase.getDbc();
        for(int iteration=0;iteration<totalIterations;++iteration) {
            
            logger.debug("Iteration {}", iteration);
            
            Map<List<Object>, Double> tmp_EpFj_model = dbc.getBigMap("tmp_EpFj_model", true);
            Collection<List<Object>> infiniteLambdaWeights = new ArrayList<>();
            
            //initialize the model probabilities with 0. We will start estimating them piece by piece
            for(Map.Entry<List<Object>, Double> featureClassCounts : EpFj_observed.entrySet()) {
                List<Object> tp = featureClassCounts.getKey();
                tmp_EpFj_model.put(tp, 0.0);
            }
            
            //calculate the model probabilities
            for(Record r : trainingData) { 
                AssociativeArray classScores = new AssociativeArray();
                
                for(Object theClass : classesSet) {
                    double score = calculateClassScore(r.getX(), theClass);
                    classScores.put(theClass, score);
                }
                
                Descriptives.normalizeExp(classScores);
                
                //The below seems a bit strange but this is actually how the model probabilities are estimated. It is the average probability across all documents for a specific characteristic. The code is optimized for speed and this makes it less readable
                for(Map.Entry<Object, Object> entry : classScores.entrySet()) {
                    Object theClass = entry.getKey();
                    Double score = TypeInference.toDouble(entry.getValue());
                    
                    double probabilityFraction = score/n;
                    
                    for(Map.Entry<Object, Object> entry2 : r.getX().entrySet()) {
                        Double occurrences=TypeInference.toDouble(entry2.getValue());
                        
                        if(occurrences==null || occurrences==0.0) {
                            continue;
                        }
                        Object feature = entry2.getKey();
                        List<Object> featureClassTuple = Arrays.<Object>asList(feature, theClass);
                        
                        tmp_EpFj_model.put(featureClassTuple, tmp_EpFj_model.get(featureClassTuple) + probabilityFraction);
                    }
                }
                
                classScores=null;
            }
            
            Double minimumNonInfiniteLambdaWeight = null;
            Double maximumNonInfiniteLambdaWeight = null;
            //Now we have the model probabilities. We will use it to estimate the Deltas and finally update the lamdas
            for(Map.Entry<List<Object>, Double> featureClassCounts : tmp_EpFj_model.entrySet()) {
                List<Object> tp = featureClassCounts.getKey();
                //Object feature = tp.getKey1();
                //Object theClass = tp.getKey2();
                
                Double EpFj_observed_value = EpFj_observed.get(tp);
                Double EpFj_model_value = featureClassCounts.getValue();    
                
                
                if(Math.abs(EpFj_observed_value-EpFj_model_value)<=1e-8) {
                    //if those two are equal or both zero (pay attention to this, we try to handle the zero cases here)
                    //then do nothing. The two are equal so no change on weights is required.
                }
                else if(EpFj_observed_value==0.0) {
                    //The feature did not appear at all in the dataset for this class
                    //
                    //Intuitive Meaning: this feature obviously appears in SOME of the classes
                    //Those that do not include the keyword are less likely to be
                    //the correct class for this observation.
                    //
                    //Mathematical view: a 0 value on observed with non-zero value on
                    //the model suggests that the division between them is zero
                    //and thus when we take the logarithm it, the delta will become
                    //minus infinite. This will cause the weight to go to -inf.
                    //Thus if the feature appears, the probability of assigning it
                    //to this class will be 0 and the class will not be selected
                    //despite the value of the other features.
                    //
                    //Implementation/Programming view: if we indeed assign -inf
                    //all the other values will become insignificant. This means
                    //that a small noise (the occurrence of this feature in this class)
                    //can lead to discaring the correct class. Instead of assigning
                    //-inf or -Double.MAX_VALUE we will treat this more cleverly.
                    //We will originally store -Infinite here and once we calculate
                    //all the values we will replace this infinite with the smallest 
                    //non-negative infinite weight in the dataset. This is something
                    //similar to the plus1 smoothing.
                    
                    lambdas.put(tp, Double.NEGATIVE_INFINITY); //this will be revised, see comment above
                    infiniteLambdaWeights.add(tp);
                }
                else if(EpFj_model_value==0.0) {
                    //the model did not assign any positive probability for this feature in this class
                    //even if in real data it has a positive probability. This 
                    //should really never happen but we treat this case anyway.
                    //
                    //Mathematical view: a 0 on the denominator and a positive
                    //value on numerator will cause the logarithm to go to +inf.
                    //Logarithm will still produce a +inf and thus the lambda weight
                    //will become positive infinity. This effect is caused only
                    //because we choose to construct the model this way. It is
                    //not logical to happen.
                    //
                    //Implementation/Programming view: Assigning +inf is completely
                    //wrong. Instead we will again mark it as +inf and revise its
                    //value later on by updating it with the highest non-infite
                    //weight.
                    
                    
                    lambdas.put(tp, Double.POSITIVE_INFINITY); //this will be revised, see comment above
                    infiniteLambdaWeights.add(tp);
                }
                else {
                    //the formula below can't produce a +inf or -inf value
                    double deltaJ = Math.log(EpFj_observed_value/EpFj_model_value)/Cmax;
                    double newValue = lambdas.get(tp) + deltaJ;
                    lambdas.put(tp, newValue); //update lamdas by delta
                    
                    if(minimumNonInfiniteLambdaWeight==null || newValue<minimumNonInfiniteLambdaWeight) {
                        minimumNonInfiniteLambdaWeight=newValue;
                    }
                    if(maximumNonInfiniteLambdaWeight==null || newValue>maximumNonInfiniteLambdaWeight) {
                        maximumNonInfiniteLambdaWeight=newValue;
                    }
                }
                
                
            }
            
            
            
            if(!infiniteLambdaWeights.isEmpty()) {
                
                for(List<Object> featureClass : infiniteLambdaWeights) {
                    Double value = lambdas.get(featureClass);
                    
                    if(value==Double.NEGATIVE_INFINITY) {
                        lambdas.put(featureClass, minimumNonInfiniteLambdaWeight);
                    }
                    else if(value==Double.POSITIVE_INFINITY) {
                        lambdas.put(featureClass, maximumNonInfiniteLambdaWeight);
                    }
                    else { //this should never happen!
                        lambdas.put(featureClass, 0.0);
                    }
                }
            }
            
            
            
            //Drop the temporary Collection
            dbc.dropBigMap("tmp_EpFj_model", tmp_EpFj_model);
            infiniteLambdaWeights = null; //dbc.dropTable("infiniteLambdaWeights", infiniteLambdaWeights);
        }
        
    }
    
    private Double calculateClassScore(AssociativeArray x, Object theClass) {
        double score = 0;
        
        Map<List<Object>, Double> lambdas = knowledgeBase.getModelParameters().getLambdas();
        
        for(Map.Entry<Object, Object> entry : x.entrySet()) {
            Double value = TypeInference.toDouble(entry.getValue());
            if(value==null || value==0.0) {
                continue; //ignore the feature if it has no value
            }
            //note that we will not use the value any more. MaxEntropy classifier is binarized.
            
            Object feature = entry.getKey();
            List<Object> featureClassTuple = Arrays.<Object>asList(feature, theClass);
            
            Double lambdaWeight = lambdas.get(featureClassTuple);
            if(lambdaWeight!=null) {//ensure that the feature is in the dictionary
                score+=lambdaWeight;
            }
        }
        
        return score;
    }

}
