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
package com.datumbox.framework.core.machinelearning.classification;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.common.storage.interfaces.BigMap;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.common.storage.interfaces.StorageEngine.MapType;
import com.datumbox.framework.common.storage.interfaces.StorageEngine.StorageHint;
import com.datumbox.framework.common.utilities.RandomGenerator;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelers.AbstractClassifier;
import com.datumbox.framework.core.machinelearning.common.interfaces.PredictParallelizable;
import com.datumbox.framework.core.statistics.descriptivestatistics.Descriptives;
import libsvm.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * The SupportVectorMachine class enables you to train SVM models. This implementation
 uses internally the LIBSVM library.
 
 WARNING: This class copies the Dataframe to double arrays which forces all of the
 data to be loaded in memory.
 
 References: 
 http://phpir.com/svm 
 https://github.com/ianbarber/php-svm 
 https://github.com/encog/libsvm-java
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class SupportVectorMachine extends AbstractClassifier<SupportVectorMachine.ModelParameters, SupportVectorMachine.TrainingParameters> implements PredictParallelizable {
    
    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractClassifier.AbstractModelParameters {
        private static final long serialVersionUID = 1L;

        @BigMap(keyClass=Object.class, valueClass=Integer.class, mapType= MapType.HASHMAP, storageHint= StorageHint.IN_MEMORY, concurrent=false)
        private Map<Object, Integer> featureIds; //list of all the supported features

        private Map<Object, Integer> classIds = new HashMap<>(); //this is small. Size equal to class numbers;
        
        private svm_model svmModel; //the parameters of the svm model
        
        /** 
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected ModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
        }
        
        /**
         * Getter for the mapping of the column names to column ids. This mapping is estimated during training.
         * 
         * @return 
         */
        public Map<Object, Integer> getFeatureIds() {
            return featureIds;
        }
        
        /**
         * Setter for the mapping of the column names to column ids. 
         * 
         * @param featureIds 
         */
        protected void setFeatureIds(Map<Object, Integer> featureIds) {
            this.featureIds = featureIds;
        }
        
        /**
         * Getter for the internal svm_model of the LIBSVM library.
         * 
         * @return 
         */
        public svm_model getSvmModel() {
            return svmModel;
        }

        
        /**
         * Setter for the internal svm_model of the LIBSVM library.
         * 
         * @param svmModel
         */
        protected void setSvmModel(svm_model svmModel) {
            this.svmModel = svmModel;
        }
        
        /**
         * Getter for the mapping between class names and ids. The implementation
         * requires converting any category names into integer codes. This mapping
         * is estimated during training.
         * 
         * @return 
         */
        public Map<Object, Integer> getClassIds() {
            return classIds;
        }
        
        /**
         * Setter for the mapping between class names and ids. 
         * 
         * @param classIds 
         */
        protected void setClassIds(Map<Object, Integer> classIds) {
            this.classIds = classIds;
        }
        
    } 
    
    /** {@inheritDoc} */
    public static class TrainingParameters extends AbstractClassifier.AbstractTrainingParameters {     
        private static final long serialVersionUID = 1L;
        
        private svm_parameter svmParameter = new svm_parameter();
        
        /**
         * Default constructor.
         */
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
        
        /**
         * Getter for the svm_parameter object.
         * 
         * @return 
         */
        public svm_parameter getSvmParameter() {
            return svmParameter;
        }
        
        /**
         * Setter for the svm_parameter object. It contains many different 
         * parameters that tune the LIBSVM algorithm such as the kernel, the 
         * weights etc.
         * 
         * @param svmParameter 
         */
        public void setSvmParameter(svm_parameter svmParameter) {
            this.svmParameter = svmParameter;
        }
    }

    //Instance initialization block
    {
        svm.rand.setSeed(RandomGenerator.getThreadLocalRandom().nextLong()); //seed the internal random of the SVM class
    }

    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainingParameters, Configuration)
     */
    protected SupportVectorMachine(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected SupportVectorMachine(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }
    
    private boolean parallelized = true;
    
    /** {@inheritDoc} */
    @Override
    public boolean isParallelized() {
        return parallelized;
    }

    /** {@inheritDoc} */
    @Override
    public void setParallelized(boolean parallelized) {
        this.parallelized = parallelized;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _predict(Dataframe newData) {
        _predictDatasetParallel(newData, knowledgeBase.getStorageEngine(), knowledgeBase.getConfiguration().getConcurrencyConfiguration());
    }

    /** {@inheritDoc} */
    @Override
    public Prediction _predictRecord(Record r) {
        AssociativeArray predictionScores = calculateClassScores(r.getX());
        
        Object predictedClass=getSelectedClassFromClassScores(predictionScores);

        Descriptives.normalize(predictionScores);
        
        return new Prediction(predictedClass, predictionScores);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        knowledgeBase.getTrainingParameters().getSvmParameter().probability=1; //probabilities are required from the algorithm
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        
        Map<Object, Integer> featureIds = modelParameters.getFeatureIds();
        Map<Object, Integer> classIds = modelParameters.getClassIds();
        Set<Object> classesSet = modelParameters.getClasses(); //we need to maintain this because inherited code relies on it
        
        //build the classIds and featureIds maps
        int classId = 0;
        int featureId = 0;
        for(Record r : trainingData) { 
            Object theClass=r.getY();
            
            if(classesSet.add(theClass)) {
                classIds.put(theClass, classId++);
            }
            
            for(Map.Entry<Object, Object> entry : r.getX().entrySet()) {
                Object feature = entry.getKey();
                if(featureIds.putIfAbsent(feature, featureId) == null) {
                    featureId++;
                }
            }
        }
        
        //calling the method that handles the training with LibSVM library
        libSVMTrainer(trainingData);
    }
    
    private void libSVMTrainer(Dataframe trainingData) {
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
        int rowId = 0;
        for(Record r : trainingData.values()) { 
            Object theClass=r.getY();
            
            int classId = classIds.get(theClass);
            prob.y[rowId] = classId;
            
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
                
                prob.x[rowId][featureId] = node;
            }
            
            //fill with zeros the rest of the features
            for(int featureId = 0;featureId<sparseD; ++featureId) {
                if(prob.x[rowId][featureId]==null) {
                    svm_node node=new svm_node();
                    node.index=(featureId+1);
                    node.value=0.0;
                    prob.x[rowId][featureId] = node;
                }
            }
            ++rowId;
        }
        
        //get the parameters for svm
        svm_parameter params = knowledgeBase.getTrainingParameters().getSvmParameter();
        
        //train the model
        svm.svm_set_print_string_function((String s) -> {
            if(s != null) {
                logger.debug(s.trim());
            }
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
        svm.svm_predict_probability(model, xSVM, prob_estimates);
        
        AssociativeArray classScores = new AssociativeArray();
        for(Map.Entry<Object, Integer> entry : classIds.entrySet()) {
            Object theClass = entry.getKey();
            int classId = entry.getValue();
            
            classScores.put(theClass, prob_estimates[classId]);
        }
        
        return classScores;
    }
}
