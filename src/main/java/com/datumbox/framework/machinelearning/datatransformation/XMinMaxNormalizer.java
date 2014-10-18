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
 * Transforms the X vector of the Records to the 0.0-1.0 scale.
 * 
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class XMinMaxNormalizer extends BaseMinMaxNormalizer {

    public static final String SHORT_METHOD_NAME = "XNrm";
    
    public XMinMaxNormalizer(String dbName) {
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
        }
    }
    
    @Override
    protected void _normalize(Dataset data) {
        Map<Object, Double> minColumnValues = knowledgeBase.getModelParameters().getMinColumnValues();
        Map<Object, Double> maxColumnValues = knowledgeBase.getModelParameters().getMaxColumnValues();

        BaseMinMaxNormalizer.normalizeX(data, minColumnValues, maxColumnValues);
    }

    @Override
    protected void _denormalize(Dataset data) {
        Map<Object, Double> minColumnValues = knowledgeBase.getModelParameters().getMinColumnValues();
        Map<Object, Double> maxColumnValues = knowledgeBase.getModelParameters().getMaxColumnValues();

        BaseMinMaxNormalizer.normalizeX(data, minColumnValues, maxColumnValues);
    }
}
