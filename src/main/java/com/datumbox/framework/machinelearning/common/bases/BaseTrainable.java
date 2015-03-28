/* 
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
package com.datumbox.framework.machinelearning.common.bases;

import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.objecttypes.Trainable;
import com.datumbox.configuration.GeneralConfiguration;
import com.datumbox.framework.machinelearning.common.bases.dataobjects.BaseModelParameters;
import com.datumbox.framework.machinelearning.common.bases.dataobjects.BaseTrainingParameters;
import com.datumbox.framework.machinelearning.common.dataobjects.KnowledgeBase;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <KB>
 */
public abstract class BaseTrainable<MP extends BaseModelParameters, TP extends BaseTrainingParameters, KB extends KnowledgeBase<MP, TP>> implements Trainable<MP, TP> {
    
    //Variables necessary for hanlding the BigDataStructureContainerHolder
    
    protected KB knowledgeBase;
    protected String dbName;

    @Override
    public TP getTrainingParameters() {
        return knowledgeBase.getTrainingParameters();
    }
    
    @Override
    public void erase() {
        knowledgeBase.erase();
    }
    
    @Override
     public MP getModelParameters() {
       return knowledgeBase.getModelParameters();

    }
    
    
    @Override
    public void fit(Dataset trainingData, TP trainingParameters) {
        if(GeneralConfiguration.DEBUG) {
            System.out.println("fit()");
        }
        
        initializeTrainingConfiguration(trainingParameters);
        _fit(trainingData);
    }
    
    protected void initializeTrainingConfiguration(TP trainingParameters) {
        //reset knowledge base
        knowledgeBase.reinitialize();
        knowledgeBase.setTrainingParameters(trainingParameters);
    }
    
    protected abstract void _fit(Dataset trainingData);
    
}
