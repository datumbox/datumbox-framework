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
package com.datumbox.framework.core.machinelearning.modelselection.validators;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.FlatDataList;
import com.datumbox.framework.common.utilities.PHPMethods;
import com.datumbox.framework.core.machinelearning.MLBuilder;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelers.AbstractModeler;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelselection.validators.AbstractValidator;
import com.datumbox.framework.core.machinelearning.common.interfaces.TrainingParameters;
import com.datumbox.framework.core.machinelearning.common.interfaces.ValidationMetrics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Performs K-fold cross validation.
 *
 * @param <VM>
 */
public class KFoldValidator<VM extends ValidationMetrics> extends AbstractValidator<VM> {

    /**
     * Stores the number of folds.
     */
    private final int k;

    /**
     * The constructor of the K-Fold cross validator.
     *
     * @param vmClass
     * @param k
     */
    public KFoldValidator(Class<VM> vmClass, Configuration conf, int k) {
        super(vmClass, conf);
        this.k = k;
    }

    /**
     * Performs K-fold cross validation by using the provided dataset and number
     * of folds and returns the average metrics across all folds.
     *
     * @param dataset
     * @param trainingParameters
     * @return
     */
    @Override
    public VM validate(Dataframe dataset, TrainingParameters trainingParameters) {
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

        //initialize modeler
        AbstractModeler modeler = MLBuilder.create(trainingParameters, conf);

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


            Dataframe trainingData = dataset.getSubset(foldTrainingIds);
            modeler.fit(trainingData);
            trainingData.delete();


            Dataframe validationData = dataset.getSubset(foldValidationIds);

            //fetch validation metrics
            modeler.predict(validationData);

            VM entrySample = ValidationMetrics.newInstance(vmClass, validationData);
            validationData.delete();

            //add the validationMetrics in the list
            validationMetricsList.add(entrySample);
        }
        modeler.delete();

        VM avgValidationMetrics = ValidationMetrics.newInstance(vmClass, validationMetricsList);

        return avgValidationMetrics;
    }
}
