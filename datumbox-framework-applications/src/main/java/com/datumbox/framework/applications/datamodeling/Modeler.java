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
package com.datumbox.framework.applications.datamodeling;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.core.machinelearning.MLBuilder;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.transformers.AbstractEncoder;
import com.datumbox.framework.core.machinelearning.common.abstracts.transformers.AbstractScaler;
import com.datumbox.framework.core.machinelearning.common.abstracts.featureselectors.AbstractFeatureSelector;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelers.AbstractModeler;
import com.datumbox.framework.core.machinelearning.common.dataobjects.TrainableBundle;
import com.datumbox.framework.core.machinelearning.common.interfaces.Parallelizable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Modeler is a convenience class which can be used to train Machine Learning
 * models. It is a wrapper class which automatically takes care of the data 
 transformation, feature selection and modeler training processes.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Modeler extends AbstractModeler<Modeler.ModelParameters, Modeler.TrainingParameters> implements Parallelizable {
    /**
     * Key for Numeric Scaling.
     */
    protected static final String NS_KEY = "ns";

    /**
     * Key for Categorical Encoding.
     */
    protected static final String CE_KEY = "ce";

    /**
     * Key for Feature Selection.
     */
    protected static final String FS_KEY = "fs";

    /**
     * Key for Modeling.
     */
    protected static final String ML_KEY = "ml";

    /**
     * The steps of the pipeline.
     */
    protected List<String> pipeline = Arrays.asList(NS_KEY, CE_KEY, FS_KEY, ML_KEY);

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
        private AbstractScaler.AbstractTrainingParameters numericalScalerTrainingParameters;
        private AbstractEncoder.AbstractTrainingParameters categoricalEncoderTrainingParameters;
        private List<AbstractTrainingParameters> featureSelectorTrainingParametersList = new ArrayList<>();
        private AbstractModeler.AbstractTrainingParameters modelerTrainingParameters;

        /**
         * Getter for the Training Parameters of the numerical scaler.
         *
         * @return
         */
        public AbstractScaler.AbstractTrainingParameters getNumericalScalerTrainingParameters() {
            return numericalScalerTrainingParameters;
        }

        /**
         * Setter for the Training Parameters of the numerical scaler.
         *
         * @param numericalScalerTrainingParameters
         */
        public void setNumericalScalerTrainingParameters(AbstractScaler.AbstractTrainingParameters numericalScalerTrainingParameters) {
            this.numericalScalerTrainingParameters = numericalScalerTrainingParameters;
        }

        /**
         * Getter for the Training Parameters of the categorical encoder.
         *
         * @return
         */
        public AbstractEncoder.AbstractTrainingParameters getCategoricalEncoderTrainingParameters() {
            return categoricalEncoderTrainingParameters;
        }

        /**
         * Setter for the Training Parameters of the categorical encoder.
         *
         * @param categoricalEncoderTrainingParameters
         */
        public void setCategoricalEncoderTrainingParameters(AbstractEncoder.AbstractTrainingParameters categoricalEncoderTrainingParameters) {
            this.categoricalEncoderTrainingParameters = categoricalEncoderTrainingParameters;
        }

        /**
         * Getter for the Training Parameters of the Feature Selectors.
         *
         * @return
         */
        public List<AbstractFeatureSelector.AbstractTrainingParameters> getFeatureSelectorTrainingParametersList() {
            return featureSelectorTrainingParametersList;
        }

        /**
         * Setter for the Training Parameters of the Feature Selectors.
         *
         * @param featureSelectorTrainingParametersList
         */
        public void setFeatureSelectorTrainingParametersList(List<AbstractFeatureSelector.AbstractTrainingParameters> featureSelectorTrainingParametersList) {
            this.featureSelectorTrainingParametersList = featureSelectorTrainingParametersList;
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
        for(String step : pipeline) {
            switch (step) {
                case NS_KEY:
                    AbstractScaler numericalScaler = (AbstractScaler) bundle.get(NS_KEY);
                    if(numericalScaler != null) {
                        numericalScaler.transform(newData);
                    }
                    break;
                case CE_KEY:
                    AbstractEncoder categoricalEncoder = (AbstractEncoder) bundle.get(CE_KEY);
                    if(categoricalEncoder != null) {
                        categoricalEncoder.transform(newData);
                    }
                    break;
                case FS_KEY:
                    int numOfFS = getTrainingParameters().getFeatureSelectorTrainingParametersList().size();
                    for(int i=0;i<numOfFS;i++) {
                        AbstractFeatureSelector featureSelector = (AbstractFeatureSelector) bundle.get(FS_KEY+i);
                        featureSelector.transform(newData);
                    }
                    break;
                case ML_KEY:
                    AbstractModeler modeler = (AbstractModeler) bundle.get(ML_KEY);
                    modeler.predict(newData);
                    break;
                default:
                    throw new RuntimeException("Invalid Pipeline Step");
            }
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
        AbstractScaler.AbstractTrainingParameters nsParams = trainingParameters.getNumericalScalerTrainingParameters();
        AbstractScaler numericalScaler = null;
        if(nsParams != null) {
            numericalScaler = MLBuilder.create(nsParams, configuration);
        }
        bundle.put(NS_KEY, numericalScaler);

        AbstractEncoder.AbstractTrainingParameters ceParams = trainingParameters.getCategoricalEncoderTrainingParameters();
        AbstractEncoder categoricalEncoder = null;
        if(ceParams != null) {
            categoricalEncoder = MLBuilder.create(ceParams, configuration);
        }
        bundle.put(CE_KEY, categoricalEncoder);

        List<AbstractFeatureSelector.AbstractTrainingParameters> fsParamsList = trainingParameters.getFeatureSelectorTrainingParametersList();
        int numOfFS = fsParamsList.size();
        for(int i=0;i<numOfFS;i++) {
            AbstractFeatureSelector.AbstractTrainingParameters fsParams = fsParamsList.get(i);
            AbstractFeatureSelector featureSelector = MLBuilder.create(fsParams, configuration);
            bundle.put(FS_KEY+i, featureSelector);
        }

        AbstractModeler.AbstractTrainingParameters mlParams = trainingParameters.getModelerTrainingParameters();
        AbstractModeler modeler = MLBuilder.create(mlParams, configuration);
        bundle.put(ML_KEY, modeler);

        //set the parallized flag to all algorithms
        bundle.setParallelized(isParallelized());

        //run the pipeline
        for(String step : pipeline) {
            switch (step) {
                case NS_KEY:
                    if(numericalScaler != null) {
                        numericalScaler.fit_transform(trainingData);
                    }
                    break;
                case CE_KEY:
                    if(categoricalEncoder != null) {
                        categoricalEncoder.fit_transform(trainingData);
                    }
                    break;
                case FS_KEY:
                    for(int i=0;i<numOfFS;i++) {
                        AbstractFeatureSelector featureSelector = (AbstractFeatureSelector) bundle.get(FS_KEY+i);
                        featureSelector.fit_transform(trainingData);
                    }
                    break;
                case ML_KEY:
                    modeler.fit(trainingData);
                    break;
                default:
                    throw new RuntimeException("Invalid Pipeline Step");
            }
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
        String storageName = knowledgeBase.getStorageEngine().getStorageName();
        String separator = configuration.getStorageConfiguration().getStorageNameSeparator();

        if(!bundle.containsKey(NS_KEY)) {
            AbstractScaler.AbstractTrainingParameters nsParams = trainingParameters.getNumericalScalerTrainingParameters();

            AbstractScaler numericalScaler = null;
            if(nsParams != null) {
                numericalScaler = MLBuilder.load(nsParams.getTClass(), storageName + separator + NS_KEY, configuration);
            }
            bundle.put(NS_KEY, numericalScaler);
        }

        if(!bundle.containsKey(CE_KEY)) {
            AbstractEncoder.AbstractTrainingParameters ceParams = trainingParameters.getCategoricalEncoderTrainingParameters();

            AbstractEncoder categoricalEncoder = null;
            if(ceParams != null) {
                categoricalEncoder = MLBuilder.load(ceParams.getTClass(), storageName + separator + CE_KEY, configuration);
            }
            bundle.put(CE_KEY, categoricalEncoder);
        }


        List<AbstractFeatureSelector.AbstractTrainingParameters> fsParamsList = trainingParameters.getFeatureSelectorTrainingParametersList();
        int numOfFS = fsParamsList.size();
        for(int i=0;i<numOfFS;i++) {
            if(!bundle.containsKey(FS_KEY+i)) {
                AbstractFeatureSelector.AbstractTrainingParameters fsParams = fsParamsList.get(i);
                AbstractFeatureSelector featureSelector = MLBuilder.load(fsParams.getTClass(), storageName + separator + FS_KEY + i, configuration);
                bundle.put(FS_KEY+i, featureSelector);
            }
        }

        if(!bundle.containsKey(ML_KEY)) {
            AbstractModeler.AbstractTrainingParameters mlParams = trainingParameters.getModelerTrainingParameters();

            bundle.put(ML_KEY, MLBuilder.load(mlParams.getTClass(), storageName + separator + ML_KEY, configuration));
        }
    }

}
