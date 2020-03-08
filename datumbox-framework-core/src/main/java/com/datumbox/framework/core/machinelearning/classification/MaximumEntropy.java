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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * The MaximumEntropy implements the Max Ent classifier which is an alternative
 * implementation of Multinomial Logistic Regression model.
 * 
 * References: 
 * http://blog.datumbox.com/machine-learning-tutorial-the-max-entropy-text-classifier/
 * http://www.cs.cmu.edu/afs/cs/user/aberger/www/html/tutorial/node3.html 
 * https://web.archive.org/web/20121021173335/http://acl.ldc.upenn.edu/P/P02/P02-1002.pdf
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class MaximumEntropy extends AbstractClassifier<MaximumEntropy.ModelParameters, MaximumEntropy.TrainingParameters> implements PredictParallelizable, TrainParallelizable {

    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractClassifier.AbstractModelParameters {
        private static final long serialVersionUID = 1L;
        
        @BigMap(keyClass=List.class, valueClass=Double.class, mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=true)
        private Map<List<Object>, Double> lambdas; //the lambda parameters of the model
        
        /** 
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected ModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
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
    
    /** {@inheritDoc} */
    public static class TrainingParameters extends AbstractClassifier.AbstractTrainingParameters { 
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
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainingParameters, Configuration)
     */
    protected MaximumEntropy(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
        streamExecutor = new ForkJoinStream(knowledgeBase.getConfiguration().getConcurrencyConfiguration());
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected MaximumEntropy(String storageName, Configuration configuration) {
        super(storageName, configuration);
        streamExecutor = new ForkJoinStream(knowledgeBase.getConfiguration().getConcurrencyConfiguration());
    }
    
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
        Set<Object> classesSet = knowledgeBase.getModelParameters().getClasses();
        
        AssociativeArray predictionScores = new AssociativeArray();
        for(Object theClass : classesSet) {
            predictionScores.put(theClass, calculateClassScore(r.getX(), theClass));
        }
        
        Object predictedClass=getSelectedClassFromClassScores(predictionScores);

        Descriptives.normalizeExp(predictionScores);
        
        return new Prediction(predictedClass, predictionScores);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        int n = trainingData.size();
        
        
        Map<List<Object>, Double> lambdas = modelParameters.getLambdas();
        Set<Object> classesSet = modelParameters.getClasses();
        double Cmax = 0.0; //max number of activated features in the dataset. Required from the IIS algorithm
        
        //first we need to find all the classes
        for(Record r : trainingData) { 
            Object theClass=r.getY();
            
            classesSet.add(theClass); 
            
            //counts the number of non-zero (active) features of the record
            int activeFeatures=(int) r.getX().values().stream().filter(e -> e !=null && TypeInference.toDouble(e) > 0.0).count();

            //NOTE: we try to find the Cmax the maximum number of active featured in the training dataset. The total number of features in original IIS were required to be constant. NEVERTHELESS as it is mentioned here http://acl.ldc.upenn.edu/P/P02/P02-1002.pdf the Cmax only needs to constrain the number of features and not necessarily to be equal to them.
            //NOTE2: In this implementation the Cmax is equal to the maximum number of features that were found in the training dataset. We don't need to go through all the classes to find the Cmax. This is because of the way that the features are selected.
            if(activeFeatures>Cmax) {
                Cmax=activeFeatures;
            }
            
        }
        
        //create a temporary map for the observed probabilities in training set
        StorageEngine storageEngine = knowledgeBase.getStorageEngine();
        Map<List<Object>, Double> tmp_EpFj_observed = storageEngine.getBigMap("tmp_EpFj_observed", (Class<List<Object>>)(Class<?>)List.class, Double.class, MapType.HASHMAP, StorageHint.IN_MEMORY, true, true);
        
        //Loop through all the classes to ensure that the feature-class combination is initialized for ALL the classes
        //The math REQUIRE us to have scores for all classes to make the probabilities comparable.
        streamExecutor.forEach(StreamMethods.stream(trainingData.getXDataTypes().keySet().stream(), isParallelized()), feature -> {
            for(Object theClass : classesSet) {
                List<Object> featureClassTuple = Arrays.asList(feature, theClass);
                tmp_EpFj_observed.put(featureClassTuple, 0.0);
                lambdas.put(featureClassTuple, 0.0);
            }
        });
        
    
        double increment = 1.0/n; //this is done for speed reasons. We don't want to repeat the same division over and over
        
        //then we calculate the observed probabilities in training set
        streamExecutor.forEach(StreamMethods.stream(trainingData.stream(), isParallelized()), r -> {
            Object theClass = r.getY();
            //store the occurrances of the features
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Double occurrences=TypeInference.toDouble(entry.getValue());
                if (occurrences!=null && occurrences>0.0) {
                    Object feature = entry.getKey();

                    //find the class of this particular example
                    List<Object> featureClassTuple = Arrays.asList(feature, theClass);
                    synchronized(tmp_EpFj_observed) {
                        tmp_EpFj_observed.put(featureClassTuple, tmp_EpFj_observed.get(featureClassTuple) + increment);
                    }
                }
            }
            
        });
        
        
        //IIS algorithm
        IIS(trainingData, tmp_EpFj_observed, Cmax);
        
        
        //Drop the temporary Collection
        storageEngine.dropBigMap("tmp_EpFj_observed", tmp_EpFj_observed);
    }
    
    private void IIS(Dataframe trainingData, Map<List<Object>, Double> EpFj_observed, double Cmax) {
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();

        int totalIterations = knowledgeBase.getTrainingParameters().getTotalIterations();
        Set<Object> classesSet = modelParameters.getClasses();
        Map<List<Object>, Double> lambdas = modelParameters.getLambdas();
        
        int n = trainingData.size();
        
        StorageEngine storageEngine = knowledgeBase.getStorageEngine();
        for(int iteration=0;iteration<totalIterations;++iteration) {
            
            logger.debug("Iteration {}", iteration);
            
            Map<List<Object>, Double> tmp_EpFj_model = storageEngine.getBigMap("tmp_EpFj_model", (Class<List<Object>>)(Class<?>)List.class, Double.class, MapType.HASHMAP, StorageHint.IN_MEMORY, false, true);
            
            //calculate the model probabilities
            streamExecutor.forEach(StreamMethods.stream(trainingData.stream(), isParallelized()), r -> { //slow parallel loop
                
                //build a map with the scores of the record for each class
                AssociativeArray classScores = new AssociativeArray();
                AssociativeArray xData = r.getX();
                for(Object theClass : classesSet) {
                    double score = calculateClassScore(xData, theClass);
                    classScores.put(theClass, score);
                }
                
                Descriptives.normalizeExp(classScores);
                
                
                //It is the average probability across all documents for a specific characteristic
                for(Map.Entry<Object, Object> entry : classScores.entrySet()) {
                    Object theClass = entry.getKey();
                    Double score = TypeInference.toDouble(entry.getValue());

                    double probabilityFraction = score/n;
                    
                    synchronized(tmp_EpFj_model) {
                        for(Map.Entry<Object, Object> entry2 : r.getX().entrySet()) {
                            Double occurrences=TypeInference.toDouble(entry2.getValue());

                            if(occurrences==null || occurrences==0.0) {
                                continue;
                            }
                            Object feature = entry2.getKey();
                            List<Object> featureClassTuple = Arrays.asList(feature, theClass);

                            tmp_EpFj_model.put(featureClassTuple, tmp_EpFj_model.getOrDefault(featureClassTuple, 0.0) + probabilityFraction);           
                        }
                    }
                }
                //classScores=null;
                
            });
            
            AtomicBoolean infiniteValuesDetected = new AtomicBoolean(false);
            //Now we have the model probabilities. We will use it to estimate the Deltas and finally update the lamdas
            streamExecutor.forEach(StreamMethods.stream(tmp_EpFj_model.entrySet().stream(), isParallelized()), featureClassCounts -> {
                List<Object> tp = featureClassCounts.getKey();
                
                Double EpFj_observed_value = EpFj_observed.get(tp);
                Double EpFj_model_value = featureClassCounts.getValue();    
                
                
                if(Math.abs(EpFj_observed_value-EpFj_model_value)<=1e-8) {
                    //if those two are equal or both zero then do nothing.
                    //The two are equal so no change on weights is required.
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
                    
                    lambdas.put(tp, Double.NEGATIVE_INFINITY);
                    infiniteValuesDetected.set(true);
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
                    
                    
                    lambdas.put(tp, Double.POSITIVE_INFINITY);
                    infiniteValuesDetected.set(true);
                }
                else {
                    //the formula below can't produce a +inf or -inf value
                    double deltaJ = Math.log(EpFj_observed_value/EpFj_model_value)/Cmax;
                    double newValue = lambdas.get(tp) + deltaJ;
                    lambdas.put(tp, newValue); //update lamdas by delta
                }
            });
            
            
            if(infiniteValuesDetected.get()) {
            
            
                Double minimumNonInfiniteLambdaWeight = streamExecutor.min(StreamMethods.stream(lambdas.values().stream(), isParallelized()).filter(v -> Double.isFinite(v)), Double::compare).get();
                Double maximumNonInfiniteLambdaWeight = streamExecutor.max(StreamMethods.stream(lambdas.values().stream(), isParallelized()).filter(v -> Double.isFinite(v)), Double::compare).get();
                
                streamExecutor.forEach(StreamMethods.stream(lambdas.entrySet().stream(), isParallelized()), e -> {
                    List<Object> featureClass = e.getKey();
                    Double value = e.getValue();
                    
                    if(Double.isInfinite(value)) {
                        if(value<0.0) { //value==Double.NEGATIVE_INFINITY
                            lambdas.put(featureClass, minimumNonInfiniteLambdaWeight);
                        }
                        else { //value==Double.POSITIVE_INFINITY
                            lambdas.put(featureClass, maximumNonInfiniteLambdaWeight);
                        }
                    }
                });
            }
            
            
            
            //Drop the temporary Collection
            storageEngine.dropBigMap("tmp_EpFj_model", tmp_EpFj_model);
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
            List<Object> featureClassTuple = Arrays.asList(feature, theClass);
            
            Double lambdaWeight = lambdas.get(featureClassTuple);
            if(lambdaWeight!=null) {//ensure that the feature is in the dictionary
                score+=lambdaWeight;
            }
        }
        
        return score;
    }

}
