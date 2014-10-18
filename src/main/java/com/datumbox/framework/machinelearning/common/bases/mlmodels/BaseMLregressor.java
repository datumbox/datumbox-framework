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
package com.datumbox.framework.machinelearning.common.bases.mlmodels;

import com.datumbox.framework.machinelearning.common.bases.validation.ModelValidation;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <VM>
 */
public abstract class BaseMLregressor<MP extends BaseMLregressor.ModelParameters, TP extends BaseMLregressor.TrainingParameters, VM extends BaseMLregressor.ValidationMetrics> extends BaseMLmodel<MP, TP, VM> {
    
    public static abstract class ModelParameters extends BaseMLmodel.ModelParameters {
        //number of observations used for training
        private Integer n =0 ;
        
        //number of features in data. IN DATA not in the algorithm. Typically the features of the algortihm is d*c
        private Integer d =0 ;
        /*
        @Override
        public void bigDataStructureInitializer(BigDataStructureFactory bdsf, MemoryConfiguration memoryConfiguration) {
            super.bigDataStructureInitializer(bdsf, memoryConfiguration);
        }
        */
        public Integer getN() {
            return n;
        }

        public void setN(Integer n) {
            this.n = n;
        }

        public Integer getD() {
            return d;
        }

        public void setD(Integer d) {
            this.d = d;
        }
        
    } 
    
    
    public static abstract class TrainingParameters extends BaseMLmodel.TrainingParameters {    

    } 

    //DO NOT DECLARE ABSTRACT!!!! IT IS INITIALIZED BY StepwiseRegression class
    public static class ValidationMetrics extends BaseMLmodel.ValidationMetrics {
        
    }
    
    protected BaseMLregressor(String dbName, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass, ModelValidation<MP, TP, VM> modelValidator) {
        super(dbName, mpClass, tpClass, vmClass, modelValidator);
    } 
    
}