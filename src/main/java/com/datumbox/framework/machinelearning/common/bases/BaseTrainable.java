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
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.configuration.GeneralConfiguration;
import com.datumbox.framework.machinelearning.common.bases.dataobjects.BaseModelParameters;
import com.datumbox.framework.machinelearning.common.bases.dataobjects.BaseTrainingParameters;
import com.datumbox.framework.machinelearning.common.dataobjects.KnowledgeBase;
import java.lang.reflect.InvocationTargetException;

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
    
    
    public static <BT extends BaseTrainable> BT newInstance(Class<BT> aClass, String dbName, DatabaseConfiguration dbConfig) {
        BT algorithm = null;
        try {
            algorithm = aClass.getConstructor(String.class, DatabaseConfiguration.class).newInstance(dbName, dbConfig);;
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
        
        return algorithm;
    }

    protected BaseTrainable(String dbName, DatabaseConfiguration dbConf) {
        String methodName = this.getClass().getSimpleName();
        if(!dbName.contains(methodName)) { //patch for the K-fold cross validation which already contains the name of the algorithm in the dbname
            dbName += dbConf.getDBnameSeparator() + methodName;
        }
        
        this.dbName = dbName;
    }
    
    protected BaseTrainable(String dbName, DatabaseConfiguration dbConf, Class<MP> mpClass, Class<TP> tpClass) {
        this(dbName, dbConf);
        
        knowledgeBase = (KB) new KnowledgeBase(this.dbName, dbConf, mpClass, tpClass);
    } 
    
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
        
        //store database
        knowledgeBase.save();
    }
    
    @Override
    public boolean modifiesData() {
        //check if the algorithm itself modifies the data
        try { 
            Boolean dataSafe = (Boolean) this.getClass().getDeclaredField("DATA_SAFE_CALL_BY_REFERENCE").get(this);
            //see if the data are safe mearning that algorithm does not modify the data internally.
            //if the data are not safe, mark it for deep copy
            if(dataSafe!=true) {
                return true;
            }
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
            return true; //if no information available play it safe and mark it as true
        }
        
        return false;
    }
    
    protected void initializeTrainingConfiguration(TP trainingParameters) {
        //reset knowledge base
        knowledgeBase.reinitialize();
        knowledgeBase.setTrainingParameters(trainingParameters);
    }
    
    protected abstract void _fit(Dataset trainingData);
    
}
