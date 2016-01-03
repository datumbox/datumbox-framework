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
package com.datumbox.framework.machinelearning.ensemblelearning;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.framework.machinelearning.common.bases.basemodels.BaseBoostingBagging;
import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
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
public class Adaboost extends BaseBoostingBagging<Adaboost.ModelParameters, Adaboost.TrainingParameters, Adaboost.ValidationMetrics> {
 
    /**
     * The ModelParameters class stores the coefficients that were learned during
     * the training of the algorithm.
     */
    public static class ModelParameters extends BaseBoostingBagging.ModelParameters {

        /**
         * Protected constructor which accepts as argument the DatabaseConnector.
         * 
         * @param dbc 
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
    } 

    /**
     * The TrainingParameters class stores the parameters that can be changed
     * before training the algorithm.
     */
    public static class TrainingParameters extends BaseBoostingBagging.TrainingParameters {      
        
    } 
        
    /**
     * The ValidationMetrics class stores information about the performance of the
     * algorithm.
     */
    public static class ValidationMetrics extends BaseBoostingBagging.ValidationMetrics {

    }
    
    /**
     * Public constructor of the algorithm.
     * 
     * @param dbName
     * @param dbConf 
     */
    public Adaboost(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, Adaboost.ModelParameters.class, Adaboost.TrainingParameters.class, Adaboost.ValidationMetrics.class);
    } 

    @Override
    protected Status updateObservationAndClassifierWeights(Dataframe validationDataset, AssociativeArray observationWeights, FlatDataList idMapping) { 
        //calculate prediction error for this classifier
        double error = 0.0;
        for(Integer rId : validationDataset.index()) {
            Record r = validationDataset.get(rId);
            if(!r.getY().equals(r.getYPredicted())) {
                Integer original_rId = (Integer) idMapping.get(rId);
                error+= TypeInference.toDouble(observationWeights.get(original_rId));
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
            for(Integer rId : validationDataset.index()) {
                Record r = validationDataset.get(rId);
                if(!r.getY().equals(r.getYPredicted())) {
                    Integer original_rId = (Integer) idMapping.get(rId);
                    Double value = TypeInference.toDouble(observationWeights.get(original_rId));
                    observationWeights.put(original_rId, value*Math.exp(weight)); //increase the weight for misclassified observations
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
