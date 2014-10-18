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
import com.datumbox.common.persistentstorage.factories.BigDataStructureFactory;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureContainer;
import com.datumbox.common.persistentstorage.interfaces.BigDataStructureContainerHolder;
import com.datumbox.configuration.MemoryConfiguration;
import java.lang.reflect.InvocationTargetException;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 * @param <MP>
 * @param <TP>
 */
@Entity("KnowledgeBase")
public class TrainableKnowledgeBase<MP extends Learnable, TP extends Parameterizable & TrainableKnowledgeBase.SelfConstructible> implements BigDataStructureContainerHolder {

    public interface SelfConstructible<O> {
        public O getEmptyObject();
    }

    
    /*
        VARIABLES
        =========
    */
    @Id 
    protected ObjectId id;
    
    @Transient
    protected transient String dbName; 
    
    @Transient
    protected transient MemoryConfiguration memoryConfiguration;
    
    @Transient
    protected transient BigDataStructureFactory bdsf;
    
    
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

    protected TrainableKnowledgeBase() {
        //constructor only used in serialization/deserialization
    }

    public TrainableKnowledgeBase(String dbName, Class<MP> mpClass, Class<TP> tpClass) {
        this.dbName = dbName;
        //get an instance on the permanent storage handler
        bdsf = BigDataStructureFactory.newInstance(dbName);
        
        this.mpClass = mpClass;
        this.tpClass = tpClass;
    }
    
        
    @Override
    public boolean isTrained() {
        return trained;
    }
    
    @Override
    public void setTrained(boolean trained) {
        this.trained = trained;
    }

    @Override
    public String getDbName() {
        return dbName;
    }
    
    @Override
    public MemoryConfiguration getMemoryConfiguration() {
        try {
            return (MemoryConfiguration) memoryConfiguration.clone(); //MAKE SURE you clone it. The algorithms modify the contents of the memoryConfiguration during save
        } 
        catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void setMemoryConfiguration(MemoryConfiguration memoryConfiguration) {
        try {
            this.memoryConfiguration = (MemoryConfiguration) memoryConfiguration.clone(); //MAKE SURE you clone it. The algorithms modify the contents of the memoryConfiguration during save
        } 
        catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public BigDataStructureFactory getBdsf() {
        return bdsf;
    }
    
    @Override
    public Class<? extends Trainable> getOwnerClass() {
        return ownerClass;
    }

    @Override
    public void setOwnerClass(Class<? extends Trainable> ownerClass) {
        this.ownerClass = ownerClass;
    }    

    
    
    
    
    
    
    
    
    
    
    

    @Override
    public void save(boolean callPresave) {
        if(modelParameters==null) {
            throw new IllegalArgumentException("Can not store an empty KnowledgeBase.");
        }
        
        if(BigDataStructureContainer.class.isAssignableFrom(modelParameters.getClass())) {
            //do preSave
            if(callPresave) {
                //clears unnecessary variables
                ((BigDataStructureContainer)modelParameters).bigDataStructureCleaner(bdsf);
                
                //perform any necessary manipulation on data before save based on the selected big data structure factory
                bdsf.preSave((BigDataStructureContainer) modelParameters, memoryConfiguration);
            }
        }
        
        bdsf.save(this);
        bdsf.cleanUp();
    }

    @Override
    public boolean alreadyExists() {
        return (bdsf.load(this.getClass())!=null);
    }
    
    @Override
    public void load() {
        if(modelParameters==null) {

            //NOTE: the kbObject was constructed with the default protected no-argument
            //constructor. As a result it does not have an initialized bdsf object.
            //We don't care for that though because this instance has a valid bdsf object
            //and the kbObject is only used to copy its values (we don't use it).
            TrainableKnowledgeBase kbObject = bdsf.load(this.getClass());
            if(kbObject==null) {
                throw new IllegalArgumentException("The KnowledgeBase could not be loaded.");
            }
            
            id = kbObject.id;
            
            trainingParameters = (TP) kbObject.trainingParameters;
            modelParameters = (MP) kbObject.modelParameters;
            
            if(BigDataStructureContainer.class.isAssignableFrom(modelParameters.getClass())) {
                //perform postLoading to the modelParameters 
                bdsf.postLoad((BigDataStructureContainer) modelParameters, memoryConfiguration);
            }
            
            setTrained(true);
        }
    }

    @Override
    public boolean isConfigured() {
        if(modelParameters==null || trainingParameters==null) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public void erase(boolean complete) {
        if(complete) {
            bdsf.dropDatabase();
        }
        else {
            bdsf.clearDatabase();
        }
        
        modelParameters = null;
        trainingParameters = null;
        setTrained(false);
    }
    
    @Override
    public void reinitialize() {
        erase(false);

        try {
            modelParameters = mpClass.getConstructor().newInstance();
            if(BigDataStructureContainer.class.isAssignableFrom(modelParameters.getClass())) {
                ((BigDataStructureContainer)modelParameters).bigDataStructureInitializer(bdsf, memoryConfiguration);
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
