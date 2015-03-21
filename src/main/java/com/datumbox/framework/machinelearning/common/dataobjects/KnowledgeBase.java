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
package com.datumbox.framework.machinelearning.common.dataobjects;

import com.datumbox.common.objecttypes.Learnable;
import com.datumbox.common.objecttypes.Parameterizable;
import com.datumbox.common.objecttypes.Trainable;
import com.datumbox.common.persistentstorage.factories.DatabaseFactory;
import com.datumbox.common.persistentstorage.interfaces.BigMapContainer;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;


/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 */
public class KnowledgeBase<MP extends Learnable, TP extends Parameterizable & KnowledgeBase.SelfConstructible> implements Serializable {

    public interface SelfConstructible<O> {
        public O getEmptyObject();
    }

    
    /*
        VARIABLES
        =========
    */
    protected transient String dbName; 
    
    
    protected transient DatabaseFactory dbf;
    
    
    protected Class<? extends Trainable> ownerClass; //the Class name of the algorithm
    
    protected boolean trained = false;

    
    
    protected Class<MP> mpClass;
    protected Class<TP> tpClass;
    
    protected MP modelParameters;
    protected TP trainingParameters;
    
    
    
    
    
    /*
        EXTENDING INTERFACE
        ==================
    */

    protected KnowledgeBase() {
        //constructor only used in serialization/deserialization
    }

    public KnowledgeBase(String dbName, Class<MP> mpClass, Class<TP> tpClass) {
        this.dbName = dbName;
        //get an instance on the permanent storage handler
        dbf = DatabaseFactory.newInstance(dbName);
        
        this.mpClass = mpClass;
        this.tpClass = tpClass;
    }
    
    public boolean isTrained() {
        return trained;
    }
    
    public void setTrained(boolean trained) {
        this.trained = trained;
    }

    public String getDbName() {
        return dbName;
    }

    public DatabaseFactory getDbf() {
        return dbf;
    }
    
    public Class<? extends Trainable> getOwnerClass() {
        return ownerClass;
    }

    public void setOwnerClass(Class<? extends Trainable> ownerClass) {
        this.ownerClass = ownerClass;
    }       

    public void save() {
        if(modelParameters==null) {
            throw new IllegalArgumentException("Can not store an empty KnowledgeBase.");
        }
        
        dbf.save(this);
    }
    
    public void load() {
        if(modelParameters==null) {

            //NOTE: the kbObject was constructed with the default protected no-argument
            //constructor. As a result it does not have an initialized dbf object.
            //We don't care for that though because this instance has a valid dbf object
            //and the kbObject is only used to copy its values (we don't use it).
            KnowledgeBase kbObject = dbf.load(this.getClass());
            if(kbObject==null) {
                throw new IllegalArgumentException("The KnowledgeBase could not be loaded.");
            }
            
            trainingParameters = (TP) kbObject.trainingParameters;
            modelParameters = (MP) kbObject.modelParameters;
            
            setTrained(true);
        }
    }

    public boolean isConfigured() {
        if(modelParameters==null || trainingParameters==null) {
            return false;
        }
        
        return true;
    }
    
    public void erase() {
    	dbf.dropDatabase();
        
        modelParameters = null;
        trainingParameters = null;
        setTrained(false);
    }
    
    public void reinitialize() {
        erase();

        try {
            modelParameters = mpClass.getConstructor().newInstance();
            if(BigMapContainer.class.isAssignableFrom(modelParameters.getClass())) {
                ((BigMapContainer)modelParameters).bigDataStructureInitializer(dbf);
            }
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
        
        trainingParameters = getEmptyTrainingParametersObject();
    }

    
    
    
    /*
        IMPORTANT PUBLIC METHODS
        ========================
    */
    
    public TP getEmptyTrainingParametersObject() {
        //There is already an object set, call its getEmptyObjec() method to generate one
        if(trainingParameters!=null) {
            return (TP) trainingParameters.getEmptyObject();
        }
        
        try {
            return tpClass.getConstructor().newInstance();
        } 
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
    
    
    /*
        GETTER SETTERS
        ==============
    */
    
    public TP getTrainingParameters() {
        return trainingParameters;
    }

    public void setTrainingParameters(TP trainingParameters) {
        this.trainingParameters = trainingParameters;
    }

    public MP getModelParameters() {
        return modelParameters;
    }

    public void setModelParameters(MP modelParameters) {
        this.modelParameters = modelParameters;
    }
    
    
}
