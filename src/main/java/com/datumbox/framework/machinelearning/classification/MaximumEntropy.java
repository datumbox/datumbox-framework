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
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mongodb.morphia.annotations.Transient;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class MaximumEntropy extends BaseMLclassifier<MaximumEntropy.ModelParameters, MaximumEntropy.TrainingParameters, MaximumEntropy.ValidationMetrics> {
    //References: http://www.cs.cmu.edu/afs/cs/user/aberger/www/html/tutorial/node3.html http://acl.ldc.upenn.edu/P/P02/P02-1002.pdf
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    public static final String SHORT_METHOD_NAME = "MaxEn";
    
    @Override
    public final String shortMethodName() {
        return SHORT_METHOD_NAME;
    }
    
    public static class ModelParameters extends BaseMLclassifier.ModelParameters {

        /**
         * Lambda weights
         */
        @BigDataStructureMarker
        @Transient
        private Map<List<Object>, Double> lambdas; //the lambda parameters of the model

        
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            super.bigDataStructureInitializer(bdsf, memoryConfiguration);
            
            BigDataStructureFactory.MapType mapType = memoryConfiguration.getMapType();
            int LRUsize = memoryConfiguration.getLRUsize();
            
            lambdas = bdsf.getMap("lambdas", mapType, LRUsize);
        }
        
        public Map<List<Object>, Double> getLambdas() {
            return lambdas;
        }

        public void setLambdas(Map<List<Object>, Double> lambdas) {
            this.lambdas = lambdas;
        }
    } 

    
    public static class TrainingParameters extends BaseMLclassifier.TrainingParameters {         
        private int totalIterations=100; 

        public int getTotalIterations() {
            return totalIterations;
        }

        public void setTotalIterations(int totalIterations) {
            this.totalIterations = totalIterations;
        }

    } 
    
    
    public static class ValidationMetrics extends BaseMLclassifier.ValidationMetrics {

    }
        

    
    public MaximumEntropy(String dbName) {
        super(dbName, MaximumEntropy.ModelParameters.class, MaximumEntropy.TrainingParameters.class, MaximumEntropy.ValidationMetrics.class);
    }
    
    @Override
    protected void predictDataset(Dataset newData) { 
        Set<Object> classesSet = knowledgeBase.getModelParameters().getClasses();
                
        for(Record r : newData) {
            AssociativeArray predictionScores = new AssociativeArray();
            for(Object theClass : classesSet) {
                predictionScores.put(theClass, calculateClassScore(r.getX(),theClass));
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
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        String tmpPrefix=StorageConfiguration.getTmpPrefix();
        
        int n = trainingData.size();
        int d = trainingData.getColumnSize();
        
        
        //initialization
        modelParameters.setN(n);
        modelParameters.setD(d);
        
        
        Map<List<Object>, Double> lambdas = modelParameters.getLambdas();
        Set<Object> classesSet = modelParameters.getClasses();
        
        //first we need to find all the classes
        for(Record r : trainingData) {
            Object theClass=r.getY();
            
            classesSet.add(theClass); 
        }

        int c = classesSet.size();
        modelParameters.setC(c);
        
        
        //create a temporary map for the observed probabilities in training set
        BigDataStructureFactory bdsf = knowledgeBase.getBdsf();
        Map<List<Object>, Double> EpFj_observed = bdsf.getMap(tmpPrefix+"EpFj_observed", knowledgeBase.getMemoryConfiguration().getMapType(), knowledgeBase.getMemoryConfiguration().getLRUsize());
        
        double Cmax = 0.0; //max number of activated features in the dataset. Required from the IIS algorithm
        double increment = 1.0/n; //this is done for speed reasons. We don't want to repeat the same division over and over
        
        //then we calculate the observed probabilities in training set
        for(Record r : trainingData) {
            int activeFeatures=0; //counts the number of non-zero (active) features of the record
            
            //store the occurrances of the features
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Double occurrences=Dataset.toDouble(entry.getValue());
                
                if(occurrences==null || occurrences==0.0) {
                    continue;
                }
                Object feature = entry.getKey();
                
                //loop through all the classes to ensure that the feature-class combination is initialized for ALL the classes
                //in a previous implementation I did not loop through all the classes and used only the one of the record.
                //THIS IS WRONG. By not assigning 0 scores to the rest of the classes for this feature, we don't penalties for the non occurrance. 
                //The math REQUIRE us to have scores for all classes to make the probabilities comparable.
                for(Object theClass : classesSet) {
                    List<Object> featureClassTuple = Arrays.<Object>asList(feature, theClass);
                    Double previousValue = EpFj_observed.get(featureClassTuple);
                    if(previousValue==null) {
                        previousValue=0.0;
                        EpFj_observed.put(featureClassTuple, 0.0);
                        lambdas.put(featureClassTuple, 0.0); //initialize lambda
                    }
                    
                    //find the class of this particular example
                    if(theClass.equals(r.getY())) {
                        //update the statistics of the feature
                        EpFj_observed.put(featureClassTuple, previousValue + increment);
                    }
                } 
                
                ++activeFeatures;
            }
            

            //NOTE: we try to find the Cmax the maximum number of active featured in the training dataset. The total number of features in original IIS were required to be constant. NEVERTHELESS as it is mentioned here http://acl.ldc.upenn.edu/P/P02/P02-1002.pdf the Cmax only needs to constrain the number of features and not necessarily to be equal to them.
            //NOTE2: In this implementation the Cmax is equal to the maximum number of features that were found in the training dataset. We don't need to go through all the classes to find the Cmax. This is because of the way that the features are selected.
            if(activeFeatures>Cmax) {
                Cmax=activeFeatures;
            }
            
        }
        
        
        //IIS algorithm
        IIS(trainingData, EpFj_observed, Cmax);
        
        
        //Drop the temporary Collection
        bdsf.dropTable(tmpPrefix+"EpFj_observed", EpFj_observed);
    }
    

    private void IIS(Dataset trainingData, Map<List<Object>, Double> EpFj_observed, double Cmax) {
        String tmpPrefix=StorageConfiguration.getTmpPrefix();
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();

        int totalIterations = knowledgeBase.getTrainingParameters().getTotalIterations();
        Set<Object> classesSet = modelParameters.getClasses();
        Map<List<Object>, Double> lambdas = modelParameters.getLambdas();
        
        int n = modelParameters.getN();
        
        BigDataStructureFactory bdsf = knowledgeBase.getBdsf();
        for(int iteration=0;iteration<totalIterations;++iteration) {
            
            if(GeneralConfiguration.DEBUG) {
                System.out.println("Iteration "+iteration);
            }
            
            Map<List<Object>, Double> EpFj_model = bdsf.getMap(tmpPrefix+"EpFj_model", knowledgeBase.getMemoryConfiguration().getMapType(), knowledgeBase.getMemoryConfiguration().getLRUsize());
            Collection<List<Object>> infiniteLambdaWeights = new ArrayList<>();//bdsf.getCollection(tmpPrefix+"infiniteLambdaWeights", knowledgeBase.getMemoryConfiguration().getCollectionType());
            
            //initialize the model probabilities with 0. We will start estimating them piece by piece
            for(Map.Entry<List<Object>, Double> featureClassCounts : EpFj_observed.entrySet()) {
                List<Object> tp = featureClassCounts.getKey();
                EpFj_model.put(tp, 0.0);
            }
            
            //calculate the model probabilities
            for(Record r : trainingData) {
                
                AssociativeArray classScores = new AssociativeArray();
                
                for(Object theClass : classesSet) {
                    double score = calculateClassScore(r.getX(), theClass);
                    classScores.put(theClass, score);
                }
                
                Descriptives.normalizeExp(classScores);
                
                //The below seems a bit strange but this is actually how the model probabilities are estimated. It is the average probability across all documents for a specific characteristic. The code is optimized for speed and this makes it less readable
                for(Map.Entry<Object, Object> entry : classScores.entrySet()) {
                    Object theClass = entry.getKey();
                    Double score = Dataset.toDouble(entry.getValue());
                    
                    double probabilityFraction = score/n;
                    
                    for(Map.Entry<Object, Object> entry2 : r.getX().entrySet()) {
                        Double occurrences=Dataset.toDouble(entry2.getValue());
                        
                        if(occurrences==null || occurrences==0.0) {
                            continue;
                        }
                        Object feature = entry2.getKey();
                        List<Object> featureClassTuple = Arrays.<Object>asList(feature, theClass);
                        
                        EpFj_model.put(featureClassTuple, EpFj_model.get(featureClassTuple) + probabilityFraction);
                    }
                }
                
                classScores=null;
            }
            
            Double minimumNonInfiniteLambdaWeight = null;
            Double maximumNonInfiniteLambdaWeight = null;
            //Now we have the model probabilities. We will use it to estimate the Deltas and finally update the lamdas
            for(Map.Entry<List<Object>, Double> featureClassCounts : EpFj_model.entrySet()) {
                List<Object> tp = featureClassCounts.getKey();
                //Object feature = tp.getKey1();
                //Object theClass = tp.getKey2();
                
                Double EpFj_observed_value = EpFj_observed.get(tp);
                Double EpFj_model_value = featureClassCounts.getValue();    
                
                
                if(Math.abs(EpFj_observed_value-EpFj_model_value)<=1e-8) {
                    //if those two are equal or both zero (pay attention to this, we try to handle the zero cases here)
                    //then continue. The two are equal so no change on weights is required.
                    continue;
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
                    
                    lambdas.put(tp, Double.NEGATIVE_INFINITY); //this will be revised, see comment above
                    infiniteLambdaWeights.add(tp);
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
                    
                    
                    lambdas.put(tp, Double.POSITIVE_INFINITY); //this will be revised, see comment above
                    infiniteLambdaWeights.add(tp);
                }
                else {
                    //the formula below can't produce a +inf or -inf value
                    double deltaJ = Math.log(EpFj_observed_value/EpFj_model_value)/Cmax;
                    double newValue = lambdas.get(tp) + deltaJ;
                    lambdas.put(tp, newValue); //update lamdas by delta
                    
                    if(minimumNonInfiniteLambdaWeight==null || newValue<minimumNonInfiniteLambdaWeight) {
                        minimumNonInfiniteLambdaWeight=newValue;
                    }
                    if(maximumNonInfiniteLambdaWeight==null || newValue>maximumNonInfiniteLambdaWeight) {
                        maximumNonInfiniteLambdaWeight=newValue;
                    }
                }
                
                
            }
            
            
            
            if(!infiniteLambdaWeights.isEmpty()) {
                
                for(List<Object> featureClass : infiniteLambdaWeights) {
                    Double value = lambdas.get(featureClass);
                    
                    if(value==Double.NEGATIVE_INFINITY) {
                        lambdas.put(featureClass, minimumNonInfiniteLambdaWeight);
                    }
                    else if(value==Double.POSITIVE_INFINITY) {
                        lambdas.put(featureClass, maximumNonInfiniteLambdaWeight);
                    }
                    else { //this should never happen!
                        lambdas.put(featureClass, 0.0);
                    }
                }
            }
            
            
            
            //Drop the temporary Collection
            bdsf.dropTable(tmpPrefix+"EpFj_model", EpFj_model);
            infiniteLambdaWeights = null; //bdsf.dropTable(tmpPrefix+"infiniteLambdaWeights", infiniteLambdaWeights);
        }
        
    }
    

    private Double calculateClassScore(AssociativeArray x, Object theClass) {
        double score = 0;
        
        Map<List<Object>, Double> lambdas = knowledgeBase.getModelParameters().getLambdas();
        
        for(Map.Entry<Object, Object> entry : x.entrySet()) {
            Double value = Dataset.toDouble(entry.getValue());
            if(value==null || value==0.0) {
                continue; //ignore the feature if it has no value
            }
            //note that we will not use the value any more. MaxEntropy classifier is binarized.
            
            Object feature = entry.getKey();
            List<Object> featureClassTuple = Arrays.<Object>asList(feature, theClass);
            
            Double lambdaWeight = lambdas.get(featureClassTuple);
            if(lambdaWeight!=null) {//ensure that the feature is in the dictionary
                score+=lambdaWeight;
            }
        }
        
        return score;
    }
}
