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
import com.datumbox.common.utilities.DeepCopy;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLclassifier;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureMarker;
import com.datumbox.configuration.GeneralConfiguration;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_print_interface;
import libsvm.svm_problem;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Transient;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class SupportVectorMachine extends BaseMLclassifier<SupportVectorMachine.ModelParameters, SupportVectorMachine.TrainingParameters, SupportVectorMachine.ValidationMetrics> {
    //References: http://phpir.com/svm https://github.com/ianbarber/php-svm https://github.com/encog/libsvm-java
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    public static final String SHORT_METHOD_NAME = "SVM";
    
    @Override
    public final String shortMethodName() {
        return SHORT_METHOD_NAME;
    }
    
    public static class ModelParameters extends BaseMLclassifier.ModelParameters {

        /**
         * Feature set
         */
        @BigDataStructureMarker
        @Transient
        private Map<Object, Integer> featureIds; //list of all the supported features

        private Map<Object, Integer> classIds = new HashMap<>(); //this is small. Size equal to class numbers;
        
        
        //MORPHIA does not support storing 2d arrays. svmModel has 2d arrays 
        //inside it. Thus to store it I have to serialize deserialize it and store
        //it as binary.
        @Transient
        private svm_model svmModel; //the parameters of the svm model
        private transient byte[] svmModel_serialized;
        
        @PrePersist
        public void prePersist(){
            if(svmModel != null && svmModel_serialized==null){
                svmModel_serialized = DeepCopy.serialize(svmModel);
            }
        }

        @PostLoad
        public void postLoad(){
            if(svmModel == null && svmModel_serialized!=null){
                svmModel = (svm_model) DeepCopy.deserialize(svmModel_serialized);
            }
        }
        
        
        
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            super.bigDataStructureInitializer(bdsf, memoryConfiguration);
            
            BigDataStructureFactory.MapType mapType = memoryConfiguration.getMapType();
            int LRUsize = memoryConfiguration.getLRUsize();
            
            featureIds = bdsf.getMap("featureIds", mapType, LRUsize);
        }
        
        public Map<Object, Integer> getFeatureIds() {
            return featureIds;
        }

        public void setFeatureIds(Map<Object, Integer> featureIds) {
            this.featureIds = featureIds;
        }

        public svm_model getSvmModel() {
            return svmModel;
        }

        public void setSvmModel(svm_model svmModel) {
            this.svmModel = svmModel;
        }

        public Map<Object, Integer> getClassIds() {
            return classIds;
        }

        public void setClassIds(Map<Object, Integer> classIds) {
            this.classIds = classIds;
        }
        
    } 

    
    public static class TrainingParameters extends BaseMLclassifier.TrainingParameters {         
        private svm_parameter svmParameter = new svm_parameter();
        
        public TrainingParameters() {
            super();
            
            // default values from svm_train.java example
            svmParameter.svm_type = svm_parameter.C_SVC;
            svmParameter.kernel_type = svm_parameter.LINEAR; //overwritten svm_parameter.RBF
            svmParameter.degree = 3;
            svmParameter.gamma = 0;	// 1/num_features
            svmParameter.coef0 = 0;
            svmParameter.nu = 0.5;
            svmParameter.cache_size = 100;
            svmParameter.C = 1;
            svmParameter.eps = 1e-3;
            svmParameter.p = 0.1;
            svmParameter.shrinking = 1;
            svmParameter.probability = 1; //overwritten 0
            svmParameter.nr_weight = 0;
            svmParameter.weight_label = new int[0];
            svmParameter.weight = new double[0];
        }

        public svm_parameter getSvmParameter() {
            return svmParameter;
        }

        public void setSvmParameter(svm_parameter svmParameter) {
            this.svmParameter = svmParameter;
        }
    } 
    
    
    public static class ValidationMetrics extends BaseMLclassifier.ValidationMetrics {

    }
        

    
    public SupportVectorMachine(String dbName) {
        super(dbName, SupportVectorMachine.ModelParameters.class, SupportVectorMachine.TrainingParameters.class, SupportVectorMachine.ValidationMetrics.class);
    }
    
    
    @Override
    public void train(Dataset trainingData, Dataset validationData) {
        knowledgeBase.getTrainingParameters().getSvmParameter().probability=1; //probabilities are required from the algorithm
        super.train(trainingData, validationData);
    }
    
    @Override
    protected void predictDataset(Dataset newData) { 
        for(Record r : newData) {
            AssociativeArray predictionScores = calculateClassScores(r.getX());
            
            Object theClass=getSelectedClassFromClassScores(predictionScores);
            
            Descriptives.normalize(predictionScores);
            
            r.setYPredicted(theClass);
            r.setYPredictedProbabilities(predictionScores);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected void estimateModelParameters(Dataset trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        int n = trainingData.size();
        int d = trainingData.getColumnSize();
        
        
        //initialization
        modelParameters.setN(n);
        modelParameters.setD(d);
        
        
        Map<Object, Integer> featureIds = modelParameters.getFeatureIds();
        Map<Object, Integer> classIds = modelParameters.getClassIds();
        Set<Object> classesSet = modelParameters.getClasses(); //we need to maintain this because inherited code relies on it
        
        //build the classIds and featureIds maps
        int previousClassId = 0;
        int previousFeatureId = 0;
        for(Record r : trainingData) {
            Object theClass=r.getY();
            
            if(!classIds.containsKey(theClass)) {
                classIds.put(theClass, previousClassId++);
                classesSet.add(theClass);
            }
            
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                if(!featureIds.containsKey(feature)) {
                    featureIds.put(feature, previousFeatureId++);
                }
            }
        }
        
        int c = classIds.size();
        modelParameters.setC(c);
        
        //calling the method that handles the training with LibSVM library
        LibSVMTrainer(trainingData);
    }
    

    private void LibSVMTrainer(Dataset trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        Map<Object, Integer> featureIds = modelParameters.getFeatureIds();
        Map<Object, Integer> classIds = modelParameters.getClassIds();
        
        int n = trainingData.size();
        int sparseD = featureIds.size();
        
        //creating a new SVM problem
        svm_problem prob = new svm_problem();
        prob.l = n;
        prob.y = new double[n];
        prob.x = new svm_node[n][sparseD];
        
        //converting the dataset in the way that LibSVM can handle it
        for(Record r : trainingData) {
            Integer recordId = r.getId();
            Object theClass=r.getY();
            
            int classId = classIds.get(theClass);
            prob.y[recordId] = classId;
            
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                int featureId = featureIds.get(feature);
                Double value = Dataset.toDouble(entry.getValue());
                if(value==null) {
                    value = 0.0;
                }
                
                svm_node node=new svm_node();
                node.index=(featureId+1); //the indexes in the library start from 1!!!
                node.value=value;
                
                prob.x[recordId][featureId] = node;
            }
            
            //fill with zeros the rest of the features
            for(int featureId = 0;featureId<sparseD; ++featureId) {
                if(prob.x[recordId][featureId]==null) {
                    svm_node node=new svm_node();
                    node.index=(featureId+1);
                    node.value=0.0;
                    prob.x[recordId][featureId] = node;
                }
            }
        }
        
        //get the parameters for svm
        svm_parameter params = knowledgeBase.getTrainingParameters().getSvmParameter();
        
        //train the model
        if(!GeneralConfiguration.DEBUG) {
            svm.svm_set_print_string_function(new svm_print_interface() { 
                    public void print(String s) { }
            });
        }
        svm_model model = svm.svm_train(prob, params);
        
        //store it in the Model Parameters
        modelParameters.setSvmModel(model);
    }
    

    private AssociativeArray calculateClassScores(AssociativeArray x) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        Map<Object, Integer> featureIds = modelParameters.getFeatureIds();
        Map<Object, Integer> classIds = modelParameters.getClassIds();
        svm_model model = modelParameters.getSvmModel();
        
        int sparseD = featureIds.size();
        int c = modelParameters.getC();
        
        //convert x into a svm node array in order to pass it to the library of SVMLib
        svm_node[] xSVM = new svm_node[sparseD];
        for(Map.Entry<Object, Object> entry : x.entrySet()) {

            Object feature = entry.getKey();
            Integer featureId = featureIds.get(feature);
            if(featureId==null) {
                continue; //the feature does not exist
            }
            
            Double value = Dataset.toDouble(entry.getValue());
            if(value==null) {
                value = 0.0;
            }
            
            svm_node node = new svm_node();

            node.index = (featureId+1); //the indexes in the library start from 1!!!
            node.value = value;
            
            xSVM[featureId] = node;
        }
        
        //fill with zeros the rest of the features
        for(int featureId = 0;featureId<sparseD; ++featureId) {
            if(xSVM[featureId]==null) {
                svm_node node=new svm_node();
                node.index=(featureId+1);
                node.value=0.0;
                xSVM[featureId] = node;
            }
        }
        
        int[] labels = new int[c];
        
        svm.svm_get_labels(model,labels);
        
        double[] prob_estimates = new double[c];
        double v = svm.svm_predict_probability(model, xSVM, prob_estimates);
        
        AssociativeArray classScores = new AssociativeArray();
        for(Map.Entry<Object, Integer> entry : classIds.entrySet()) {
            Object theClass = entry.getKey();
            int classId = entry.getValue();
            
            classScores.put(theClass, prob_estimates[classId]);
        }
        
        return classScores;
    }
}
