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
package com.datumbox.framework.machinelearning.datatransformation;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.framework.machinelearning.common.bases.datatransformation.BaseMinMaxNormalizer;
import java.util.Map;

/**
 * Dummy XY MinMax Normalizer: Transforms the X vector and Y values of the 
 * Records to the 0.0-1.0 scale and builds dummy variables when non-numerics are
 * detected. This normalizer can be used in continuous regression models.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class DummyXYMinMaxNormalizer extends BaseMinMaxNormalizer {


    public static final String SHORT_METHOD_NAME = "DXYNrm";
           
    public DummyXYMinMaxNormalizer(String dbName) {
        super(dbName);
    }
    
    @Override
    public String shortMethodName() {
        return SHORT_METHOD_NAME;
    }

    @Override
    protected void _transform(Dataset data, boolean trainingMode) {       
        if(trainingMode) {
            Map<Object, Double> minColumnValues = knowledgeBase.getModelParameters().getMinColumnValues();
            Map<Object, Double> maxColumnValues = knowledgeBase.getModelParameters().getMaxColumnValues();

            BaseMinMaxNormalizer.transformX(data, minColumnValues, maxColumnValues);
            BaseMinMaxNormalizer.transformY(data, minColumnValues, maxColumnValues);
        }
        
        BaseMinMaxNormalizer.transformDummy(data, knowledgeBase.getModelParameters().getReferenceLevels(), trainingMode);
    }
    
    @Override
    protected void _normalize(Dataset data) {
        Map<Object, Double> minColumnValues = knowledgeBase.getModelParameters().getMinColumnValues();
        Map<Object, Double> maxColumnValues = knowledgeBase.getModelParameters().getMaxColumnValues();

        BaseMinMaxNormalizer.normalizeX(data, minColumnValues, maxColumnValues);
        BaseMinMaxNormalizer.normalizeY(data, minColumnValues, maxColumnValues);
    }
    
    @Override
    protected void _denormalize(Dataset data) {
        Map<Object, Double> minColumnValues = knowledgeBase.getModelParameters().getMinColumnValues();
        Map<Object, Double> maxColumnValues = knowledgeBase.getModelParameters().getMaxColumnValues();

        BaseMinMaxNormalizer.denormalizeX(data, minColumnValues, maxColumnValues);
        BaseMinMaxNormalizer.denormalizeY(data, minColumnValues, maxColumnValues);
    }
       
}
