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
package com.datumbox.framework.machinelearning.featureselection.continuous;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.framework.machinelearning.common.bases.featureselection.ContinuousFeatureSelection;
import com.datumbox.common.dataobjects.Dataframe;
import com.datumbox.common.dataobjects.MatrixDataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.common.persistentstorage.interfaces.BigMap;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.Map;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.StatUtils;


/**
 * Implementation of Principal Component Analysis. The method can be used to project
 the Dataframe to the orthogonal space and to eliminate components with low variance.
 
 WARNING: This class copies the Dataframe to a RealMatrix which forces all of the
 data to be loaded in memory.
 
 References: 
 Intelligent Data Analysis and Probabilistic Inference Slide 15
 Advanced statistical machine learning and pattern recognition slides 2, tutorial 3, cw 1 matlab code
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class PCA extends ContinuousFeatureSelection<PCA.ModelParameters, PCA.TrainingParameters> {
    
    /**
     * The ModelParameters class stores the coefficients that were learned during
     * the training of the algorithm.
     */
    public static class ModelParameters extends ContinuousFeatureSelection.ModelParameters {
        @BigMap
        private Map<Object, Integer> featureIds;
        
        private int rows; //rows of the eigenvector matrix
        private int cols; //cols of the eigenvector matrix
        
        private double[] mean; //mean values for each column
        private double[] eigenValues; //eigenvalues
        
        private double[][] components; //components weights 

        /**
         * Protected constructor which accepts as argument the DatabaseConnector.
         * 
         * @param dbc 
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
            return mean;
        }
        
        /**
         * Setter for the mean values of each column.
         * 
         * @param mean 
         */
        protected void setMean(double[] mean) {
            this.mean = mean;
        }
        
        /**
         * Getter for the array with the Eigenvalues.
         * 
         * @return 
         */
        public double[] getEigenValues() {
            return eigenValues;
        }
        
        /**
         * Setter for the array with the Eigenvalues.
         * 
         * @param eigenValues 
         */
        protected void setEigenValues(double[] eigenValues) {
            this.eigenValues = eigenValues;
        }
        
        /**
         * Getter of the components matrix.
         * 
         * @return 
         */
        public double[][] getComponents() {
            return components;
        }
        
        /**
         * Setter of the components matrix.
         * 
         * @param components 
         */
        protected void setComponents(double[][] components) {
            this.components = components;
        }
    
    }

    /**
     * The TrainingParameters class stores the parameters that can be changed
     * before training the algorithm.
     */    
    public static class TrainingParameters extends ContinuousFeatureSelection.TrainingParameters {
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
     * @param dbConf 
     */
    public PCA(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, PCA.ModelParameters.class, PCA.TrainingParameters.class);
    }

    @Override
    protected void _fit(Dataframe originalData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        int n = modelParameters.getN();
        int d = modelParameters.getD();
        
        //convert data into matrix
        Map<Object, Integer> featureIds= modelParameters.getFeatureIds();
        MatrixDataset matrixDataset = MatrixDataset.newInstance(originalData, false, featureIds);
        RealMatrix X = matrixDataset.getX();
        
        //calculate means and subtract them from data
        double[] meanValues = new double[d];
        for(Map.Entry<Object, Integer> entry : featureIds.entrySet()) {
            Object feature = entry.getKey();
            Integer columnId = entry.getValue();
            
            meanValues[columnId] = Descriptives.mean(originalData.getXColumn(feature).toFlatDataCollection());
            
            for(int row=0;row<n;++row) {
                X.addToEntry(row, columnId, -meanValues[columnId]); //inplace subtraction!!!
            }
        }
        modelParameters.setMean(meanValues);
        
        RealMatrix components;
        double[] eigenValues;
        /*
        if(d>n) { //turned off because of the algorithm could not be validated
            //Karhunen Lowe Transform to speed up calculations
            
            //nxn matrix
            RealMatrix covarianceNN = (X.multiply(X.transpose())).scalarMultiply(1.0/(n-1.0)); 
            
            EigenDecomposition decomposition = new EigenDecomposition(covarianceNN);
            eigenValues = decomposition.getRealEigenvalues();
            
            
            RealMatrix eigenVectors = decomposition.getV();
            
            double[] sqrtInverseEigenValues = new double[eigenValues.length];
            for(int i=0;i<eigenValues.length;++i) {
                if(eigenValues[i]==0.0) {
                    sqrtInverseEigenValues[i] = 0.0;
                }
                else {
                    sqrtInverseEigenValues[i] = 1.0/Math.sqrt(eigenValues[i]);
                }
            }
            
            components = X.transpose().multiply(eigenVectors);
            //Components = X'*V*L^-0.5; To whiten them we multiply with L^0.5 which 
            //cancels out the previous multiplication. So below we multiply by
            //L^-0.5 ONLY if we don't whiten.
            if(!knowledgeBase.getTrainingParameters().isWhitened()) { 
                components = components.multiply(new DiagonalMatrix(sqrtInverseEigenValues));
            }
        }
        else {
            //Normal PCA goes here
        }
        */
        //dxd matrix
        RealMatrix covarianceDD = (X.transpose().multiply(X)).scalarMultiply(1.0/(n-1.0)); 

        EigenDecomposition decomposition = new EigenDecomposition(covarianceDD);
        eigenValues = decomposition.getRealEigenvalues();

        components = decomposition.getV();
        
        //Whiten Components W = U*L^0.5; To whiten them we multiply with L^0.5.
        if(knowledgeBase.getTrainingParameters().isWhitened()) { 

            double[] sqrtEigenValues = new double[eigenValues.length];
            for(int i=0;i<eigenValues.length;++i) {
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
                ++varCounter;
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

    @Override
    protected void filterFeatures(Dataframe newData) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        //convert data into matrix
        Map<Object, Integer> featureIds= modelParameters.getFeatureIds();
        MatrixDataset matrixDataset = MatrixDataset.parseDataset(newData, featureIds);
        RealMatrix X = matrixDataset.getX();
        /*
        //subtracting means
        double[] meanValues = modelParameters.getMean();
        int n = newdata.size();
        int cols = featureIds.size();
        for(int row=0;row<n;++row) {
            for(int columnId=0;columnId<cols;++columnId) {
                X.addToEntry(row, columnId, -meanValues[columnId]); //inplace subtraction!!!
            }
        }
        */
        RealMatrix components = new BlockRealMatrix(modelParameters.getComponents());
        
        
        //multiplying the data with components
        X = X.multiply(components);
        
        for(Integer rId : newData.index()) { //CONTINUOUS_ID_ASSUMPTION
            Record r = newData.get(rId);
            
            AssociativeArray xData = new AssociativeArray();
            int componentId=0;
            for(double value : X.getRow(rId)) {
                xData.put(componentId, value);
                ++componentId;
            }
            
            newData._set(rId, new Record(xData, r.getY(), r.getYPredicted(), r.getYPredictedProbabilities()));
        }
        
        newData.recalculateMeta();
    }
    
}
