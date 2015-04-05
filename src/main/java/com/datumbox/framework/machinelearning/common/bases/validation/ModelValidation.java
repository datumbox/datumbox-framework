/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public abstract class ModelValidation<MP extends BaseMLmodel.ModelParameters, TP extends BaseMLmodel.TrainingParameters, VM extends BaseMLmodel.ValidationMetrics> {
    
    protected final Logger logger;
    
    public static final String DB_INDICATOR="Kfold";
    
    public ModelValidation() {
        logger = LoggerFactory.getLogger(this.getClass());
    }
    
    public VM kFoldCrossValidation(Dataset dataset, int k, String dbName, DatabaseConfiguration dbConf, Class<? extends BaseMLmodel> aClass, TP trainingParameters) {
        int n = dataset.size();
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
            
            logger.info("Kfold "+(fold+1));
            
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
            boolean copyData = mlmodel.modifiesData();
            
            
            Dataset trainingData = dataset.generateNewSubset(foldTrainingIds);
            if(copyData) {
                trainingData = trainingData.copy();
            }
            mlmodel.fit(trainingData, trainingParameters); 
            trainingData = null;
                        
            
            Dataset validationData = dataset.generateNewSubset(foldValidationIds);
            if(copyData) {
                validationData = validationData.copy();
            }
            //fetch validation metrics
            VM entrySample = mlmodel.validate(validationData);
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
    
    public abstract VM calculateAverageValidationMetrics(List<VM> validationMetricsList);
    

}
