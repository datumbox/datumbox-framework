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
package com.datumbox.framework.machinelearning.regression;

import com.datumbox.framework.machinelearning.common.bases.basemodels.BaseLinearRegression;
import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.utilities.TypeConversions;


import java.util.Map;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class NLMS extends BaseLinearRegression<NLMS.ModelParameters, NLMS.TrainingParameters, NLMS.ValidationMetrics> {
    /*
    * Normalised Least Mean Squares Algorithm
    * References:
    * http://cs229.stanford.edu/notes/cs229-notes1.pdf
    * http://www.holehouse.org/mlclass/04_Linear_Regression_with_multiple_variables.html
    * https://class.coursera.org/ml-003/lecture/index
    */
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    public static class ModelParameters extends BaseLinearRegression.ModelParameters {


        public ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
    } 

    
    public static class TrainingParameters extends BaseLinearRegression.TrainingParameters {         
        private int totalIterations=1000; 
        private double learningRate=0.1;

        public int getTotalIterations() {
            return totalIterations;
        }

        public void setTotalIterations(int totalIterations) {
            this.totalIterations = totalIterations;
        }

        public double getLearningRate() {
            return learningRate;
        }

        public void setLearningRate(double learningRate) {
            this.learningRate = learningRate;
        }

    } 
    
    
    public static class ValidationMetrics extends BaseLinearRegression.ValidationMetrics {
        
    }

    
    public NLMS(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, NLMS.ModelParameters.class, NLMS.TrainingParameters.class, NLMS.ValidationMetrics.class);
    }

    @Override
    protected void _fit(Dataset trainingData) {
        int n = trainingData.size();
        int d = trainingData.getColumnSize()+1;//plus one for the constant
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        //initialization
        modelParameters.setN(n);
        modelParameters.setD(d);
        
        
        Map<Object, Double> thitas = modelParameters.getThitas();
        
        //we initialize the thitas to zero for all features
        thitas.put(Dataset.constantColumnName, 0.0);
        for(Object feature : trainingData.getColumns().keySet()) {
            thitas.put(feature, 0.0);
        }
        
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();

        double minError = Double.POSITIVE_INFINITY;
        
        double learningRate = trainingParameters.getLearningRate();
        int totalIterations = trainingParameters.getTotalIterations();
        DatabaseConnector dbc = knowledgeBase.getDbc();
        for(int iteration=0;iteration<totalIterations;++iteration) {
            
            logger.debug("Iteration "+iteration);
            
            Map<Object, Double> tmp_newThitas = dbc.getBigMap("tmp_newThitas", true);
            
            tmp_newThitas.putAll(thitas);
            
            batchGradientDescent(trainingData, tmp_newThitas, learningRate);
            //stochasticGradientDescent(trainingData, newThitas, learningRate);
            
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
    protected void predictDataset(Dataset newData) {
        Map<Object, Double> thitas = knowledgeBase.getModelParameters().getThitas();
        
        for(Integer rId : newData) {
            Record r = newData.get(rId);
            double yPredicted = hypothesisFunction(r.getX(), thitas);
            newData.set(rId, new Record(r.getX(), r.getY(), yPredicted, r.getYPredictedProbabilities()));
        }
    }
    
    private void batchGradientDescent(Dataset trainingData, Map<Object, Double> newThitas, double learningRate) {
        //NOTE! This is not the stochastic gradient descent. It is the batch gradient descent optimized for speed (despite it looks more than the stochastic). 
        //Despite the fact that the loops are inverse, the function still changes the values of Thitas at the end of the function. We use the previous thitas 
        //to estimate the costs and only at the end we update the new thitas.
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        double multiplier = learningRate/modelParameters.getN();
        Map<Object, Double> thitas = modelParameters.getThitas();
        
        for(Integer rId : trainingData) { 
            Record r = trainingData.get(rId);
            //mind the fact that we use the previous thitas to estimate the new ones! this is because the thitas must be updated simultaniously
            double error = TypeConversions.toDouble(r.getY()) - hypothesisFunction(r.getX(), thitas);
            
            double errorMultiplier = multiplier*error;
            
            
            //update the weight of constant
            newThitas.put(Dataset.constantColumnName, newThitas.get(Dataset.constantColumnName)+errorMultiplier);

            //update the rest of the weights
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                
                Double thitaWeight = newThitas.get(feature);
                if(thitaWeight!=null) {//ensure that the feature is in the supported features
                    Double value = TypeConversions.toDouble(entry.getValue());
                    newThitas.put(feature, thitaWeight+errorMultiplier*value);
                }
            }
        }
    }
    
    private void stochasticGradientDescent(Dataset trainingData, Map<Object, Double> newThitas, double learningRate) {
        double multiplier = learningRate/knowledgeBase.getModelParameters().getN();
        
        for(Integer rId : trainingData) { 
            Record r = trainingData.get(rId);
            //mind the fact that we use the new thitas to estimate the cost! 
            double error = TypeConversions.toDouble(r.getY()) - hypothesisFunction(r.getX(), newThitas);
            
            double errorMultiplier = multiplier*error;
            
            
            //update the weight of constant
            newThitas.put(Dataset.constantColumnName, newThitas.get(Dataset.constantColumnName)+errorMultiplier);

            //update the rest of the weights
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                
                Double thitaWeight = newThitas.get(feature);
                if(thitaWeight!=null) {//ensure that the feature is in the supported features
                    Double value = TypeConversions.toDouble(entry.getValue());
                    newThitas.put(feature, thitaWeight+errorMultiplier*value);
                }
            }
        }
    }
    
    private double calculateError(Dataset trainingData, Map<Object, Double> thitas) {
        //The cost function as described on http://ufldl.stanford.edu/wiki/index.php/Softmax_Regression
        //It is optimized for speed to reduce the amount of loops
        double error=0.0;
        
        for(Integer rId : trainingData) { 
            Record r = trainingData.get(rId);
            double yPredicted = hypothesisFunction(r.getX(), thitas);
            trainingData.set(rId, new Record(r.getX(), r.getY(), yPredicted, r.getYPredictedProbabilities()));
            error+=Math.pow(TypeConversions.toDouble(r.getY()) -yPredicted, 2);
        }
        
        return error;
    }
    
    private double hypothesisFunction(AssociativeArray x, Map<Object, Double> thitas) {
        double sum = thitas.get(Dataset.constantColumnName);
        
        for(Map.Entry<Object, Object> entry : x.entrySet()) {
            Object feature = entry.getKey();
            
            Double thitaWeight = thitas.get(feature);
            if(thitaWeight!=null) {//ensure that the feature is in the supported features
                Double xj = TypeConversions.toDouble(entry.getValue());
                sum+=thitaWeight*xj;
            }
        }
        
        return sum;
    }
}
