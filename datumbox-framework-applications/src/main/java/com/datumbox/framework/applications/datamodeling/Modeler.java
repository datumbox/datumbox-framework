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
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.core.machinelearning.MLBuilder;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.datatransformers.AbstractTransformer;
import com.datumbox.framework.core.machinelearning.common.abstracts.featureselectors.AbstractFeatureSelector;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelers.AbstractModeler;
import com.datumbox.framework.core.machinelearning.common.dataobjects.TrainableBundle;
import com.datumbox.framework.core.machinelearning.common.interfaces.Parallelizable;

/**
 * Modeler is a convenience class which can be used to train Machine Learning
 * models. It is a wrapper class which automatically takes care of the data 
 transformation, feature selection and modeler training processes.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Modeler extends AbstractTrainer<Modeler.ModelParameters, Modeler.TrainingParameters> implements Parallelizable {

    private TrainableBundle bundle = new TrainableBundle();

    /**
     * It contains all the Model Parameters which are learned during the training.
     */
    public static class ModelParameters extends AbstractTrainer.AbstractModelParameters {
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
    public static class TrainingParameters extends AbstractTrainer.AbstractTrainingParameters {
        private static final long serialVersionUID = 1L;

        //Parameter Objects
        private AbstractTransformer.AbstractTrainingParameters dataTransformerTrainingParameters;

        private AbstractFeatureSelector.AbstractTrainingParameters featureSelectorTrainingParameters;

        private AbstractModeler.AbstractTrainingParameters modelerTrainingParameters;

        /**
         * Getter for the Training Parameters of the Data Transformer.
         *
         * @return
         */
        public AbstractTransformer.AbstractTrainingParameters getDataTransformerTrainingParameters() {
            return dataTransformerTrainingParameters;
        }

        /**
         * Setter for the Training Parameters of the Data Transformer. Pass null
         * for none.
         *
         * @param dataTransformerTrainingParameters
         */
        public void setDataTransformerTrainingParameters(AbstractTransformer.AbstractTrainingParameters dataTransformerTrainingParameters) {
            this.dataTransformerTrainingParameters = dataTransformerTrainingParameters;
        }

        /**
         * Getter for the Training Parameters of the Feature Selector.
         *
         * @return
         */
        public AbstractFeatureSelector.AbstractTrainingParameters getFeatureSelectorTrainingParameters() {
            return featureSelectorTrainingParameters;
        }

        /**
         * Setter for the Training Parameters of the Feature Selector. Pass null
         * for none.
         *
         * @param featureSelectorTrainingParameters
         */
        public void setFeatureSelectorTrainingParameters(AbstractFeatureSelector.AbstractTrainingParameters featureSelectorTrainingParameters) {
            this.featureSelectorTrainingParameters = featureSelectorTrainingParameters;
        }

        /**
         * Getter for the Training Parameters of the Machine Learning modeler.
         *
         * @return
         */
        public AbstractModeler.AbstractTrainingParameters getModelerTrainingParameters() {
            return modelerTrainingParameters;
        }

        /**
         * Setter for the Training Parameters of the Machine Learning modeler.
         *
         * @param modelerTrainingParameters
         */
        public void setModelerTrainingParameters(AbstractModeler.AbstractTrainingParameters modelerTrainingParameters) {
            this.modelerTrainingParameters = modelerTrainingParameters;
        }

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

    /**
     * Generates predictions for the given dataset.
     * 
     * @param newData 
     */
    public void predict(Dataframe newData) {
        logger.info("predict()");

        //load all trainables on the bundles
        initBundle();

        //set the parallized flag to all algorithms
        bundle.setParallelized(isParallelized());

        //run the pipeline
        AbstractTransformer dataTransformer = (AbstractTransformer) bundle.get("dataTransformer");
        if(dataTransformer != null) {
            dataTransformer.transform(newData);
        }
        AbstractFeatureSelector featureSelector = (AbstractFeatureSelector) bundle.get("featureSelector");
        if(featureSelector != null) {
            featureSelector.transform(newData);
        }
        AbstractModeler modeler = (AbstractModeler) bundle.get("modeler");
        modeler.predict(newData);
        if(dataTransformer != null) {
            dataTransformer.denormalize(newData);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Configuration conf = knowledgeBase.getConf();
        String dbName = knowledgeBase.getDbc().getDatabaseName();

        //reset previous entries on the bundle
        resetBundle();

        //initialize the parts of the pipeline
        AbstractTransformer.AbstractTrainingParameters dtParams = trainingParameters.getDataTransformerTrainingParameters();
        AbstractTransformer dataTransformer = null;
        if(dtParams != null) {
            dataTransformer = MLBuilder.create(dtParams, dbName, conf);
            bundle.put("dataTransformer", dataTransformer);
        }

        AbstractFeatureSelector.AbstractTrainingParameters fsParams = trainingParameters.getFeatureSelectorTrainingParameters();
        AbstractFeatureSelector featureSelector = null;
        if(fsParams != null) {
            featureSelector = MLBuilder.create(fsParams, dbName, conf);
            bundle.put("featureSelector", featureSelector);
        }

        AbstractModeler.AbstractTrainingParameters mlParams = trainingParameters.getModelerTrainingParameters();
        AbstractModeler modeler = MLBuilder.create(mlParams, dbName, conf);
        bundle.put("modeler", modeler);

        //set the parallized flag to all algorithms
        bundle.setParallelized(isParallelized());

        //run the pipeline
        if(dataTransformer != null) {
            dataTransformer.fit_transform(trainingData);
        }
        if(featureSelector != null) {
            featureSelector.fit_transform(trainingData);
        }
        modeler.fit(trainingData);
        if(dataTransformer != null) {
            dataTransformer.denormalize(trainingData);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void save() {
        initBundle();
        bundle.save();
        super.save();
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
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Configuration conf = knowledgeBase.getConf();
        String dbName = knowledgeBase.getDbc().getDatabaseName();

        if(!bundle.containsKey("dataTransformer")) {
            AbstractTransformer.AbstractTrainingParameters dtParams = trainingParameters.getDataTransformerTrainingParameters();

            AbstractTransformer dataTransformer = null;
            if(dtParams != null) {
                dataTransformer = MLBuilder.load(dtParams.getTClass(), dbName, conf);
            }
            bundle.put("dataTransformer", dataTransformer);
        }

        if(!bundle.containsKey("featureSelector")) {
            AbstractFeatureSelector.AbstractTrainingParameters fsParams = trainingParameters.getFeatureSelectorTrainingParameters();

            AbstractFeatureSelector featureSelector = null;
            if(fsParams != null) {
                featureSelector = MLBuilder.load(fsParams.getTClass(), dbName, conf);
            }
            bundle.put("featureSelector", featureSelector);
        }

        if(!bundle.containsKey("modeler")) {
            AbstractModeler.AbstractTrainingParameters mlParams = trainingParameters.getModelerTrainingParameters();

            bundle.put("modeler", MLBuilder.load(mlParams.getTClass(), dbName, conf));
        }
    }

}
