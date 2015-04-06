/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.FlatDataList;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import java.util.List;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class BootstrapAggregating extends BaseBoostingBagging<BootstrapAggregating.ModelParameters, BootstrapAggregating.TrainingParameters, BootstrapAggregating.ValidationMetrics> {

    /*
    References: 
            www.cis.temple.edu/~latecki/Courses/AI-Fall10/Lectures/ch7EL.ppt         
            http://www2.icmc.usp.br/~moacir/papers/PontiJr_TutorialMCS_SIBGRAPI2011.pdf
            https://en.wikipedia.org/wiki/Bootstrap_aggregating 
            http://artint.info/html/ArtInt_184.html
            http://www.cs.man.ac.uk/~gbrown/research/brown10ensemblelearning.pdf
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
    
    
    public BootstrapAggregating(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, BootstrapAggregating.ModelParameters.class, BootstrapAggregating.TrainingParameters.class, BootstrapAggregating.ValidationMetrics.class);
    } 


    @Override
    protected Status updateObservationAndClassifierWeights(Dataset validationDataset, AssociativeArray observationWeights, FlatDataList idMapping) {
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
