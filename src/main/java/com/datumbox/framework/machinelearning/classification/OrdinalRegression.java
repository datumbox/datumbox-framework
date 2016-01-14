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

import com.datumbox.common.concurrency.ForkJoinStream;
import com.datumbox.common.concurrency.StreamMethods;
import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.MapType;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector.StorageHint;
import com.datumbox.framework.machinelearning.common.interfaces.PredictParallelizable;
import com.datumbox.framework.machinelearning.common.interfaces.PredictParallelizable.Prediction;


import com.datumbox.framework.machinelearning.common.abstracts.modelers.AbstractClassifier;
import com.datumbox.framework.machinelearning.common.interfaces.TrainParallelizable;
import com.datumbox.framework.machinelearning.common.validators.OrdinalRegressionValidator;
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
public class OrdinalRegression extends AbstractClassifier<OrdinalRegression.ModelParameters, OrdinalRegression.TrainingParameters, OrdinalRegression.ValidationMetrics> implements PredictParallelizable, TrainParallelizable {
    
    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractClassifier.AbstractModelParameters {
        private static final long serialVersionUID = 1L;
        
        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=false)
        private Map<Object, Double> weights; //the W parameters of the model

        /**
         * Right-side limits of the class on the ordinal regression line. 
         */
        @BigMap(mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=false)
        private Map<Object, Double> thitas; 

        /** 
         * @param dbc
         * @see com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer.AbstractModelParameters#AbstractModelParameters(com.datumbox.common.persistentstorage.interfaces.DatabaseConnector) 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
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

    } 
    
    /** {@inheritDoc} */
    public static class ValidationMetrics extends AbstractClassifier.AbstractValidationMetrics {
        private static final long serialVersionUID = 1L;
        
        private double SSE = 0.0; 
        private double CountRSquare = 0.0; // http://www.ats.ucla.edu/stat/mult_pkg/faq/general/Psuedo_RSquareds.htm
        
        /**
         * Getter for the SSE metric.
         * 
         * @return 
         */
        public double getSSE() {
            return SSE;
        }
        
        /**
         * Setter for the SSE metric.
         * 
         * @param SSE 
         */
        public void setSSE(double SSE) {
            this.SSE = SSE;
        }
        
        /**
         * Getter for the Count R^2 metric.
         * 
         * @return 
         */
        public double getCountRSquare() {
            return CountRSquare;
        }
        
        /**
         * Setter for the Count R^2 metric.
         * 
         * @param CountRSquare 
         */
        public void setCountRSquare(double CountRSquare) {
            this.CountRSquare = CountRSquare;
        }
        
    }
    
    /**
     * Public constructor of the algorithm.
     * 
     * @param dbName
     * @param dbConf 
     */
    public OrdinalRegression(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, OrdinalRegression.ModelParameters.class, OrdinalRegression.TrainingParameters.class, OrdinalRegression.ValidationMetrics.class, new OrdinalRegressionValidator());
        streamExecutor = new ForkJoinStream();
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
        DatabaseConnector dbc = kb().getDbc();
        Map<Integer, Prediction> resultsBuffer = dbc.getBigMap("tmp_resultsBuffer", MapType.HASHMAP, StorageHint.IN_DISK, true, true);
        _predictDatasetParallel(newData, resultsBuffer);
        dbc.dropBigMap("tmp_resultsBuffer", resultsBuffer);
    }
    
    /** {@inheritDoc} */
    @Override
    public Prediction _predictRecord(Record r) { 
        ModelParameters modelParameters = kb().getModelParameters();
        
        AssociativeArray predictionProbabilities = hypothesisFunction(r.getX(), getPreviousThitaMappings(), modelParameters.getWeights(), modelParameters.getThitas());

        Object predictedClass=getSelectedClassFromClassScores(predictionProbabilities);

        return new Prediction(predictedClass, predictionProbabilities);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        ModelParameters modelParameters = kb().getModelParameters();
        TrainingParameters trainingParameters = kb().getTrainingParameters();
                
        Map<Object, Double> weights = modelParameters.getWeights();
        Map<Object, Double> thitas = modelParameters.getThitas();
        
        //add classes in a sorted way (ordinal ascending order)
        //also we initialize the weights and thitas to zero
        TreeSet<Object> sortedClasses = new TreeSet<>();
        for(Record r : trainingData) { 
            Object theClass=r.getY();
            
            sortedClasses.add(theClass); 
            
            thitas.put(r.getY(), 0.0);
        }
        
        Object finalClass = sortedClasses.last();
        Set<Object> classesSet = modelParameters.getClasses();
        classesSet.addAll(sortedClasses);
        
        for(Object feature: trainingData.getXDataTypes().keySet()) {
            weights.put(feature, 0.0);
        }        
        thitas.put(finalClass, Double.POSITIVE_INFINITY);
        
        
        //mapping between the thita and the exact previous thita value
        Map<Object, Object> previousThitaMapping = getPreviousThitaMappings();
        
        double minError = Double.POSITIVE_INFINITY;
        
        double learningRate = trainingParameters.getLearningRate();
        int totalIterations = trainingParameters.getTotalIterations();
        DatabaseConnector dbc = kb().getDbc();
        for(int iteration=0;iteration<totalIterations;++iteration) {
            
            logger.debug("Iteration {}", iteration);
            
            Map<Object, Double> tmp_newThitas = dbc.getBigMap("tmp_newThitas", MapType.HASHMAP, StorageHint.IN_MEMORY, false, true);
            
            Map<Object, Double> tmp_newWeights = dbc.getBigMap("tmp_newWeights", MapType.HASHMAP, StorageHint.IN_MEMORY, false, true);
            
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
            dbc.dropBigMap("tmp_newWeights", tmp_newWeights);
            dbc.dropBigMap("tmp_newThitas", tmp_newThitas);
        }
    }
   
    /** {@inheritDoc} */
    @Override
    protected ValidationMetrics validateModel(Dataframe validationData) {
        ValidationMetrics validationMetrics = super.validateModel(validationData);
        
        
        //mapping between the thita and the exact previous thita value
        Map<Object, Object> previousThitaMapping = getPreviousThitaMappings();
        
        validationMetrics.setCountRSquare(validationMetrics.getAccuracy()); //CountRSquare is equal to Accuracy
        
        double SSE = calculateError(validationData, previousThitaMapping, kb().getModelParameters().getWeights(), kb().getModelParameters().getThitas());
        validationMetrics.setSSE(SSE);
        
        return validationMetrics;
    }

    private void batchGradientDescent(Dataframe trainingData, Map<Object, Object> previousThitaMapping, Map<Object, Double> newWeights, Map<Object, Double> newThitas, double learningRate) {
        //NOTE! This is not the stochastic gradient descent. It is the batch gradient descent optimized for speed (despite it looks more than the stochastic). 
        //Despite the fact that the loops are inverse, the function still changes the values of Thitas at the end of the function. We use the previous thitas 
        //to estimate the costs and only at the end we update the new thitas.
        ModelParameters modelParameters = kb().getModelParameters();

        double multiplier = -learningRate/modelParameters.getN(); 
        Map<Object, Double> weights = modelParameters.getWeights();
        Map<Object, Double> thitas = modelParameters.getThitas();
        
        streamExecutor.forEach(StreamMethods.stream(trainingData.stream(), isParallelized()), r -> { 
            Object rClass = r.getY();
            Object rPreviousClass = previousThitaMapping.get(rClass);
            
            //mind the fact that we use the previous weights and thitas to estimate the new ones! this is because the thitas must be updated simultaniously
            
            //first calculate the commonly used dot product between weights and x
            double xTw = xTw(r.getX(), weights);
            
            double gOfCurrent = g(xTw-thitas.get(rClass));
            double gOfPrevious = 0.0;
            if(rPreviousClass!=null) {
                gOfPrevious = g(thitas.get(rPreviousClass)-xTw);
            }
                    
            double dtG_multiplier = (gOfCurrent-gOfPrevious)*multiplier;
            
            synchronized(newWeights) {
                //update weights                
                for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                    Object column = entry.getKey();
                    Double xij = TypeInference.toDouble(entry.getValue());

                    newWeights.put(column, newWeights.get(column)+xij*dtG_multiplier);
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
    }
    
    private AssociativeArray hypothesisFunction(AssociativeArray x, Map<Object, Object> previousThitaMapping, Map<Object, Double> weights, Map<Object, Double> thitas) {
        AssociativeArray probabilities = new AssociativeArray();
    
            
        //first calculate the commonly used dot product between weights and x
        double xTw = xTw(x, weights);
        
        Set<Object> classesSet = kb().getModelParameters().getClasses();
        
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
        
        return error/kb().getModelParameters().getN();
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
        for(Object thita : kb().getModelParameters().getClasses()) {
            previousThitaMapping.put(thita, previousThita);
            previousThita = thita;
        }
        
        return previousThitaMapping;
    }
    
}
