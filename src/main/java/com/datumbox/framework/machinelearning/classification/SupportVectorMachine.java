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


/**
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class SupportVectorMachine extends BaseMLclassifier<SupportVectorMachine.ModelParameters, SupportVectorMachine.TrainingParameters, SupportVectorMachine.ValidationMetrics> {
    //References: http://phpir.com/svm https://github.com/ianbarber/php-svm https://github.com/encog/libsvm-java
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    public static class ModelParameters extends BaseMLclassifier.ModelParameters {

        /**
         * Feature set
         */
        @BigMap
        private Map<Object, Integer> featureIds; //list of all the supported features

        private Map<Object, Integer> classIds = new HashMap<>(); //this is small. Size equal to class numbers;
        
        
        private svm_model svmModel; //the parameters of the svm model
        

        public ModelParameters(DatabaseConnector dbc) {
            super(dbc);
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
        

    
    public SupportVectorMachine(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, SupportVectorMachine.ModelParameters.class, SupportVectorMachine.TrainingParameters.class, SupportVectorMachine.ValidationMetrics.class);
    }
    
    
    
    @Override
    protected void predictDataset(Dataset newData) { 
        for(Integer rId : newData) {
            Record r = newData.get(rId);
            AssociativeArray predictionScores = calculateClassScores(r.getX());
            
            Object theClass=getSelectedClassFromClassScores(predictionScores);
            
            Descriptives.normalize(predictionScores);
            
            newData.set(rId, new Record(r.getX(), r.getY(), theClass, predictionScores));
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected void _fit(Dataset trainingData) {
        knowledgeBase.getTrainingParameters().getSvmParameter().probability=1; //probabilities are required from the algorithm
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        int n = trainingData.getRecordNumber();
        int d = trainingData.getVariableNumber();
        
        
        //initialization
        modelParameters.setN(n);
        modelParameters.setD(d);
        
        
        Map<Object, Integer> featureIds = modelParameters.getFeatureIds();
        Map<Object, Integer> classIds = modelParameters.getClassIds();
        Set<Object> classesSet = modelParameters.getClasses(); //we need to maintain this because inherited code relies on it
        
        //build the classIds and featureIds maps
        int previousClassId = 0;
        int previousFeatureId = 0;
        for(Integer rId : trainingData) { 
            Record r = trainingData.get(rId);
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
        
        int n = trainingData.getRecordNumber();
        int sparseD = featureIds.size();
        
        //creating a new SVM problem
        svm_problem prob = new svm_problem();
        prob.l = n;
        prob.y = new double[n];
        prob.x = new svm_node[n][sparseD];
        
        //converting the dataset in the way that LibSVM can handle it
        for(Integer rId : trainingData) { 
            Record r = trainingData.get(rId);
            
            Object theClass=r.getY();
            
            int classId = classIds.get(theClass);
            prob.y[rId] = classId;
            
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                int featureId = featureIds.get(feature);
                Double value = TypeInference.toDouble(entry.getValue());
                if(value==null) {
                    value = 0.0;
                }
                
                svm_node node=new svm_node();
                node.index=(featureId+1); //the indexes in the library start from 1!!!
                node.value=value;
                
                prob.x[rId][featureId] = node;
            }
            
            //fill with zeros the rest of the features
            for(int featureId = 0;featureId<sparseD; ++featureId) {
                if(prob.x[rId][featureId]==null) {
                    svm_node node=new svm_node();
                    node.index=(featureId+1);
                    node.value=0.0;
                    prob.x[rId][featureId] = node;
                }
            }
        }
        
        //get the parameters for svm
        svm_parameter params = knowledgeBase.getTrainingParameters().getSvmParameter();
        
        //train the model
        svm.svm_set_print_string_function(new svm_print_interface() { 
            public void print(String s) { logger.debug(s); }
        });
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
            
            Double value = TypeInference.toDouble(entry.getValue());
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
