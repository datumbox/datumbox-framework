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
import com.datumbox.framework.common.storageengines.interfaces.StorageEngine;
import com.datumbox.framework.core.machinelearning.MLBuilder;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.transformers.AbstractCategoricalEncoder;
import com.datumbox.framework.core.machinelearning.common.abstracts.transformers.AbstractNumericalScaler;
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
public class Modeler extends AbstractModeler<Modeler.ModelParameters, Modeler.TrainingParameters> implements Parallelizable {

    private static final String NS_KEY = "ns";
    private static final String CE_KEY = "ce";
    private static final String FS_KEY = "fs";
    private static final String ML_KEY = "ml";

    private final TrainableBundle bundle;

    /**
     * It contains all the Model Parameters which are learned during the training.
     */
    public static class ModelParameters extends AbstractModeler.AbstractModelParameters {
        private static final long serialVersionUID = 1L;
        
        /**
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected ModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
        }

    }
    
    /**
     * It contains the Training Parameters of the Modeler.
     */
    public static class TrainingParameters extends AbstractModeler.AbstractTrainingParameters {
        private static final long serialVersionUID = 1L;

        //Parameter Objects
        private AbstractNumericalScaler.AbstractTrainingParameters numericalScalerTrainingParameters;
        private AbstractCategoricalEncoder.AbstractTrainingParameters categoricalEncoderTrainingParameters;
        private AbstractFeatureSelector.AbstractTrainingParameters featureSelectorTrainingParameters;
        private AbstractModeler.AbstractTrainingParameters modelerTrainingParameters;

        /**
         * Getter for the Training Parameters of the numerical scaler.
         *
         * @return
         */
        public AbstractNumericalScaler.AbstractTrainingParameters getNumericalScalerTrainingParameters() {
            return numericalScalerTrainingParameters;
        }

        /**
         * Setter for the Training Parameters of the numerical scaler.
         *
         * @param numericalScalerTrainingParameters
         */
        public void setNumericalScalerTrainingParameters(AbstractNumericalScaler.AbstractTrainingParameters numericalScalerTrainingParameters) {
            this.numericalScalerTrainingParameters = numericalScalerTrainingParameters;
        }

        /**
         * Getter for the Training Parameters of the categorical encoder.
         *
         * @return
         */
        public AbstractCategoricalEncoder.AbstractTrainingParameters getCategoricalEncoderTrainingParameters() {
            return categoricalEncoderTrainingParameters;
        }

        /**
         * Setter for the Training Parameters of the categorical encoder.
         *
         * @param categoricalEncoderTrainingParameters
         */
        public void setCategoricalEncoderTrainingParameters(AbstractCategoricalEncoder.AbstractTrainingParameters categoricalEncoderTrainingParameters) {
            this.categoricalEncoderTrainingParameters = categoricalEncoderTrainingParameters;
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

    /** {@inheritDoc} */
    public void _predict(Dataframe newData) {
        //load all trainables on the bundles
        initBundle();

        //set the parallized flag to all algorithms
        bundle.setParallelized(isParallelized());

        //run the pipeline
        AbstractNumericalScaler numericalScaler = (AbstractNumericalScaler) bundle.get(NS_KEY);
        if(numericalScaler != null) {
            numericalScaler.transform(newData);
        }
        AbstractCategoricalEncoder categoricalEncoder = (AbstractCategoricalEncoder) bundle.get(CE_KEY);
        if(categoricalEncoder != null) {
            categoricalEncoder.transform(newData);
        }
        AbstractFeatureSelector featureSelector = (AbstractFeatureSelector) bundle.get(FS_KEY);
        if(featureSelector != null) {
            featureSelector.transform(newData);
        }
        AbstractModeler modeler = (AbstractModeler) bundle.get(ML_KEY);
        modeler.predict(newData);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        TrainingParameters trainingParameters = knowledgeBase.getTrainingParameters();
        Configuration configuration = knowledgeBase.getConfiguration();

        //reset previous entries on the bundle
        resetBundle();

        //initialize the parts of the pipeline
        AbstractNumericalScaler.AbstractTrainingParameters nsParams = trainingParameters.getNumericalScalerTrainingParameters();
        AbstractNumericalScaler numericalScaler = null;
        if(nsParams != null) {
            numericalScaler = MLBuilder.create(nsParams, configuration);
        }
        bundle.put(NS_KEY, numericalScaler);

        AbstractCategoricalEncoder.AbstractTrainingParameters ceParams = trainingParameters.getCategoricalEncoderTrainingParameters();
        AbstractCategoricalEncoder categoricalEncoder = null;
        if(ceParams != null) {
            categoricalEncoder = MLBuilder.create(ceParams, configuration);
        }
        bundle.put(CE_KEY, categoricalEncoder);

        AbstractFeatureSelector.AbstractTrainingParameters fsParams = trainingParameters.getFeatureSelectorTrainingParameters();
        AbstractFeatureSelector featureSelector = null;
        if(fsParams != null) {
            featureSelector = MLBuilder.create(fsParams, configuration);
        }
        bundle.put(FS_KEY, featureSelector);

        AbstractModeler.AbstractTrainingParameters mlParams = trainingParameters.getModelerTrainingParameters();
        AbstractModeler modeler = MLBuilder.create(mlParams, configuration);
        bundle.put(ML_KEY, modeler);

        //set the parallized flag to all algorithms
        bundle.setParallelized(isParallelized());

        //run the pipeline
        if(numericalScaler != null) {
            numericalScaler.fit_transform(trainingData);
        }
        if(categoricalEncoder != null) {
            categoricalEncoder.fit_transform(trainingData);
        }
        if(featureSelector != null) {
            featureSelector.fit_transform(trainingData);
        }
        modeler.fit(trainingData);
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
        String storageName = knowledgeBase.getStorageEngine().getStorageName();
        String separator = configuration.getStorageConfiguration().getStorageNameSeparator();

        if(!bundle.containsKey(NS_KEY)) {
            AbstractNumericalScaler.AbstractTrainingParameters nsParams = trainingParameters.getNumericalScalerTrainingParameters();

            AbstractNumericalScaler numericalScaler = null;
            if(nsParams != null) {
                numericalScaler = MLBuilder.load(nsParams.getTClass(), storageName + separator + NS_KEY, configuration);
            }
            bundle.put(NS_KEY, numericalScaler);
        }

        if(!bundle.containsKey(CE_KEY)) {
            AbstractCategoricalEncoder.AbstractTrainingParameters ceParams = trainingParameters.getCategoricalEncoderTrainingParameters();

            AbstractCategoricalEncoder categoricalEncoder = null;
            if(ceParams != null) {
                categoricalEncoder = MLBuilder.load(ceParams.getTClass(), storageName + separator + CE_KEY, configuration);
            }
            bundle.put(CE_KEY, categoricalEncoder);
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
