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
package com.datumbox.framework.machinelearning.ensemblelearning;

import com.datumbox.framework.machinelearning.common.bases.basemodels.BaseBoostingBagging;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class Adaboost extends BaseBoostingBagging<Adaboost.ModelParameters, Adaboost.TrainingParameters, Adaboost.ValidationMetrics> {

    /*
    References: 
            ftp://vista.eng.tau.ac.il/dropbox/Alon.Harell/samme.pdf
    */
    
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    public static final String SHORT_METHOD_NAME = "AdaBo";
    
    
    public static class ModelParameters extends BaseBoostingBagging.ModelParameters {
        
    } 

    
    public static class TrainingParameters extends BaseBoostingBagging.TrainingParameters {      
        
    } 
    
    
    public static class ValidationMetrics extends BaseBoostingBagging.ValidationMetrics {

    }
    
    
    public Adaboost(String dbName) {
        super(dbName, Adaboost.ModelParameters.class, Adaboost.TrainingParameters.class, Adaboost.ValidationMetrics.class);
    } 
    
    @Override
    public final String shortMethodName() {
        return SHORT_METHOD_NAME;
    }


    @Override
    protected boolean updateObservationAndClassifierWeights(Dataset validationDataset, Map<Object, Object> observationWeights) { 
        //calculate prediction error for this classifier
        double error = 0.0;
        for(Record r : validationDataset) {
            if(!r.getY().equals(r.getYPredicted())) {
                error+= Dataset.toDouble(observationWeights.get(r.getId()));
            }
        }
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();

        boolean stop;
        int c = modelParameters.getC();
        if((1.0-error)<=1.0/c) { //if accuracy is less than random
            stop = false; //do not include the classifier in our list
        }
        else {
            stop = false; //continue training
            
            if(error==0.0) { //if the error is 0.0 stop, you can't make it bettter
                stop = true;
                error=1e-8;
            }
            
            List<Double> weakClassifierWeights = modelParameters.getWeakClassifierWeights();

            //add the new weak learner in the list of weights
            double weight = Math.log((1.0-error)/error)+Math.log(c-1.0);
            weakClassifierWeights.add(weight);

            //update the weights of observations
            for(Map.Entry<Object, Object> entry : observationWeights.entrySet()) {
                Integer recordId = Dataset.toInteger(entry.getKey());
                Double value = Dataset.toDouble(entry.getValue());

                Record r = validationDataset.get(recordId);
                if(!r.getY().equals(r.getYPredicted())) {
                    observationWeights.put(entry.getKey(), value*Math.exp(weight)); //increase the weight for misclassified observations
                }
            }
            
            //normalize weights
            double normalizer = Descriptives.sum(new FlatDataCollection(observationWeights.values()));
            if(normalizer!=0.0) {
                for(Map.Entry<Object, Object> entry : observationWeights.entrySet()) {
                    Double value = Dataset.toDouble(entry.getValue());
                    observationWeights.put(entry.getKey(), value/normalizer);
                }
            }
            
        }
        
        return stop; 
    }
}
