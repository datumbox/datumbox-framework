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
package com.datumbox.framework.core.machinelearning.ensemblelearning;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.core.machinelearning.common.abstracts.algorithms.AbstractBoostingBagging;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;

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
public class BootstrapAggregating extends AbstractBoostingBagging<BootstrapAggregating.ModelParameters, BootstrapAggregating.TrainingParameters, BootstrapAggregating.ValidationMetrics> {

    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractBoostingBagging.AbstractModelParameters {
        private static final long serialVersionUID = 1L;

        /** 
         * @param dbc
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(DatabaseConnector)
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
    } 

    /** {@inheritDoc} */
    public static class TrainingParameters extends AbstractBoostingBagging.AbstractTrainingParameters { 
        private static final long serialVersionUID = 1L;
        
    } 
    
    /** {@inheritDoc} */
    public static class ValidationMetrics extends AbstractBoostingBagging.AbstractValidationMetrics {
        private static final long serialVersionUID = 1L;

    }
    
    /**
     * Public constructor of the algorithm.
     * 
     * @param dbName
     * @param conf 
     */
    public BootstrapAggregating(String dbName, Configuration conf) {
        super(dbName, conf, BootstrapAggregating.ModelParameters.class, BootstrapAggregating.TrainingParameters.class, BootstrapAggregating.ValidationMetrics.class);
    } 

    /** {@inheritDoc} */
    @Override
    protected Status updateObservationAndClassifierWeights(Dataframe validationDataset, AssociativeArray observationWeights) {
        //no update on the observationWeights, all observations have equal probability 1/n
        
        //update classifier weights with equal weights
        List<Double> weakClassifierWeights = kb().getModelParameters().getWeakClassifierWeights();

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
