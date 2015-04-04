/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis at datumbox.com>
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
package com.datumbox.framework.machinelearning.datatransformation;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConnector;
import com.datumbox.framework.machinelearning.common.bases.datatransformation.BaseDummyExtractor;

/**
 * Simple Dummy Variable Extractor: Removes the categorical and ordinal features 
 * and creates new dummy variables that are combinations of feature-value. It does not
 * take into consideration reference levels. It is used only by BayesianEnsembleMethod
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class SimpleDummyVariableExtractor extends BaseDummyExtractor<BaseDummyExtractor.ModelParameters, BaseDummyExtractor.TrainingParameters> {


    public SimpleDummyVariableExtractor(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf, BaseDummyExtractor.ModelParameters.class, BaseDummyExtractor.TrainingParameters.class);
    }

    
    @Override
    protected void _transform(Dataset data, boolean trainingMode) {
        //handle non-numeric types, extract columns to dummy variables
        
        BaseDummyExtractor.extractDummies(data, null, trainingMode);
    }

    @Override
    protected void _normalize(Dataset data) {
        //no normalization performed
    }

    @Override
    protected void _denormalize(Dataset data) {
        //no denormalization required
    }
    
}
