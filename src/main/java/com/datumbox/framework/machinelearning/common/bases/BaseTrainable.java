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
package com.datumbox.framework.machinelearning.common.bases;

import com.datumbox.common.objecttypes.Parameterizable;
import com.datumbox.common.objecttypes.Trainable;
import com.datumbox.framework.machinelearning.common.dataobjects.KnowledgeBase;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 * @param <KB>
 */
public abstract class BaseTrainable<MP extends BaseModelParameters, TP extends Parameterizable & KnowledgeBase.SelfConstructible, KB extends KnowledgeBase<MP, TP>> implements Trainable<MP, TP> {
    
    //Variables necessary for hanlding the BigDataStructureContainerHolder
    
    protected KB knowledgeBase;
    protected String dbName;
    protected boolean temporary=false; //flag that indicates that the model is temporary and should store as little data as possible to the db to avoid wasting IO
    
    @Override
    public String getDBname() {
        return dbName;
    }

    @Override
    public TP getTrainingParametersObject() {
        return knowledgeBase.getTrainingParameters();
    }
    
    @Override
    public TP getEmptyTrainingParametersObject() {
        return knowledgeBase.getEmptyTrainingParametersObject();
    }
    
    @Override
    public void initializeTrainingConfiguration(TP trainingParameters) {
        //reset knowledge base
        knowledgeBase.reinitialize();
        knowledgeBase.setTrainingParameters(trainingParameters);
    }
    
    @Override
    public void erase() {
        knowledgeBase.erase();
    }
    
    @Override
    public boolean isTemporary() {
        return temporary;
    }

    @Override
    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }
    
    @Override
     public MP getModelParameters() {
       return knowledgeBase.getModelParameters();

     }


        
}
