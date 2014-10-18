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
import java.util.List;
import java.util.Map;

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
    
    public static final String SHORT_METHOD_NAME = "BooAg";
    
    
    public static class ModelParameters extends BaseBoostingBagging.ModelParameters {
        
    } 

    
    public static class TrainingParameters extends BaseBoostingBagging.TrainingParameters {      
        
    } 
    
    
    public static class ValidationMetrics extends BaseBoostingBagging.ValidationMetrics {

    }
    
    
    public BootstrapAggregating(String dbName) {
        super(dbName, BootstrapAggregating.ModelParameters.class, BootstrapAggregating.TrainingParameters.class, BootstrapAggregating.ValidationMetrics.class);
    } 
    
    @Override
    public final String shortMethodName() {
        return SHORT_METHOD_NAME;
    }


    @Override
    protected boolean updateObservationAndClassifierWeights(Dataset validationDataset, Map<Object, Object> observationWeights) {
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
        
        boolean stop = false; //always continue
        return stop; 
    }
}
