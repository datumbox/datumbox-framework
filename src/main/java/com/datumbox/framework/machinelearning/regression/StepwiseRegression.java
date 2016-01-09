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
package com.datumbox.framework.machinelearning.regression;

import com.datumbox.framework.machinelearning.common.interfaces.StepwiseCompatible;
import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.interfaces.Trainable;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.utilities.MapMethods;
import com.datumbox.framework.machinelearning.common.abstracts.modelers.AbstractRegressor;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Implementation of Stepwise Regression algorithm. The method takes as argument
 * an other regression model which uses to estimate the best model. This implementation
 * uses the backwards elimination algorithm.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class StepwiseRegression extends AbstractRegressor<StepwiseRegression.ModelParameters, StepwiseRegression.TrainingParameters, AbstractRegressor.ValidationMetrics>  {
    
    private transient AbstractRegressor mlregressor = null;
    
    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractRegressor.AbstractModelParameters {
        private static final long serialVersionUID = 1L;

        /** 
         * @param dbc
         * @see com.datumbox.framework.machinelearning.common.abstracts.AbstractTrainer.AbstractModelParameters#AbstractModelParameters(com.datumbox.common.persistentstorage.interfaces.DatabaseConnector) 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
        //EMPTY Model parameters. It relies on the mlregressor DB instead        
    } 

    /** {@inheritDoc} */
    public static class TrainingParameters extends AbstractRegressor.AbstractTrainingParameters {
        private static final long serialVersionUID = 1L;
        
        //primitives/wrappers
        private Integer maxIterations = null;
        
        private Double aout = 0.05;
        
        //Classes
        private Class<? extends AbstractRegressor> regressionClass;

        //Parameter Objects
        private AbstractRegressor.AbstractTrainingParameters regressionTrainingParameters;

        //Field Getters/Setters

        /**
         * Getter for the maximum permitted iterations during training.
         * 
         * @return 
         */
        public Integer getMaxIterations() {
            return maxIterations;
        }

        /**
         * Setter for the maximum permitted iterations during training.
         * 
         * @param maxIterations 
         */
        public void setMaxIterations(Integer maxIterations) {
            this.maxIterations = maxIterations;
        }
        
        /**
         * Getter for the threshold of the maximum p-value; a variable must
         * have a p-value less or equal than the threshold to be retained in the 
         * model. Variables with higher p-value than this threshold are removed.
         * 
         * 
         * @return 
         */
        public Double getAout() {
            return aout;
        }
        
        /**
         * Setter for the threshold of the maximum p-value; a variable must
         * have a p-value less or equal than the threshold to be retained in the 
         * model. Variables with higher p-value than this threshold are removed.
         * 
         * @param aout 
         */
        public void setAout(Double aout) {
            this.aout = aout;
        }
        
        /**
         * Getter for the class of the regression algorithm that is used internally
         * by the Stepwise Regression. The regressor must implement the StepwiseCompatible
         * interface in order to be used in the analysis.
         * 
         * @return 
         */
        public Class<? extends AbstractRegressor> getRegressionClass() {
            return regressionClass;
        }
        
        /**
         * Setter for the class of the regression algorithm that is used internally
         * by the Stepwise Regression. The regressor must implement the StepwiseCompatible
         * interface in order to be used in the analysis.
         * 
         * @param regressionClass 
         */
        public void setRegressionClass(Class<? extends AbstractRegressor> regressionClass) {
            if(!StepwiseCompatible.class.isAssignableFrom(regressionClass)) {
                throw new IllegalArgumentException("The regression model is not Stepwise Compatible.");
            }
            this.regressionClass = regressionClass;
        }
        
        /**
         * Getter for the training parameters of the regression algorithm that is
         * used internally by the Stepwise Regression.
         * 
         * @return 
         */
        public AbstractRegressor.AbstractTrainingParameters getRegressionTrainingParameters() {
            return regressionTrainingParameters;
        }
        
        /**
         * Setter for the training parameters of the regression algorithm that is
         * used internally by the Stepwise Regression.
         * 
         * @param regressionTrainingParameters 
         */
        public void setRegressionTrainingParameters(AbstractRegressor.AbstractTrainingParameters regressionTrainingParameters) {
            this.regressionTrainingParameters = regressionTrainingParameters;
        }
        
    }


    /*
    The no ValidationMetrics Class here!!!!!! The algorithm fetches the
    validations metrics of the mlregressor and cast them to AbstractRegressor.ValidationMetrics.
    */
    //public static class ValidationMetrics extends AbstractRegressor.ValidationMetrics { }
    
    
    /**
     * Public constructor of the algorithm.
     * 
     * @param dbName
     * @param dbConf 
     */
    public StepwiseRegression(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, StepwiseRegression.ModelParameters.class, StepwiseRegression.TrainingParameters.class, StepwiseRegression.ValidationMetrics.class, null); //do not define a validator. pass null and overload the kcross validation method to validate with the mlregressor object
    } 
     
    /** {@inheritDoc} */
    @Override
    public void delete() {
        loadRegressor();
        mlregressor.delete();
        mlregressor = null;
        
        super.delete();
    }
         
    /** {@inheritDoc} */
    @Override
    public void close() {
        loadRegressor();
        mlregressor.close();
        mlregressor = null;
        
        super.close();
    }
    
    /**
     * Performs k-fold cross validation on the dataset using an already trained
     * Regressor and returns its ValidationMetrics Object. Before calling this method
     * you must have already trained a model.
     * 
     * @param trainingData
     * @param trainingParameters
     * @param k
     * @return 
     */
    @Override
    public AbstractRegressor.ValidationMetrics kFoldCrossValidation(Dataframe trainingData, TrainingParameters trainingParameters, int k) {
        if(mlregressor == null) {
            throw new RuntimeException("You need to train a Regressor before running k-fold cross validation.");
        }
        else {
            return (ValidationMetrics) mlregressor.kFoldCrossValidation(trainingData, trainingParameters, k);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    protected AbstractRegressor.ValidationMetrics validateModel(Dataframe validationData) {
        loadRegressor();
        
        return (AbstractRegressor.ValidationMetrics) mlregressor.validate(validationData);
    }

    /** {@inheritDoc} */
    @Override
    protected void _predictDataset(Dataframe newData) {
        loadRegressor();
        
        mlregressor.predict(newData);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        TrainingParameters trainingParameters = kb().getTrainingParameters();
        
        Integer maxIterations = trainingParameters.getMaxIterations();
        if(maxIterations==null) {
            maxIterations = Integer.MAX_VALUE;
        }
        double aOut = trainingParameters.getAout();
        
        //copy data before starting
        Dataframe copiedTrainingData = trainingData.copy();
        
        //backword elimination algorithm
        for(int iteration = 0; iteration<maxIterations ; ++iteration) {
            
            Map<Object, Double> pvalues = runRegression(copiedTrainingData);
            
            if(pvalues.isEmpty()) {
                break; //no more features
            }
            
            //fetch the feature with highest pvalue, excluding constant
            pvalues.remove(Dataframe.COLUMN_NAME_CONSTANT);
            Map.Entry<Object, Double> maxPvalueEntry = MapMethods.selectMaxKeyValue(pvalues);
            //pvalues=null;
            
            if(maxPvalueEntry.getValue()<=aOut) {
                break; //nothing to remove, the highest pvalue is less than the aOut
            }
            
            
            Set<Object> removedFeatures = new HashSet<>();
            removedFeatures.add(maxPvalueEntry.getKey());
            copiedTrainingData.dropXColumns(removedFeatures);
            //removedFeatures = null;
            
            if(copiedTrainingData.xColumnSize()==0) {
                break; //if no more features exit
            }
        }
        
        //once we have the dataset has been cleared from the unnecessary columns train the model once again
        mlregressor = generateRegressor();
        
        mlregressor.fit(copiedTrainingData, trainingParameters.getRegressionTrainingParameters());
        copiedTrainingData.delete();
        //copiedTrainingData = null;
    }

    private void loadRegressor() {
        if(mlregressor==null) {
            mlregressor = generateRegressor();
        }
    }
    
    private AbstractRegressor generateRegressor() {
        return Trainable.<AbstractRegressor>newInstance((Class<AbstractRegressor>) kb().getTrainingParameters().getRegressionClass(), dbName, kb().getDbConf());
    }
    
    private Map<Object, Double> runRegression(Dataframe trainingData) {
        TrainingParameters trainingParameters = kb().getTrainingParameters();
        
        //initialize algorithm
        mlregressor = generateRegressor();

        //train the regressor
        mlregressor.fit(trainingData, trainingParameters.getRegressionTrainingParameters());

        //get pvalues
        Map<Object, Double> pvalues = ((StepwiseCompatible)mlregressor).getFeaturePvalues();
        mlregressor.delete();
        
        return pvalues;
    }
}
