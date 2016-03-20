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
package com.datumbox.framework.core.machinelearning.common.abstracts.validators;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.FlatDataList;
import com.datumbox.framework.common.interfaces.Trainable;
import com.datumbox.framework.common.utilities.PHPMethods;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelers.AbstractModeler;
import com.datumbox.framework.core.machinelearning.common.interfaces.ModelParameters;
import com.datumbox.framework.core.machinelearning.common.interfaces.TrainingParameters;
import com.datumbox.framework.core.machinelearning.common.interfaces.ValidationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The AbstractValidator class is an abstract class responsible for the K-fold Cross
 Validation and for the estimation of the average validation metrics. Given that
 * different models use different validation metrics, each model family implements
 * its own validator.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public abstract class AbstractValidator<MP extends ModelParameters, TP extends TrainingParameters, VM extends ValidationMetrics> {
    
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
     * @param conf
     * @param aClass
     * @param trainingParameters
     * @return 
     */
    public VM kFoldCrossValidation(Dataframe dataset, int k, String dbName, Configuration conf, Class<? extends AbstractModeler> aClass, TP trainingParameters) {
        int n = dataset.size();
        if(k<=0 || n<=k) {
            throw new IllegalArgumentException("Invalid number of folds.");
        }
        
        int foldSize= n/k; //floor the number
        
        
        //shuffle the ids of the records
        Integer[] ids = new Integer[n];
        int j =0;
        for(Integer rId : dataset.index()) {
            ids[j]=rId;
            ++j;
        }
        PHPMethods.shuffle(ids);
        
        String foldDBname=dbName+conf.getDbConfig().getDBnameSeparator()+DB_INDICATOR;
        
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
            
            
            //initialize modeler
            AbstractModeler modeler = Trainable.<AbstractModeler>newInstance((Class<AbstractModeler>)aClass, foldDBname+(fold+1), conf);
            
            
            Dataframe trainingData = dataset.getSubset(foldTrainingIds);
            modeler.fit(trainingData, (AbstractTrainer.AbstractTrainingParameters) trainingParameters);
            trainingData.delete();
            //trainingData = null;
                        
            
            Dataframe validationData = dataset.getSubset(foldValidationIds);
            
            //fetch validation metrics
            VM entrySample = (VM) modeler.validate(validationData);
            validationData.delete();
            //validationData = null;
            
            //delete algorithm
            modeler.delete();
            //modeler = null;
            
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
