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
package com.datumbox.framework.machinelearning.classification;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclassifier;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureMarker;
import com.datumbox.configuration.GeneralConfiguration;
import com.datumbox.configuration.StorageConfiguration;
import com.datumbox.framework.machinelearning.common.validation.SoftMaxRegressionValidation;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mongodb.morphia.annotations.Transient;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class SoftMaxRegression extends BaseMLclassifier<SoftMaxRegression.ModelParameters, SoftMaxRegression.TrainingParameters, SoftMaxRegression.ValidationMetrics> {
    //References: http://www.cs.cmu.edu/afs/cs/user/aberger/www/html/tutorial/node3.html http://acl.ldc.upenn.edu/P/P02/P02-1002.pdf
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    public static final String SHORT_METHOD_NAME = "SMReg";
    
    public static class ModelParameters extends BaseMLclassifier.ModelParameters {

        /**
         * Thita weights
         */
        @BigDataStructureMarker
        @Transient
        private Map<List<Object>, Double> thitas; //the thita parameters of the model

        
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            super.bigDataStructureInitializer(bdsf, memoryConfiguration);
            
            BigDataStructureFactory.MapType mapType = memoryConfiguration.getMapType();
            int LRUsize = memoryConfiguration.getLRUsize();
            
            thitas = bdsf.getMap("thitas", mapType, LRUsize);
        }
        
        public Map<List<Object>, Double> getThitas() {
            return thitas;
        }

        public void setThitas(Map<List<Object>, Double> thitas) {
            this.thitas = thitas;
        }
    } 

    
    public static class TrainingParameters extends BaseMLclassifier.TrainingParameters {         
        private int totalIterations=100; 
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
    
    
    public static class ValidationMetrics extends BaseMLclassifier.ValidationMetrics {
        private double SSE = 0.0; 
        private double CountRSquare = 0.0; // http://www.ats.ucla.edu/stat/mult_pkg/faq/general/Psuedo_RSquareds.htm
        
        public double getSSE() {
            return SSE;
        }

        public void setSSE(double SSE) {
            this.SSE = SSE;
        }

        public double getCountRSquare() {
            return CountRSquare;
        }

        public void setCountRSquare(double CountRSquare) {
            this.CountRSquare = CountRSquare;
        }
        
    }
    
    public SoftMaxRegression(String dbName) {
        super(dbName, SoftMaxRegression.ModelParameters.class, SoftMaxRegression.TrainingParameters.class, SoftMaxRegression.ValidationMetrics.class, new SoftMaxRegressionValidation());
    }
    
    @Override
    public final String shortMethodName() {
        return SHORT_METHOD_NAME;
    }
    
    @Override
    protected void predictDataset(Dataset newData) { 
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        Set<Object> classesSet = modelParameters.getClasses();
        Map<List<Object>, Double> thitas = modelParameters.getThitas();
        
        for(Record r : newData) {
            AssociativeArray predictionScores = new AssociativeArray();
            for(Object theClass : classesSet) {
                predictionScores.put(theClass, calculateClassScore(r.getX(), theClass, thitas));
            }
            
            Object theClass=getSelectedClassFromClassScores(predictionScores);
            
            Descriptives.normalizeExp(predictionScores);
            
            r.setYPredicted(theClass);
            r.setYPredictedProbabilities(predictionScores);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected void estimateModelParameters(Dataset trainingData) {
        String tmpPrefix=StorageConfiguration.getTmpPrefix();
        
        int n = trainingData.size();
        int d = trainingData.getColumnSize()+1;//plus one for the constant
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        //initialization
        modelParameters.setN(n);
        modelParameters.setD(d);
        
        
        Map<List<Object>, Double> thitas = modelParameters.getThitas();
        Set<Object> classesSet = modelParameters.getClasses();
        
        //first we need to find all the classes
        for(Record r : trainingData) {
            Object theClass=r.getY();
            
            classesSet.add(theClass); 
        }

        int c = classesSet.size();
        modelParameters.setC(c);
        
        //we initialize the thitas to zero for all features and all classes compinations
        for(Object theClass : classesSet) {
            thitas.put(Arrays.<Object>asList(Dataset.constantColumnName, theClass), 0.0);
            
            for(Record r : trainingData) {
                for(Object feature : r.getX().keySet()) {
                    thitas.put(Arrays.<Object>asList(feature, theClass), 0.0);
                }
            }
        }
        
        double minError = Double.POSITIVE_INFINITY;
        
        double learningRate = trainingParameters.getLearningRate();
        int totalIterations = trainingParameters.getTotalIterations();
        BigDataStructureFactory bdsf = knowledgeBase.getBdsf();
        for(int iteration=0;iteration<totalIterations;++iteration) {
            
            if(GeneralConfiguration.DEBUG) {
                System.out.println("Iteration "+iteration);
            }
            
            Map<List<Object>, Double> newThitas = bdsf.getMap(tmpPrefix+"newThitas", knowledgeBase.getMemoryConfiguration().getMapType(), knowledgeBase.getMemoryConfiguration().getLRUsize());
            
            newThitas.putAll(thitas);
            batchGradientDescent(trainingData, newThitas, learningRate);
            
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
        
        for(Record r : trainingData) {
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
                    Double value = Dataset.toDouble(entry.getValue());

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
            Double value = Dataset.toDouble(entry.getValue());
            
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
        
        for(Record r : trainingData) {
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
