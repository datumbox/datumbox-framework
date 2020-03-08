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
import com.datumbox.framework.core.mathematics.regularization.ElasticNetRegularizer;
import com.datumbox.framework.core.mathematics.regularization.L1Regularizer;
import com.datumbox.framework.core.mathematics.regularization.L2Regularizer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * The SoftMaxRegression implements the Multinomial Logistic Regression classifier.
 * 
 * References:
 * http://blog.datumbox.com/machine-learning-tutorial-the-multinomial-logistic-regression-softmax-regression/
 * http://blog.datumbox.com/tuning-the-learning-rate-in-gradient-descent/
 * http://www.cs.cmu.edu/afs/cs/user/aberger/www/html/tutorial/node3.html
 * http://acl.ldc.upenn.edu/P/P02/P02-1002.pdf
 * http://www.aclweb.org/anthology/P09-1054
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class SoftMaxRegression extends AbstractClassifier<SoftMaxRegression.ModelParameters, SoftMaxRegression.TrainingParameters> implements PredictParallelizable, TrainParallelizable {
    
    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractClassifier.AbstractModelParameters {
        private static final long serialVersionUID = 1L;

        @BigMap(keyClass=List.class, valueClass=Double.class, mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=true)
        private Map<List<Object>, Double> thitas; //the thita parameters of the model
        
        /** 
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected ModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
        }
        
        /**
         * Getter for the Thita coefficients of the model.
         * 
         * @return 
         */
        public Map<List<Object>, Double> getThitas() {
            return thitas;
        }
        
        /**
         * Setter for the Thita coefficients of the model.
         * 
         * @param thitas 
         */
        protected void setThitas(Map<List<Object>, Double> thitas) {
            this.thitas = thitas;
        }
    } 
    
    /** {@inheritDoc} */
    public static class TrainingParameters extends AbstractClassifier.AbstractTrainingParameters { 
        private static final long serialVersionUID = 1L;
        
        private int totalIterations=100; 
        private double learningRate=0.1;
        private double l1=0.0;
        private double l2=0.0;
        
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
        
        /**
         * Getter for the initial value of the Learning Rate.
         * 
         * @return 
         */
        public double getLearningRate() {
            return learningRate;
        }
        
        /**
         * Setter for the initial value of the Learning Rate. This value will be
         * adapted during the iterations.
         * 
         * @param learningRate 
         */
        public void setLearningRate(double learningRate) {
            this.learningRate = learningRate;
        }

        /**
         * Getter for the value of L1 regularization.
         *
         * @return
         */
        public double getL1() {
            return l1;
        }

        /**
         * Setter for the value of the L1 regularization.
         *
         * @param l1
         */
        public void setL1(double l1) {
            this.l1 = l1;
        }

        /**
         * Getter for the value of L2 regularization.
         *
         * @return
         */
        public double getL2() {
            return l2;
        }

        /**
         * Setter for the value of the L2 regularization.
         *
         * @param l2
         */
        public void setL2(double l2) {
            this.l2 = l2;
        }

    }


    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainingParameters, Configuration)
     */
    protected SoftMaxRegression(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
        streamExecutor = new ForkJoinStream(knowledgeBase.getConfiguration().getConcurrencyConfiguration());
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected SoftMaxRegression(String storageName, Configuration configuration) {
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
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        Set<Object> classesSet = modelParameters.getClasses();
        Map<List<Object>, Double> thitas = modelParameters.getThitas();
        
        AssociativeArray predictionScores = new AssociativeArray();
        for(Object theClass : classesSet) {
            predictionScores.put(theClass, calculateClassScore(r.getX(), theClass, thitas));
        }

        Object predictedClass=getSelectedClassFromClassScores(predictionScores);

        Descriptives.normalizeExp(predictionScores);
        
        return new Prediction(predictedClass, predictionScores);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        
        Map<List<Object>, Double> thitas = modelParameters.getThitas();
        Set<Object> classesSet = modelParameters.getClasses();
        
        //first we need to find all the classes
        for(Record r : trainingData) { 
            Object theClass=r.getY();
            
            classesSet.add(theClass); 
        }
        
        //we initialize the thitas to zero for all features and all classes compinations
        for(Object theClass : classesSet) {
            thitas.put(Arrays.asList(Dataframe.COLUMN_NAME_CONSTANT, theClass), 0.0);
        }
        
        streamExecutor.forEach(StreamMethods.stream(trainingData.getXDataTypes().keySet().stream(), isParallelized()), feature -> {
            for(Object theClass : classesSet) {
                thitas.putIfAbsent(Arrays.asList(feature, theClass), 0.0);
            }
        });
        
        
        double minError = Double.POSITIVE_INFINITY;
        
        double learningRate = trainingParameters.getLearningRate();
        int totalIterations = trainingParameters.getTotalIterations();
        StorageEngine storageEngine = knowledgeBase.getStorageEngine();
        for(int iteration=0;iteration<totalIterations;++iteration) {
            
            logger.debug("Iteration {}", iteration);
            
            Map<List<Object>, Double> tmp_newThitas = storageEngine.getBigMap("tmp_newThitas", (Class<List<Object>>)(Class<?>)List.class, Double.class, MapType.HASHMAP, StorageHint.IN_MEMORY, true, true);
            
            tmp_newThitas.putAll(thitas);
            batchGradientDescent(trainingData, tmp_newThitas, learningRate);
            
            double newError = calculateError(trainingData,tmp_newThitas);
            
            //bold driver
            if(newError>minError) {
                learningRate/=2.0;
            }
            else {
                learningRate*=1.05;
                minError=newError;
                
                //keep the new thitas
                thitas.clear();
                thitas.putAll(tmp_newThitas);
            }
            
            //Drop the temporary Collection
            storageEngine.dropBigMap("tmp_newThitas", tmp_newThitas);
        }
    }

    private void batchGradientDescent(Dataframe trainingData, Map<List<Object>, Double> newThitas, double learningRate) {
        //NOTE! This is not the stochastic gradient descent. It is the batch gradient descent optimized for speed (despite it looks more than the stochastic). 
        //Despite the fact that the loops are inverse, the function still changes the values of Thitas at the end of the function. We use the previous thitas 
        //to estimate the costs and only at the end we update the new thitas.
        ModelParameters modelParameters = knowledgeBase.getModelParameters();

        double multiplier = learningRate/trainingData.size();
        Map<List<Object>, Double> thitas = modelParameters.getThitas();
        Set<Object> classesSet = modelParameters.getClasses();
        
        streamExecutor.forEach(StreamMethods.stream(trainingData.stream(), isParallelized()), r -> { //slow parallel loop
            //mind the fact that we use the previous thitas to estimate the new ones! this is because the thitas must be updated simultaniously
            AssociativeArray classProbabilities = hypothesisFunction(r.getX(), thitas);
            for(Object theClass : classesSet) {
                
                double error;
                double score = classProbabilities.getDouble(theClass);
                if(r.getY().equals(theClass)) {
                    error = 1 - score;
                }
                else {
                    error = - score;
                }
                
                double errorMultiplier = multiplier*error;
                
                synchronized(newThitas) {
                    //update the weights
                    for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                        Double value = TypeInference.toDouble(entry.getValue());

                        Object feature = entry.getKey();
                        List<Object> featureClassTuple = Arrays.asList(feature, theClass);
                        
                        newThitas.put(featureClassTuple, newThitas.get(featureClassTuple)+errorMultiplier*value);   
                    }
                    List<Object> featureClassTuple = Arrays.asList(Dataframe.COLUMN_NAME_CONSTANT, theClass);
                    newThitas.put(featureClassTuple, newThitas.get(featureClassTuple)+errorMultiplier); //update the weight of constant
                }
            }
        });

        double l1 = knowledgeBase.getTrainingParameters().getL1();
        double l2 = knowledgeBase.getTrainingParameters().getL2();

        if(l1>0.0 && l2>0.0) {
            ElasticNetRegularizer.updateWeights(l1, l2, learningRate, thitas, newThitas);
        }
        else if(l1>0.0) {
            L1Regularizer.updateWeights(l1, learningRate, thitas, newThitas);
        }
        else if(l2>0.0) {
            L2Regularizer.updateWeights(l2, learningRate, thitas, newThitas);
        }
        
    }
    
    private Double calculateClassScore(AssociativeArray x, Object theClass, Map<List<Object>, Double> thitas) {
        double score = thitas.get(Arrays.asList(Dataframe.COLUMN_NAME_CONSTANT, theClass));
        
        for(Map.Entry<Object, Object> entry : x.entrySet()) {
            Double value = TypeInference.toDouble(entry.getValue());
            
            Object feature = entry.getKey();
            List<Object> featureClassTuple = Arrays.asList(feature, theClass);
            
            Double thitaWeight = thitas.get(featureClassTuple);
            if(thitaWeight!=null) {//ensure that the feature is in the dictionary
                score+=thitaWeight*value;
            }
        }
        
        return score;
    }
    
    private double calculateError(Dataframe trainingData, Map<List<Object>, Double> thitas) {
        //The cost function as described on http://ufldl.stanford.edu/wiki/index.php/Softmax_Regression
        //It is optimized for speed to reduce the amount of loops
        
        double error = streamExecutor.sum(StreamMethods.stream(trainingData.stream(), isParallelized()).mapToDouble(r -> { 
            AssociativeArray classProbabilities = hypothesisFunction(r.getX(), thitas);
            Double score = classProbabilities.getDouble(r.getY());
            return Math.log(score); //no need to loop through the categories. Just grab the one that we are interested in
        }));

        error = -error/trainingData.size();

        double l1 = knowledgeBase.getTrainingParameters().getL1();
        double l2 = knowledgeBase.getTrainingParameters().getL2();

        if(l1>0.0 && l2>0.0) {
            error += ElasticNetRegularizer.estimatePenalty(l1, l2, thitas);
        }
        else if(l1>0.0) {
            error += L1Regularizer.estimatePenalty(l1, thitas);
        }
        else if(l2>0.0) {
            error += L2Regularizer.estimatePenalty(l2, thitas);
        }

        return error;
    }
    
    private AssociativeArray hypothesisFunction(AssociativeArray x, Map<List<Object>, Double> thitas) {
        Set<Object> classesSet = knowledgeBase.getModelParameters().getClasses();
        AssociativeArray predictionProbabilities = new AssociativeArray(); 
        
        for(Object theClass : classesSet) {
            double score=calculateClassScore(x, theClass, thitas);
            if(score<=0) {
                score=1e-8;
            }
            predictionProbabilities.put(theClass, score);
        }
        
        Descriptives.normalize(predictionProbabilities);
        
        return predictionProbabilities;
    }

}
