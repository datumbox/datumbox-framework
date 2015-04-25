/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
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
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclassifier;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.dataobjects.TypeInference;


import com.datumbox.framework.machinelearning.common.validation.SoftMaxRegressionValidation;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * The SoftMaxRegression implements the Multinomial Logistic Regression classifier.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class SoftMaxRegression extends BaseMLclassifier<SoftMaxRegression.ModelParameters, SoftMaxRegression.TrainingParameters, SoftMaxRegression.ValidationMetrics> {
    //References: http://www.cs.cmu.edu/afs/cs/user/aberger/www/html/tutorial/node3.html http://acl.ldc.upenn.edu/P/P02/P02-1002.pdf
    
    /**
     * The ModelParameters class stores the coefficients that were learned during
     * the training of the algorithm.
     */
    public static class ModelParameters extends BaseMLclassifier.ModelParameters {

        @BigMap
        private Map<List<Object>, Double> thitas; //the thita parameters of the model

        /**
         * Public constructor which accepts as argument the DatabaseConnector.
         * 
         * @param dbc 
         */
        public ModelParameters(DatabaseConnector dbc) {
            super(dbc);
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
        public void setThitas(Map<List<Object>, Double> thitas) {
            this.thitas = thitas;
        }
    } 
    
    /**
     * The TrainingParameters class stores the parameters that can be changed
     * before training the algorithm.
     */
    public static class TrainingParameters extends BaseMLclassifier.TrainingParameters {         
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
    
    /**
     * The ValidationMetrics class stores information about the performance of the
     * algorithm.
     */
    public static class ValidationMetrics extends BaseMLclassifier.ValidationMetrics {
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
    public SoftMaxRegression(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, SoftMaxRegression.ModelParameters.class, SoftMaxRegression.TrainingParameters.class, SoftMaxRegression.ValidationMetrics.class, new SoftMaxRegressionValidation());
    }
    
    @Override
    protected void predictDataset(Dataset newData) { 
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        Set<Object> classesSet = modelParameters.getClasses();
        Map<List<Object>, Double> thitas = modelParameters.getThitas();
        
        for(Integer rId : newData) {
            Record r = newData.get(rId);
            AssociativeArray predictionScores = new AssociativeArray();
            for(Object theClass : classesSet) {
                predictionScores.put(theClass, calculateClassScore(r.getX(), theClass, thitas));
            }
            
            Object theClass=getSelectedClassFromClassScores(predictionScores);
            
            Descriptives.normalizeExp(predictionScores);
            
            newData.set(rId, new Record(r.getX(), r.getY(), theClass, predictionScores));
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected void _fit(Dataset trainingData) {
        
        int n = trainingData.getRecordNumber();
        int d = trainingData.getVariableNumber();
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        //initialization
        modelParameters.setN(n);
        modelParameters.setD(d);
        
        
        Map<List<Object>, Double> thitas = modelParameters.getThitas();
        Set<Object> classesSet = modelParameters.getClasses();
        
        //first we need to find all the classes
        for(Integer rId : trainingData) { 
            Record r = trainingData.get(rId);
            Object theClass=r.getY();
            
            classesSet.add(theClass); 
        }

        int c = classesSet.size();
        modelParameters.setC(c);
        
        //we initialize the thitas to zero for all features and all classes compinations
        for(Object theClass : classesSet) {
            thitas.put(Arrays.<Object>asList(Dataset.constantColumnName, theClass), 0.0);
            
            for(Integer rId : trainingData) { 
                Record r = trainingData.get(rId);
                for(Object feature : r.getX().keySet()) {
                    thitas.put(Arrays.<Object>asList(feature, theClass), 0.0);
                }
            }
        }
        
        double minError = Double.POSITIVE_INFINITY;
        
        double learningRate = trainingParameters.getLearningRate();
        int totalIterations = trainingParameters.getTotalIterations();
        DatabaseConnector dbc = knowledgeBase.getDbc();
        for(int iteration=0;iteration<totalIterations;++iteration) {
            
            logger.debug("Iteration {}", iteration);
            
            Map<List<Object>, Double> tmp_newThitas = dbc.getBigMap("tmp_newThitas", true);
            
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
    
    @Override
    protected SoftMaxRegression.ValidationMetrics validateModel(Dataset validationData) {
        SoftMaxRegression.ValidationMetrics validationMetrics = super.validateModel(validationData);
        
        validationMetrics.setCountRSquare(validationMetrics.getAccuracy()); //CountRSquare is equal to Accuracy
        
        double SSE = calculateError(validationData,knowledgeBase.getModelParameters().getThitas());
        validationMetrics.setSSE(SSE);
        
        return validationMetrics;
    }

    private void batchGradientDescent(Dataset trainingData, Map<List<Object>, Double> newThitas, double learningRate) {
        //NOTE! This is not the stochastic gradient descent. It is the batch gradient descent optimized for speed (despite it looks more than the stochastic). 
        //Despite the fact that the loops are inverse, the function still changes the values of Thitas at the end of the function. We use the previous thitas 
        //to estimate the costs and only at the end we update the new thitas.
        ModelParameters modelParameters = knowledgeBase.getModelParameters();

        double multiplier = learningRate/modelParameters.getN();
        Map<List<Object>, Double> thitas = modelParameters.getThitas();
        Set<Object> classesSet = modelParameters.getClasses();
        
        for(Integer rId : trainingData) { 
            Record r = trainingData.get(rId);
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
                
                
                //update the weight of constant
                List<Object> featureClassTuple = Arrays.<Object>asList(Dataset.constantColumnName, theClass);
                newThitas.put(featureClassTuple, newThitas.get(featureClassTuple)+errorMultiplier);
                
                //update the rest of the weights
                
                for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                    Double value = TypeInference.toDouble(entry.getValue());

                    Object feature = entry.getKey();
                    featureClassTuple = Arrays.<Object>asList(feature, theClass);

                    Double thitaWeight = newThitas.get(featureClassTuple);
                    if(thitaWeight!=null) {//ensure that the feature is in the dictionary
                        newThitas.put(featureClassTuple, thitaWeight+errorMultiplier*value);
                    }
                }
            }
        }
        
    }
    
    private Double calculateClassScore(AssociativeArray x, Object theClass, Map<List<Object>, Double> thitas) {
        double score = thitas.get(Arrays.<Object>asList(Dataset.constantColumnName, theClass));
        
        for(Map.Entry<Object, Object> entry : x.entrySet()) {
            Double value = TypeInference.toDouble(entry.getValue());
            
            Object feature = entry.getKey();
            List<Object> featureClassTuple = Arrays.<Object>asList(feature, theClass);
            
            Double thitaWeight = thitas.get(featureClassTuple);
            if(thitaWeight!=null) {//ensure that the feature is in the dictionary
                score+=thitaWeight*value;
            }
        }
        
        return score;
    }
    
    private double calculateError(Dataset trainingData, Map<List<Object>, Double> thitas) {
        //The cost function as described on http://ufldl.stanford.edu/wiki/index.php/Softmax_Regression
        //It is optimized for speed to reduce the amount of loops
        double error=0.0;
        
        for(Integer rId : trainingData) { 
            Record r = trainingData.get(rId);
            AssociativeArray classProbabilities = hypothesisFunction(r.getX(), thitas);
            Double score = classProbabilities.getDouble(r.getY());
            error+=Math.log(score); //no need to loop through the categories. Just grab the one that we are interested in
        }
        
        return -error/knowledgeBase.getModelParameters().getN();
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
