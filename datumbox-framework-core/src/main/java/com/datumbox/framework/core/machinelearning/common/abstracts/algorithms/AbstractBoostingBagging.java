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
package com.datumbox.framework.core.machinelearning.common.abstracts.algorithms;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.*;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.common.storage.interfaces.StorageEngine.MapType;
import com.datumbox.framework.common.storage.interfaces.StorageEngine.StorageHint;
import com.datumbox.framework.core.common.utilities.MapMethods;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.core.machinelearning.MLBuilder;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelers.AbstractClassifier;
import com.datumbox.framework.core.machinelearning.common.dataobjects.TrainableBundle;
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

    private final TrainableBundle bundle;

    private static final String STORAGE_INDICATOR = "Cmp";
    private static final int MAX_NUM_OF_RETRIES = 2;
    
    /** {@inheritDoc} */
    public static abstract class AbstractModelParameters extends AbstractClassifier.AbstractModelParameters {
        
        private List<Double> weakClassifierWeights = new ArrayList<>();

        /** 
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected AbstractModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
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
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainer.AbstractTrainingParameters, Configuration)
     */
    protected AbstractBoostingBagging(TP trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
        bundle  = new TrainableBundle(configuration.getStorageConfiguration().getStorageNameSeparator());
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected AbstractBoostingBagging(String storageName, Configuration configuration) {
        super(storageName, configuration);
        bundle  = new TrainableBundle(configuration.getStorageConfiguration().getStorageNameSeparator());
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _predict(Dataframe newData) {
        //load all trainables on the bundles
        initBundle();

        List<Double> weakClassifierWeights = knowledgeBase.getModelParameters().getWeakClassifierWeights();

        //create a temporary map for the observed probabilities in training set
        StorageEngine storageEngine = knowledgeBase.getStorageEngine();
        Map<Object, DataTable2D> tmp_recordDecisions = storageEngine.getBigMap("tmp_recordDecisions", Object.class, DataTable2D.class, MapType.HASHMAP, StorageHint.IN_DISK, false, true);
        
        //initialize array of recordDecisions
        for(Integer rId : newData.index()) {
            tmp_recordDecisions.put(rId, new DataTable2D());
        }
        
        //using the weak classifiers
        AssociativeArray classifierWeightsArray = new AssociativeArray();
        int totalWeakClassifiers = weakClassifierWeights.size();
        for(int i=0;i<totalWeakClassifiers;++i) {

            AbstractClassifier mlclassifier = (AbstractClassifier) bundle.get(STORAGE_INDICATOR + i);
            mlclassifier.predict(newData);
            
            classifierWeightsArray.put(i, weakClassifierWeights.get(i));
            
            for(Map.Entry<Integer, Record> e : newData.entries()) {
                Integer rId = e.getKey();
                Record r = e.getValue();
                AssociativeArray classProbabilities = r.getYPredictedProbabilities();
                
                DataTable2D rDecisions = tmp_recordDecisions.get(rId);
                rDecisions.put(i, classProbabilities);
                
                tmp_recordDecisions.put(rId, rDecisions); //WARNING: Do not remove this! We must put it back to the Map to store it on Disk-backed maps
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
        storageEngine.dropBigMap("tmp_recordDecisions", tmp_recordDecisions);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        Configuration configuration = knowledgeBase.getConfiguration();
        TP trainingParameters = knowledgeBase.getTrainingParameters();
        MP modelParameters = knowledgeBase.getModelParameters();

        //reset previous entries on the bundle
        resetBundle();


        //first we need to find all the classes
        int n = trainingData.size();
        Set<Object> classesSet = modelParameters.getClasses();
        for(Record r : trainingData) { 
            Object theClass=r.getY();
            
            classesSet.add(theClass); 
        }
        
        
        
        AssociativeArray observationWeights = new AssociativeArray();
        
        //calculate the training parameters of bagging
        for(Integer rId : trainingData.index()) { 
            observationWeights.put(rId, 1.0/n); //initialize observation weights
        }

        AbstractClassifier.AbstractTrainingParameters weakClassifierTrainingParameters = trainingParameters.getWeakClassifierTrainingParameters();
        int totalWeakClassifiers = trainingParameters.getMaxWeakClassifiers();
        
        //training the weak classifiers
        int i=0;
        int retryCounter = 0;
        while(i<totalWeakClassifiers) {
            logger.debug("Training Weak learner {}", i);

            //We sample a list of Ids based on their weights
            FlatDataList sampledIDs = SimpleRandomSampling.weightedSampling(observationWeights, n, true).toFlatDataList();

            //We construct a new Dataframe from the sampledIDs
            Dataframe sampledTrainingDataset = trainingData.getSubset(sampledIDs);


            AbstractClassifier mlclassifier = MLBuilder.create(weakClassifierTrainingParameters, configuration);
            mlclassifier.fit(sampledTrainingDataset);
            sampledTrainingDataset.close();
            mlclassifier.predict(trainingData);

            
            Status status = updateObservationAndClassifierWeights(trainingData, observationWeights);
            if(status == Status.IGNORE) {
                mlclassifier.close();
            }
            else {
                bundle.put(STORAGE_INDICATOR + i, mlclassifier);
            }

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
            
            i++;
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
    public void save(String storageName) {
        initBundle();
        super.save(storageName);

        String knowledgeBaseName = createKnowledgeBaseName(storageName, knowledgeBase.getConfiguration().getStorageConfiguration().getStorageNameSeparator());
        bundle.save(knowledgeBaseName);
    }

    /** {@inheritDoc} */
    @Override
    public void delete() {
        initBundle();
        bundle.delete();
        super.delete();
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        initBundle();
        bundle.close();
        super.close();
    }

    private void resetBundle() {
        bundle.delete();
    }

    private void initBundle() {
        Configuration configuration = knowledgeBase.getConfiguration();
        StorageEngine storageEngine = knowledgeBase.getStorageEngine();
        MP modelParameters = knowledgeBase.getModelParameters();
        TP trainingParameters = knowledgeBase.getTrainingParameters();
        String separator = configuration.getStorageConfiguration().getStorageNameSeparator();

        //the number of weak classifiers is the minimum between the classifiers that were defined in training parameters AND the number of the weak classifiers that were kept
        Class<AbstractClassifier> weakClassifierClass = trainingParameters.getWeakClassifierTrainingParameters().getTClass();
        int totalWeakClassifiers = Math.min(modelParameters.getWeakClassifierWeights().size(), trainingParameters.getMaxWeakClassifiers());
        for(int i=0;i<totalWeakClassifiers;i++) {
            String key = STORAGE_INDICATOR + i;
            if (!bundle.containsKey(key)) {
                bundle.put(key, MLBuilder.load(weakClassifierClass, storageEngine.getStorageName() + separator + key, configuration));
            }
        }
    }
}
