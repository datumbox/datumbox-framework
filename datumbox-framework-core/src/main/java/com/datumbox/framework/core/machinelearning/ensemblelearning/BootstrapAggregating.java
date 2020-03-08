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
package com.datumbox.framework.core.machinelearning.ensemblelearning;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.algorithms.AbstractBoostingBagging;

import java.util.List;

/**
 * Implementation of Bagging algorithm.
 * 
 * References: 
 * www.cis.temple.edu/~latecki/Courses/AI-Fall10/Lectures/ch7EL.ppt         
 * http://www2.icmc.usp.br/~moacir/papers/PontiJr_TutorialMCS_SIBGRAPI2011.pdf
 * https://en.wikipedia.org/wiki/Bootstrap_aggregating 
 * http://artint.info/html/ArtInt_184.html
 * http://www.cs.man.ac.uk/~gbrown/research/brown10ensemblelearning.pdf
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class BootstrapAggregating extends AbstractBoostingBagging<BootstrapAggregating.ModelParameters, BootstrapAggregating.TrainingParameters> {

    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractBoostingBagging.AbstractModelParameters {
        private static final long serialVersionUID = 1L;

        /** 
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected ModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
        }
        
    } 

    /** {@inheritDoc} */
    public static class TrainingParameters extends AbstractBoostingBagging.AbstractTrainingParameters { 
        private static final long serialVersionUID = 1L;
        
    }

    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainer.AbstractTrainingParameters, Configuration)
     */
    protected BootstrapAggregating(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected BootstrapAggregating(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }

    /** {@inheritDoc} */
    @Override
    protected Status updateObservationAndClassifierWeights(Dataframe validationDataset, AssociativeArray observationWeights) {
        //no update on the observationWeights, all observations have equal probability 1/n
        
        //update classifier weights with equal weights
        List<Double> weakClassifierWeights = knowledgeBase.getModelParameters().getWeakClassifierWeights();

        //add the new weak learner in the list of weights
        weakClassifierWeights.add(0.0);
        
        int numberOfWeakClassifiers = weakClassifierWeights.size();
        double weight = 1.0/numberOfWeakClassifiers;
        for(int t=0;t<numberOfWeakClassifiers;++t) {
            weakClassifierWeights.set(t, weight); //equal weight to all classifiers
        }
        
        return Status.NEXT; //always go to next 
    }

}
