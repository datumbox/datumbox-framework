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
package com.datumbox.framework.machinelearning.common.bases.featureselection;

/**
 * Abstract class which is the base of every Categorical Feature Selection algorithm.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public abstract class ContinuousFeatureSelection<MP extends ContinuousFeatureSelection.ModelParameters, TP extends ContinuousFeatureSelection.TrainingParameters> extends FeatureSelection<MP, TP> {

    public static abstract class ModelParameters extends FeatureSelection.ModelParameters {
        
    }
    
    
    public static abstract class TrainingParameters extends FeatureSelection.TrainingParameters {
        
    }
    

    protected ContinuousFeatureSelection(String dbName, Class<MP> mpClass, Class<TP> tpClass) {
        super(dbName, mpClass, tpClass);
    }
    
}
