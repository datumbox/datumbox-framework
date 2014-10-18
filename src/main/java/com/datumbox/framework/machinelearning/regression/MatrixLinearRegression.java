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
package com.datumbox.framework.machinelearning.regression;

import com.datumbox.framework.machinelearning.common.interfaces.StepwiseCompatible;
import com.datumbox.framework.machinelearning.common.bases.basemodels.BaseLinearRegression;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.MatrixDataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureMarker;
import com.datumbox.common.utilities.PHPfunctions;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.framework.statistics.distributions.ContinuousDistributions;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.mongodb.morphia.annotations.Transient;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class MatrixLinearRegression extends BaseLinearRegression<MatrixLinearRegression.ModelParameters, MatrixLinearRegression.TrainingParameters, MatrixLinearRegression.ValidationMetrics> implements StepwiseCompatible {

    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    public static final String SHORT_METHOD_NAME = "MatLR";

    public static class ModelParameters extends BaseLinearRegression.ModelParameters {

        /**
         * Feature set
         */
        @BigDataStructureMarker
        @Transient
        private Map<Object, Integer> featureIds; //list of all the supported features
        
        /**
         * This is NOT always available. Calculated during training ONLY if the model is
         * configured to. It is useful when we perform StepwiseRegression.
         */
        private Map<Object, Double> featurePvalues; //array with all the pvalues of the features
        
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            super.bigDataStructureInitializer(bdsf, memoryConfiguration);
            
            BigDataStructureFactory.MapType mapType = memoryConfiguration.getMapType();
            int LRUsize = memoryConfiguration.getLRUsize();
            
            featureIds = bdsf.getMap("featureIds", mapType, LRUsize);
        }
        
        public Map<Object, Integer> getFeatureIds() {
            return featureIds;
        }

        public void setFeatureIds(Map<Object, Integer> featureIds) {
            this.featureIds = featureIds;
        }
        
        public Map<Object, Double> getFeaturePvalues() {
            return featurePvalues;
        } 
        
        protected void setFeaturePvalues(Map<Object, Double> featurePvalues) {
            this.featurePvalues = featurePvalues;
        } 
    } 

    
    public static class TrainingParameters extends BaseLinearRegression.TrainingParameters {    

        private boolean calculatePvalue = false;

        public boolean getCalculatePvalue() {
            return calculatePvalue;
        }

        public void setCalculatePvalue(boolean calculatePvalue) {
            this.calculatePvalue = calculatePvalue;
        }
        
    } 
    
    
    public static class ValidationMetrics extends BaseLinearRegression.ValidationMetrics {
        
    }

    
    public MatrixLinearRegression(String dbName) {
        super(dbName, MatrixLinearRegression.ModelParameters.class, MatrixLinearRegression.TrainingParameters.class, MatrixLinearRegression.ValidationMetrics.class);
    }
    
    @Override
    public final String shortMethodName() {
        return SHORT_METHOD_NAME;
    }

    @Override
    protected void estimateModelParameters(Dataset trainingData) {
        
        ModelParameters modelParameters = knowledgeBase.getModelParameters();

        int n = trainingData.size();
        int d = trainingData.getColumnSize()+1;//plus one for the constant
        
        //initialization
        modelParameters.setN(n);
        modelParameters.setD(d);
        
        Map<Object, Double> thitas = modelParameters.getThitas();
        Map<Object, Integer> featureIds = modelParameters.getFeatureIds();
        
        MatrixDataset matrixDataset = MatrixDataset.newInstance(trainingData, true, featureIds);
        
        RealVector Y = matrixDataset.getY();
        RealMatrix X = matrixDataset.getX();
        
        //(X'X)^-1
        RealMatrix Xt = X.transpose();
        LUDecomposition lud = new LUDecomposition(Xt.multiply(X));
        RealMatrix XtXinv = lud.getSolver().getInverse();
        lud =null;
        
        //(X'X)^-1 * X'Y
        RealVector coefficients = XtXinv.multiply(Xt).operate(Y);
        Xt = null;
        
        //put the features coefficients in the thita map
        thitas.put(Dataset.constantColumnName, coefficients.getEntry(0));
        for(Map.Entry<Object, Integer> entry : featureIds.entrySet()) {
            Object feature = entry.getKey();
            Integer featureId = entry.getValue();
            
            thitas.put(feature, coefficients.getEntry(featureId));
        }
        
        if(knowledgeBase.getTrainingParameters().getCalculatePvalue()) { //calculate them only if 
            //get the predictions and subtact the Y vector. Sum the squared differences to get the error
            double SSE = 0.0;
            for(double v : X.operate(coefficients).subtract(Y).toArray()) {
                SSE += v*v;
            }
            Y = null;

            //standard error matrix
            double MSE = SSE/(n-d); //mean square error = SSE / dfResidual
            RealMatrix SE = XtXinv.scalarMultiply(MSE);
            XtXinv = null;

            //creating a flipped map of ids to features
            Map<Integer, Object> idsFeatures = PHPfunctions.array_flip(featureIds);
            
            
            Map<Object, Double> pvalues = new HashMap<>(); //This is not small, but it does not make sense to store it in the db
            for(int i =0;i<d;++i) {
                double error = SE.getEntry(i, i);
                Object feature = idsFeatures.get(i);
                if(error<=0.0) {
                    //double tstat = Double.MAX_VALUE;
                    pvalues.put(feature, 0.0);
                }
                else {
                    double tstat = coefficients.getEntry(i)/Math.sqrt(error);
                    pvalues.put(feature, 1.0-ContinuousDistributions.StudentsCdf(tstat, n-d)); //n-d degrees of freedom
                }
            }
            SE=null;
            coefficients=null;
            idsFeatures=null;
            matrixDataset = null;
            
            modelParameters.setFeaturePvalues(pvalues);
        }
    }

    @Override
    protected void predictDataset(Dataset newData) {
        //read model params
        ModelParameters modelParameters = knowledgeBase.getModelParameters();

        int d = modelParameters.getD();
        
        Map<Object, Double> thitas = modelParameters.getThitas();
        Map<Object, Integer> featureIds = modelParameters.getFeatureIds();
        
        RealVector coefficients = new ArrayRealVector(d);
        for(Map.Entry<Object, Double> entry : thitas.entrySet()) {
            Integer featureId = featureIds.get(entry.getKey());
            coefficients.setEntry(featureId, entry.getValue());
        }
        
        MatrixDataset matrixDataset = MatrixDataset.parseDataset(newData, featureIds);
        
        RealMatrix X = matrixDataset.getX();
        
        RealVector Y = X.operate(coefficients);
        for(Record r : newData) {
            r.setYPredicted(Y.getEntry(r.getId()));
        }
        
        matrixDataset = null;
    }

    //Methods required by the StepwiseCompatible Intefrace
    
    @Override
    public void setCalculateFeaturePvalues(boolean calculateFeaturePvalues) {
        knowledgeBase.getTrainingParameters().setCalculatePvalue(calculateFeaturePvalues);
    }

    @Override
    public Map<Object, Double> getFeaturePvalues() {
        return knowledgeBase.getModelParameters().getFeaturePvalues();
    }
    
}
