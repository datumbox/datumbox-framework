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
package com.datumbox.framework.machinelearning.datatransformation;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.framework.machinelearning.common.bases.datatransformation.BaseDummyMinMaxTransformer;
import java.util.Map;

/**
 * XY MinMax Normalizer: Transforms the X vector and Y values of the Records to 
 * the 0.0-1.0 scale. This normalizer can be used in continuous regression models.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class XYMinMaxNormalizer extends BaseDummyMinMaxTransformer {
    
    /**
     * Public constructor of the algorithm.
     * 
     * @param dbName
     * @param dbConf 
     */
    public XYMinMaxNormalizer(String dbName, DatabaseConfiguration dbConf) {
        super(dbName, dbConf);
    }
    
    @Override
    protected void _fit(Dataset data) {       
        Map<Object, Double> minColumnValues = knowledgeBase.getModelParameters().getMinColumnValues();
        Map<Object, Double> maxColumnValues = knowledgeBase.getModelParameters().getMaxColumnValues();
        
        BaseDummyMinMaxTransformer.fitX(data, minColumnValues, maxColumnValues);
        BaseDummyMinMaxTransformer.fitY(data, minColumnValues, maxColumnValues);
    }
    
    @Override
    protected void _convert(Dataset data) {
        
    }
    
    @Override
    protected void _normalize(Dataset data) {
        Map<Object, Double> minColumnValues = knowledgeBase.getModelParameters().getMinColumnValues();
        Map<Object, Double> maxColumnValues = knowledgeBase.getModelParameters().getMaxColumnValues();

        BaseDummyMinMaxTransformer.normalizeX(data, minColumnValues, maxColumnValues);
        BaseDummyMinMaxTransformer.normalizeY(data, minColumnValues, maxColumnValues);
    }
    
    @Override
    protected void _denormalize(Dataset data) {
        Map<Object, Double> minColumnValues = knowledgeBase.getModelParameters().getMinColumnValues();
        Map<Object, Double> maxColumnValues = knowledgeBase.getModelParameters().getMaxColumnValues();

        BaseDummyMinMaxTransformer.denormalizeX(data, minColumnValues, maxColumnValues);
        BaseDummyMinMaxTransformer.denormalizeY(data, minColumnValues, maxColumnValues);
    }

}
