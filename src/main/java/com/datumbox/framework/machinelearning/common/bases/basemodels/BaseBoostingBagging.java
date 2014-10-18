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
package com.datumbox.framework.machinelearning.common.bases.basemodels;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.DataTable2D;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.common.utilities.DeepCopy;
import com.datumbox.common.utilities.MapFunctions;
import com.datumbox.configuration.StorageConfiguration;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclassifier;
import com.datumbox.framework.machinelearning.ensemblelearning.FixedCombinationRules;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import com.datumbox.framework.statistics.sampling.SRS;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for Adaboost and BoostrapAgregating.
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public abstract class BaseBoostingBagging<MP extends BaseBoostingBagging.ModelParameters, TP extends BaseBoostingBagging.TrainingParameters, VM extends BaseBoostingBagging.ValidationMetrics> extends BaseMLclassifier<MP, TP, VM> {

    public static final String DB_INDICATOR="Cmp";

    public static abstract class ModelParameters extends BaseMLclassifier.ModelParameters {
        
        private List<Double> weakClassifierWeights = new ArrayList<>(); //this is small. maximum as the total number of classifiers
        
        public List<Double> getWeakClassifierWeights() {
            return weakClassifierWeights;
        }

        public void setWeakClassifierWeights(List<Double> weakClassifierWeights) {
            this.weakClassifierWeights = weakClassifierWeights;
        }
        
    } 

    
    public static abstract class TrainingParameters extends BaseMLclassifier.TrainingParameters {      
        
        //primitives/wrappers
        private int maxWeakClassifiers = 5; //the total number of classifiers that Bagging will initialize
        
        //Classes
        private Class<? extends BaseMLclassifier> weakClassifierClass; //the class name of weak classifiear
        
        //Parameter Objects
        private BaseMLclassifier.TrainingParameters weakClassifierTrainingParameters; //the parameters of the weak classifier
        
        public int getMaxWeakClassifiers() {
            return maxWeakClassifiers;
        }

        public void setMaxWeakClassifiers(int maxWeakClassifiers) {
            this.maxWeakClassifiers = maxWeakClassifiers;
        }

        public Class<? extends BaseMLclassifier> getWeakClassifierClass() {
            return weakClassifierClass;
        }

        public void setWeakClassifierClass(Class<? extends BaseMLclassifier> weakClassifierClass) {
            this.weakClassifierClass = weakClassifierClass;
        }

        public BaseMLclassifier.TrainingParameters getWeakClassifierTrainingParameters() {
            return weakClassifierTrainingParameters;
        }

        public void setWeakClassifierTrainingParameters(BaseMLclassifier.TrainingParameters weakClassifierTrainingParameters) {
            this.weakClassifierTrainingParameters = weakClassifierTrainingParameters;
        }
        
        
        
    } 
    
    
    public static abstract class ValidationMetrics extends BaseMLclassifier.ValidationMetrics {

    }
    
    
    protected BaseBoostingBagging(String dbName, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass) {
        super(dbName, mpClass, tpClass, vmClass);
    } 
    
    @Override
    protected void predictDataset(Dataset newData) { 
        Class<? extends BaseMLclassifier> weakClassifierClass = knowledgeBase.getTrainingParameters().getWeakClassifierClass();
        List<Double> weakClassifierWeights = knowledgeBase.getModelParameters().getWeakClassifierWeights();
        
        int n = newData.size();
        
        //initialize array of recordDecisions
        DataTable2D[] recordDecisionsArray = new DataTable2D[n];
        for(Record r : newData) {
            recordDecisionsArray[r.getId()]= new DataTable2D();
        }
        
        //using the weak classifiers
        AssociativeArray classifierWeightsArray = new AssociativeArray();
        int totalWeakClassifiers = weakClassifierWeights.size();
        for(int t=0;t<totalWeakClassifiers;++t) {
            BaseMLclassifier mlclassifier = BaseMLmodel.newInstance(weakClassifierClass, dbName+StorageConfiguration.getDBnameSeparator()+DB_INDICATOR+String.valueOf(t));
            mlclassifier.setMemoryConfiguration(knowledgeBase.getMemoryConfiguration());
            mlclassifier.predict(newData);
            mlclassifier = null;
            
            classifierWeightsArray.put(t, weakClassifierWeights.get(t));
            
            for(Record r : newData) {
                AssociativeArray classProbabilities = r.getYPredictedProbabilities();
                
                DataTable2D currentRecordDecisions = recordDecisionsArray[r.getId()];
                
                currentRecordDecisions.put(t, classProbabilities);
            }
        }
        
        //for each record find the combined classification by majority vote
        for(Record r : newData) {
            DataTable2D currentRecordDecisions = recordDecisionsArray[r.getId()];
            
            //AssociativeArray combinedClassVotes = FixedCombinationRules.majorityVote(currentRecordDecisions);
            AssociativeArray combinedClassVotes = FixedCombinationRules.weightedAverage(currentRecordDecisions, classifierWeightsArray);
            Descriptives.normalize(combinedClassVotes);
            
            r.setYPredictedProbabilities(combinedClassVotes);
            
            r.setYPredicted(MapFunctions.selectMaxKeyValue(combinedClassVotes).getKey());
        }
        
        recordDecisionsArray = null;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected void estimateModelParameters(Dataset trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        
        String tmpPrefix=StorageConfiguration.getTmpPrefix();
        int n = trainingData.size();
        int d = trainingData.getColumnSize();
        
        //initialization
        modelParameters.setN(n);
        modelParameters.setD(d);
        
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
        
        //Define it as Object,Object instead of Interger,Double to be able to wrap it in an AssociativeArray and use the Statistics Layer
        Map<Object, Object> observationWeights = bdsf.getMap(tmpPrefix+"observationWeights", knowledgeBase.getMemoryConfiguration().getMapType(), knowledgeBase.getMemoryConfiguration().getLRUsize());
        
        //calculate the training parameters of bagging
        for(Record r : trainingData) {
            observationWeights.put(r.getId(), 1.0/n); //initialize observation weights
        }
        
        Class<? extends BaseMLclassifier> weakClassifierClass = trainingParameters.getWeakClassifierClass();
        BaseMLclassifier.TrainingParameters weakClassifierTrainingParameters = trainingParameters.getWeakClassifierTrainingParameters();
        int totalWeakClassifiers = trainingParameters.getMaxWeakClassifiers();
        
        //training the weak classifiers
        for(int t=0;t<totalWeakClassifiers;++t) {
            FlatDataCollection sampledIDs = SRS.weightedProbabilitySampling(new AssociativeArray(observationWeights), n, true);
            
            Dataset sampledTrainingDataset = new Dataset();
            for(Object id : sampledIDs) {
                sampledTrainingDataset.add(trainingData.get((Integer)id));
            }
            
            BaseMLclassifier mlclassifier = BaseMLmodel.newInstance(weakClassifierClass, dbName+StorageConfiguration.getDBnameSeparator()+DB_INDICATOR+String.valueOf(t));
            mlclassifier.initializeTrainingConfiguration(knowledgeBase.getMemoryConfiguration(), weakClassifierTrainingParameters);
            
            Dataset validationDataset = trainingData;
            if(mlclassifier.modifiesData()) {
                sampledTrainingDataset = DeepCopy.<Dataset>cloneObject(sampledTrainingDataset);
                validationDataset = DeepCopy.<Dataset>cloneObject(validationDataset);
            }
            mlclassifier.train(sampledTrainingDataset, validationDataset); 
            
            
            boolean stop = updateObservationAndClassifierWeights(validationDataset, observationWeights);
            
            mlclassifier = null;
            sampledTrainingDataset = null;
            validationDataset = null;
            
            if(stop==true) {
                break;
            }
        }
        
        //Drop the temporary Collection
        bdsf.dropTable(tmpPrefix+"observationWeights", observationWeights);
    }

    /**
     * Updates the weights of observations and the weights of the classifiers.
     * 
     * @param validationDataset
     * @param observationWeights
     * @return 
     */
    protected abstract boolean updateObservationAndClassifierWeights(Dataset validationDataset, Map<Object, Object> observationWeights);
    
    @Override
    public void erase(boolean complete) {
        eraseWeakClassifiers(complete);
        super.erase(complete);
    }
    
    protected void eraseWeakClassifiers(boolean complete) {
        if(knowledgeBase.isConfigured()) {
            ModelParameters modelParameters = knowledgeBase.getModelParameters();
            TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
            
            Class<? extends BaseMLclassifier> weakClassifierClass = trainingParameters.getWeakClassifierClass();
            //the number of weak classifiers is the minimum between the classifiers that were defined in training parameters AND the number of the weak classifiers that were kept +1 for the one that was abandoned due to high error
            int totalWeakClassifiers = Math.min(modelParameters.getWeakClassifierWeights().size()+1, trainingParameters.getMaxWeakClassifiers());
            for(int t=0;t<totalWeakClassifiers;++t) {
                BaseMLclassifier mlclassifier = BaseMLmodel.newInstance(weakClassifierClass, dbName+StorageConfiguration.getDBnameSeparator()+DB_INDICATOR+String.valueOf(t));
                //We can't use erase(false) here because it leaves undeleted databases of the Kcross validation
                mlclassifier.erase(complete);
            }
        }
    }
}
