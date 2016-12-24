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
import com.datumbox.framework.common.persistentstorage.interfaces.StorageConnector;
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

    private static final String DT_KEY = "dt";
    private static final String FS_KEY = "fs";
    private static final String ML_KEY = "ml";

    private final TrainableBundle bundle;

    /**
     * It contains all the Model Parameters which are learned during the training.
     */
    public static class ModelParameters extends AbstractTrainer.AbstractModelParameters {
        private static final long serialVersionUID = 1L;
        
        /**
         * @param storageConnector
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageConnector)
         */
        protected ModelParameters(StorageConnector storageConnector) {
            super(storageConnector);
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
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainingParameters, Configuration)
     */
    protected Modeler(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
        bundle  = new TrainableBundle(configuration.getStorageConfiguration().getStorageNameSeparator());
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(java.lang.String, Configuration)
     */
    protected Modeler(String storageName, Configuration configuration) {
        super(storageName, configuration);
        bundle  = new TrainableBundle(configuration.getStorageConfiguration().getStorageNameSeparator());
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
        AbstractTransformer dataTransformer = (AbstractTransformer) bundle.get(DT_KEY);
        if(dataTransformer != null) {
            dataTransformer.transform(newData);
        }
        AbstractFeatureSelector featureSelector = (AbstractFeatureSelector) bundle.get(FS_KEY);
        if(featureSelector != null) {
            featureSelector.transform(newData);
        }
        AbstractModeler modeler = (AbstractModeler) bundle.get(ML_KEY);
        modeler.predict(newData);
        if(dataTransformer != null) {
            dataTransformer.denormalize(newData);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Configuration configuration = knowledgeBase.getConfiguration();

        //reset previous entries on the bundle
        resetBundle();

        //initialize the parts of the pipeline
        AbstractTransformer.AbstractTrainingParameters dtParams = trainingParameters.getDataTransformerTrainingParameters();
        AbstractTransformer dataTransformer = null;
        if(dtParams != null) {
            dataTransformer = MLBuilder.create(dtParams, configuration);
            bundle.put(DT_KEY, dataTransformer);
        }

        AbstractFeatureSelector.AbstractTrainingParameters fsParams = trainingParameters.getFeatureSelectorTrainingParameters();
        AbstractFeatureSelector featureSelector = null;
        if(fsParams != null) {
            featureSelector = MLBuilder.create(fsParams, configuration);
            bundle.put(FS_KEY, featureSelector);
        }

        AbstractModeler.AbstractTrainingParameters mlParams = trainingParameters.getModelerTrainingParameters();
        AbstractModeler modeler = MLBuilder.create(mlParams, configuration);
        bundle.put(ML_KEY, modeler);

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
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Configuration configuration = knowledgeBase.getConfiguration();
        String storageName = knowledgeBase.getStorageConnector().getStorageName();
        String separator = configuration.getStorageConfiguration().getStorageNameSeparator();

        if(!bundle.containsKey(DT_KEY)) {
            AbstractTransformer.AbstractTrainingParameters dtParams = trainingParameters.getDataTransformerTrainingParameters();

            AbstractTransformer dataTransformer = null;
            if(dtParams != null) {
                dataTransformer = MLBuilder.load(dtParams.getTClass(), storageName + separator + DT_KEY, configuration);
            }
            bundle.put(DT_KEY, dataTransformer);
        }

        if(!bundle.containsKey(FS_KEY)) {
            AbstractFeatureSelector.AbstractTrainingParameters fsParams = trainingParameters.getFeatureSelectorTrainingParameters();

            AbstractFeatureSelector featureSelector = null;
            if(fsParams != null) {
                featureSelector = MLBuilder.load(fsParams.getTClass(), storageName + separator + FS_KEY, configuration);
            }
            bundle.put(FS_KEY, featureSelector);
        }

        if(!bundle.containsKey(ML_KEY)) {
            AbstractModeler.AbstractTrainingParameters mlParams = trainingParameters.getModelerTrainingParameters();

            bundle.put(ML_KEY, MLBuilder.load(mlParams.getTClass(), storageName + separator + ML_KEY, configuration));
        }
    }

}
