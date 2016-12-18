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
package com.datumbox.framework.core.machinelearning.regression;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.concurrency.ForkJoinStream;
import com.datumbox.framework.common.concurrency.StreamMethods;
import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.Record;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.common.persistentstorage.interfaces.BigMap;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector.MapType;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector.StorageHint;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelers.AbstractRegressor;
import com.datumbox.framework.core.machinelearning.common.interfaces.PredictParallelizable;
import com.datumbox.framework.core.machinelearning.common.interfaces.TrainParallelizable;
import com.datumbox.framework.core.utilities.regularization.ElasticNetRegularizer;
import com.datumbox.framework.core.utilities.regularization.L1Regularizer;
import com.datumbox.framework.core.utilities.regularization.L2Regularizer;

import java.util.Map;

/**
 * Linear Regression model which uses the Normalised Least Mean Squares Algorithm.
 * This implementation should be preferred from MatrixLinearRegression when the 
 data can't fit the memory.
 
 References:
 http://cs229.stanford.edu/notes/cs229-notes1.pdf
 http://www.holehouse.org/mlclass/04_Linear_Regression_with_multiple_variables.html
 https://class.coursera.org/ml-003/lecture/index
 http://www.analyticsvidhya.com/blog/2016/01/complete-tutorial-ridge-lasso-regression-python/
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class NLMS extends AbstractRegressor<NLMS.ModelParameters, NLMS.TrainingParameters> implements PredictParallelizable, TrainParallelizable {
     
    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractRegressor.AbstractModelParameters {
        private static final long serialVersionUID = 1L;

        @BigMap(keyClass=Object.class, valueClass=Double.class, mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=false)
        private Map<Object, Double> thitas; //the thita parameters of the model

        /** 
         * @param dbc
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(DatabaseConnector)
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
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
    public static class TrainingParameters extends AbstractRegressor.AbstractTrainingParameters {
        private static final long serialVersionUID = 1L;
        
        private int totalIterations=1000; 
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
     * Public constructor of the algorithm.
     * 
     * @param dbName
     * @param conf 
     */
    public NLMS(String dbName, Configuration conf) {
        super(dbName, conf, NLMS.ModelParameters.class, NLMS.TrainingParameters.class);
        streamExecutor = new ForkJoinStream(knowledgeBase.getConf().getConcurrencyConfig());
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
    protected void _predictDataset(Dataframe newData) {
        DatabaseConnector dbc = knowledgeBase.getDbc();
        Map<Integer, Prediction> resultsBuffer = dbc.getBigMap("tmp_resultsBuffer", Integer.class, Prediction.class, MapType.HASHMAP, StorageHint.IN_DISK, true, true);
        _predictDatasetParallel(newData, resultsBuffer, knowledgeBase.getConf().getConcurrencyConfig());
        dbc.dropBigMap("tmp_resultsBuffer", resultsBuffer);
    }

    /** {@inheritDoc} */
    @Override
    public Prediction _predictRecord(Record r) {
        Map<Object, Double> thitas = knowledgeBase.getModelParameters().getThitas();
        
        double yPredicted = hypothesisFunction(r.getX(), thitas);
        
        return new Prediction(yPredicted, null);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        Map<Object, Double> thitas = modelParameters.getThitas();
        
        //we initialize the thitas to zero for all features
        thitas.put(Dataframe.COLUMN_NAME_CONSTANT, 0.0);
        for(Object feature : trainingData.getXDataTypes().keySet()) {
            thitas.put(feature, 0.0);
        }
        
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();

        double minError = Double.POSITIVE_INFINITY;
        
        double learningRate = trainingParameters.getLearningRate();
        int totalIterations = trainingParameters.getTotalIterations();
        DatabaseConnector dbc = knowledgeBase.getDbc();
        for(int iteration=0;iteration<totalIterations;++iteration) {
            
            logger.debug("Iteration {}", iteration);
            
            Map<Object, Double> tmp_newThitas = dbc.getBigMap("tmp_newThitas", Object.class, Double.class, MapType.HASHMAP, StorageHint.IN_MEMORY, false, true);
            
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
            dbc.dropBigMap("tmp_newThitas", tmp_newThitas);
        }
    }

    private void batchGradientDescent(Dataframe trainingData, Map<Object, Double> newThitas, double learningRate) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        double multiplier = learningRate/trainingData.size();
        Map<Object, Double> thitas = modelParameters.getThitas();
        
        streamExecutor.forEach(StreamMethods.stream(trainingData.stream(), isParallelized()), r -> { 
            //mind the fact that we use the previous thitas to estimate the new ones! this is because the thitas must be updated simultaniously
            double error = TypeInference.toDouble(r.getY()) - hypothesisFunction(r.getX(), thitas);

            double errorMultiplier = multiplier*error;
            
            synchronized(newThitas) {
                //update the weights
                for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                    Object feature = entry.getKey();
                    Double value = TypeInference.toDouble(entry.getValue());

                    newThitas.put(feature, newThitas.get(feature)+errorMultiplier*value);
                }
                newThitas.put(Dataframe.COLUMN_NAME_CONSTANT, newThitas.get(Dataframe.COLUMN_NAME_CONSTANT)+errorMultiplier);
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
    
    private double calculateError(Dataframe trainingData, Map<Object, Double> thitas) {
        //It is optimized for speed to reduce the amount of loops
        
        double error = streamExecutor.sum(StreamMethods.stream(trainingData.stream(), isParallelized()).mapToDouble(r -> { 
            double yPredicted = hypothesisFunction(r.getX(), thitas);
            return Math.pow(TypeInference.toDouble(r.getY()) -yPredicted, 2);
        }));
        error /= trainingData.size();

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
    
    private double hypothesisFunction(AssociativeArray x, Map<Object, Double> thitas) {
        double sum = thitas.get(Dataframe.COLUMN_NAME_CONSTANT);
        
        for(Map.Entry<Object, Object> entry : x.entrySet()) {
            Object feature = entry.getKey();
            Double xj = TypeInference.toDouble(entry.getValue());
            
            sum+=thitas.getOrDefault(feature, 0.0)*xj;
        }
        
        return sum;
    }
}
