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
package com.datumbox.framework.machinelearning.ensemblelearning;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.framework.machinelearning.common.bases.basemodels.BaseBoostingBagging;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataCollection;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.utilities.TypeConversions;
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
    
    
    public static class ModelParameters extends BaseBoostingBagging.ModelParameters {

        public ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
    } 

    
    public static class TrainingParameters extends BaseBoostingBagging.TrainingParameters {      
        
    } 
    
    
    public static class ValidationMetrics extends BaseBoostingBagging.ValidationMetrics {

    }
    
    
    public Adaboost(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, Adaboost.ModelParameters.class, Adaboost.TrainingParameters.class, Adaboost.ValidationMetrics.class);
    } 


    @Override
    protected Status updateObservationAndClassifierWeights(Dataset validationDataset, AssociativeArray observationWeights, FlatDataList idMapping) { 
        //calculate prediction error for this classifier
        double error = 0.0;
        for(Integer rId : validationDataset) {
            Record r = validationDataset.get(rId);
            if(!r.getY().equals(r.getYPredicted())) {
                Integer original_rId = (Integer) idMapping.get(rId);
                error+= TypeConversions.toDouble(observationWeights.get(original_rId));
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
            for(Integer rId : validationDataset) {
                Record r = validationDataset.get(rId);
                if(!r.getY().equals(r.getYPredicted())) {
                    Integer original_rId = (Integer) idMapping.get(rId);
                    Double value = TypeConversions.toDouble(observationWeights.get(original_rId));
                    observationWeights.put(original_rId, value*Math.exp(weight)); //increase the weight for misclassified observations
                }
            }
            
            //normalize weights
            double normalizer = Descriptives.sum(new FlatDataCollection(observationWeights.values()));
            if(normalizer!=0.0) {
                for(Map.Entry<Object, Object> entry : observationWeights.entrySet()) {
                    Double value = TypeConversions.toDouble(entry.getValue());
                    observationWeights.put(entry.getKey(), value/normalizer);
                }
            }
            
        }
        
        return status; 
    }
}
