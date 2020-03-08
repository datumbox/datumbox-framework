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
package com.datumbox.framework.core.machinelearning.featureselection;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.concurrency.StreamMethods;
import com.datumbox.framework.common.dataobjects.*;
import com.datumbox.framework.common.storage.interfaces.BigMap;
import com.datumbox.framework.common.storage.interfaces.StorageEngine;
import com.datumbox.framework.common.storage.interfaces.StorageEngine.MapType;
import com.datumbox.framework.common.storage.interfaces.StorageEngine.StorageHint;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.common.dataobjects.DataframeMatrix;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.featureselectors.AbstractFeatureSelector;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.util.FastMath;

import java.util.*;


/**
 * Implementation of Principal Component Analysis. The method can be used to project
 * the Dataframe to the orthogonal space and to eliminate components with low variance.
 * 
 * References: 
 * Intelligent Data Analysis and Probabilistic Inference Slide 15
 * Advanced statistical machine learning and pattern recognition slides 2, tutorial 3, cw 1 matlab code
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class PCA extends AbstractFeatureSelector<PCA.ModelParameters, PCA.TrainingParameters> {
    
    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractFeatureSelector.AbstractModelParameters {
        private static final long serialVersionUID = 2L;
        
        @BigMap(keyClass=Object.class, valueClass=Integer.class, mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=false)
        private Map<Object, Integer> featureIds;
        
        private RealVector mean; //mean values for each column
        private RealVector eigenValues; //eigenvalues
        
        private RealMatrix components; //components weights
        
        /** 
         * @param storageEngine
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(StorageEngine)
         */
        protected ModelParameters(StorageEngine storageEngine) {
            super(storageEngine);
        }
        
        /**
         * Getter for the mapping of the column names to column ids.  This mapping is estimated during training.
         * 
         * @return 
         */
        public Map<Object, Integer> getFeatureIds() {
            return featureIds;
        }

        /**
         * Setter for the mapping of the column names to column ids. 
         * 
         * @param featureIds 
         */
        protected void setFeatureIds(Map<Object, Integer> featureIds) {
            this.featureIds = featureIds;
        }

        /**
         * Getter for the mean values of each column.
         * 
         * @return 
         */
        public RealVector getMean() {
            return mean;
        }
        
        /**
         * Setter for the mean values of each column.
         * 
         * @param mean 
         */
        protected void setMean(RealVector mean) {
            this.mean = mean;
        }
        
        /**
         * Getter for the array with the Eigenvalues.
         * 
         * @return 
         */
        public RealVector getEigenValues() {
            return eigenValues;
        }
        
        /**
         * Setter for the array with the Eigenvalues.
         * 
         * @param eigenValues 
         */
        protected void setEigenValues(RealVector eigenValues) {
            this.eigenValues = eigenValues;
        }
        
        /**
         * Getter of the components matrix.
         * 
         * @return 
         */
        public RealMatrix getComponents() {
            return components;
        }
        
        /**
         * Setter of the components matrix.
         * 
         * @param components 
         */
        protected void setComponents(RealMatrix components) {
            this.components = components;
        }
    
    }

    /** {@inheritDoc} */  
    public static class TrainingParameters extends AbstractFeatureSelector.AbstractTrainingParameters {
        private static final long serialVersionUID = 1L;
        
        private boolean whitened = false;
        private Integer maxDimensions = null;
        private Double variancePercentageThreshold = null;
        
        /**
         * Getter for whether we should run whitened PCA.
         * 
         * @return 
         */
        public boolean isWhitened() {
            return whitened;
        }
        
        /**
         * Setter for whether we should run whitened PCA.
         * 
         * @param whitened 
         */
        public void setWhitened(boolean whitened) {
            this.whitened = whitened;
        }

        /**
         * Getter for the maximum number of dimensions/components that should be 
         * kept by the algorithm.
         * 
         * @return 
         */
        public Integer getMaxDimensions() {
            return maxDimensions;
        }
        
        /**
         * Setter for the maximum number of dimensions/components that should be 
         * kept by the algorithm.
         * 
         * @param maxDimensions 
         */
        public void setMaxDimensions(Integer maxDimensions) {
            this.maxDimensions = maxDimensions;
        }

        /**
         * Getter for the variance percentage threshold. Setting this value will
         * cause the algorithm to keep the only fist X components whose the cumulative
         * variance percentage is higher than this number.
         * 
         * @return 
         */
        public Double getVariancePercentageThreshold() {
            return variancePercentageThreshold;
        }
        
        /**
         * Setter for the variance percentage threshold. Setting this value will
         * cause the algorithm to keep the only fist X components whose the cumulative
         * variance percentage is higher than this number.
         * 
         * @param variancePercentageThreshold 
         */
        public void setVariancePercentageThreshold(Double variancePercentageThreshold) {
            this.variancePercentageThreshold = variancePercentageThreshold;
        }

    }

    /**
     * @param trainingParameters
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(AbstractTrainingParameters, Configuration)
     */
    protected PCA(TrainingParameters trainingParameters, Configuration configuration) {
        super(trainingParameters, configuration);
    }

    /**
     * @param storageName
     * @param configuration
     * @see AbstractTrainer#AbstractTrainer(String, Configuration)
     */
    protected PCA(String storageName, Configuration configuration) {
        super(storageName, configuration);
    }

    /** {@inheritDoc} */
    @Override
    public void fit(Dataframe trainingData) {
        Set<TypeInference.DataType> supportedXDataTypes = getSupportedXDataTypes();
        for(TypeInference.DataType d : trainingData.getXDataTypes().values()) {
            if(!supportedXDataTypes.contains(d)) {
                throw new IllegalArgumentException("A DataType that is not supported by this method was detected in the Dataframe.");
            }
        }
        super.fit(trainingData);
    }

    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe trainingData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        int n = trainingData.size();
        int d = trainingData.xColumnSize();
        
        //convert data into matrix
        Map<Object, Integer> featureIds= modelParameters.getFeatureIds();
        DataframeMatrix matrixDataset = DataframeMatrix.newInstance(trainingData, false, null, featureIds);
        RealMatrix X = matrixDataset.getX();
        
        //calculate means and subtract them from data
        RealVector meanValues = new OpenMapRealVector(d);
        for(Integer columnId : featureIds.values()) {
            double mean = 0.0;
            for(int row=0;row<n;row++) {
                mean += X.getEntry(row, columnId);
            }
            mean /= n;

            for(int row=0;row<n;row++) {
                X.addToEntry(row, columnId, -mean);
            }

            meanValues.setEntry(columnId, mean);
        }
        modelParameters.setMean(meanValues);

        //dxd matrix
        RealMatrix covarianceDD = (X.transpose().multiply(X)).scalarMultiply(1.0/(n-1.0)); 

        EigenDecomposition decomposition = new EigenDecomposition(covarianceDD);
        RealVector eigenValues = new ArrayRealVector(decomposition.getRealEigenvalues(), false);

        RealMatrix components = decomposition.getV();
        
        //Whiten Components W = U*L^0.5; To whiten them we multiply with L^0.5.
        if(knowledgeBase.getTrainingParameters().isWhitened()) {

            RealMatrix sqrtEigenValues = new DiagonalMatrix(d);
            for(int i=0;i<d;i++) {
                sqrtEigenValues.setEntry(i, i, FastMath.sqrt(eigenValues.getEntry(i)));
            }

            components = components.multiply(sqrtEigenValues);
        }
        
        //the eigenvalues and their components are sorted by descending order no need to resort them
        Integer maxDimensions = knowledgeBase.getTrainingParameters().getMaxDimensions();
        Double variancePercentageThreshold = knowledgeBase.getTrainingParameters().getVariancePercentageThreshold();
        if(variancePercentageThreshold!=null && variancePercentageThreshold<=1) {
            double totalVariance = 0.0;
            for(int i=0;i<d;i++) {
                totalVariance += eigenValues.getEntry(i);
            }

            double sum=0.0;
            int varCounter=0;
            for(int i=0;i<d;i++) {
                sum+=eigenValues.getEntry(i)/totalVariance;
                varCounter++;
                if(sum>=variancePercentageThreshold) {
                    break;
                }
            }
            
            if(maxDimensions==null || maxDimensions>varCounter) {
                maxDimensions=varCounter;
            }
        }
        
        if(maxDimensions!=null && maxDimensions<d) {  
            //keep only the maximum selected eigenvalues
            eigenValues=eigenValues.getSubVector(0, maxDimensions);

            
            //keep only the maximum selected eigenvectors
            components = components.getSubMatrix(0, components.getRowDimension()-1, 0, maxDimensions-1);
        }
        
        modelParameters.setEigenValues(eigenValues);
        modelParameters.setComponents(components);
    }

    /** {@inheritDoc} */
    @Override
    protected void _transform(Dataframe newData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        //convert data into matrix
        Map<Object, Integer> featureIds= modelParameters.getFeatureIds();
        
        Map<Integer, Integer> recordIdsReference = new HashMap<>();
        DataframeMatrix matrixDataset = DataframeMatrix.parseDataset(newData, recordIdsReference, featureIds);
        
        RealMatrix components = modelParameters.getComponents();
        
        
        //multiplying the data with components
        final RealMatrix X = matrixDataset.getX().multiply(components);
        
        streamExecutor.forEach(StreamMethods.stream(newData.entries(), isParallelized()), e -> {
            Integer rId = e.getKey();
            Record r = e.getValue();
            int rowId = recordIdsReference.get(rId);
            
            AssociativeArray xData = new AssociativeArray();
            int componentId=0;
            for(double value : X.getRow(rowId)) {
                xData.put(componentId++, value);
            }

            Record newR = new Record(xData, r.getY(), r.getYPredicted(), r.getYPredictedProbabilities());
            
            //we call below the recalculateMeta()
            newData._unsafe_set(rId, newR);
        });
        
        //recordIdsReference = null;
        //matrixDataset = null;
        
        newData.recalculateMeta();
    }

    /** {@inheritDoc} */
    @Override
    protected Set<TypeInference.DataType> getSupportedXDataTypes() {
        return new HashSet<>(Arrays.asList(TypeInference.DataType.BOOLEAN, TypeInference.DataType.NUMERICAL));
    }

    /** {@inheritDoc} */
    @Override
    protected Set<TypeInference.DataType> getSupportedYDataTypes() {
        return null;
    }
    
}
