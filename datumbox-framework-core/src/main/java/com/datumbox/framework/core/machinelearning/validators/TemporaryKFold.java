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
package com.datumbox.framework.core.machinelearning.validators;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.FlatDataList;
import com.datumbox.framework.common.interfaces.Trainable;
import com.datumbox.framework.common.utilities.PHPMethods;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.modelers.AbstractModeler;
import com.datumbox.framework.core.machinelearning.common.abstracts.validators.AbstractValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TemporaryKFold<VM extends AbstractValidator.AbstractValidationMetrics> {

    //TODO: remove this temporary class and create a permanent solution

    /**
     * The Logger of all Validators.
     * We want this to be non-static in order to print the names of the inherited classes.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String DB_INDICATOR="Kfold";

    private final AbstractValidator<VM> validator;

    public TemporaryKFold(AbstractValidator<VM> validator) {
        this.validator = validator;
    }

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
    public VM kFoldCrossValidation(Dataframe dataset, int k, String dbName, Configuration conf, Class<? extends AbstractModeler> aClass, AbstractTrainer.AbstractTrainingParameters trainingParameters) {
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
            modeler.predict(validationData);



            VM entrySample = validator.validate(validationData);
            validationData.delete();
            //validationData = null;

            //delete algorithm
            modeler.delete();
            //modeler = null;

            //add the validationMetrics in the list
            validationMetricsList.add(entrySample);
        }

        VM avgValidationMetrics = validator.average(validationMetricsList);

        return avgValidationMetrics;
    }
}
