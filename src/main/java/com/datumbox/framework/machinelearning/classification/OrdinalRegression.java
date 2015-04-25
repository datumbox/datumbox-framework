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
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.dataobjects.TypeInference;


import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclassifier;
import com.datumbox.framework.machinelearning.common.validation.OrdinalRegressionValidation;
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
public class OrdinalRegression extends BaseMLclassifier<OrdinalRegression.ModelParameters, OrdinalRegression.TrainingParameters, OrdinalRegression.ValidationMetrics> {
    
    /**
     * The ModelParameters class stores the coefficients that were learned during
     * the training of the algorithm.
     */
    public static class ModelParameters extends BaseMLclassifier.ModelParameters {

        @BigMap
        private Map<Object, Double> weights; //the W parameters of the model

        /**
         * Right-side limits of the class on the ordinal regression line. 
         */
        @BigMap
        private Map<Object, Double> thitas; 

        /**
         * Public constructor which accepts as argument the DatabaseConnector.
         * 
         * @param dbc 
         */
        public ModelParameters(DatabaseConnector dbc) {
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
        public void setWeights(Map<Object, Double> weights) {
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
        public void setThitas(Map<Object, Double> thitas) {
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
    public OrdinalRegression(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, OrdinalRegression.ModelParameters.class, OrdinalRegression.TrainingParameters.class, OrdinalRegression.ValidationMetrics.class, new OrdinalRegressionValidation());
    }
    
    @Override
    protected void predictDataset(Dataset newData) { 
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        Map<Object, Double> weights = modelParameters.getWeights();
        Map<Object, Double> thitas = modelParameters.getThitas();
        
        
        //mapping between the thita and the exact previous thita value
        Map<Object, Object> previousThitaMapping = getPreviousThitaMappings();
        
        for(Integer rId : newData) {
            Record r = newData.get(rId);
            AssociativeArray predictionProbabilities = hypothesisFunction(r.getX(), previousThitaMapping, weights, thitas);
            
            Object theClass=getSelectedClassFromClassScores(predictionProbabilities);
            
            newData.set(rId, new Record(r.getX(), r.getY(), theClass, predictionProbabilities));
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
        
        Map<Object, Double> weights = modelParameters.getWeights();
        Map<Object, Double> thitas = modelParameters.getThitas();
        
        //add classes in a sorted way (ordinal ascending order)
        Set<Object> sortedClasses = new TreeSet<>();
        for(Integer rId : trainingData) { 
            Record r = trainingData.get(rId);
            Object theClass=r.getY();
            
            sortedClasses.add(theClass); 
        }
        Set<Object> classesSet = modelParameters.getClasses();
        classesSet.addAll(sortedClasses);
        
        int c = classesSet.size();
        modelParameters.setC(c);
        
        //we initialize the weights and thitas to zero
        for(Object feature: trainingData.getXDataTypes().keySet()) {
            weights.put(feature, 0.0);
        }
        for(Integer rId : trainingData) { 
            Record r = trainingData.get(rId);
            thitas.put(r.getY(), 0.0);
        }
        
        Object finalClass = null;
        for(Object theClass : classesSet) {
            finalClass=theClass;
        }
        thitas.put(finalClass, Double.POSITIVE_INFINITY);
        
        
        //mapping between the thita and the exact previous thita value
        Map<Object, Object> previousThitaMapping = getPreviousThitaMappings();
        
        double minError = Double.POSITIVE_INFINITY;
        
        double learningRate = trainingParameters.getLearningRate();
        int totalIterations = trainingParameters.getTotalIterations();
        DatabaseConnector dbc = knowledgeBase.getDbc();
        for(int iteration=0;iteration<totalIterations;++iteration) {
            
            logger.debug("Iteration {}", iteration);
            
            Map<Object, Double> tmp_newThitas = dbc.getBigMap("tmp_newThitas", true);
            
            Map<Object, Double> tmp_newWeights = dbc.getBigMap("tmp_newWeights", true);
            
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
   
    @Override
    protected ValidationMetrics validateModel(Dataset validationData) {
        ValidationMetrics validationMetrics = super.validateModel(validationData);
        
        
        //mapping between the thita and the exact previous thita value
        Map<Object, Object> previousThitaMapping = getPreviousThitaMappings();
        
        validationMetrics.setCountRSquare(validationMetrics.getAccuracy()); //CountRSquare is equal to Accuracy
        
        double SSE = calculateError(validationData, previousThitaMapping, knowledgeBase.getModelParameters().getWeights(), knowledgeBase.getModelParameters().getThitas());
        validationMetrics.setSSE(SSE);
        
        return validationMetrics;
    }

    private void batchGradientDescent(Dataset trainingData, Map<Object, Object> previousThitaMapping, Map<Object, Double> newWeights, Map<Object, Double> newThitas, double learningRate) {
        //NOTE! This is not the stochastic gradient descent. It is the batch gradient descent optimized for speed (despite it looks more than the stochastic). 
        //Despite the fact that the loops are inverse, the function still changes the values of Thitas at the end of the function. We use the previous thitas 
        //to estimate the costs and only at the end we update the new thitas.
        ModelParameters modelParameters = knowledgeBase.getModelParameters();

        double multiplier = -learningRate/modelParameters.getN(); 
        Map<Object, Double> weights = modelParameters.getWeights();
        Map<Object, Double> thitas = modelParameters.getThitas();
        
        for(Integer rId : trainingData) { 
            Record r = trainingData.get(rId);
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
                    
            
            
            //update weights                
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object column = entry.getKey();
                Double xij = TypeInference.toDouble(entry.getValue());
                
                newWeights.put(column, newWeights.get(column)+multiplier*xij*(gOfCurrent-gOfPrevious));
            }
            
            
            //update thitas
            newThitas.put(rClass, newThitas.get(rClass)+multiplier*(-gOfCurrent));
            if(rPreviousClass!=null) {
                newThitas.put(rPreviousClass, newThitas.get(rPreviousClass)+multiplier*gOfPrevious);
            }
        }
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
    
    private double calculateError(Dataset trainingData, Map<Object, Object> previousThitaMapping, Map<Object, Double> weights, Map<Object, Double> thitas) {
        double error=0.0;
        
        for(Integer rId : trainingData) { 
            Record r = trainingData.get(rId);
            double xTw = xTw(r.getX(), weights);
            
            Object theClass = r.getY();
            Object previousClass = previousThitaMapping.get(theClass);
            
            
            if(previousClass!=null) {
                error += h(thitas.get(previousClass)-xTw);
            }
            
            error += h(xTw-thitas.get(theClass));
        }
        
        return error/knowledgeBase.getModelParameters().getN();
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
