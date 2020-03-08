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
package com.datumbox.framework.core.statistics.descriptivestatistics;

import com.datumbox.framework.common.dataobjects.DataTable2D;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.common.dataobjects.TransposeDataList;
import com.datumbox.framework.common.dataobjects.TypeInference;
import com.datumbox.framework.core.statistics.nonparametrics.relatedsamples.KendallTauCorrelation;
import com.datumbox.framework.core.statistics.nonparametrics.relatedsamples.SpearmanCorrelation;
import com.datumbox.framework.core.statistics.parametrics.relatedsamples.PearsonCorrelation;

import java.util.Map;

/**
 * The Bivariate class enables us to estimate correlation metrics between different
 * variables.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class Bivariate {
    
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
    private static DataTable2D bivariateMatrix(Dataframe dataSet, BivariateType type) {
        DataTable2D bivariateMatrix = new DataTable2D();
        
        //extract values of first variable
        Map<Object, TypeInference.DataType> columnTypes = dataSet.getXDataTypes();
        Object[] allVariables = columnTypes.keySet().toArray();
        int numberOfVariables = allVariables.length;
        
        TransposeDataList transposeDataList;
        for(int i=0;i<numberOfVariables;++i) {
            Object variable0 = allVariables[i];
            if(columnTypes.get(variable0)==TypeInference.DataType.CATEGORICAL) {
                continue;
            }
            
            transposeDataList = new TransposeDataList();
            
            //extract values of first variable
            transposeDataList.put(0, dataSet.getXColumn(variable0));
            
            for(int j=i;j<numberOfVariables;++j) {
                Object variable1 = allVariables[j];
                if(columnTypes.get(variable1)==TypeInference.DataType.CATEGORICAL) {
                    continue;
                }
            
                transposeDataList.put(1, dataSet.getXColumn(variable1));
                
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
            //transposeDataList = null;
        }
        
        return bivariateMatrix;
    }
    
    /**
     * Calculates Covariance Matrix.
     * 
     * @param dataSet
     * @return 
     */
    public static DataTable2D covarianceMatrix(Dataframe dataSet) {
        return bivariateMatrix(dataSet, Bivariate.BivariateType.COVARIANCE);
    }
    
    /**
     * Calculates Pearson Matrix.
     * 
     * @param dataSet
     * @return 
     */
    public static DataTable2D pearsonMatrix(Dataframe dataSet) {
        return bivariateMatrix(dataSet, Bivariate.BivariateType.PEARSONCORRELATION);
    }
    
    /**
     * Calculates Spearman Matrix.
     * 
     * @param dataSet
     * @return 
     */
    public static DataTable2D spearmanMatrix(Dataframe dataSet) {
        return bivariateMatrix(dataSet, Bivariate.BivariateType.SPEARMANCORRELATION);
    }
    
    /**
     * Calculates Kendall Tau Matrix.
     * 
     * @param dataSet
     * @return 
     */
    public static DataTable2D kendalltauMatrix(Dataframe dataSet) {
        return bivariateMatrix(dataSet, Bivariate.BivariateType.KENDALLTAUCORRELATION);
    }
    
}
