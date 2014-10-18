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
package com.datumbox.framework.machinelearning.regression;

import com.datumbox.framework.machinelearning.common.bases.basemodels.BaseLinearRegression;
import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.configuration.GeneralConfiguration;
import com.datumbox.configuration.StorageConfiguration;
import java.util.Map;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
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
    
    public static final String SHORT_METHOD_NAME = "NLMS";
    
    public static class ModelParameters extends BaseLinearRegression.ModelParameters {

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

    
    public NLMS(String dbName) {
        super(dbName, NLMS.ModelParameters.class, NLMS.TrainingParameters.class, NLMS.ValidationMetrics.class);
    }
    
    @Override
    public final String shortMethodName() {
        return SHORT_METHOD_NAME;
    }

    @Override
    protected void estimateModelParameters(Dataset trainingData) {
        String tmpPrefix=StorageConfiguration.getTmpPrefix();
        
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
        BigDataStructureFactory bdsf = knowledgeBase.getBdsf();
        for(int iteration=0;iteration<totalIterations;++iteration) {
            
            if(GeneralConfiguration.DEBUG) {
                System.out.println("Iteration "+iteration);
            }
            
            Map<Object, Double> newThitas = bdsf.getMap(tmpPrefix+"newThitas", knowledgeBase.getMemoryConfiguration().getMapType(), knowledgeBase.getMemoryConfiguration().getLRUsize());
            
            newThitas.putAll(thitas);
            
            batchGradientDescent(trainingData, newThitas, learningRate);
            //stochasticGradientDescent(trainingData, newThitas, learningRate);
            
            double newError = calculateError(trainingData,newThitas);
            
            //bold driver
            if(newError>minError) {
                learningRate/=2.0;
            }
            else {
                learningRate*=1.05;
                minError=newError;
                
                //keep the new thitas
                thitas.clear();
                thitas.putAll(newThitas);
            }
            
            //Drop the temporary Collection
            bdsf.dropTable(tmpPrefix+"newThitas", newThitas);
        }
    }

    @Override
    protected void predictDataset(Dataset newData) {
        Map<Object, Double> thitas = knowledgeBase.getModelParameters().getThitas();
        
        for(Record r : newData) {
            double yPredicted = hypothesisFunction(r.getX(), thitas);
            r.setYPredicted(yPredicted);
        }
    }
    
    private void batchGradientDescent(Dataset trainingData, Map<Object, Double> newThitas, double learningRate) {
        //NOTE! This is not the stochastic gradient descent. It is the batch gradient descent optimized for speed (despite it looks more than the stochastic). 
        //Despite the fact that the loops are inverse, the function still changes the values of Thitas at the end of the function. We use the previous thitas 
        //to estimate the costs and only at the end we update the new thitas.
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        double multiplier = learningRate/modelParameters.getN();
        Map<Object, Double> thitas = modelParameters.getThitas();
        
        for(Record r : trainingData) {
            //mind the fact that we use the previous thitas to estimate the new ones! this is because the thitas must be updated simultaniously
            double error = Dataset.toDouble(r.getY()) - hypothesisFunction(r.getX(), thitas);
            
            double errorMultiplier = multiplier*error;
            
            
            //update the weight of constant
            newThitas.put(Dataset.constantColumnName, newThitas.get(Dataset.constantColumnName)+errorMultiplier);

            //update the rest of the weights
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                
                Double thitaWeight = newThitas.get(feature);
                if(thitaWeight!=null) {//ensure that the feature is in the supported features
                    Double value = Dataset.toDouble(entry.getValue());
                    newThitas.put(feature, thitaWeight+errorMultiplier*value);
                }
            }
        }
    }
    
    private void stochasticGradientDescent(Dataset trainingData, Map<Object, Double> newThitas, double learningRate) {
        double multiplier = learningRate/knowledgeBase.getModelParameters().getN();
        
        for(Record r : trainingData) {
            //mind the fact that we use the new thitas to estimate the cost! 
            double error = Dataset.toDouble(r.getY()) - hypothesisFunction(r.getX(), newThitas);
            
            double errorMultiplier = multiplier*error;
            
            
            //update the weight of constant
            newThitas.put(Dataset.constantColumnName, newThitas.get(Dataset.constantColumnName)+errorMultiplier);

            //update the rest of the weights
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                
                Double thitaWeight = newThitas.get(feature);
                if(thitaWeight!=null) {//ensure that the feature is in the supported features
                    Double value = Dataset.toDouble(entry.getValue());
                    newThitas.put(feature, thitaWeight+errorMultiplier*value);
                }
            }
        }
    }
    
    private double calculateError(Dataset trainingData, Map<Object, Double> thitas) {
        //The cost function as described on http://ufldl.stanford.edu/wiki/index.php/Softmax_Regression
        //It is optimized for speed to reduce the amount of loops
        double error=0.0;
        
        for(Record r : trainingData) {
            double yPredicted = hypothesisFunction(r.getX(), thitas);
            r.setYPredicted(yPredicted);
            error+=Math.pow(Dataset.toDouble(r.getY()) -yPredicted, 2);
        }
        
        return error;
    }
    
    private double hypothesisFunction(AssociativeArray x, Map<Object, Double> thitas) {
        double sum = thitas.get(Dataset.constantColumnName);
        
        for(Map.Entry<Object, Object> entry : x.entrySet()) {
            Object feature = entry.getKey();
            
            Double thitaWeight = thitas.get(feature);
            if(thitaWeight!=null) {//ensure that the feature is in the supported features
                Double xj = Dataset.toDouble(entry.getValue());
                sum+=thitaWeight*xj;
            }
        }
        
        return sum;
    }
}
