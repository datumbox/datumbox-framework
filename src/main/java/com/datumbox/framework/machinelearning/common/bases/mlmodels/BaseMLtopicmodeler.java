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
public abstract class BaseMLtopicmodeler<MP extends BaseMLtopicmodeler.ModelParameters, TP extends BaseMLtopicmodeler.TrainingParameters, VM extends BaseMLtopicmodeler.ValidationMetrics> extends BaseMLmodel<MP, TP, VM> {
    
    public static abstract class ModelParameters extends BaseMLmodel.ModelParameters {
        
    } 
    
    public static abstract class TrainingParameters extends BaseMLmodel.TrainingParameters {    

    } 

    public static abstract class ValidationMetrics extends BaseMLmodel.ValidationMetrics {
  
    }
    
    protected BaseMLtopicmodeler(String dbName, Class<MP> mpClass, Class<TP> tpClass, Class<VM> vmClass, ModelValidation<MP, TP, VM> modelValidator) {
        super(dbName, mpClass, tpClass, vmClass, modelValidator);
    } 
    
}