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
import com.datumbox.framework.common.dataobjects.*;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.algorithms.AbstractBoostingBagging;
import com.datumbox.framework.core.statistics.descriptivestatistics.Descriptives;

import java.util.List;
import java.util.Map;

/**
 * Implementation of Adaboost algorithm.
 * 
 * References: 
 * ftp://vista.eng.tau.ac.il/dropbox/Alon.Harell/samme.pdf
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Adaboost extends AbstractBoostingBagging<Adaboost.ModelParameters, Adaboost.TrainingParameters> {
 
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
    protected Adaboost(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected Adaboost(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }

    /** {@inheritDoc} */
    @Override
    protected Status updateObservationAndClassifierWeights(Dataframe validationDataset, AssociativeArray observationWeights) {
        //calculate prediction error for this classifier
        double error = 0.0;
        for(Map.Entry<Integer, Record> e : validationDataset.entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            if(!r.getY().equals(r.getYPredicted())) {
                error+= TypeInference.toDouble(observationWeights.get(rId));
            }
        }
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();

        Status status;
        int c = modelParameters.getC();
        if((1.0-error)<=1.0/c) { //if accuracy is less than random
            status = Status.IGNORE; //do not include the classifier in our list
        }
        else {
            status = Status.NEXT; //continue training
            
            if(error==0.0) { //if the error is 0.0 stop, you can't make it bettter
                status = Status.STOP;
                error=1e-8;
            }
            
            List<Double> weakClassifierWeights = modelParameters.getWeakClassifierWeights();

            //add the new weak learner in the list of weights
            double weight = Math.log((1.0-error)/error)+Math.log(c-1.0);
            weakClassifierWeights.add(weight);

            //update the weights of observations
            for(Map.Entry<Integer, Record> e : validationDataset.entries()) {
                Integer rId = e.getKey();
                Record r = e.getValue();
                if(!r.getY().equals(r.getYPredicted())) {
                    Double value = TypeInference.toDouble(observationWeights.get(rId));
                    observationWeights.put(rId, value*Math.exp(weight)); //increase the weight for misclassified observations
                }
            }
            
            //normalize weights
            double normalizer = Descriptives.sum(new FlatDataCollection(observationWeights.values()));
            if(normalizer!=0.0) {
                for(Map.Entry<Object, Object> entry : observationWeights.entrySet()) {
                    Double value = TypeInference.toDouble(entry.getValue());
                    observationWeights.put(entry.getKey(), value/normalizer);
                }
            }
            
        }
        
        return status; 
    }

}
