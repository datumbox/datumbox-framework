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
package com.datumbox.framework.machinelearning.common.bases.validation;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.PHPfunctions;

import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel;
import com.datumbox.common.dataobjects.FlatDataList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ModelValidation class is an abstract class responsible for the K-fold Cross
 * Validation and for the estimation of the average validation metrics. Given that
 * different models use different validation metrics, each model family implements
 * its own validator.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public abstract class ModelValidation<MP extends BaseMLmodel.ModelParameters, TP extends BaseMLmodel.TrainingParameters, VM extends BaseMLmodel.ValidationMetrics> {
    
    /**
     * The Logger of all Validators.
     * We want this to be non-static in order to print the names of the inherited classes.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private static final String DB_INDICATOR="Kfold";
    
    /**
     * Performs K-fold cross validation by using the provided dataset and number
     * of folds and returns the average metrics across all folds.
     * 
     * @param dataset
     * @param k
     * @param dbName
     * @param dbConf
     * @param aClass
     * @param trainingParameters
     * @return 
     */
    public VM kFoldCrossValidation(Dataset dataset, int k, String dbName, DatabaseConfiguration dbConf, Class<? extends BaseMLmodel> aClass, TP trainingParameters) {
        int n = dataset.getRecordNumber();
        if(k<=0 || n<=k) {
            throw new IllegalArgumentException("Invalid number of folds");
        }
        
        int foldSize= n/k; //floor the number
        
        
        //shuffle the ids of the records
        Integer[] ids = new Integer[n];
        int j =0;
        for(Integer rId : dataset) {
            ids[j]=rId;
            ++j;
        }
        PHPfunctions.shuffle(ids);
        
        BaseMLmodel<MP, TP, VM> mlmodel = null;
        
        String foldDBname=dbName+dbConf.getDBnameSeparator()+DB_INDICATOR;
        
        List<VM> validationMetricsList = new LinkedList<>();
        for(int fold=0;fold<k;++fold) {
            
            logger.info("Kfold {}", fold);
            
            //as fold window we consider the part of the ids that are used for validation
            FlatDataList foldTrainingIds = new FlatDataList(new ArrayList<>(n-foldSize));
            FlatDataList foldValidationIds = new FlatDataList(new ArrayList<>(foldSize));
            
            for(int i=0;i<n;++i) {
                boolean isInValidationFoldRange = false;
                
                //determine if the current i value is in the validation fold range
                if(fold*foldSize<=i && i<(fold+1)*foldSize) {
                    isInValidationFoldRange = true;
                }
                
                if(isInValidationFoldRange) {
                    foldValidationIds.add(ids[i]);
                }
                else {
                    foldTrainingIds.add(ids[i]);
                }
            }
            
            if(k==1) {
                //if the number of k folds is 1 then the trainindIds are empty
                //and the all the data are on validation fold. In this case
                //we should set the training and validation sets equal
                foldTrainingIds = foldValidationIds;
            }
            
            
            //initialize mlmodel
            mlmodel = BaseMLmodel.newInstance(aClass, foldDBname+(fold+1), dbConf);
            
            
            Dataset trainingData = dataset.generateNewSubset(foldTrainingIds);
            mlmodel.fit(trainingData, trainingParameters); 
            trainingData.erase();
            trainingData = null;
                        
            
            Dataset validationData = dataset.generateNewSubset(foldValidationIds);
            
            //fetch validation metrics
            VM entrySample = mlmodel.validate(validationData);
            validationData.erase();
            validationData = null;
            
            //delete algorithm
            mlmodel.erase();
            mlmodel = null;
            
            //add the validationMetrics in the list
            validationMetricsList.add(entrySample);
        }
        
        VM avgValidationMetrics = calculateAverageValidationMetrics(validationMetricsList);
        
        return avgValidationMetrics;
    }
    
    /**
     * Calculates the average validation metrics by combining the results of the
     * provided list.
     * 
     * @param validationMetricsList
     * @return 
     */
    protected abstract VM calculateAverageValidationMetrics(List<VM> validationMetricsList);
    

}
