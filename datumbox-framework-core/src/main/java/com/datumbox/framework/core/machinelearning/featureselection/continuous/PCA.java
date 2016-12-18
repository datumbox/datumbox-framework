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
package com.datumbox.framework.core.machinelearning.featureselection.continuous;

import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.concurrency.ForkJoinStream;
import com.datumbox.framework.common.concurrency.StreamMethods;
import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.MatrixDataframe;
import com.datumbox.framework.common.dataobjects.Record;
import com.datumbox.framework.common.persistentstorage.interfaces.BigMap;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector.MapType;
import com.datumbox.framework.common.persistentstorage.interfaces.DatabaseConnector.StorageHint;
import com.datumbox.framework.common.utilities.PHPMethods;
import com.datumbox.framework.core.machinelearning.common.abstracts.AbstractTrainer;
import com.datumbox.framework.core.machinelearning.common.abstracts.featureselectors.AbstractContinuousFeatureSelector;
import com.datumbox.framework.core.machinelearning.common.interfaces.Parallelizable;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.StatUtils;

import java.util.HashMap;
import java.util.Map;


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
public class PCA extends AbstractContinuousFeatureSelector<PCA.ModelParameters, PCA.TrainingParameters> implements Parallelizable {
    
    /** {@inheritDoc} */
    public static class ModelParameters extends AbstractContinuousFeatureSelector.AbstractModelParameters {
        private static final long serialVersionUID = 1L;
        
        @BigMap(keyClass=Object.class, valueClass=Integer.class, mapType=MapType.HASHMAP, storageHint=StorageHint.IN_MEMORY, concurrent=false)
        private Map<Object, Integer> featureIds;
        
        private int rows; //rows of the eigenvector matrix
        private int cols; //cols of the eigenvector matrix
        
        private double[] mean; //mean values for each column
        private double[] eigenValues; //eigenvalues
        
        private double[][] components; //components weights 
        
        /** 
         * @param dbc
         * @see AbstractTrainer.AbstractModelParameters#AbstractModelParameters(DatabaseConnector)
         */
        protected ModelParameters(DatabaseConnector dbc) {
            super(dbc);
        }
        
        /**
         * Getter for the mapping of the column names to column ids. The implementation
         * internally converts the data into double[] and as a result we need to 
         * estimate and store the mapping between the column names and their 
         * positions in the array. This mapping is estimated during training.
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
         * Getter for the number of rows of the eigenvector matrix.
         * 
         * @return 
         */
        public int getRows() {
            return rows;
        }
        
        /**
         * Setter for the number of rows of the eigenvector matrix.
         * 
         * @param rows 
         */
        protected void setRows(int rows) {
            this.rows = rows;
        }
        
        /**
         * Getter for the number of columns of the eigenvector matrix.
         * 
         * @return 
         */
        public int getCols() {
            return cols;
        }
        
        /**
         * Setter for the number of columns of the eigenvector matrix.
         * 
         * @param cols 
         */
        protected void setCols(int cols) {
            this.cols = cols;
        }
        
        /**
         * Getter for the mean values of each column.
         * 
         * @return 
         */
        public double[] getMean() {
            return PHPMethods.array_clone(mean);
        }
        
        /**
         * Setter for the mean values of each column.
         * 
         * @param mean 
         */
        protected void setMean(double[] mean) {
            this.mean = PHPMethods.array_clone(mean);
        }
        
        /**
         * Getter for the array with the Eigenvalues.
         * 
         * @return 
         */
        public double[] getEigenValues() {
            return PHPMethods.array_clone(eigenValues);
        }
        
        /**
         * Setter for the array with the Eigenvalues.
         * 
         * @param eigenValues 
         */
        protected void setEigenValues(double[] eigenValues) {
            this.eigenValues = PHPMethods.array_clone(eigenValues);
        }
        
        /**
         * Getter of the components matrix.
         * 
         * @return 
         */
        public double[][] getComponents() {
            return PHPMethods.array_clone(components);
        }
        
        /**
         * Setter of the components matrix.
         * 
         * @param components 
         */
        protected void setComponents(double[][] components) {
            this.components = PHPMethods.array_clone(components);
        }
    
    }

    /** {@inheritDoc} */  
    public static class TrainingParameters extends AbstractContinuousFeatureSelector.AbstractTrainingParameters {
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
     * Public constructor of the algorithm.
     * 
     * @param dbName
     * @param conf 
     */
    public PCA(String dbName, Configuration conf) {
        super(dbName, conf, PCA.ModelParameters.class, PCA.TrainingParameters.class);
        streamExecutor = new ForkJoinStream(knowledgeBase.getConf().getConcurrencyConfig());
    }

    private boolean parallelized = true;
    
    /**
     * This executor is used for the parallel processing of streams with custom 
     * Thread pool.
     */
    protected final ForkJoinStream streamExecutor;
    
    /** {@inheritDoc} */
    @Override
    public boolean isParallelized() {
        return parallelized;
    }

    /** {@inheritDoc} */
    @Override
    public void setParallelized(boolean parallelized) {
        this.parallelized = parallelized;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void _fit(Dataframe originalData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        int n = originalData.size();
        int d = originalData.xColumnSize();
        
        //convert data into matrix
        Map<Object, Integer> featureIds= modelParameters.getFeatureIds();
        MatrixDataframe matrixDataset = MatrixDataframe.newInstance(originalData, false, null, featureIds);
        RealMatrix X = matrixDataset.getX();
        
        //calculate means and subtract them from data
        double[] meanValues = new double[d];
        for(Integer columnId : featureIds.values()) {
            
            meanValues[columnId] = 0.0;
            for(double v : X.getColumn(columnId)) {
                meanValues[columnId] += v;
            }
            meanValues[columnId] /= n;
            
            for(int row=0;row<n;row++) {
                X.addToEntry(row, columnId, -meanValues[columnId]);
            }
        }
        modelParameters.setMean(meanValues);

        //dxd matrix
        RealMatrix covarianceDD = (X.transpose().multiply(X)).scalarMultiply(1.0/(n-1.0)); 

        EigenDecomposition decomposition = new EigenDecomposition(covarianceDD);
        double[] eigenValues = decomposition.getRealEigenvalues();

        RealMatrix components = decomposition.getV();
        
        //Whiten Components W = U*L^0.5; To whiten them we multiply with L^0.5.
        if(knowledgeBase.getTrainingParameters().isWhitened()) {

            double[] sqrtEigenValues = new double[eigenValues.length];
            for(int i=0;i<eigenValues.length;i++) {
                sqrtEigenValues[i] = Math.sqrt(eigenValues[i]);
            }

            components = components.multiply(new DiagonalMatrix(sqrtEigenValues));
        }
        
        //the eigenvalues and their components are sorted by descending order no need to resort them
        Integer maxDimensions = knowledgeBase.getTrainingParameters().getMaxDimensions();
        Double variancePercentageThreshold = knowledgeBase.getTrainingParameters().getVariancePercentageThreshold();
        if(variancePercentageThreshold!=null && variancePercentageThreshold<=1) {
            double sum=0.0;
            double totalVariance = StatUtils.sum(eigenValues);
            int varCounter=0;
            for(double l : eigenValues) {
                sum+=l/totalVariance;
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
            double[] newEigenValues = new double[maxDimensions];            
            System.arraycopy(eigenValues, 0, newEigenValues, 0, maxDimensions);
            eigenValues=newEigenValues;
            
            //keep only the maximum selected eigenvectors
            components = components.getSubMatrix(0, components.getRowDimension()-1, 0, maxDimensions-1);
        }
        
        modelParameters.setRows(components.getRowDimension());
        modelParameters.setCols(components.getColumnDimension());
        
        modelParameters.setEigenValues(eigenValues);
        modelParameters.setComponents(components.getData());      
    }

    /** {@inheritDoc} */
    @Override
    protected void _transform(Dataframe dataset) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        //convert data into matrix
        Map<Object, Integer> featureIds= modelParameters.getFeatureIds();
        
        Map<Integer, Integer> recordIdsReference = new HashMap<>();
        MatrixDataframe matrixDataset = MatrixDataframe.parseDataset(dataset, recordIdsReference, featureIds);
        
        RealMatrix components = new BlockRealMatrix(modelParameters.getComponents());
        
        
        //multiplying the data with components
        final RealMatrix X = matrixDataset.getX().multiply(components);
        
        streamExecutor.forEach(StreamMethods.stream(dataset.entries(), isParallelized()), e -> {
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
            dataset._unsafe_set(rId, newR);
        });
        
        //recordIdsReference = null;
        //matrixDataset = null;
        
        dataset.recalculateMeta(); 
    }
    
}
