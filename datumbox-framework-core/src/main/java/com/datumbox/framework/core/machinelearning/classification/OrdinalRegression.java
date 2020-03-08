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
import com.datumbox.framework.core.mathematics.regularization.L2Regularizer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 * The OrdinalRegression implements the Multinomial Ordinal Regression.
 * 
 * References: 
 * http://qwone.com/~jason/writing/olr.pdf
 * http://www.rbsd.de/PDF/olr_mr.pdf
 * http://www.econ.kuleuven.be/public/ndbae06/pdf-files/Robust%20estimation%20for%20ordinal%20regression.pdf
 * http://fa.bianp.net/blog/2013/logistic-ordinal-regression/ 
 * https://github.com/fabianp/minirank/blob/master/minirank/logistic.py 
 * https://github.com/gcapan/recommender/blob/master/recommender-core/src/main/java/com/discovery/recommender/gradient/RegularizedOrdinalGradient.java
 * http://www.stat.uchicago.edu/~pmcc/pubs/paper2.pdf
 * http://ttic.uchicago.edu/~nati/Publications/RennieSrebroIJCAI05.pdf
 * http://rbakker.myweb.uga.edu/pols8501/MLENotes6a.pdf
 * http://www.academicjournals.org/article/article1379683447_Tamayo.pdf
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class OrdinalRegression extends AbstractClassifier<OrdinalRegression.ModelParameters, OrdinalRegression.TrainingParameters> implements PredictParallelizable, TrainParallelizable {
    
    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractClassifier.AbstractModelParameters {
        private static final long serialVersionUID = 1L;
        
        @BigMap(keyClass=Object.class, valueClass=Double.class, mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=false)
        private Map<Object, Double> weights; //the W parameters of the model

        /**
         * Right-side limits of the class on the ordinal regression line. 
         */
        private Map<Object, Double> thitas = new HashMap<>(); 

        /** 
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected ModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
        }
        
        /**
         * Getter for the weight coefficients.
         * 
         * @return 
         */
        public Map<Object, Double> getWeights() {
            return weights;
        }
        
        /**
         * Setter for the weight coefficients.
         * 
         * @param weights 
         */
        protected void setWeights(Map<Object, Double> weights) {
            this.weights = weights;
        }
        
        /**
         * Getter for the Thita coefficients.
         * 
         * @return 
         */
        public Map<Object, Double> getThitas() {
            return thitas;
        }
        
        /**
         * Setter for the Thita coefficients.
         * 
         * @param thitas 
         */
        protected void setThitas(Map<Object, Double> thitas) {
            this.thitas = thitas;
        }
        
    } 
    
    /** {@inheritDoc} */
    public static class TrainingParameters extends AbstractClassifier.AbstractTrainingParameters {    
        private static final long serialVersionUID = 1L;
        
        private int totalIterations=100; 
        private double learningRate=0.1;
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
    protected OrdinalRegression(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
        streamExecutor = new ForkJoinStream(knowledgeBase.getConfiguration().getConcurrencyConfiguration());
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected OrdinalRegression(String storageName, Configuration configuration) {
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
        
        AssociativeArray predictionProbabilities = hypothesisFunction(r.getX(), getPreviousThitaMappings(), modelParameters.getWeights(), modelParameters.getThitas());

        Object predictedClass=getSelectedClassFromClassScores(predictionProbabilities);

        return new Prediction(predictedClass, predictionProbabilities);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
                
        Map<Object, Double> weights = modelParameters.getWeights();
        Map<Object, Double> thitas = modelParameters.getThitas();
        
        //add classes in a sorted way (ordinal ascending order)
        //also we initialize the weights and thitas to zero
        TreeSet<Object> sortedClasses = new TreeSet<>();
        for(Record r : trainingData) { 
            Object theClass=r.getY();
            
            sortedClasses.add(theClass); 
            
            thitas.put(theClass, 0.0);
        }
        Object finalClass = sortedClasses.last();
        thitas.put(finalClass, Double.POSITIVE_INFINITY);
        
        Set<Object> classesSet = modelParameters.getClasses();
        classesSet.addAll(sortedClasses);
        
        for(Object feature: trainingData.getXDataTypes().keySet()) {
            weights.put(feature, 0.0);
        }        
        
        
        //mapping between the thita and the exact previous thita value
        Map<Object, Object> previousThitaMapping = getPreviousThitaMappings();
        
        double minError = Double.POSITIVE_INFINITY;
        
        double learningRate = trainingParameters.getLearningRate();
        int totalIterations = trainingParameters.getTotalIterations();
        StorageEngine storageEngine = knowledgeBase.getStorageEngine();
        for(int iteration=0;iteration<totalIterations;++iteration) {
            
            logger.debug("Iteration {}", iteration);
            
            Map<Object, Double> tmp_newThitas = new HashMap<>();
            
            Map<Object, Double> tmp_newWeights = storageEngine.getBigMap("tmp_newWeights", Object.class, Double.class, MapType.HASHMAP, StorageHint.IN_MEMORY, false, true);
            
            tmp_newThitas.putAll(thitas);
            tmp_newWeights.putAll(weights);
            batchGradientDescent(trainingData, previousThitaMapping, tmp_newWeights, tmp_newThitas, learningRate);
            
            double newError = calculateError(trainingData, previousThitaMapping, tmp_newWeights, tmp_newThitas);
            
            //bold driver
            if(newError>minError) {
                learningRate/=2.0;
            }
            else {
                learningRate*=1.05;
                minError=newError;
                
                //keep the new weights
                weights.clear();
                weights.putAll(tmp_newWeights);
                
                //keep the new thitas
                thitas.clear();
                thitas.putAll(tmp_newThitas);
            }
            
            //Drop the temporary Collections
            storageEngine.dropBigMap("tmp_newWeights", tmp_newWeights);
        }
    }

    private void batchGradientDescent(Dataframe trainingData, Map<Object, Object> previousThitaMapping, Map<Object, Double> newWeights, Map<Object, Double> newThitas, double learningRate) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();

        double multiplier = -learningRate/trainingData.size();
        Map<Object, Double> weights = modelParameters.getWeights();
        Map<Object, Double> thitas = modelParameters.getThitas();
        
        streamExecutor.forEach(StreamMethods.stream(trainingData.stream(), isParallelized()), r -> { 
            Object rClass = r.getY();
            Object rPreviousClass = previousThitaMapping.get(rClass);
            
            //mind the fact that we use the previous weights and thitas to estimate the new ones! this is because the thitas must be updated simultaniously
            
            //first calculate the commonly used dot product between weights and x
            double xTw = xTw(r.getX(), weights);
            
            double gOfCurrent = g(xTw-thitas.get(rClass));
            double gOfPrevious = (rPreviousClass!=null)?g(thitas.get(rPreviousClass)-xTw):0.0;
                    
            double dtG_multiplier = (gOfCurrent-gOfPrevious)*multiplier;
            
            
            //update weights                
            synchronized(newWeights) {
                for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                    Object column = entry.getKey();
                    Double xij = TypeInference.toDouble(entry.getValue());

                    double xij_dtG_multiplier = xij*dtG_multiplier;
                    newWeights.put(column, newWeights.get(column)+xij_dtG_multiplier);
                }
            }
            
            synchronized(newThitas) {
                //update thitas
                newThitas.put(rClass, newThitas.get(rClass)+multiplier*(-gOfCurrent));
                if(rPreviousClass!=null) {
                    newThitas.put(rPreviousClass, newThitas.get(rPreviousClass)+multiplier*gOfPrevious);
                }
            }
        });

        L2Regularizer.updateWeights(knowledgeBase.getTrainingParameters().getL2(), learningRate, weights, newWeights);

    }
    
    private AssociativeArray hypothesisFunction(AssociativeArray x, Map<Object, Object> previousThitaMapping, Map<Object, Double> weights, Map<Object, Double> thitas) {
        AssociativeArray probabilities = new AssociativeArray();
    
            
        //first calculate the commonly used dot product between weights and x
        double xTw = xTw(x, weights);
        
        Set<Object> classesSet = knowledgeBase.getModelParameters().getClasses();
        
        for(Object theClass : classesSet) {
            Object previousClass = previousThitaMapping.get(theClass);
            
            if(previousClass!=null) {
                probabilities.put(theClass, g(thitas.get(theClass)-xTw) - g(thitas.get(previousClass)-xTw) );
            }
            else {
                probabilities.put(theClass, g(thitas.get(theClass)-xTw) );
            }
            
            
        }
        
        return probabilities;
    }
    
    private double calculateError(Dataframe trainingData, Map<Object, Object> previousThitaMapping, Map<Object, Double> weights, Map<Object, Double> thitas) {
        
        double error = streamExecutor.sum(StreamMethods.stream(trainingData.stream(), isParallelized()).mapToDouble(r -> { 
            double e=0.0;
            double xTw = xTw(r.getX(), weights);
            
            Object theClass = r.getY();
            Object previousClass = previousThitaMapping.get(theClass);
            
            
            if(previousClass!=null) {
                e += h(thitas.get(previousClass)-xTw);
            }
            
            e += h(xTw-thitas.get(theClass));
            
            return e;
        }));
        error /= trainingData.size();

        error += L2Regularizer.estimatePenalty(knowledgeBase.getTrainingParameters().getL2(), weights);
        
        return error;
    }
    
    private double h(double z) {
        if(z>30) {
            return z;
        }
        else if(z<-30) {
            return 0.0;
        }
        return Math.log(1.0+Math.exp(z));
    }
    
    private double g(double z) {
        if(z>30) {
            return 1.0;
        }
        else if(z<-30) {
            return 0.0;
        }
        return 1.0/(1.0+Math.exp(-z));
    }
    
    private double xTw(AssociativeArray x, Map<Object, Double> weights) {
        double xTw = 0.0;
        for(Map.Entry<Object, Object> entry : x.entrySet()) {
            Double value = TypeInference.toDouble(entry.getValue());
            if(value==null || value==0.0) {
                continue;
            }
            Object column = entry.getKey();
            Double w = weights.get(column);
            if(w==null) {
                continue; //unsupported feature
            }
            xTw += value*w;
        }
        
        return xTw;
    }
    
    private Map<Object, Object> getPreviousThitaMappings() {
        Map<Object, Object> previousThitaMapping = new HashMap<>();
        Object previousThita = null; //null = the left bound thita0 which has thita equal to -inf
        for(Object thita : knowledgeBase.getModelParameters().getClasses()) {
            previousThitaMapping.put(thita, previousThita);
            previousThita = thita;
        }
        
        return previousThitaMapping;
    }
    
}
