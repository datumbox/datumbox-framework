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
package com.datumbox.framework.statistics.descriptivestatistics;

import com.datumbox.common.dataobjects.DataTable2D;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.TransposeDataList;
import com.datumbox.framework.statistics.nonparametrics.relatedsamples.KendallTauCorrelation;
import com.datumbox.framework.statistics.nonparametrics.relatedsamples.SpearmanCorrelation;
import com.datumbox.framework.statistics.parametrics.relatedsamples.PearsonCorrelation;
import java.util.Map;

/**
 *
 * @author bbriniotis
 */
public class Bivariate {
    /**
     * The internalDataCollections that are passed in this function are NOT modified after the analysis. 
     * You can safely pass directly the internalDataCollection without worrying about having them modified.
     */
    public static final boolean DATA_SAFE_CALL_BY_REFERENCE = true;
    
    private enum BivariateType  {
        COVARIANCE, PEARSONCORRELATION, SPEARMANCORRELATION, KENDALLTAUCORRELATION
    };
    
    /**
     * Calculates BivariateMatrix for a given statistic
     * 
     * @param dataSet
     * @param type
     * @return 
     */
    private static DataTable2D bivariateMatrix(Dataset dataSet, BivariateType type) {        
        DataTable2D bivariateMatrix = new DataTable2D();
        
        //extract values of first variable
        Map<Object, Dataset.ColumnType> variable2Type = dataSet.getColumns();
        Object[] allVariables = variable2Type.keySet().toArray();
        int numberOfVariables = allVariables.length;
        
        TransposeDataList transposeDataList = null;
        for(int i=0;i<numberOfVariables;++i) {
            Object variable0 = allVariables[i];
            if(variable2Type.get(variable0)!=Dataset.ColumnType.NUMERICAL &&
               variable2Type.get(variable0)!=Dataset.ColumnType.ORDINAL) {
                continue;
            }
            
            transposeDataList = new TransposeDataList();
            
            //extract values of first variable
            transposeDataList.put(0, dataSet.extractColumnValues(variable0));
            
            for(int j=i;j<numberOfVariables;++j) {
                Object variable1 = allVariables[j];
                if(variable2Type.get(variable1)!=Dataset.ColumnType.NUMERICAL &&
                   variable2Type.get(variable1)!=Dataset.ColumnType.ORDINAL) {
                    continue;
                }
            
                transposeDataList.put(1, dataSet.extractColumnValues(variable1));
                
                double value = 0.0;
                if(type==BivariateType.COVARIANCE) {
                    value = Descriptives.covariance(transposeDataList, true);
                }
                else if(type==BivariateType.PEARSONCORRELATION) {
                    if(variable0.equals(variable1)) {
                        value=1.0;
                    }
                    else {
                        value = PearsonCorrelation.calculateCorrelation(transposeDataList);
                    }
                }
                else if(type==BivariateType.SPEARMANCORRELATION) {
                    if(variable0.equals(variable1)) {
                        value=1.0;
                    }
                    else {
                        value = SpearmanCorrelation.calculateCorrelation(transposeDataList);
                    }
                }
                else if(type==BivariateType.KENDALLTAUCORRELATION) {
                    if(variable0.equals(variable1)) {
                        value=1.0;
                    }
                    else {
                        value = KendallTauCorrelation.calculateCorrelation(transposeDataList);
                    }
                }
                
                //bivariateMatrix.internalData.get(variable0).internalData.put(variable1, value);
                bivariateMatrix.put2d(variable0, variable1, value);
                
                if(!variable0.equals(variable1)) {
                    /*
                    if(!bivariateMatrix.internalData.containsKey(variable1)) {
                        bivariateMatrix.internalData.put(variable1, new AssociativeArray());
                    }
                    bivariateMatrix.internalData.get(variable1).internalData.put(variable0, value);
                    */
                    bivariateMatrix.put2d(variable1, variable0, value);
                }
            }
            transposeDataList = null;
        }
        
        return bivariateMatrix;
    }
    
    /**
     * Calculates Covariance Matrix.
     * 
     * @param dataSet
     * @return 
     */
    public static DataTable2D covarianceMatrix(Dataset dataSet) {
        return bivariateMatrix(dataSet, Bivariate.BivariateType.COVARIANCE);
    }
    
    /**
     * Calculates Pearson Matrix.
     * 
     * @param dataSet
     * @return 
     */
    public static DataTable2D pearsonMatrix(Dataset dataSet) {
        return bivariateMatrix(dataSet, Bivariate.BivariateType.PEARSONCORRELATION);
    }
    
    /**
     * Calculates Spearman Matrix.
     * 
     * @param dataSet
     * @return 
     */
    public static DataTable2D spearmanMatrix(Dataset dataSet) {
        return bivariateMatrix(dataSet, Bivariate.BivariateType.SPEARMANCORRELATION);
    }
    
    /**
     * Calculates Kendall Tau Matrix.
     * 
     * @param dataSet
     * @return 
     */
    public static DataTable2D kendalltauMatrix(Dataset dataSet) {
        return bivariateMatrix(dataSet, Bivariate.BivariateType.KENDALLTAUCORRELATION);
    }
    
}
