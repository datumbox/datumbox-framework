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
package com.datumbox.framework.core.machinelearning.common.abstracts.algorithms;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.*;
import com.datumbox.framework.common.interfaces.Trainable;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector.MapType;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector.StorageHint;
import com.datumbox.framework.common.utilities.MapMethods;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelers.AbstractClassifier;
import com.datumbox.framework.core.machinelearning.validators.ClassifierValidator;
import com.datumbox.framework.core.machinelearning.ensemblelearning.FixedCombinationRules;
import com.datumbox.framework.core.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.core.statistics.sampling.SimpleRandomSampling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for Adaboost and BoostrapAgregating.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class AbstractBoostingBagging<MP extends AbstractBoostingBagging.AbstractModelParameters, TP extends AbstractBoostingBagging.AbstractTrainingParameters> extends AbstractClassifier<MP, TP> {

    private static final String DB_INDICATOR = "Cmp";
    private static final int MAX_NUM_OF_RETRIES = 2;
    
    /** {@inheritDoc} */
    public static abstract class AbstractModelParameters extends AbstractClassifier.AbstractModelParameters {
        
        private List<Double> weakClassifierWeights = new ArrayList<>();

        /** 
         * @param dbc
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(DatabaseConnector)
         */
        protected AbstractModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
        /**
         * Getter for the weights of the weak classifiers.
         * 
         * @return 
         */
        public List<Double> getWeakClassifierWeights() {
            return weakClassifierWeights;
        }
        
        /**
         * Setter for the weights of the weak classifiers.
         * 
         * @param weakClassifierWeights 
         */
        protected void setWeakClassifierWeights(List<Double> weakClassifierWeights) {
            this.weakClassifierWeights = weakClassifierWeights;
        }
        
    } 

    /** {@inheritDoc} */
    public static abstract class AbstractTrainingParameters extends AbstractClassifier.AbstractTrainingParameters {      
        
        //primitives/wrappers
        private int maxWeakClassifiers = 5; //the total number of classifiers that Bagging will initialize
        
        //Classes
        private Class<? extends AbstractClassifier> weakClassifierClass; //the class name of weak classifiear
        
        //Parameter Objects
        private AbstractClassifier.AbstractTrainingParameters weakClassifierTrainingParameters; //the parameters of the weak classifier
        
        /**
         * Getter for the maximum number of weak classifiers.
         * 
         * @return 
         */
        public int getMaxWeakClassifiers() {
            return maxWeakClassifiers;
        }
        
        /**
         * Setter for the maximum number of weak classifiers.
         * 
         * @param maxWeakClassifiers 
         */
        public void setMaxWeakClassifiers(int maxWeakClassifiers) {
            this.maxWeakClassifiers = maxWeakClassifiers;
        }
        
        /**
         * Getter for the Java class of the weak classifier.
         * 
         * @return 
         */
        public Class<? extends AbstractClassifier> getWeakClassifierClass() {
            return weakClassifierClass;
        }
        
        /**
         * Setter for the Java class of the weak classifier.
         * 
         * @param weakClassifierClass 
         */
        public void setWeakClassifierClass(Class<? extends AbstractClassifier> weakClassifierClass) {
            this.weakClassifierClass = weakClassifierClass;
        }
        
        /**
         * Getter for the Training Parameters of the weak classifier.
         * 
         * @return 
         */
        public AbstractClassifier.AbstractTrainingParameters getWeakClassifierTrainingParameters() {
            return weakClassifierTrainingParameters;
        }

        /**
         * Setter for the Training Parameters of the weak classifier.
         * 
         * @param weakClassifierTrainingParameters 
         */
        public void setWeakClassifierTrainingParameters(AbstractClassifier.AbstractTrainingParameters weakClassifierTrainingParameters) {
            this.weakClassifierTrainingParameters = weakClassifierTrainingParameters;
        }
        
    } 

    /**
     * @param dbName
     * @param conf
     * @param mpClass
     * @param tpClass
     * @see AbstractTrainer#AbstractTrainer(java.lang.String, Configuration, java.lang.Class, java.lang.Class)
     */
    protected AbstractBoostingBagging(String dbName, Configuration conf, Class<MP> mpClass, Class<TP> tpClass) {
        super(dbName, conf, mpClass, tpClass);
    } 
    
    /** {@inheritDoc} */
    @Override
    protected void _predictDataset(Dataframe newData) {
        Class<? extends AbstractClassifier> weakClassifierClass = knowledgeBase.getTrainingParameters().getWeakClassifierClass();
        List<Double> weakClassifierWeights = knowledgeBase.getModelParameters().getWeakClassifierWeights();
        
        //create a temporary map for the observed probabilities in training set
        DatabaseConnector dbc = knowledgeBase.getDbc();
        Map<Object, DataTable2D> tmp_recordDecisions = dbc.getBigMap("tmp_recordDecisions", Object.class, DataTable2D.class, MapType.HASHMAP, StorageHint.IN_DISK, false, true);
        
        //initialize array of recordDecisions
        for(Integer rId : newData.index()) {
            tmp_recordDecisions.put(rId, new DataTable2D());
        }
        
        //using the weak classifiers
        AssociativeArray classifierWeightsArray = new AssociativeArray();
        int totalWeakClassifiers = weakClassifierWeights.size();
        for(int t=0;t<totalWeakClassifiers;++t) {
            try (AbstractClassifier mlclassifier = Trainable.<AbstractClassifier>newInstance(
                    (Class<AbstractClassifier>)weakClassifierClass, 
                    dbName+knowledgeBase.getConf().getDbConfig().getDBnameSeparator()+DB_INDICATOR+String.valueOf(t),
                    knowledgeBase.getConf())) {
                mlclassifier.predict(newData);
            }
            
            classifierWeightsArray.put(t, weakClassifierWeights.get(t));
            
            for(Map.Entry<Integer, Record> e : newData.entries()) {
                Integer rId = e.getKey();
                Record r = e.getValue();
                AssociativeArray classProbabilities = r.getYPredictedProbabilities();
                
                DataTable2D rDecisions = tmp_recordDecisions.get(rId);
                rDecisions.put(t, classProbabilities);
                
                tmp_recordDecisions.put(rId, rDecisions); //WARNING: Do not remove this! We must put it back to the Map to persist it on Disk-backed maps
            }
        }
        
        //for each record find the combined classification by majority vote
        for(Map.Entry<Integer, Record> e : newData.entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            
            AssociativeArray combinedClassVotes = FixedCombinationRules.weightedAverage(tmp_recordDecisions.get(rId), classifierWeightsArray);
            Descriptives.normalize(combinedClassVotes);
            
            newData._unsafe_set(rId, new Record(r.getX(), r.getY(), MapMethods.selectMaxKeyValue(combinedClassVotes).getKey(), combinedClassVotes));
        }
        
        //Drop the temporary Collection
        dbc.dropBigMap("tmp_recordDecisions", tmp_recordDecisions);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        AbstractModelParameters modelParameters = knowledgeBase.getModelParameters();
        AbstractTrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        int n = modelParameters.getN();
        
        Set<Object> classesSet = modelParameters.getClasses();
        
        //first we need to find all the classes
        for(Record r : trainingData) { 
            Object theClass=r.getY();
            
            classesSet.add(theClass); 
        }
        
        
        
        AssociativeArray observationWeights = new AssociativeArray();
        
        //calculate the training parameters of bagging
        for(Integer rId : trainingData.index()) { 
            observationWeights.put(rId, 1.0/n); //initialize observation weights
        }
        
        Class<? extends AbstractClassifier> weakClassifierClass = trainingParameters.getWeakClassifierClass();
        AbstractClassifier.AbstractTrainingParameters weakClassifierTrainingParameters = trainingParameters.getWeakClassifierTrainingParameters();
        int totalWeakClassifiers = trainingParameters.getMaxWeakClassifiers();
        
        //training the weak classifiers
        int t=0;
        int retryCounter = 0;
        while(t<totalWeakClassifiers) {
            logger.debug("Training Weak learner {}", t);
            //We sample a list of Ids based on their weights
            FlatDataList sampledIDs = SimpleRandomSampling.weightedSampling(observationWeights, n, true).toFlatDataList();
            
            //We construct a new Dataframe from the sampledIDs
            Dataframe sampledTrainingDataset = trainingData.getSubset(sampledIDs);
            
            Dataframe validationDataset;
            try (AbstractClassifier mlclassifier = Trainable.<AbstractClassifier>newInstance(
                    (Class<AbstractClassifier>)weakClassifierClass, 
                    dbName+knowledgeBase.getConf().getDbConfig().getDBnameSeparator()+DB_INDICATOR+String.valueOf(t),
                    knowledgeBase.getConf())) {
                mlclassifier.fit(sampledTrainingDataset, weakClassifierTrainingParameters);
                sampledTrainingDataset.delete();
                //sampledTrainingDataset = null;
                validationDataset = trainingData;
                mlclassifier.predict(validationDataset);
            }
            
            Status status = updateObservationAndClassifierWeights(validationDataset, observationWeights);
            //validationDataset = null;
            
            if(status==Status.STOP) {
                logger.debug("Skipping further training due to low error");
                break;
            }
            else if(status==Status.IGNORE) {
                if(retryCounter<MAX_NUM_OF_RETRIES) {
                    logger.debug("Ignoring last weak learner due to high error");
                    ++retryCounter;
                    continue; 
                }
                else {
                    logger.debug("Too many retries, skipping further training");
                    break;
                }
            }
            else if(status==Status.NEXT) {
                retryCounter = 0; //reset the retry counter
            }
            
            ++t; //increase counter here. This is because some times we might want to redo the 
        }
        
    }
    
    /**
     * The status of the weight estimation process.
     */
    protected enum Status {
        /**
         * Keep the weak learner and move to the next one.
         */
        NEXT,
        
        /**
         * Keep the weak learner and stop.
         */
        STOP,
        
        /**
         * Ignore the weak learner and move to the next one.
         */
        IGNORE;
    }
    
    /**
     * Updates the weights of observations and the weights of the classifiers.
     * 
     * @param validationDataset
     * @param observationWeights
     * @return 
     */
    protected abstract Status updateObservationAndClassifierWeights(Dataframe validationDataset, AssociativeArray observationWeights);
    
    /** {@inheritDoc} */
    @Override
    public void delete() {
        deleteWeakClassifiers();
        super.delete();
    }
    
    private void deleteWeakClassifiers() {
        AbstractModelParameters modelParameters = knowledgeBase.getModelParameters();
        AbstractTrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        if(modelParameters==null) {
            return;
        }
        
        Class<? extends AbstractClassifier> weakClassifierClass = trainingParameters.getWeakClassifierClass();
        //the number of weak classifiers is the minimum between the classifiers that were defined in training parameters AND the number of the weak classifiers that were kept +1 for the one that was abandoned due to high error
        int totalWeakClassifiers = Math.min(modelParameters.getWeakClassifierWeights().size()+1, trainingParameters.getMaxWeakClassifiers());
        for(int t=0;t<totalWeakClassifiers;++t) {
            AbstractClassifier mlclassifier = Trainable.<AbstractClassifier>newInstance(
                    (Class<AbstractClassifier>)weakClassifierClass, 
                    dbName+knowledgeBase.getConf().getDbConfig().getDBnameSeparator()+DB_INDICATOR+String.valueOf(t),
                    knowledgeBase.getConf()
            );
            mlclassifier.delete();
        }
    }
}
