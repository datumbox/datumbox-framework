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
package com.datumbox.framework.applications.datamodeling;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.interfaces.Trainable;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.datatransformers.AbstractTransformer;
import com.datumbox.framework.core.machinelearning.common.abstracts.featureselectors.AbstractFeatureSelector;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelers.AbstractModeler;
import com.datumbox.framework.core.machinelearning.common.abstracts.wrappers.AbstractWrapper;

/**
 * Modeler is a convenience class which can be used to train Machine Learning
 * models. It is a wrapper class which automatically takes care of the data 
 transformation, feature selection and modeler training processes.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Modeler extends AbstractWrapper<Modeler.ModelParameters, Modeler.TrainingParameters> {
    
    /**
     * It contains all the Model Parameters which are learned during the training.
     */
    public static class ModelParameters extends AbstractWrapper.AbstractModelParameters {
        private static final long serialVersionUID = 1L;
        
        /**
         * @param dbc
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(DatabaseConnector)
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
    }
    
    /**
     * It contains the Training Parameters of the Modeler.
     */
    public static class TrainingParameters extends AbstractWrapper.AbstractTrainingParameters<AbstractTransformer, AbstractFeatureSelector, AbstractModeler> {
        private static final long serialVersionUID = 1L;

    }


    /**
     * @param dbName
     * @param conf
     * @param trainingParameters
     * @see AbstractTrainer#AbstractTrainer(String, Configuration, AbstractTrainer.AbstractTrainingParameters)
     */
    public Modeler(String dbName, Configuration conf, TrainingParameters trainingParameters) {
        super(dbName, conf, trainingParameters);
    }

    /**
     * @param dbName
     * @param conf
     * @see AbstractTrainer#AbstractTrainer(java.lang.String, Configuration)
     */
    public Modeler(String dbName, Configuration conf) {
        super(dbName, conf);
    }

    /**
     * Generates predictions for the given dataset.
     * 
     * @param newData 
     */
    public void predict(Dataframe newData) {
        logger.info("predict()");

        Modeler.TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Configuration conf = knowledgeBase.getConf();

        AbstractTrainer.AbstractTrainingParameters dtParams = trainingParameters.getDataTransformerTrainingParameters();
        boolean transformData = dtParams!=null;
        if(transformData) {
            if(dataTransformer==null) {
                dataTransformer = Trainable.newInstance(dtParams.getTClass(), dbName, conf);
            }
            setParallelized(dataTransformer);
            dataTransformer.transform(newData);
        }

        AbstractTrainer.AbstractTrainingParameters fsParams = trainingParameters.getFeatureSelectorTrainingParameters();
        boolean selectFeatures = fsParams!=null;
        if(selectFeatures) {
            if(featureSelector==null) {
                featureSelector = Trainable.newInstance(fsParams.getTClass(), dbName, conf);
            }
            setParallelized(featureSelector);
            featureSelector.transform(newData);
        }

        if(modeler==null) {
            modeler = Trainable.newInstance(trainingParameters.getModelerTrainingParameters().getTClass(), dbName, conf);
        }
        setParallelized(modeler);
        modeler.predict(newData);

        if(transformData) {
            dataTransformer.denormalize(newData);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Configuration conf = knowledgeBase.getConf();

        AbstractTrainer.AbstractTrainingParameters dtParams = trainingParameters.getDataTransformerTrainingParameters();
        boolean transformData = dtParams!=null;
        if(transformData) {
            dataTransformer = Trainable.newInstance(dtParams, dbName, conf);
            setParallelized(dataTransformer);
            dataTransformer.fit_transform(trainingData);
        }

        AbstractTrainer.AbstractTrainingParameters fsParams = trainingParameters.getFeatureSelectorTrainingParameters();
        boolean selectFeatures = fsParams!=null;
        if(selectFeatures) {
            featureSelector = Trainable.newInstance(fsParams, dbName, conf);
            setParallelized(featureSelector);
            featureSelector.fit_transform(trainingData);
        }

        AbstractTrainer.AbstractTrainingParameters mlParams = trainingParameters.getModelerTrainingParameters();
        modeler = Trainable.newInstance(mlParams, dbName, conf);
        setParallelized(modeler);
        modeler.fit(trainingData);
        
        if(transformData) {
            dataTransformer.denormalize(trainingData);
        }
    }
}
