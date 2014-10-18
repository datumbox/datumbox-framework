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
package com.datumbox.framework.machinelearning.featureselection.continuous;

import com.datumbox.framework.machinelearning.common.bases.featureselection.ContinuousFeatureSelection;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.MatrixDataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureMarker;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.framework.statistics.descriptivestatistics.Descriptives;
import java.util.Map;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.StatUtils;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Transient;


public class PCA extends ContinuousFeatureSelection<PCA.ModelParameters, PCA.TrainingParameters> {
    /*
        References: 
            Intelligent Data Analysis and Probabilistic Inference Slide 15
            Advanced statistical machine learning and patern recognition Lecure 2 & Tutorial 3
            Advanced statistical machine learning and patern recognition Coursework 1 Matlab code
    */
    public static final String SHORT_METHOD_NAME = "PCA";
    
    
    public static class TrainingParameters extends ContinuousFeatureSelection.TrainingParameters {
        private boolean whitened = false;
        private Integer maxDimensions = null;
        private Double varianceThreshold = null;
        
        public boolean isWhitened() {
            return whitened;
        }

        public void setWhitened(boolean whitened) {
            this.whitened = whitened;
        }

        public Integer getMaxDimensions() {
            return maxDimensions;
        }

        public void setMaxDimensions(Integer maxDimensions) {
            this.maxDimensions = maxDimensions;
        }

        public Double getVarianceThreshold() {
            return varianceThreshold;
        }

        public void setVarianceThreshold(Double varianceThreshold) {
            this.varianceThreshold = varianceThreshold;
        }

    }
    
    public static class ModelParameters extends ContinuousFeatureSelection.ModelParameters {
        @BigDataStructureMarker
        @Transient
        private Map<Object, Integer> feature2ColumnId;
        
        private int rows; //rows of the eigenvector matrix
        private int cols; //cols of the eigenvector matrix
        
        private double[] mean; //mean values for each column
        private double[] eigenValues; //eigenvalues

        //MORPHIA does not support storing 2d arrays. Thus to store it I have to
        //make it a single 1d array and convert it back to 2d
        @Transient
        private double[][] components; //components weights 
        private transient double [] components_1d;
        
        @PrePersist
        public void prePersist(){
            if(components != null && components_1d==null){
                components_1d = new double[rows*cols];
                for(int row=0;row<rows;++row) {
                    for(int col=0;col<cols;++col) {
                        components_1d[row*cols+col] = components[row][col];
                    }
                }
            }
        }

        @PostLoad
        public void postLoad(){
            if(components == null && components_1d!=null){
                components = new double[rows][cols];
                for(int row=0;row<rows;++row) {
                    for(int col=0;col<cols;++col) {
                        components[row][col] = components_1d[row*cols+col];
                    }
                }
            }
        }
        
        
        
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            super.bigDataStructureInitializer(bdsf, memoryConfiguration);
            
            BigDataStructureFactory.MapType mapType = memoryConfiguration.getMapType();
            int LRUsize = memoryConfiguration.getLRUsize();

            feature2ColumnId = bdsf.getMap("feature2ColumnId", mapType, LRUsize);
        }

        public Map<Object, Integer> getFeature2ColumnId() {
            return feature2ColumnId;
        }

        public void setFeature2ColumnId(Map<Object, Integer> feature2ColumnId) {
            this.feature2ColumnId = feature2ColumnId;
        }

        public int getRows() {
            return rows;
        }

        public void setRows(int rows) {
            this.rows = rows;
        }

        public int getCols() {
            return cols;
        }

        public void setCols(int cols) {
            this.cols = cols;
        }
        
        public double[] getMean() {
            return mean;
        }

        public void setMean(double[] mean) {
            this.mean = mean;
        }

        public double[] getEigenValues() {
            return eigenValues;
        }

        public void setEigenValues(double[] eigenValues) {
            this.eigenValues = eigenValues;
        }

        public double[][] getComponents() {
            return components;
        }

        public void setComponents(double[][] components) {
            this.components = components;
        }

        public double[] getComponents_1d() {
            return components_1d;
        }

        public void setComponents_1d(double[] components_1d) {
            this.components_1d = components_1d;
        }

        
    }

    public PCA(String dbName) {
        super(dbName, PCA.ModelParameters.class, PCA.TrainingParameters.class);
    }
    
    @Override
    public String shortMethodName() {
        return SHORT_METHOD_NAME;
    }

    @Override
    protected void estimateModelParameters(Dataset originaldata) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        int n = originaldata.size();
        int d = originaldata.getColumnSize();
        
        //convert data into matrix
        Map<Object, Integer> feature2ColumnId= modelParameters.getFeature2ColumnId();
        MatrixDataset matrixDataset = MatrixDataset.newInstance(originaldata, false, feature2ColumnId);
        RealMatrix X = matrixDataset.getX();
        
        //calculate means and subtract them from data
        double[] meanValues = new double[d];
        for(Map.Entry<Object, Integer> entry : feature2ColumnId.entrySet()) {
            Object feature = entry.getKey();
            Integer columnId = entry.getValue();
            
            meanValues[columnId] = Descriptives.mean(originaldata.extractColumnValues(feature).toFlatDataCollection());
            
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
        Double varianceThreshold = knowledgeBase.getTrainingParameters().getVarianceThreshold();
        if(varianceThreshold!=null && varianceThreshold<=1) {
            double sum=0.0;
            double totalVariance = StatUtils.sum(eigenValues);
            int varCounter=0;
            for(double l : eigenValues) {
                sum+=l/totalVariance;
                ++varCounter;
                if(sum>=varianceThreshold) {
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
    protected void filterFeatures(Dataset newdata) {
        ModelParameters modelParameters = knowledgeBase.getModelParameters();
        
        //convert data into matrix
        Map<Object, Integer> feature2ColumnId= modelParameters.getFeature2ColumnId();
        MatrixDataset matrixDataset = MatrixDataset.parseDataset(newdata, feature2ColumnId);
        RealMatrix X = matrixDataset.getX();
        /*
        //subtracting means
        double[] meanValues = modelParameters.getMean();
        int n = newdata.size();
        int cols = feature2ColumnId.size();
        for(int row=0;row<n;++row) {
            for(int columnId=0;columnId<cols;++columnId) {
                X.addToEntry(row, columnId, -meanValues[columnId]); //inplace subtraction!!!
            }
        }
        */
        RealMatrix components = new BlockRealMatrix(modelParameters.getComponents());
        
        
        //multiplying the data with components
        X = X.multiply(components);
        
        Dataset transformedDataset = new Dataset();
        
        for(Record r : newdata) {
            Integer recordId = r.getId();
            
            Record newR = new Record();
            int componentId=0;
            for(double value : X.getRow(recordId)) {
                newR.getX().put(componentId, value);
                ++componentId;
            }
            newR.setY(r.getY());
            
            transformedDataset.add(newR);
        }
        
        newdata.clear();
        newdata.merge(transformedDataset);
    }
    
    
}
