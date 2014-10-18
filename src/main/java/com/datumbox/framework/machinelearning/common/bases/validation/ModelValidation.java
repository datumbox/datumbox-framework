/* 
 * Copyright (C) 2014 Vasilis Vryniotis <bbriniotis at datumbox.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.datumbox.framework.machinelearning.common.bases.validation;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.utilities.DeepCopy;
import com.datumbox.common.utilities.PHPfunctions;
import com.datumbox.configuration.GeneralConfiguration;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.configuration.StorageConfiguration;
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public abstract class ModelValidation<MP extends BaseMLmodel.ModelParameters, TP extends BaseMLmodel.TrainingParameters, VM extends BaseMLmodel.ValidationMetrics> {
    
    public static final String DB_INDICATOR="Kfold";
    
    public ModelValidation() {
        super();
    }
    
    public VM kFoldCrossValidation(Dataset dataset, int k, String dbName, Class<? extends BaseMLmodel> aClass, TP trainingParameters, MemoryConfiguration memoryConfiguration) {
        int n = dataset.size();
        if(k<=0 || n<=k) {
            throw new IllegalArgumentException("Invalid number of folds");
        }
        
        int foldSize= n/k; //floor the number
        
        
        //shuffle the ids of the records
        Integer[] ids = new Integer[n];
        int j =0;
        for(Record r : dataset) {
            ids[j]=r.getId();
            ++j;
        }
        PHPfunctions.shuffle(ids);
        
        BaseMLmodel<MP, TP, VM> mlmodel = null;
        
        String foldDBname=dbName+StorageConfiguration.getDBnameSeparator()+DB_INDICATOR;
        
        List<VM> validationMetricsList = new LinkedList<>();
        for(int fold=0;fold<k;++fold) {
            
            if(GeneralConfiguration.DEBUG) {
                System.out.println("Kfold "+(fold+1));
            }
            
            //as fold window we consider the part of the ids that are used for validation
            List<Integer> foldTrainingIds = new ArrayList<>(n-foldSize);
            List<Integer> foldValidationIds = new ArrayList<>(foldSize);
            
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
            mlmodel = BaseMLmodel.newInstance(aClass, foldDBname);
            
            //set the temporary flag on
            mlmodel.setTemporary(true);
            
            //set training configuration
            mlmodel.initializeTrainingConfiguration(memoryConfiguration, trainingParameters);
                        
            //shallow copy for the trainingData and the validaitonData. 
            Dataset trainingData = dataset.generateNewSubset(foldTrainingIds);
            Dataset validationData = dataset.generateNewSubset(foldValidationIds);
            
            //if the data are modified produce a deep copy
            boolean algorithmModifiesDataset = mlmodel.modifiesData();
            if(algorithmModifiesDataset) {
                trainingData = DeepCopy.<Dataset>cloneObject(trainingData);
                validationData = DeepCopy.<Dataset>cloneObject(validationData);
            }
            
            
            //train it
            mlmodel.train(trainingData, validationData);
            
            
            
            //fetch validation metrics
            VM entrySample = mlmodel.getValidationMetrics();
            
            //perform complete erase ONLY if it is the last fold. 
            //There is unnecessary code here which is written for clarification purposes.
            boolean isLast=(fold==k-1);
            boolean complete=(isLast)?true:false; 
            
            //delete algorithm
            mlmodel.erase(complete);
            mlmodel = null;
            
            //add the validationMetrics in the list
            validationMetricsList.add(entrySample);
            
            /*
            //if we used the original data then reset all predictions. this is unnecessary. we never use the predictions, we always overwrite them
            if(!algorithmModifiesDataset) {
                for(Record r : trainingData) {
                    r.setYPredicted(null);
                }
                for(Record r : validationData) {
                    r.setYPredicted(null);
                }
            }
            */
        }
        
        
        VM avgValidationMetrics = calculateAverageValidationMetrics(validationMetricsList);
        
        return avgValidationMetrics;
    }
    
    public abstract VM calculateAverageValidationMetrics(List<VM> validationMetricsList);
    

}
